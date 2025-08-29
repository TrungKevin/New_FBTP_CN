package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Review(
    val reviewId: String,
    val fieldId: String,
    val renterId: String,
    val renterName: String,
    val renterAvatar: String = "",
    
    // Nội dung đánh giá
    val rating: Int, // 1 | 2 | 3 | 4 | 5
    val comment: String,
    val images: List<String> = emptyList(),
    
    // Tương tác xã hội
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val shares: Int = 0,
    val reportCount: Int = 0,
    
    // Phản hồi và comment con
    val replies: List<ReviewReply> = emptyList(),
    
    // Thông tin thời gian
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Trạng thái và kiểm duyệt
    val status: String = "ACTIVE", // "ACTIVE" | "HIDDEN" | "DELETED" | "PENDING_REVIEW"
    val isVerified: Boolean = false,
    val moderationNote: String? = null,
    
    // Thông tin bổ sung
    val tags: List<String> = emptyList(),
    val helpfulCount: Int = 0,
    val helpfulBy: List<String> = emptyList(),
    val isAnonymous: Boolean = false
)

@Keep
data class ReviewReply(
    val replyId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val userRole: String, // "OWNER" | "RENTER"
    val comment: String,
    val images: List<String> = emptyList(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isOwner: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
