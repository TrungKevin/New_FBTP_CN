package com.trungkien.fbtp_cn.service

import com.trungkien.fbtp_cn.model.*
import java.util.UUID

/**
 * Builder class để tạo notifications một cách dễ dàng và consistent
 */
class NotificationBuilder {// dùng để tạo các notification khác nhau trong hệ thống
    
    /**
     * Tạo notification cho booking được tạo
     */
    fun buildBookingCreatedNotification(
        ownerId: String,
        renterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String,
        fieldId: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = ownerId,
            type = NotificationType.BOOKING_CREATED.value,
            title = "Đặt sân mới!",
            body = "$renterName đã đặt sân $fieldName vào lúc $time ngày $date.",
            data = NotificationData(
                bookingId = bookingId,
                fieldId = fieldId,
                customData = mapOf(
                    "renterName" to renterName,
                    "fieldName" to fieldName,
                    "date" to date,
                    "time" to time
                )
            ),
            priority = NotificationPriority.HIGH.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = bookingId,
            relatedEntityType = "BOOKING",
            category = "BOOKING",
            tags = listOf("booking", "new"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho booking thành công
     */
    fun buildBookingSuccessNotification(
        renterId: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String,
        fieldId: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = renterId,
            type = NotificationType.BOOKING_SUCCESS.value,
            title = "Đặt sân thành công!",
            body = "Bạn đã đặt sân $fieldName vào lúc $time ngày $date thành công.",
            data = NotificationData(
                bookingId = bookingId,
                fieldId = fieldId,
                customData = mapOf(
                    "fieldName" to fieldName,
                    "date" to date,
                    "time" to time
                )
            ),
            priority = NotificationPriority.NORMAL.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = bookingId,
            relatedEntityType = "BOOKING",
            category = "BOOKING",
            tags = listOf("booking", "success"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho booking bị hủy
     */
    fun buildBookingCancelledNotification(
        ownerId: String,
        renterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String,
        fieldId: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = ownerId,
            type = NotificationType.BOOKING_CANCELLED.value,
            title = "Đặt sân bị hủy!",
            body = "$renterName đã hủy đặt sân $fieldName vào lúc $time ngày $date.",
            data = NotificationData(
                bookingId = bookingId,
                fieldId = fieldId,
                customData = mapOf(
                    "renterName" to renterName,
                    "fieldName" to fieldName,
                    "date" to date,
                    "time" to time
                )
            ),
            priority = NotificationPriority.HIGH.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = bookingId,
            relatedEntityType = "BOOKING",
            category = "BOOKING",
            tags = listOf("booking", "cancelled"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho đối thủ tham gia
     */
    fun buildOpponentJoinedNotification(
        renterAId: String,
        opponentName: String,
        fieldName: String,
        date: String,
        time: String,
        matchId: String?,
        fieldId: String?
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = renterAId,
            type = NotificationType.OPPONENT_JOINED.value,
            title = "Có đối thủ tham gia!",
            body = "$opponentName đã tham gia trận đấu của bạn tại sân $fieldName vào lúc $time ngày $date.",
            data = NotificationData(
                matchId = matchId,
                fieldId = fieldId,
                customData = mapOf(
                    "opponentName" to opponentName,
                    "fieldName" to fieldName,
                    "date" to date,
                    "time" to time
                )
            ),
            priority = NotificationPriority.HIGH.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = matchId,
            relatedEntityType = "MATCH",
            category = "MATCH",
            tags = listOf("match", "opponent"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho kết quả trận đấu
     */
    fun buildMatchResultNotification(
        renterAId: String,
        renterBId: String,
        fieldName: String,
        result: String,
        matchId: String,
        fieldId: String?
    ): Notification {
        val title = "Kết quả trận đấu!"
        val body = "Trận đấu tại sân $fieldName đã kết thúc với tỷ số $result."
        val data = NotificationData(
            matchId = matchId,
            fieldId = fieldId,
            customData = mapOf(
                "fieldName" to fieldName,
                "result" to result
            )
        )
        
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = renterAId, // Sẽ duplicate cho renterB
            type = NotificationType.MATCH_RESULT.value,
            title = title,
            body = body,
            data = data,
            priority = NotificationPriority.HIGH.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = matchId,
            relatedEntityType = "MATCH",
            category = "MATCH",
            tags = listOf("match", "result"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho đánh giá mới
     */
    fun buildReviewAddedNotification(
        ownerId: String,
        reviewerName: String,
        fieldName: String,
        rating: Int,
        reviewId: String,
        fieldId: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = ownerId,
            type = NotificationType.REVIEW_ADDED.value,
            title = "Đánh giá mới!",
            body = "Bạn nhận được đánh giá $rating sao từ $reviewerName cho sân $fieldName.",
            data = NotificationData(
                reviewId = reviewId,
                fieldId = fieldId,
                customData = mapOf(
                    "reviewerName" to reviewerName,
                    "fieldName" to fieldName,
                    "rating" to rating
                )
            ),
            priority = NotificationPriority.NORMAL.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = reviewId,
            relatedEntityType = "REVIEW",
            category = "REVIEW",
            tags = listOf("review", "rating"),
            source = "USER"
        )
    }
    
    /**
     * Tạo notification cho cập nhật sân
     */
    fun buildFieldUpdatedNotification(
        ownerId: String,
        fieldName: String,
        fieldId: String,
        updateType: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = ownerId,
            type = NotificationType.FIELD_UPDATED.value,
            title = "Cập nhật sân!",
            body = "Sân $fieldName của bạn đã được cập nhật thông tin về $updateType.",
            data = NotificationData(
                fieldId = fieldId,
                customData = mapOf(
                    "fieldName" to fieldName,
                    "updateType" to updateType
                )
            ),
            priority = NotificationPriority.NORMAL.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = fieldId,
            relatedEntityType = "FIELD",
            category = "FIELD",
            tags = listOf("field", "update"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho thanh toán thành công
     */
    fun buildPaymentSuccessNotification(
        userId: String,
        amount: Long,
        fieldName: String,
        paymentId: String,
        bookingId: String?
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = userId,
            type = NotificationType.PAYMENT_SUCCESS.value,
            title = "Thanh toán thành công!",
            body = "Bạn đã thanh toán thành công ${amount}đ cho sân $fieldName.",
            data = NotificationData(
                paymentId = paymentId,
                bookingId = bookingId,
                customData = mapOf(
                    "amount" to amount,
                    "fieldName" to fieldName
                )
            ),
            priority = NotificationPriority.NORMAL.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = paymentId,
            relatedEntityType = "PAYMENT",
            category = "PAYMENT",
            tags = listOf("payment", "success"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho thanh toán thất bại
     */
    fun buildPaymentFailedNotification(
        userId: String,
        amount: Long,
        fieldName: String,
        paymentId: String,
        reason: String
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = userId,
            type = NotificationType.PAYMENT_FAILED.value,
            title = "Thanh toán thất bại!",
            body = "Thanh toán ${amount}đ cho sân $fieldName thất bại. Lý do: $reason",
            data = NotificationData(
                paymentId = paymentId,
                customData = mapOf(
                    "amount" to amount,
                    "fieldName" to fieldName,
                    "reason" to reason
                )
            ),
            priority = NotificationPriority.HIGH.value,
            channel = NotificationChannel.IN_APP.value,
            relatedEntityId = paymentId,
            relatedEntityType = "PAYMENT",
            category = "PAYMENT",
            tags = listOf("payment", "failed"),
            source = "SYSTEM"
        )
    }
    
    /**
     * Tạo notification cho thông báo hệ thống
     */
    fun buildSystemAnnouncementNotification(
        userId: String,
        title: String,
        body: String,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        actionUrl: String? = null
    ): Notification {
        return Notification(
            notificationId = UUID.randomUUID().toString(),
            toUserId = userId,
            type = NotificationType.SYSTEM_ANNOUNCEMENT.value,
            title = title,
            body = body,
            data = NotificationData(),
            priority = priority.value,
            channel = NotificationChannel.IN_APP.value,
            actionUrl = actionUrl,
            category = "SYSTEM",
            tags = listOf("system", "announcement"),
            source = "ADMIN"
        )
    }
}
