package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp

/**
 * MODEL ĐÁNH GIÁ SÂN - Hệ thống review và comment
 * 
 * CẤU TRÚC:
 * - Review chính: Đánh giá sao + comment của khách hàng
 * - Replies: Hệ thống comment con (giống Facebook)
 * - Tương tác xã hội: Like, share, report
 * - Quyền hạn: Owner có quyền xóa tất cả, Renter chỉ xóa comment của mình
 */
@Keep
data class Review(
    val reviewId: String = "",                    // ID duy nhất
    val fieldId: String = "",                     // ID sân (liên kết Fields)
    val renterId: String = "",                    // ID khách hàng (liên kết Users)
    val renterName: String = "",                  // Tên khách hàng (để hiển thị nhanh)
    val renterAvatar: String = "",                // Avatar khách hàng (để hiển thị nhanh)
    
    // Nội dung đánh giá
    val rating: Int = 5,                          // Điểm đánh giá (1-5)
    val comment: String = "",                     // Nội dung comment/đánh giá
    val images: List<String> = emptyList(),       // Ảnh đánh giá (nếu có)
    
    // Tương tác xã hội
    val likes: Int = 0,                           // Số lượt thích
    val likedBy: List<String> = emptyList(),      // Danh sách userId đã like
    val shares: Int = 0,                          // Số lượt chia sẻ
    val reportCount: Int = 0,                     // Số lượt báo cáo
    
    // Phản hồi và comment con
    val replies: List<Reply> = emptyList(),       // Phản hồi (comment con)
    
    // Thông tin thời gian
    val createdAt: Timestamp? = null,             // Thời điểm tạo
    val updatedAt: Timestamp? = null,             // Thời điểm cập nhật
    
    // Trạng thái và kiểm duyệt
    val status: String = "ACTIVE",                // "ACTIVE" | "HIDDEN" | "DELETED" | "PENDING_REVIEW"
    val verified: Boolean = false,                // Đã xác minh chưa (nếu là khách VIP) - Firebase field name
    val moderationNote: String = "",              // Ghi chú kiểm duyệt
    
    // Thông tin bổ sung
    val tags: List<String> = emptyList(),         // Tags liên quan ["CHẤT LƯỢNG", "GIÁ CẢ", "DỊCH VỤ"]
    val helpfulCount: Int = 0,                    // Số người đánh giá hữu ích
    val helpfulBy: List<String> = emptyList(),    // Danh sách userId đánh giá hữu ích
    val anonymous: Boolean = false                // Có ẩn danh không - Firebase field name
)

/**
 * MODEL PHẢN HỒI (Reply) - Comment con trong review
 */
@Keep
data class Reply(
    val replyId: String = "",                     // ID phản hồi
    val userId: String = "",                      // ID người phản hồi
    val userName: String = "",                    // Tên người phản hồi
    val userAvatar: String = "",                  // Avatar người phản hồi
    val userRole: String = "RENTER",              // Vai trò người phản hồi: "OWNER" | "RENTER"
    val comment: String = "",                     // Nội dung phản hồi
    val images: List<String> = emptyList(),       // Ảnh phản hồi (nếu có)
    val likes: Int = 0,                           // Số lượt thích phản hồi
    val likedBy: List<String> = emptyList(),      // Danh sách userId đã like phản hồi
    val owner: Boolean = false,                   // ✅ FIX: Firebase expects 'owner' field, not 'owner'
    val createdAt: Timestamp? = null,             // Thời điểm phản hồi
    val updatedAt: Timestamp? = null              // Thời điểm cập nhật
)

/**
 * MODEL TẠO REVIEW MỚI - Dùng để tạo review
 */
@Keep
data class CreateReviewRequest(
    val fieldId: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isAnonymous: Boolean = false
)

/**
 * MODEL TẠO REPLY MỚI - Dùng để tạo reply
 */
@Keep
data class CreateReplyRequest(
    val reviewId: String = "",
    val comment: String = "",
    val images: List<String> = emptyList()
)

/**
 * MODEL THỐNG KÊ REVIEW - Dùng để hiển thị tổng quan
 */
@Keep
data class ReviewSummary(
    val fieldId: String = "",
    val averageRating: Float = 0f,                // Điểm trung bình
    val totalReviews: Int = 0,                    // Tổng số đánh giá
    val ratingDistribution: Map<Int, Int> = emptyMap(), // Phân bố sao (1-5)
    val tagStats: Map<String, Int> = emptyMap(),  // Thống kê theo tags
    val lastUpdated: Timestamp? = null            // Lần cập nhật cuối
)

/**
 * ENUM TRẠNG THÁI REVIEW
 */
enum class ReviewStatus(val value: String) {
    ACTIVE("ACTIVE"),
    HIDDEN("HIDDEN"),
    DELETED("DELETED"),
    PENDING_REVIEW("PENDING_REVIEW")
}

/**
 * ENUM VAI TRÒ NGƯỜI DÙNG
 */
enum class UserRole(val value: String) {
    OWNER("OWNER"),
    RENTER("RENTER")
}
