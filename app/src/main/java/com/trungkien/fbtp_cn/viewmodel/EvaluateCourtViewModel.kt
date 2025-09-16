package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * VIEWMODEL QUẢN LÝ ĐÁNH GIÁ SÂN - Xử lý tất cả logic liên quan đến review
 */
class EvaluateCourtViewModel(
    private val repository: ReviewRepository = ReviewRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EvaluateCourtState())
    val uiState: StateFlow<EvaluateCourtState> = _uiState.asStateFlow()
    
    /**
     * Xử lý các events từ UI
     */
    fun handleEvent(event: EvaluateCourtEvent) {
        println("🎮 DEBUG: ViewModel.handleEvent called - event: ${event::class.simpleName}")
        when (event) {
            is EvaluateCourtEvent.LoadReviews -> loadReviews(event.fieldId)
            is EvaluateCourtEvent.LoadReviewSummary -> loadReviewSummary(event.fieldId)
            is EvaluateCourtEvent.AddReview -> addReview(event.review)
            is EvaluateCourtEvent.AddReply -> {
                println("🎮 DEBUG: AddReply event received - reviewId: ${event.reviewId}, reply: ${event.reply.comment}")
                addReply(event.reviewId, event.reply)
            }
            is EvaluateCourtEvent.LikeReview -> likeReview(event.reviewId, event.userId)
            is EvaluateCourtEvent.DeleteReview -> deleteReview(event.reviewId)
            is EvaluateCourtEvent.DeleteReply -> deleteReply(event.reviewId, event.replyId)
            is EvaluateCourtEvent.UpdateReply -> updateReply(event.reviewId, event.replyId, event.updates)
            is EvaluateCourtEvent.ReportReview -> reportReview(event.reviewId, event.reason)
            is EvaluateCourtEvent.UpdateReview -> updateReview(event.reviewId, event.updates)
            is EvaluateCourtEvent.SetCurrentUser -> setCurrentUser(event.user, event.isOwner)
            is EvaluateCourtEvent.SelectReview -> selectReview(event.review)
            is EvaluateCourtEvent.ShowReplyDialog -> showReplyDialog(event.show)
            is EvaluateCourtEvent.ClearError -> clearError()
            is EvaluateCourtEvent.ClearSuccess -> clearSuccess()
        }
    }
    
    /**
     * Load tất cả reviews của một sân
     */
    private fun loadReviews(fieldId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.getReviewsByFieldId(fieldId)
                result.fold(
                    onSuccess = { reviews ->
                        _uiState.value = _uiState.value.copy(
                            reviews = reviews,
                            isLoading = false
                        )
                        println("✅ DEBUG: Đã load ${reviews.size} reviews cho sân $fieldId")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi load reviews: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi load reviews: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định: ${e.message}")
            }
        }
    }
    
    /**
     * Load review summary (thống kê) của một sân
     */
    private fun loadReviewSummary(fieldId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getReviewSummary(fieldId)
                result.fold(
                    onSuccess = { summary ->
                        _uiState.value = _uiState.value.copy(reviewSummary = summary)
                        println("✅ DEBUG: Đã load review summary cho sân $fieldId")
                        println("📊 DEBUG: Điểm trung bình: ${summary.averageRating}, Tổng reviews: ${summary.totalReviews}")
                    },
                    onFailure = { exception ->
                        println("❌ DEBUG: Lỗi load review summary: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("❌ DEBUG: Lỗi không xác định khi load summary: ${e.message}")
            }
        }
    }
    
    /**
     * Thêm review mới
     */
    private fun addReview(review: Review) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.addReview(review)
                result.fold(
                    onSuccess = { reviewId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Thêm đánh giá thành công!"
                        )
                        println("✅ DEBUG: Đã thêm review thành công với ID: $reviewId")
                        
                        // Reload reviews sau khi thêm thành công
                        review.fieldId.let { loadReviews(it) }
                        review.fieldId.let { loadReviewSummary(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi thêm review: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi thêm review: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định khi thêm review: ${e.message}")
            }
        }
    }
    
    /**
     * Thêm reply mới
     */
    private fun addReply(reviewId: String, reply: Reply) {
        viewModelScope.launch {
            println("🚀 DEBUG: addReply called - reviewId: $reviewId, reply: ${reply.comment}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.addReply(reviewId, reply)
                result.fold(
                    onSuccess = { replyId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Thêm phản hồi thành công!"
                        )
                        println("✅ DEBUG: Đã thêm reply thành công với ID: $replyId")
                        
                        // Cập nhật UI ngay lập tức bằng cách thêm reply vào state hiện tại
                        val currentReviews = _uiState.value.reviews.toMutableList()
                        val reviewIndex = currentReviews.indexOfFirst { it.reviewId == reviewId }
                        
                        println("🔍 DEBUG: Optimistic update - reviewIndex: $reviewIndex, currentReplies: ${currentReviews[reviewIndex].replies.size}")
                        
                        if (reviewIndex != -1) {
                            val newReply = reply.copy(
                                replyId = replyId,
                                createdAt = com.google.firebase.Timestamp.now(),
                                updatedAt = com.google.firebase.Timestamp.now()
                            )
                            
                            val updatedReview = currentReviews[reviewIndex].copy(
                                replies = currentReviews[reviewIndex].replies + newReply
                            )
                            currentReviews[reviewIndex] = updatedReview
                            
                            println("🔍 DEBUG: Optimistic update - newReplies: ${updatedReview.replies.size}")
                            println("🔍 DEBUG: New reply: ${newReply.comment}")
                            
                            _uiState.value = _uiState.value.copy(reviews = currentReviews)
                        }
                        
                        // Reload reviews để đồng bộ với Firebase (background) - delay một chút để optimistic update có thời gian hiển thị
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { fieldId ->
                            kotlinx.coroutines.delay(1000) // Delay 1 giây để user thấy optimistic update
                            loadReviews(fieldId)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi thêm phản hồi: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi thêm reply: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định khi thêm reply: ${e.message}")
            }
        }
    }
    
    /**
     * Like/Unlike review
     */
    private fun likeReview(reviewId: String, userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.toggleLikeReview(reviewId, userId)
                result.fold(
                    onSuccess = {
                        println("✅ DEBUG: Đã toggle like review $reviewId cho user $userId")
                        // Reload reviews để cập nhật UI
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { 
                            loadReviews(it) 
                        }
                    },
                    onFailure = { exception ->
                        println("❌ DEBUG: Lỗi toggle like: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("❌ DEBUG: Lỗi không xác định khi toggle like: ${e.message}")
            }
        }
    }
    
    /**
     * Xóa review
     */
    private fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.deleteReview(reviewId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Xóa đánh giá thành công!"
                        )
                        println("✅ DEBUG: Đã xóa review $reviewId thành công")
                        
                        // Reload reviews sau khi xóa thành công
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { 
                            loadReviews(it) 
                            loadReviewSummary(it)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi xóa review: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi xóa review: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định khi xóa review: ${e.message}")
            }
        }
    }
    
    /**
     * Xóa reply
     */
    private fun deleteReply(reviewId: String, replyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.deleteReply(reviewId, replyId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Xóa phản hồi thành công!"
                        )
                        println("✅ DEBUG: Đã xóa reply $replyId thành công")
                        
                        // Reload reviews để cập nhật UI
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { 
                            loadReviews(it) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi xóa phản hồi: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi xóa reply: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định khi xóa reply: ${e.message}")
            }
        }
    }
    
    /**
     * Cập nhật reply
     */
    private fun updateReply(reviewId: String, replyId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = repository.updateReply(reviewId, replyId, updates)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Cập nhật phản hồi thành công!"
                        )
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { loadReviews(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi cập nhật phản hồi: ${exception.message}",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Báo cáo review
     */
    private fun reportReview(reviewId: String, reason: String) {
        viewModelScope.launch {
            try {
                val result = repository.reportReview(reviewId, reason)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            success = "Báo cáo đánh giá thành công!"
                        )
                        println("✅ DEBUG: Đã báo cáo review $reviewId thành công")
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi báo cáo: ${exception.message}"
                        )
                        println("❌ DEBUG: Lỗi báo cáo review: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}"
                )
                println("❌ DEBUG: Lỗi không xác định khi báo cáo: ${e.message}")
            }
        }
    }
    
    /**
     * Cập nhật review
     */
    private fun updateReview(reviewId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.updateReview(reviewId, updates)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Cập nhật đánh giá thành công!"
                        )
                        println("✅ DEBUG: Đã cập nhật review $reviewId thành công")
                        
                        // Reload reviews để cập nhật UI
                        _uiState.value.reviews.find { it.reviewId == reviewId }?.fieldId?.let { 
                            loadReviews(it) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Lỗi cập nhật review: ${exception.message}",
                            isLoading = false
                        )
                        println("❌ DEBUG: Lỗi cập nhật review: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Lỗi không xác định: ${e.message}",
                    isLoading = false
                )
                println("❌ DEBUG: Lỗi không xác định khi cập nhật review: ${e.message}")
            }
        }
    }
    
    /**
     * Set current user và quyền hạn
     */
    private fun setCurrentUser(user: User, isOwner: Boolean) {
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isOwner = isOwner
        )
        println("✅ DEBUG: Đã set current user: ${user.name}, isOwner: $isOwner")
    }
    
    /**
     * Chọn review để xem chi tiết
     */
    private fun selectReview(review: Review?) {
        _uiState.value = _uiState.value.copy(selectedReview = review)
        println("✅ DEBUG: Đã chọn review: ${review?.reviewId ?: "null"}")
    }
    
    /**
     * Hiển thị/ẩn dialog thêm reply
     */
    private fun showReplyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReplyDialog = show)
        println("✅ DEBUG: Show reply dialog: $show")
    }
    
    /**
     * Clear error message
     */
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear success message
     */
    private fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = null)
    }
}
