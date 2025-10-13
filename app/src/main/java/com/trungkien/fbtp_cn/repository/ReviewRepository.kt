package com.trungkien.fbtp_cn.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.trungkien.fbtp_cn.model.*
import kotlinx.coroutines.tasks.await

/**
 * REPOSITORY XỬ LÝ REVIEW - Tất cả operations liên quan đến đánh giá sân
 */
class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()
    
    // Collection names
    companion object {
        private const val REVIEWS_COLLECTION = "reviews"
        private const val REPLIES_COLLECTION = "replies"
    }
    
    /**
     * Lấy tất cả reviews của một sân
     */
    suspend fun getReviewsByFieldId(fieldId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("status", "ACTIVE")
                // Bỏ .orderBy để tránh cần index
                .get()
                .await()
            
            // Map base reviews
            val baseReviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(reviewId = doc.id)
            }
            
            // Load replies subcollection for each review to ensure fresh data
            val reviewsWithReplies = baseReviews.map { review ->
                try {
                    val repliesSnap = firestore.collection(REVIEWS_COLLECTION)
                        .document(review.reviewId)
                        .collection(REPLIES_COLLECTION)
                        .orderBy("createdAt", Query.Direction.ASCENDING)
                        .get()
                        .await()
                    val replies = repliesSnap.documents.mapNotNull { repDoc ->
                        repDoc.toObject(Reply::class.java)?.copy(replyId = repDoc.id)
                    }
                    review.copy(replies = replies)
                } catch (e: Exception) {
                    review
                }
            }
            
            // Sort trong memory
            val sortedReviews = reviewsWithReplies.sortedByDescending { it.createdAt }
            
            Result.success(sortedReviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy review summary (thống kê) của một sân
     */
    suspend fun getReviewSummary(fieldId: String): Result<ReviewSummary> {
        return try {
            val snapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)
            }
            
            if (reviews.isEmpty()) {
                return Result.success(ReviewSummary(fieldId = fieldId))
            }
            
            // Tính điểm trung bình
            val totalRating = reviews.sumOf { it.rating }
            val averageRating = totalRating.toFloat() / reviews.size
            
            // Phân bố sao
            val ratingDistribution = (1..5).associateWith { star ->
                reviews.count { it.rating == star }
            }
            
            // Thống kê tags
            val tagStats = mutableMapOf<String, Int>()
            reviews.forEach { review ->
                review.tags.forEach { tag ->
                    tagStats[tag] = (tagStats[tag] ?: 0) + 1
                }
            }
            
            val summary = ReviewSummary(
                fieldId = fieldId,
                averageRating = averageRating,
                totalReviews = reviews.size,
                ratingDistribution = ratingDistribution,
                tagStats = tagStats,
                lastUpdated = Timestamp.now()
            )
            
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Thêm review mới
     */
    suspend fun addReview(review: Review): Result<String> {
        return try {
            val reviewWithTimestamp = review.copy(
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val docRef = firestore.collection(REVIEWS_COLLECTION).add(reviewWithTimestamp).await()

            // ✅ Thông báo cho chủ sân (Client-side Approach A)
            try {
                val fieldSnap = firestore.collection("fields").document(review.fieldId).get().await()
                val ownerId = fieldSnap.getString("ownerId")
                if (!ownerId.isNullOrBlank()) {
                    val result = notificationRepository.createNotification(
                        toUserId = ownerId,
                        type = "REVIEW_ADDED",
                        title = "Đánh giá mới!",
                        body = "Bạn nhận được đánh giá ${review.rating} sao",
                        data = NotificationData(
                            reviewId = docRef.id,
                            fieldId = review.fieldId,
                            userId = review.renterId,
                            customData = emptyMap()
                        ),
                        priority = "NORMAL"
                    )
                    if (result.isSuccess) {
                        println("🔔 DEBUG: Notification REVIEW_ADDED CREATED -> ownerId=$ownerId, reviewId=${docRef.id}")
                    } else {
                        println("❌ ERROR: Notification REVIEW_ADDED CREATE FAILED -> ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    println("⚠️ WARN: addReview - ownerId is null for fieldId=${review.fieldId}")
                }
            } catch (e: Exception) {
                println("❌ ERROR: Notification REVIEW_ADDED EXCEPTION -> ${e.message}")
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Thêm reply mới vào review
     */
    suspend fun addReply(reviewId: String, reply: Reply): Result<String> {
        return try {
            println("🔥 DEBUG: Repository.addReply - reviewId: $reviewId, reply: ${reply.comment}")
            
            val replyWithTimestamp = reply.copy(
                replyId = "", // Để Firebase tự tạo
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            println("🔥 DEBUG: Adding reply to subcollection...")
            // Thêm reply vào subcollection
            val replyRef = firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .collection(REPLIES_COLLECTION)
                .add(replyWithTimestamp)
                .await()
            
            println("🔥 DEBUG: Reply added to subcollection with ID: ${replyRef.id}")
            
            // Cập nhật review để thêm reply mới
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            val review = reviewRef.get().await().toObject(Review::class.java)
            
            println("🔥 DEBUG: Review found: ${review != null}, current replies: ${review?.replies?.size ?: 0}")
            
            if (review != null) {
                val updatedReplies = review.replies + replyWithTimestamp.copy(replyId = replyRef.id)
                println("🔥 DEBUG: Updating embedded array with ${updatedReplies.size} replies")
                reviewRef.update("replies", updatedReplies).await()
                println("🔥 DEBUG: Embedded array updated successfully")
            }
            
            Result.success(replyRef.id)
        } catch (e: Exception) {
            println("❌ DEBUG: Repository.addReply error: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Like/Unlike review
     */
    suspend fun toggleLikeReview(reviewId: String, userId: String): Result<Unit> {
        return try {
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            val review = reviewRef.get().await().toObject(Review::class.java)
            
            if (review != null) {
                val isLiked = review.likedBy.contains(userId)
                val updatedLikedBy = if (isLiked) {
                    review.likedBy - userId
                } else {
                    review.likedBy + userId
                }
                
                val updatedLikes = if (isLiked) review.likes - 1 else review.likes + 1
                
                reviewRef.update(
                    mapOf(
                        "likes" to updatedLikes,
                        "likedBy" to updatedLikedBy
                    )
                ).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Xóa review (chỉ owner hoặc người tạo)
     */
    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update("status", "DELETED")
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Xóa reply (chỉ owner hoặc người tạo)
     */
    suspend fun deleteReply(reviewId: String, replyId: String): Result<Unit> {
        return try {
            // Xóa reply khỏi subcollection
            firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .collection(REPLIES_COLLECTION)
                .document(replyId)
                .delete()
                .await()
            
            // Cập nhật review để xóa reply
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            val review = reviewRef.get().await().toObject(Review::class.java)
            
            if (review != null) {
                val updatedReplies = review.replies.filter { it.replyId != replyId }
                reviewRef.update("replies", updatedReplies).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật reply
     */
    suspend fun updateReply(reviewId: String, replyId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            println("🔄 DEBUG: Repository.updateReply called - reviewId: $reviewId, replyId: $replyId, updates: $updates")
            
            // Update subcollection document
            val replyRef = firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .collection(REPLIES_COLLECTION)
                .document(replyId)
            val merged = updates + mapOf("updatedAt" to Timestamp.now())
            println("🔄 DEBUG: Updating subcollection document...")
            replyRef.update(merged).await()
            println("🔄 DEBUG: Subcollection document updated successfully")
            
            // Also update embedded replies array in parent review
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            val review = reviewRef.get().await().toObject(Review::class.java)
            println("🔄 DEBUG: Review found: ${review != null}, current replies: ${review?.replies?.size ?: 0}")
            
            if (review != null) {
                val newReplies = review.replies.map { r ->
                    if (r.replyId == replyId) {
                        r.copy(
                            comment = (updates["comment"] as? String) ?: r.comment,
                            images = (updates["images"] as? List<String>) ?: r.images,
                            updatedAt = Timestamp.now()
                        )
                    } else r
                }
                println("🔄 DEBUG: Updating embedded array with ${newReplies.size} replies")
                reviewRef.update("replies", newReplies).await()
                println("🔄 DEBUG: Embedded array updated successfully")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Báo cáo review
     */
    suspend fun reportReview(reviewId: String, reason: String): Result<Unit> {
        return try {
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            reviewRef.update(
                mapOf(
                    "reportCount" to com.google.firebase.firestore.FieldValue.increment(1),
                    "status" to "PENDING_REVIEW"
                )
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật review
     */
    suspend fun updateReview(reviewId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatedData = updates.toMutableMap()
            updatedData["updatedAt"] = Timestamp.now()
            
            firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update(updatedData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy reviews theo user ID
     */
    suspend fun getReviewsByUserId(userId: String): Result<List<Review>> {
        return try {
            val snapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("renterId", userId)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(reviewId = doc.id)
            }
            
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
