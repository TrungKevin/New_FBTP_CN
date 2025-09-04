package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Reply
import com.trungkien.fbtp_cn.model.Review
import com.trungkien.fbtp_cn.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * COMPONENT HIỂN THỊ TỪNG REVIEW RIÊNG BIỆT
 * 
 * Hiển thị:
 * - Thông tin người đánh giá
 * - Sao và comment
 * - Ảnh (nếu có)
 * - Các nút tương tác (like, reply, report)
 * - Danh sách phản hồi
 */
@Composable
fun ReviewItem(
    review: Review,
    currentUser: User?,
    isOwner: Boolean,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onDeleteReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReplies by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header - Thông tin người đánh giá
            ReviewHeader(
                review = review,
                currentUser = currentUser,
                isOwner = isOwner,
                onMoreOptions = { showMoreOptions = true },
                onDelete = onDelete
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rating và Comment
            ReviewContent(review = review)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions - Like, Reply, Report
            ReviewActions(
                review = review,
                currentUser = currentUser,
                onLike = onLike,
                onReply = onReply,
                onReport = onReport
            )
            
            // Replies section
            if (review.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Toggle replies
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${review.replies.size} phản hồi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { showReplies = !showReplies }
                    ) {
                        Icon(
                            imageVector = if (showReplies) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showReplies) "Ẩn phản hồi" else "Hiện phản hồi"
                        )
                    }
                }
                
                // Hiển thị replies
                if (showReplies) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReplyList(
                        replies = review.replies,
                        currentUser = currentUser,
                        isOwner = isOwner,
                        onDeleteReply = onDeleteReply
                    )
                }
            }
        }
    }
    
    // More options dialog
    if (showMoreOptions) {
        MoreOptionsDialog(
            review = review,
            currentUser = currentUser,
            isOwner = isOwner,
            onDismiss = { showMoreOptions = false },
            onDelete = {
                onDelete()
                showMoreOptions = false
            },
            onReport = {
                onReport()
                showMoreOptions = false
            }
        )
    }
}

/**
 * COMPONENT HEADER CỦA REVIEW
 */
@Composable
private fun ReviewHeader(
    review: Review,
    currentUser: User?,
    isOwner: Boolean,
    onMoreOptions: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (review.renterAvatar.isNotEmpty()) {
                    // TODO: Load image từ URL
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Thông tin user
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (review.isAnonymous) "Người dùng ẩn danh" else review.renterName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Thời gian
            review.createdAt?.let { timestamp ->
                val date = Date(timestamp.seconds * 1000)
                val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Text(
                    text = formatter.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // More options button
        if (currentUser != null && (isOwner || currentUser.userId == review.renterId)) {
            IconButton(onClick = onMoreOptions) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Thêm tùy chọn"
                )
            }
        }
    }
}

/**
 * COMPONENT NỘI DUNG REVIEW
 */
@Composable
private fun ReviewContent(
    review: Review,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Rating stars
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val starValue = index + 1
                val isFilled = starValue <= review.rating
                
                Icon(
                    imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Sao $starValue",
                    tint = if (isFilled) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "${review.rating}/5",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Comment
        if (review.comment.isNotEmpty()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Tags
        if (review.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                review.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Images (nếu có)
        if (review.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // TODO: Implement image gallery
            Text(
                text = "📷 ${review.images.size} ảnh",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * COMPONENT ACTIONS CỦA REVIEW
 */
@Composable
private fun ReviewActions(
    review: Review,
    currentUser: User?,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onReport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLike) {
                val isLiked = currentUser?.userId in review.likedBy
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Bỏ thích" else "Thích",
                    tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "${review.likes}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Reply button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onReply) {
                Icon(
                    imageVector = Icons.Default.Reply,
                    contentDescription = "Phản hồi"
                )
            }
            
            Text(
                text = "Phản hồi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Report button
        if (currentUser != null && currentUser.userId != review.renterId) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onReport) {
                    Icon(
                        imageVector = Icons.Default.Report,
                        contentDescription = "Báo cáo"
                    )
                }
                
                Text(
                    text = "Báo cáo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * COMPONENT HIỂN THỊ DANH SÁCH PHẢN HỒI
 */
@Composable
private fun ReplyList(
    replies: List<Reply>,
    currentUser: User?,
    isOwner: Boolean,
    onDeleteReply: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        replies.forEach { reply ->
            ReplyItem(
                reply = reply,
                currentUser = currentUser,
                isOwner = isOwner,
                onDelete = { onDeleteReply(reply.replyId) }
            )
            
            if (reply != replies.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * COMPONENT HIỂN THỊ TỪNG PHẢN HỒI
 */
@Composable
private fun ReplyItem(
    reply: Reply,
    currentUser: User?,
    isOwner: Boolean,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar nhỏ
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (reply.isOwner) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = if (reply.isOwner) MaterialTheme.colorScheme.onPrimaryContainer 
                                   else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Thông tin user
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reply.userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (reply.isOwner) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Chủ sân",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // Thời gian
                    reply.createdAt?.let { timestamp ->
                        val date = Date(timestamp.seconds * 1000)
                        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        Text(
                            text = formatter.format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Delete button (chỉ owner hoặc người tạo)
                if (currentUser != null && (isOwner || currentUser.userId == reply.userId)) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa phản hồi",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nội dung phản hồi
            Text(
                text = reply.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Like count
            if (reply.likes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Thích",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${reply.likes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * DIALOG HIỂN THỊ TÙY CHỌN THÊM
 */
@Composable
private fun MoreOptionsDialog(
    review: Review,
    currentUser: User?,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tùy chọn")
        },
        text = {
            Column {
                // Delete option (chỉ owner hoặc người tạo)
                if (currentUser != null && (isOwner || currentUser.userId == review.renterId)) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Xóa đánh giá",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Report option (chỉ người khác)
                if (currentUser != null && currentUser.userId != review.renterId) {
                    TextButton(
                        onClick = onReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Báo cáo")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
