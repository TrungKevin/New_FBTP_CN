package com.trungkien.fbtp_cn.viewmodel

import com.trungkien.fbtp_cn.model.Review
import com.trungkien.fbtp_cn.model.ReviewSummary
import com.trungkien.fbtp_cn.model.User

/**
 * STATE QUẢN LÝ ĐÁNH GIÁ SÂN - Định nghĩa tất cả state cần thiết
 */
data class EvaluateCourtState(
    // Dữ liệu chính
    val reviews: List<Review> = emptyList(),           // Danh sách tất cả reviews
    val reviewSummary: ReviewSummary? = null,          // Thống kê tổng quan
    
    // User và quyền hạn
    val currentUser: User? = null,                     // User hiện tại
    val isOwner: Boolean = false,                      // Có phải owner không
    
    // UI State
    val isLoading: Boolean = false,                    // Đang load dữ liệu
    val selectedReview: Review? = null,                // Review được chọn để xem chi tiết
    val showReplyDialog: Boolean = false,              // Hiển thị dialog thêm reply
    
    // Messages
    val error: String? = null,                         // Lỗi nếu có
    val success: String? = null                        // Thành công nếu có
)

/**
 * EVENTS TỪ UI - Tất cả actions mà UI có thể gửi đến ViewModel
 */
sealed class EvaluateCourtEvent {
    // Load dữ liệu
    data class LoadReviews(val fieldId: String) : EvaluateCourtEvent()
    data class LoadReviewSummary(val fieldId: String) : EvaluateCourtEvent()
    
    // CRUD Reviews
    data class AddReview(val review: Review) : EvaluateCourtEvent()
    data class UpdateReview(val reviewId: String, val updates: Map<String, Any>) : EvaluateCourtEvent()
    data class DeleteReview(val reviewId: String) : EvaluateCourtEvent()
    
    // CRUD Replies
    data class AddReply(val reviewId: String, val reply: com.trungkien.fbtp_cn.model.Reply) : EvaluateCourtEvent()
    data class DeleteReply(val reviewId: String, val replyId: String) : EvaluateCourtEvent()
    
    // Tương tác
    data class LikeReview(val reviewId: String, val userId: String) : EvaluateCourtEvent()
    data class ReportReview(val reviewId: String, val reason: String) : EvaluateCourtEvent()
    
    // UI Control
    data class SetCurrentUser(val user: User, val isOwner: Boolean) : EvaluateCourtEvent()
    data class SelectReview(val review: Review?) : EvaluateCourtEvent()
    data class ShowReplyDialog(val show: Boolean) : EvaluateCourtEvent()
    
    // Messages
    object ClearError : EvaluateCourtEvent()
    object ClearSuccess : EvaluateCourtEvent()
}
