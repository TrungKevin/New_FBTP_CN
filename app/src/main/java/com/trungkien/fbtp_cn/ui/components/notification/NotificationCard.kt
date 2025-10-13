package com.trungkien.fbtp_cn.ui.components.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.model.NotificationData
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class để define style cho từng loại notification
data class NotificationStyle(
    val backgroundColor: Color,
    val iconColor: Color,
    val iconBackgroundColor: Color,
    val indicatorColor: Color
)

// Function để get style dựa trên notification type
fun getNotificationStyle(type: String): NotificationStyle {
    return when (type) {
        "BOOKING_CREATED", "BOOKING_SUCCESS" -> NotificationStyle(
            backgroundColor = Color(0xFFE8F5E8), // Xanh nhạt
            iconColor = Color(0xFF4CAF50), // Xanh lá
            iconBackgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF4CAF50)
        )
        "REVIEW_ADDED" -> NotificationStyle(
            backgroundColor = Color(0xFFFFF3E0), // Cam nhạt
            iconColor = Color(0xFFFF9800), // Cam
            iconBackgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            indicatorColor = Color(0xFFFF9800)
        )
        "BOOKING_CANCELLED" -> NotificationStyle(
            backgroundColor = Color(0xFFFFEBEE), // Đỏ nhạt
            iconColor = Color(0xFFF44336), // Đỏ
            iconBackgroundColor = Color(0xFFF44336).copy(alpha = 0.1f),
            indicatorColor = Color(0xFFF44336)
        )
        "OPPONENT_JOINED", "OPPONENT_SEARCH" -> NotificationStyle(
            backgroundColor = Color(0xFFE3F2FD), // Xanh dương nhạt
            iconColor = Color(0xFF2196F3), // Xanh dương
            iconBackgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF2196F3)
        )
        "MATCH_RESULT" -> NotificationStyle(
            backgroundColor = Color(0xFFF3E5F5), // Tím nhạt
            iconColor = Color(0xFF9C27B0), // Tím
            iconBackgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF9C27B0)
        )
        "FIELD_UPDATED" -> NotificationStyle(
            backgroundColor = Color(0xFFE0F2F1), // Teal nhạt
            iconColor = Color(0xFF009688), // Teal
            iconBackgroundColor = Color(0xFF009688).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF009688)
        )
        else -> NotificationStyle(
            backgroundColor = Color(0xFFE0F7FA), // Mặc định
            iconColor = Color(0xFF00C853), // Primary color
            iconBackgroundColor = Color(0xFF00C853).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF00C853)
        )
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onItemClick: (Notification) -> Unit
) {
    val notificationStyle = getNotificationStyle(notification.type)
    val backgroundColor = if (notification.isRead) Color.White else notificationStyle.backgroundColor
    val textColor = if (notification.isRead) Color.Gray else Color.Black

    // Fallback tiêu đề/nội dung theo type nếu backend chưa cung cấp
    val displayTitle = when {
        notification.title.isNotBlank() -> notification.title
        else -> when (notification.type) {
            "BOOKING_CREATED" -> "Đặt sân mới!"
            "REVIEW_ADDED" -> "Đánh giá mới!"
            "BOOKING_CANCELLED" -> "Đặt sân bị hủy!"
            "OPPONENT_JOINED" -> "Có đối thủ tham gia!"
            else -> "Thông báo"
        }
    }
    val displayBody = when {
        notification.body.isNotBlank() -> notification.body
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(notification) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon với background và color phù hợp
            NotificationIcon(
                type = notification.type,
                iconColor = notificationStyle.iconColor,
                backgroundColor = notificationStyle.iconBackgroundColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // Unread indicator với color phù hợp
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                UnreadIndicator(color = notificationStyle.indicatorColor)
            }
        }
    }
}

@Composable
fun NotificationIcon(
    type: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    Icon(
        imageVector = getNotificationIcon(type),
        contentDescription = type,
        tint = iconColor,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(8.dp)
    )
}

@Composable
fun UnreadIndicator(color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun NotificationList(
    notifications: List<Notification>,
    onItemClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notifications.isEmpty()) {
        // Hiển thị 4 card placeholder cho từng nhóm thông báo
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item { PlaceholderNotificationCard(title = "Đặt sân mới!") }
            item { PlaceholderNotificationCard(title = "Đánh giá mới!") }
            item { PlaceholderNotificationCard(title = "Đặt sân bị hủy!") }
            item { PlaceholderNotificationCard(title = "Có đối thủ tham gia!") }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun PlaceholderNotificationCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.15f))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hiện chưa có thông báo mới",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Không có thông báo nào.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "BOOKING_CREATED", "BOOKING_SUCCESS" -> Icons.Default.Event
        "OPPONENT_JOINED", "OPPONENT_SEARCH" -> Icons.Default.Person
        "MATCH_RESULT" -> Icons.Default.SportsSoccer
        "FIELD_UPDATED" -> Icons.Default.Update
        "BOOKING_CANCELLED" -> Icons.Default.AlarmOff
        "REVIEW_ADDED" -> Icons.Default.Notifications
        else -> Icons.Default.Notifications
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun NotificationCardPreview() {
    FBTP_CNTheme {
        Column {
            NotificationCard(
                notification = Notification(
                    notificationId = "1",
                    toUserId = "user1",
                    type = "BOOKING_CREATED",
                    title = "Đặt sân thành công",
                    body = "Bạn đã đặt sân Court 1 - Tennis vào lúc 18:00 ngày 15/01/2024.",
                    data = NotificationData(bookingId = "booking1"),
                    isRead = false,
                    createdAt = System.currentTimeMillis() - 3600000
                )
            ) {}
            
            NotificationCard(
                notification = Notification(
                    notificationId = "2",
                    toUserId = "user1",
                    type = "OPPONENT_JOINED",
                    title = "Có đối thủ tham gia",
                    body = "Renter B đã tham gia trận đấu của bạn tại sân Court 2 - Tennis.",
                    data = NotificationData(matchId = "match1"),
                    isRead = true,
                    createdAt = System.currentTimeMillis() - 7200000
                )
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyNotificationStatePreview() {
    FBTP_CNTheme {
        EmptyNotificationState()
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceholderNotificationCardPreview() {
    FBTP_CNTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            PlaceholderNotificationCard(title = "Đặt sân mới!")
            PlaceholderNotificationCard(title = "Đánh giá mới!")
            PlaceholderNotificationCard(title = "Đặt sân bị hủy!")
            PlaceholderNotificationCard(title = "Có đối thủ tham gia!")
        }
    }
}
