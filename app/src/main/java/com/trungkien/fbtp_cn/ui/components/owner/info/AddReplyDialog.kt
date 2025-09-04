package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Reply
import com.trungkien.fbtp_cn.model.User

/**
 * DIALOG THÊM REPLY MỚI
 * 
 * Cho phép user:
 * - Nhập comment phản hồi
 * - Ẩn danh (tùy chọn)
 */
@Composable
fun AddReplyDialog(
    reviewId: String,
    currentUser: User?,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onAddReply: (Reply) -> Unit,
    modifier: Modifier = Modifier
) {
    var comment by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Validation
    val isFormValid = comment.trim().isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm phản hồi",
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
                // Comment section
                CommentSection(
                    comment = comment,
                    onCommentChanged = { comment = it }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Anonymous option
                AnonymousSection(
                    isAnonymous = isAnonymous,
                    onAnonymousChanged = { isAnonymous = it }
                )
                
                // Owner badge info
                if (isOwner) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Chủ sân",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Bạn sẽ được hiển thị là chủ sân",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid && currentUser != null) {
                        isLoading = true
                        
                        val newReply = Reply(
                            replyId = "", // Sẽ được Firebase tạo
                            userId = currentUser.userId,
                            userName = if (isAnonymous) "Người dùng ẩn danh" else currentUser.name,
                            userAvatar = if (isAnonymous) "" else currentUser.avatarUrl,
                            userRole = if (isOwner) "OWNER" else "RENTER",
                            comment = comment.trim(),
                            isOwner = isOwner
                        )
                        
                        onAddReply(newReply)
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
                    Text("Thêm phản hồi")
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
 * COMPONENT NHẬP COMMENT PHẢN HỒI
 */
@Composable
private fun CommentSection(
    comment: String,
    onCommentChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Phản hồi của bạn",
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
                Text("Hãy chia sẻ ý kiến của bạn...")
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
            text = "${comment.length}/300",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
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
                text = "Phản hồi ẩn danh",
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
