package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Review
import com.trungkien.fbtp_cn.model.User

/**
 * DIALOG THÊM REVIEW MỚI
 * 
 * Cho phép user:
 * - Chọn số sao (1-5)
 * - Nhập comment
 * - Chọn tags
 * - Ẩn danh (tùy chọn)
 */
@Composable
fun AddReviewDialog(
    fieldId: String,
    currentUser: User?,
    onDismiss: () -> Unit,
    onAddReview: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(mutableSetOf<String>()) }
    var isAnonymous by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Available tags
    val availableTags = listOf(
        "CHẤT LƯỢNG",
        "GIÁ CẢ", 
        "DỊCH VỤ",
        "VỆ SINH",
        "AN TOÀN",
        "THUẬN TIỆN",
        "NHÂN VIÊN",
        "CƠ SỞ VẬT CHẤT"
    )
    
    // Validation
    val isFormValid = comment.trim().isNotEmpty() && rating > 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm đánh giá mới",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Rating section
                RatingSection(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Comment section
                CommentSection(
                    comment = comment,
                    onCommentChanged = { comment = it }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Tags section
                TagsSection(
                    availableTags = availableTags,
                    selectedTags = selectedTags,
                    onTagSelected = { tag ->
                        if (selectedTags.contains(tag)) {
                            selectedTags.remove(tag)
                        } else {
                            selectedTags.add(tag)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Anonymous option
                AnonymousSection(
                    isAnonymous = isAnonymous,
                    onAnonymousChanged = { isAnonymous = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid && currentUser != null) {
                        isLoading = true
                        
                        val newReview = Review(
                            reviewId = "", // Sẽ được Firebase tạo
                            fieldId = fieldId,
                            renterId = currentUser.userId,
                            renterName = if (isAnonymous) "Người dùng ẩn danh" else currentUser.name,
                                                            renterAvatar = if (isAnonymous) "" else currentUser.avatarUrl,
                            rating = rating,
                            comment = comment.trim(),
                            tags = selectedTags.toList(),
                            isAnonymous = isAnonymous
                        )
                        
                        onAddReview(newReview)
                        isLoading = false
                        onDismiss()
                    }
                },
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Thêm đánh giá")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        modifier = modifier
    )
}

/**
 * COMPONENT CHỌN SỐ SAO
 */
@Composable
private fun RatingSection(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Đánh giá của bạn",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                val starValue = index + 1
                val isFilled = starValue <= rating
                
                IconButton(
                    onClick = { onRatingChanged(starValue) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Sao $starValue",
                        tint = if (isFilled) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Rating text
        Text(
            text = when (rating) {
                1 -> "Rất không hài lòng"
                2 -> "Không hài lòng"
                3 -> "Bình thường"
                4 -> "Hài lòng"
                5 -> "Rất hài lòng"
                else -> ""
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * COMPONENT NHẬP COMMENT
 */
@Composable
private fun CommentSection(
    comment: String,
    onCommentChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Nhận xét của bạn",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Hãy chia sẻ trải nghiệm của bạn về sân này...")
            },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        // Character count
        Text(
            text = "${comment.length}/500",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * COMPONENT CHỌN TAGS
 */
@Composable
private fun TagsSection(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Chọn danh mục (tùy chọn)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tags grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(availableTags) { tag ->
                val isSelected = selectedTags.contains(tag)
                
                FilterChip(
                    onClick = { onTagSelected(tag) },
                    label = { Text(tag) },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        if (selectedTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Đã chọn: ${selectedTags.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * COMPONENT TÙY CHỌN ẨN DANH
 */
@Composable
private fun AnonymousSection(
    isAnonymous: Boolean,
    onAnonymousChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isAnonymous,
            onCheckedChange = onAnonymousChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = "Đánh giá ẩn danh",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Tên của bạn sẽ không hiển thị công khai",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
