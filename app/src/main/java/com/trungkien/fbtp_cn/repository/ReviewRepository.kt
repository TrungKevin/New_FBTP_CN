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
            
            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(reviewId = doc.id)
            }
            
            // Sort trong memory thay vì trong query
            val sortedReviews = reviews.sortedByDescending { it.createdAt }
            
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
            val replyWithTimestamp = reply.copy(
                replyId = "", // Để Firebase tự tạo
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            // Thêm reply vào subcollection
            val replyRef = firestore.collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .collection(REPLIES_COLLECTION)
                .add(replyWithTimestamp)
                .await()
            
            // Cập nhật review để thêm reply mới
            val reviewRef = firestore.collection(REVIEWS_COLLECTION).document(reviewId)
            val review = reviewRef.get().await().toObject(Review::class.java)
            
            if (review != null) {
                val updatedReplies = review.replies + replyWithTimestamp.copy(replyId = replyRef.id)
                reviewRef.update("replies", updatedReplies).await()
            }
            
            Result.success(replyRef.id)
        } catch (e: Exception) {
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
