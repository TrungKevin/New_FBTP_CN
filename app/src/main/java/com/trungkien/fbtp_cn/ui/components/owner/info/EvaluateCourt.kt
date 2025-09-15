package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtEvent
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtState
import com.trungkien.fbtp_cn.viewmodel.EvaluateCourtViewModel

/**
 * COMPONENT CHÍNH HIỂN THỊ ĐÁNH GIÁ SÂN
 * 
 * GOM TẤT CẢ CÁC COMPONENT NHỎ:
 * - ReviewSummary: Tổng quan đánh giá
 * - ReviewList: Danh sách đánh giá
 * - AddReview: Thêm đánh giá mới
 */
@Composable
fun EvaluateCourt(
    fieldId: String,
    currentUser: User?,
    isOwner: Boolean,
    viewModel: EvaluateCourtViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // LaunchedEffect để load data khi component được tạo
    LaunchedEffect(fieldId, currentUser, isOwner) {
        // Set current user và quyền hạn
        if (currentUser != null) {
            viewModel.handleEvent(EvaluateCourtEvent.SetCurrentUser(currentUser, isOwner))
        }
        
        // Load reviews và summary
        viewModel.handleEvent(EvaluateCourtEvent.LoadReviews(fieldId))
        viewModel.handleEvent(EvaluateCourtEvent.LoadReviewSummary(fieldId))
        
        println("🚀 DEBUG: EvaluateCourt - Bắt đầu load data cho sân: $fieldId")
        println("👤 DEBUG: Current user: ${currentUser?.name}, isOwner: $isOwner")
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        EvaluateCourtHeader(
            fieldId = fieldId,
            currentUser = currentUser,
            isOwner = isOwner,
            onAddReview = {
                // TODO: Show add review dialog
                println("✅ DEBUG: User muốn thêm review mới")
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Main content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Review Summary
                item {
                    ReviewSummary(
                        summary = uiState.reviewSummary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Reviews List Header
                item {
                    ReviewsListHeader(
                        totalReviews = uiState.reviews.size,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Reviews List
                if (uiState.reviews.isNotEmpty()) {
                    items(uiState.reviews) { review ->
                        ReviewItem(
                            review = review,
                            currentUser = currentUser,
                            isOwner = isOwner,
                            onLike = {
                                currentUser?.let { user ->
                                    viewModel.handleEvent(
                                        EvaluateCourtEvent.LikeReview(review.reviewId, user.userId)
                                    )
                                }
                            },
                            onReply = { text ->
                                currentUser?.let { user ->
                                    viewModel.handleEvent(
                                        EvaluateCourtEvent.AddReply(
                                            reviewId = review.reviewId,
                                            reply = com.trungkien.fbtp_cn.model.Reply(
                                                userId = user.userId,
                                                userName = user.name,
                                                userAvatar = user.avatarUrl,
                                                userRole = if (isOwner) com.trungkien.fbtp_cn.model.UserRole.OWNER.name else com.trungkien.fbtp_cn.model.UserRole.RENTER.name,
                                                comment = text,
                                                isOwner = isOwner
                                            )
                                        )
                                    )
                                }
                            },
                            onReport = {
                                // TODO: Show report dialog
                                println("✅ DEBUG: User muốn report review: ${review.reviewId}")
                            },
                            onDelete = {
                                viewModel.handleEvent(
                                    EvaluateCourtEvent.DeleteReview(review.reviewId)
                                )
                            },
                            onDeleteReply = { replyId ->
                                viewModel.handleEvent(
                                    EvaluateCourtEvent.DeleteReply(review.reviewId, replyId)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Empty state
                    item {
                        EmptyReviewsState(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // Error dialog
    uiState.error?.let { error ->
        ErrorDialog(
            error = error,
            onDismiss = {
                viewModel.handleEvent(EvaluateCourtEvent.ClearError)
            }
        )
    }
    
    // Success message
    uiState.success?.let { success ->
        SuccessSnackbar(
            message = success,
            onDismiss = {
                viewModel.handleEvent(EvaluateCourtEvent.ClearSuccess)
            }
        )
    }
}

/**
 * COMPONENT HEADER CỦA EVALUATECOURT
 */
@Composable
private fun EvaluateCourtHeader(
    fieldId: String,
    currentUser: User?,
    isOwner: Boolean,
    onAddReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Đánh giá sân",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Xem và đánh giá chất lượng sân",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Add Review button (chỉ hiển thị cho renter, không phải owner)
            if (currentUser != null && !isOwner) {
                Button(
                    onClick = onAddReview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm đánh giá"
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Đánh giá")
                }
            }
        }
    }
}

/**
 * COMPONENT HEADER CỦA DANH SÁCH REVIEWS
 */
@Composable
private fun ReviewsListHeader(
    totalReviews: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tất cả đánh giá",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "$totalReviews",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * COMPONENT HIỂN THỊ KHI KHÔNG CÓ REVIEWS
 */
@Composable
private fun EmptyReviewsState(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chưa có đánh giá nào",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Hãy là người đầu tiên đánh giá sân này!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * DIALOG HIỂN THỊ LỖI
 */
@Composable
private fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Lỗi")
        },
        text = {
            Text(error)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

/**
 * SNACKBAR HIỂN THỊ THÀNH CÔNG
 */
@Composable
private fun SuccessSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    // TODO: Implement Snackbar
    // Hiện tại chỉ log ra console
    LaunchedEffect(message) {
        println("✅ SUCCESS: $message")
        // Auto dismiss sau 3 giây
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }
}



