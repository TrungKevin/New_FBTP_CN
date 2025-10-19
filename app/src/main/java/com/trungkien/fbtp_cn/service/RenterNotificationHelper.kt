package com.trungkien.fbtp_cn.service

import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class để tạo notifications cho Renter
 * Bao gồm các loại notification mà renter sẽ nhận được
 */
class RenterNotificationHelper(
    private val notificationRepository: NotificationRepository
) {
    private val notificationBuilder = NotificationBuilder()
    
    /**
     * Thông báo khi đặt sân được owner xác nhận
     */
    fun notifyBookingConfirmed(
        renterId: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            println("🔔 DEBUG: RenterNotificationHelper.notifyBookingConfirmed called:")
            println("  - renterId: $renterId")
            println("  - fieldName: $fieldName")
            println("  - date: $date")
            println("  - time: $time")
            println("  - bookingId: $bookingId")
            println("  - fieldId: $fieldId")
            
            val notification = notificationBuilder.buildBookingConfirmedNotification(
                renterId = renterId,
                fieldName = fieldName,
                date = date,
                time = time,
                bookingId = bookingId ?: "",
                fieldId = fieldId ?: ""
            )
            
            println("🔔 DEBUG: Notification created, about to save to Firebase:")
            println("  - notificationId: ${notification.notificationId}")
            println("  - toUserId: ${notification.toUserId}")
            println("  - type: ${notification.type}")
            println("  - title: ${notification.title}")
            println("  - body: ${notification.body}")
            
            val result = notificationRepository.createNotification(notification)
            if (result.isSuccess) {
                println("✅ DEBUG: Notification saved successfully: ${notification.notificationId}")
            } else {
                println("❌ ERROR: Failed to save notification: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    /**
     * Thông báo khi đặt sân bị hủy bởi owner
     */
    fun notifyBookingCancelledByOwner(
        renterId: String,
        fieldName: String,
        date: String,
        time: String,
        reason: String? = null,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildBookingCancelledByOwnerNotification(
                renterId = renterId,
                fieldName = fieldName,
                date = date,
                time = time,
                reason = reason,
                bookingId = bookingId ?: "",
                fieldId = fieldId ?: ""
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo khi owner phản hồi đánh giá
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
            val notification = notificationBuilder.buildReviewReplyNotification(
                renterId = renterId,
                ownerName = ownerName,
                fieldName = fieldName,
                replyContent = replyContent,
                reviewId = reviewId,
                fieldId = fieldId
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo khi có đối thủ tham gia trận đấu
     */
    fun notifyOpponentJoined(
        renterAId: String,
        opponentName: String,
        fieldName: String,
        date: String,
        time: String,
        matchId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildOpponentJoinedNotification(
                renterAId = renterAId,
                opponentName = opponentName,
                fieldName = fieldName,
                date = date,
                time = time,
                matchId = matchId,
                fieldId = fieldId
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo kết quả trận đấu
     */
    fun notifyMatchResult(
        renterId: String,
        fieldName: String,
        result: String,
        isWinner: Boolean,
        matchId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildMatchResultNotification(
                renterId = renterId,
                fieldName = fieldName,
                result = result,
                isWinner = isWinner,
                matchId = matchId ?: "",
                fieldId = fieldId
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo khi sân được cập nhật thông tin
     */
    fun notifyFieldUpdated(
        renterId: String,
        fieldName: String,
        updateType: String,
        fieldId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildFieldUpdatedForRenterNotification(
                renterId = renterId,
                fieldName = fieldName,
                updateType = updateType,
                fieldId = fieldId
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo khi thanh toán thành công
     */
    fun notifyPaymentSuccess(
        renterId: String,
        amount: Long,
        fieldName: String,
        paymentId: String,
        bookingId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildPaymentSuccessNotification(
                userId = renterId,
                amount = amount,
                fieldName = fieldName,
                paymentId = paymentId,
                bookingId = bookingId
            )
            notificationRepository.createNotification(notification)
        }
    }
    
    /**
     * Thông báo khi thanh toán thất bại
     */
    fun notifyPaymentFailed(
        renterId: String,
        amount: Long,
        fieldName: String,
        paymentId: String,
        reason: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildPaymentFailedNotification(
                userId = renterId,
                amount = amount,
                fieldName = fieldName,
                paymentId = paymentId,
                reason = reason
            )
            notificationRepository.createNotification(notification)
        }
    }
}
