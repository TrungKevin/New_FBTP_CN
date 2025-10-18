package com.trungkien.fbtp_cn.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.service.ReviewNotificationHelper
import com.trungkien.fbtp_cn.service.NotificationHelper
import kotlinx.coroutines.tasks.await

/**
 * REPOSITORY XỬ LÝ REVIEW - Tất cả operations liên quan đến đánh giá sân
 */
class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationHelper = ReviewNotificationHelper(NotificationRepository())
    
    /**
     * Lấy tất cả reviews của một field
     */
    suspend fun getReviewsByFieldId(fieldId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Review::class.java)?.copy(reviewId = doc.id)
                } catch (e: Exception) {
                    println("❌ DEBUG: Failed to parse review: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            Result.success(reviews)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.getReviewsByFieldId error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy review summary của một field
     */
    suspend fun getReviewSummary(fieldId: String): Result<ReviewSummary> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Review::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (reviews.isEmpty()) {
                return Result.success(ReviewSummary(
                    fieldId = fieldId,
                    averageRating = 0.0f,
                    totalReviews = 0,
                    ratingDistribution = mapOf()
                ))
            }
            
            val averageRating = reviews.sumOf { it.rating } / reviews.size.toFloat()
            val totalReviews = reviews.size
            
            val ratingDistribution = reviews.groupBy { it.rating }
                .mapValues { it.value.size }
            
            val recentReviews = reviews.sortedByDescending { it.createdAt }
                .take(5)
            
            Result.success(ReviewSummary(
                fieldId = fieldId,
                averageRating = averageRating,
                totalReviews = totalReviews,
                ratingDistribution = ratingDistribution
            ))
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.getReviewSummary error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Thêm review mới
     */
    suspend fun addReview(review: Review): Result<String> {
        return try {
            val docRef = firestore.collection("reviews").document()
            val reviewWithId = review.copy(reviewId = docRef.id)
            docRef.set(reviewWithId).await()
            
            println("✅ DEBUG: Review added successfully: ${docRef.id}")
            
            // ✅ NEW: Gửi thông báo cho Owner khi có review mới
            try {
                // Lấy thông tin field để có tên sân và ownerId
                val fieldDoc = firestore.collection("fields")
                    .document(review.fieldId)
                    .get()
                    .await()
                
                val fieldName = fieldDoc.getString("name") ?: "Sân"
                val ownerId = fieldDoc.getString("ownerId") ?: ""
                
                if (ownerId.isNotBlank()) {
                    // Gửi notification cho Owner
                    val notificationRepository = NotificationRepository()
                    val notificationHelper = NotificationHelper(notificationRepository)
                    
                    notificationHelper.notifyReviewAdded(
                        ownerId = ownerId,
                        renterName = review.renterName,
                        fieldName = fieldName,
                        rating = review.rating,
                        comment = review.comment,
                        reviewId = docRef.id,
                        fieldId = review.fieldId
                    )
                    
                    println("🔔 DEBUG: Sent review notification to owner: $ownerId")
                }
            } catch (e: Exception) {
                println("❌ ERROR: Failed to send review notification: ${e.message}")
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.addReview error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Thêm reply cho review
     */
    suspend fun addReply(reviewId: String, reply: Reply): Result<String> {
        return try {
            val reviewRef = firestore.collection("reviews").document(reviewId)
            val reviewDoc = reviewRef.get().await()
            val review = reviewDoc.toObject(Review::class.java)
            
            if (review != null) {
                val replyWithTimestamp = reply.copy(
                    createdAt = Timestamp.now(),
                    replyId = ""
                )
                // ✅ Lưu reply vào subcollection đúng cấu trúc rules: reviews/{reviewId}/replies/{replyId}
                val replyRef = reviewRef.collection("replies").document()
                replyRef.set(replyWithTimestamp.copy(replyId = replyRef.id)).await()

                // Đồng bộ danh sách replies trong chính document review để hiển thị nhanh
                val updatedReplies = review.replies + replyWithTimestamp.copy(replyId = replyRef.id)
                reviewRef.update("replies", updatedReplies).await()
                
                // Gửi notification cho renter khi owner phản hồi review
                if (reply.owner) {
                    try {
                        // Lấy thông tin field để có tên sân
                        val fieldDoc = firestore.collection("fields")
                            .document(review.fieldId)
                            .get()
                            .await()
                        
                        val fieldName = fieldDoc.getString("name") ?: "Sân"
                        
                        // Gửi notification cho renter
                        notificationHelper.notifyReviewReply(
                            renterId = review.renterId,
                            ownerName = reply.userName,
                            fieldName = fieldName,
                            replyContent = reply.comment,
                            reviewId = reviewId,
                            fieldId = review.fieldId
                        )
                        
                        println("🔔 DEBUG: Sent review reply notification to renter: ${review.renterId}")
                    } catch (e: Exception) {
                        println("❌ ERROR: Failed to send review reply notification: ${e.message}")
                    }
                }
            }
            
            Result.success("")
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.addReply error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Toggle like cho review
     */
    suspend fun toggleLikeReview(reviewId: String, userId: String): Result<Boolean> {
        return try {
            val reviewRef = firestore.collection("reviews").document(reviewId)
            val reviewDoc = reviewRef.get().await()
            val review = reviewDoc.toObject(Review::class.java)
            
            if (review != null) {
                val likedBy = review.likedBy.toMutableList()
                val isLiked = likedBy.contains(userId)
                
                if (isLiked) {
                    likedBy.remove(userId)
                } else {
                    likedBy.add(userId)
                }
                
                reviewRef.update("likedBy", likedBy).await()
                Result.success(!isLiked)
            } else {
                Result.failure(Exception("Review not found"))
            }
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.toggleLikeReview error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Xóa review
     */
    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.deleteReview error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Xóa reply
     */
    suspend fun deleteReply(reviewId: String, replyId: String): Result<Unit> {
        return try {
            val reviewRef = firestore.collection("reviews").document(reviewId)
            val reviewDoc = reviewRef.get().await()
            val review = reviewDoc.toObject(Review::class.java)
            
            if (review != null) {
                val updatedReplies = review.replies.filter { it.replyId != replyId }
                reviewRef.update("replies", updatedReplies).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.deleteReply error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật reply
     */
    suspend fun updateReply(reviewId: String, replyId: String, newContent: String): Result<Unit> {
        return try {
            val reviewRef = firestore.collection("reviews").document(reviewId)
            val reviewDoc = reviewRef.get().await()
            val review = reviewDoc.toObject(Review::class.java)
            
            if (review != null) {
                val updatedReplies = review.replies.map { reply ->
                    if (reply.replyId == replyId) {
                        reply.copy(comment = newContent)
                    } else {
                        reply
                    }
                }
                reviewRef.update("replies", updatedReplies).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.updateReply error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Báo cáo review
     */
    suspend fun reportReview(reviewId: String, reason: String): Result<Unit> {
        return try {
            val reportData = mapOf(
                "reviewId" to reviewId,
                "reason" to reason,
                "reportedAt" to Timestamp.now()
            )
            firestore.collection("reports").add(reportData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.reportReview error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật review
     */
    suspend fun updateReview(reviewId: String, updatedReview: Review): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId).set(updatedReview).await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.updateReview error: ${e.message}")
            Result.failure(e)
        }
    }
}
