package com.trungkien.fbtp_cn.ui.components.renter.reviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trungkien.fbtp_cn.R
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterReviewCard(
    review: com.trungkien.fbtp_cn.model.Review,
    currentUserId: String?,
    onEdit: (reviewId: String, rating: Int, comment: String) -> Unit,
    onDelete: (reviewId: String) -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf(review.comment) }
    var editRating by remember { mutableStateOf(review.rating) }
    var confirmDelete by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) { 
                detectTapGestures(onTap = { focusManager.clearFocus() }) 
            }, 
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar tròn: luôn lấy avatar mới nhất từ UserRepository
            val avatarData by produceState(initialValue = "", key1 = review.renterId) {
                if (review.renterId.isNotBlank()) {
                    com.trungkien.fbtp_cn.repository.UserRepository().getUserById(
                        review.renterId,
                        onSuccess = { user ->
                            val raw = user.avatarUrl ?: ""
                            value = raw
                        },
                        onError = { _ -> 
                            // Fallback về avatar từ review nếu không lấy được từ UserRepository
                            value = review.renterAvatar
                        }
                    )
                } else {
                    value = review.renterAvatar
                }
            }
            if (avatarData.isNotBlank()) {
                // Thử decode base64 trực tiếp trước (ổn định hơn)
                val decodedBitmap = try {
                    val baseData = if (avatarData.startsWith("data:image")) avatarData.substringAfter(",") else avatarData
                    val bytes = Base64.decode(baseData, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) { null }

                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val data = if (
                        avatarData.startsWith("http", true) ||
                        avatarData.startsWith("data:image", true)
                    ) avatarData else "data:image/jpeg;base64,$avatarData"
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(data)
                            .crossfade(true)
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

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = review.renterName.ifBlank { "Người dùng" }, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(if (isEditing) editRating else review.rating) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            if (!currentUserId.isNullOrEmpty() && currentUserId == review.renterId) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = { menuExpanded = false; isEditing = true; editComment = review.comment; editRating = review.rating },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa") },
                            onClick = { menuExpanded = false; confirmDelete = true },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) }
                        )
                    }
                }
            }
        }

        if (isEditing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { idx ->
                    val star = idx + 1
                    IconButton(onClick = { editRating = star }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (star <= editRating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            OutlinedTextField(
                value = editComment,
                onValueChange = { editComment = it },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { isEditing = false }) { Text("Hủy") }
                Button(onClick = { onEdit(review.reviewId, editRating, editComment); isEditing = false }) { Text("Cập nhật") }
            }
        } else {
            Text(text = review.comment, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Replies under the comment (show for everyone)
        if (review.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                review.replies.forEach { rep ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Avatar for reply: luôn lấy avatar mới nhất từ UserRepository
                        val context = LocalContext.current
                        val repAvatar by produceState(initialValue = "", key1 = rep.userId) {
                            if (rep.userId.isNotBlank()) {
                                com.trungkien.fbtp_cn.repository.UserRepository().getUserById(
                                    rep.userId,
                                    onSuccess = { user ->
                                        val raw = user.avatarUrl ?: ""
                                        value = raw
                                    },
                                    onError = { _ -> 
                                        // Fallback về avatar từ reply nếu không lấy được từ UserRepository
                                        value = rep.userAvatar
                                    }
                                )
                            } else {
                                value = rep.userAvatar
                            }
                        }
                        
                        if (repAvatar.isNotBlank()) {
                            val decoded = try {
                                val base = if (repAvatar.startsWith("data:image")) repAvatar.substringAfter(",") else repAvatar
                                val bytes = Base64.decode(base, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) { null }
                            if (decoded != null) {
                                Image(
                                    bitmap = decoded.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(if (repAvatar.startsWith("http") || repAvatar.startsWith("data:image")) repAvatar else "data:image/jpeg;base64,$repAvatar")
                                        .allowHardware(false)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).clip(CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = rep.userName.ifBlank { "Người dùng" }, fontWeight = FontWeight.Medium)
                                if (rep.isOwner) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary) {
                                        Text(
                                            text = "Chủ sân",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(text = rep.comment, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Xóa đánh giá") },
                text = { Text("Bạn có chắc muốn xóa đánh giá này?") },
                confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(review.reviewId) }) { Text("Xóa") } },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Hủy") } }
            )
        }
    }
}

@Preview
@Composable
private fun RenterReviewCardPreview() {
    val sampleReview = com.trungkien.fbtp_cn.model.Review(
        reviewId = "r1",
        fieldId = "f1",
        renterId = "u1",
        renterName = "Nguyễn Văn A",
        renterAvatar = "",
        rating = 4,
        comment = "Sân đẹp, dịch vụ tốt. Mình rất hài lòng với trải nghiệm ở đây!",
        images = emptyList(),
        tags = listOf("Sạch sẽ", "Dịch vụ tốt"),
        likedBy = listOf("u2", "u3"),

    )
    FBTP_CNTheme {
        Surface {
            RenterReviewCard(
                review = sampleReview,
                currentUserId = "u1",
                onEdit = { _, _, _ -> },
                onDelete = {}
            )
        }
    }
}

