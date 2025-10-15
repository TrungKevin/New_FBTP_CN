package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

// Enum để define các loại notification
enum class NotificationType(val value: String) {
    BOOKING_CREATED("BOOKING_CREATED"),
    BOOKING_SUCCESS("BOOKING_SUCCESS"),
    BOOKING_CANCELLED("BOOKING_CANCELLED"),
    OPPONENT_JOINED("OPPONENT_JOINED"),
    OPPONENT_SEARCH("OPPONENT_SEARCH"),
    MATCH_RESULT("MATCH_RESULT"),
    REVIEW_ADDED("REVIEW_ADDED"),
    FIELD_UPDATED("FIELD_UPDATED"),
    PAYMENT_SUCCESS("PAYMENT_SUCCESS"),
    PAYMENT_FAILED("PAYMENT_FAILED"),
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT"),
    BOOKING_CONFIRMED("BOOKING_CONFIRMED"),
    BOOKING_CANCELLED_BY_OWNER("BOOKING_CANCELLED_BY_OWNER"),
    REVIEW_REPLY("REVIEW_REPLY"),
    WAITING_OPPONENT_BOOKING("WAITING_OPPONENT_BOOKING"), // ✅ NEW: Renter đặt sân chờ đối thủ
    OPPONENT_AVAILABLE("OPPONENT_AVAILABLE") // ✅ NEW: Có renter chờ đối thủ
}

// Enum để define priority
enum class NotificationPriority(val value: String) {
    LOW("LOW"),
    NORMAL("NORMAL"),
    HIGH("HIGH"),
    URGENT("URGENT")
}

// Enum để define channel
enum class NotificationChannel(val value: String) {
    IN_APP("IN_APP"),
    PUSH("PUSH"),
    EMAIL("EMAIL"),
    SMS("SMS")
}

@Keep
data class Notification(
    val notificationId: String = "",
    val toUserId: String = "",
    val type: String = "", // Sử dụng NotificationType.value
    val title: String = "",
    val body: String = "",
    val data: NotificationData = NotificationData(),
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Thông tin bổ sung
    val channel: String = NotificationChannel.IN_APP.value,
    val priority: String = NotificationPriority.NORMAL.value,
    
    // Metadata để tracking
    val senderId: String? = null, // ID của người gửi (nếu có)
    val senderName: String? = null, // Tên người gửi
    val relatedEntityId: String? = null, // ID của entity liên quan (field, booking, etc.)
    val relatedEntityType: String? = null, // Loại entity (FIELD, BOOKING, MATCH, etc.)
    
    // Thông tin để hiển thị
    val iconUrl: String? = null, // URL icon tùy chỉnh
    val imageUrl: String? = null, // URL hình ảnh
    val actionUrl: String? = null, // URL để navigate khi click
    
    // Thông tin để grouping
    val category: String? = null, // Danh mục để group notifications
    val tags: List<String> = emptyList(), // Tags để filter
    
    // Thông tin để analytics
    val source: String? = null, // Nguồn gốc notification (SYSTEM, USER, ADMIN)
    val campaignId: String? = null, // ID campaign nếu là marketing
)

@Keep
data class NotificationData(
    val bookingId: String? = null,
    val fieldId: String? = null,
    val matchId: String? = null,
    val userId: String? = null,
    val reviewId: String? = null,
    val paymentId: String? = null,
    
    // Custom data cho từng loại notification
    val customData: Map<String, Any> = emptyMap()
)

// Model để tạo notification mới
@Keep
data class CreateNotificationRequest(
    val toUserId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: NotificationData,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val channel: NotificationChannel = NotificationChannel.IN_APP,
    val senderId: String? = null,
    val senderName: String? = null,
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null,
    val iconUrl: String? = null,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val source: String? = null,
    val campaignId: String? = null
)

// Model để filter notifications
@Keep
data class NotificationFilter(
    val userId: String,
    val type: NotificationType? = null,
    val isRead: Boolean? = null,
    val priority: NotificationPriority? = null,
    val channel: NotificationChannel? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val limit: Int = 50,
    val offset: Int = 0
)

// Model để bulk operations
@Keep
data class NotificationBulkAction(
    val notificationIds: List<String>,
    val action: String, // "MARK_AS_READ", "MARK_AS_UNREAD", "DELETE"
    val userId: String
)
