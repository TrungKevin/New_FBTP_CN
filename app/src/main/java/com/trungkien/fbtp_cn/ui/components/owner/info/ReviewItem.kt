package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Reply
import com.trungkien.fbtp_cn.model.Review
import com.trungkien.fbtp_cn.model.User
import com.trungkien.fbtp_cn.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap

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
    onReply: (String) -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onDeleteReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReplies by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    var showReplyBox by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    
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
            // Replies section
            if (review.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ReplyList(
                    replies = review.replies,
                    currentUser = currentUser,
                    isOwner = isOwner,
                    onDeleteReply = onDeleteReply
                )
            }

            if (showReplyBox) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Viết phản hồi...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val text = replyText.trim()
                            if (text.isNotEmpty()) {
                                onReply(text)
                                replyText = ""
                                showReplyBox = false
                            }
                        }
                    ) { Text("Gửi") }
                }
            }

            ReviewActions(
                review = review,
                currentUser = currentUser,
                onLike = onLike,
                onReply = {
                    showReplyBox = !showReplyBox
                },
                onReport = onReport
            )
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
        // Avatar thực: ưu tiên renterAvatar, nếu trống thì lấy từ UserRepository theo renterId
        val context = LocalContext.current
        val userRepository = remember { UserRepository() }
        val avatarData by produceState(initialValue = review.renterAvatar, key1 = review.renterId, key2 = review.renterAvatar) {
            var v = review.renterAvatar
            if (v.isBlank() && review.renterId.isNotBlank()) {
                userRepository.getUserById(
                    review.renterId,
                    onSuccess = { user -> value = user.avatarUrl ?: "" },
                    onError = { _ -> }
                )
            } else {
                value = v
            }
        }
        if (avatarData.isNotBlank()) {
            val decoded = try {
                val base = if (avatarData.startsWith("data:image")) avatarData.substringAfter(",") else avatarData
                val bytes = Base64.decode(base, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
            if (decoded != null) {
                Image(
                    bitmap = decoded.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (avatarData.startsWith("http") || avatarData.startsWith("data:image")) avatarData else "data:image/jpeg;base64,$avatarData")
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
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
                // Avatar nhỏ thực
                val context = LocalContext.current
                val userRepository = remember { UserRepository() }
                val repAvatar by produceState(initialValue = reply.userAvatar, key1 = reply.userId, key2 = reply.userAvatar) {
                    var v = reply.userAvatar
                    if (v.isBlank() && reply.userId.isNotBlank()) {
                        userRepository.getUserById(
                            reply.userId,
                            onSuccess = { user -> value = user.avatarUrl ?: "" },
                            onError = { _ -> }
                        )
                    } else {
                        value = v
                    }
                }
                if (repAvatar.isNotBlank()) {
                    val bm = try {
                        val base = if (repAvatar.startsWith("data:image")) repAvatar.substringAfter(",") else repAvatar
                        val bytes = Base64.decode(base, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) { null }
                    if (bm != null) {
                        Image(
                            bitmap = bm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (repAvatar.startsWith("http") || repAvatar.startsWith("data:image")) repAvatar else "data:image/jpeg;base64,$repAvatar")
                                .allowHardware(false)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                    )
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
