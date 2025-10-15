package com.trungkien.fbtp_cn.service

import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class để gửi notification khi owner phản hồi đánh giá của renter
 */
class ReviewNotificationHelper(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Gửi notification cho renter khi owner phản hồi đánh giá
     */
    fun notifyReviewReply(
        renterId: String,
        ownerName: String,
        fieldName: String,
        replyContent: String,
        reviewId: String,
        fieldId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notification = NotificationBuilder().buildReviewReplyNotification(
                    renterId = renterId,
                    ownerName = ownerName,
                    fieldName = fieldName,
                    replyContent = replyContent,
                    reviewId = reviewId,
                    fieldId = fieldId
                )
                
                notificationRepository.createNotification(notification)
                println("🔔 DEBUG: Sent review reply notification to renter: $renterId")
            } catch (e: Exception) {
                println("❌ ERROR: Failed to send review reply notification: ${e.message}")
            }
        }
    }
}
