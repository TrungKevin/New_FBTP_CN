package com.trungkien.fbtp_cn.model

data class Notification(
    val notificationId: String,
    val toUserId: String,
    val type: String, // "BOOKING_CREATED" | "OPPONENT_JOINED" | "BOOKING_FULL" | "BOOKING_CANCELLED" | "PAYMENT_SUCCESS" | "REVIEW_ADDED" | "FIELD_UPDATED"
    val title: String,
    val body: String,
    val data: NotificationData,
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Thông tin bổ sung
    val channel: String = "IN_APP", // "IN_APP" | "PUSH"
    val priority: String = "NORMAL" // "NORMAL" | "HIGH"
)

data class NotificationData(
    val bookingId: String? = null,
    val fieldId: String? = null,
    val matchId: String? = null
)
