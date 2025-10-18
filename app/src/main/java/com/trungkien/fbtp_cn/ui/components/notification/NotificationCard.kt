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
        "BOOKING_CREATED", "BOOKING_SUCCESS", "BOOKING_CONFIRMED" -> NotificationStyle(
            backgroundColor = Color(0xFFE8F5E8), // Xanh nhạt
            iconColor = Color(0xFF4CAF50), // Xanh lá
            iconBackgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF4CAF50)
        )
        "REVIEW_ADDED", "REVIEW_REPLY" -> NotificationStyle(
            backgroundColor = Color(0xFFFFF3E0), // Cam nhạt
            iconColor = Color(0xFFFF9800), // Cam
            iconBackgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            indicatorColor = Color(0xFFFF9800)
        )
        "BOOKING_CANCELLED", "BOOKING_CANCELLED_BY_OWNER" -> NotificationStyle(
            backgroundColor = Color(0xFFFFEBEE), // Đỏ nhạt
            iconColor = Color(0xFFF44336), // Đỏ
            iconBackgroundColor = Color(0xFFF44336).copy(alpha = 0.1f),
            indicatorColor = Color(0xFFF44336)
        )
        "OPPONENT_JOINED", "OPPONENT_MATCHED", "OPPONENT_SEARCH", "OPPONENT_AVAILABLE" -> NotificationStyle(
            backgroundColor = Color(0xFFE3F2FD), // Xanh dương nhạt
            iconColor = Color(0xFF2196F3), // Xanh dương
            iconBackgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f),
            indicatorColor = Color(0xFF2196F3)
        )
        "WAITING_OPPONENT_BOOKING" -> NotificationStyle(
            backgroundColor = Color(0xFFFFF3E0), // Cam nhạt
            iconColor = Color(0xFFFF9800), // Cam
            iconBackgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
            indicatorColor = Color(0xFFFF9800)
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
    val backgroundColor = if (notification.read) Color.White else notificationStyle.backgroundColor
    val textColor = if (notification.read) Color.Gray else Color.Black

    // Fallback tiêu đề/nội dung theo type nếu backend chưa cung cấp
    val displayTitle = when {
        notification.title.isNotBlank() -> notification.title
        else -> when (notification.type) {
            "BOOKING_CREATED" -> "Đặt sân mới!"
            "BOOKING_CONFIRMED" -> "Đặt sân được xác nhận!"
            "REVIEW_ADDED" -> "Đánh giá mới!"
            "REVIEW_REPLY" -> "Phản hồi đánh giá!"
            "BOOKING_CANCELLED" -> "Đặt sân bị hủy!"
            "BOOKING_CANCELLED_BY_OWNER" -> "Đặt sân bị hủy!"
            "OPPONENT_JOINED" -> "Có đối thủ tham gia!"
            "MATCH_RESULT" -> "Kết quả trận đấu!"
            "PAYMENT_SUCCESS" -> "Thanh toán thành công!"
            "PAYMENT_FAILED" -> "Thanh toán thất bại!"
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
            if (!notification.read) {
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
    modifier: Modifier = Modifier,
    selectedDate: String? = null // null = hiển thị ngày hôm nay, có giá trị = hiển thị ngày được chọn
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
        // Debug: In ra tất cả notifications và ngày của chúng
        println("🔍 DEBUG: NotificationList - Total notifications: ${notifications.size}")
        println("🔍 DEBUG: NotificationList - Selected date: $selectedDate")
        
        notifications.forEach { notification ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = notification.createdAt
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val notificationDate = "$day/${month + 1}/$year"
            println("🔍 DEBUG: NotificationList - Notification: ${notification.title} - Date: $notificationDate - CreatedAt: ${notification.createdAt}")
        }
        
        // Lọc notifications theo ngày
        val filteredNotifications = if (selectedDate != null) {
            // Hiển thị notifications của ngày được chọn
            println("🔍 DEBUG: NotificationList - Filtering by selected date: $selectedDate")
            val filtered = notifications.filter { notification ->
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = notification.createdAt
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val notificationDate = "$day/${month + 1}/$year"
                println("🔍 DEBUG: NotificationList - Comparing: $notificationDate == $selectedDate")
                notificationDate == selectedDate
            }
            println("🔍 DEBUG: NotificationList - Filtered notifications count: ${filtered.size}")
            filtered
        } else {
            // Mặc định chỉ hiển thị notifications của ngày hôm nay
            val today = java.util.Calendar.getInstance()
            val todayYear = today.get(java.util.Calendar.YEAR)
            val todayMonth = today.get(java.util.Calendar.MONTH)
            val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
            val todayDate = "$todayDay/${todayMonth + 1}/$todayYear"
            println("🔍 DEBUG: NotificationList - Filtering by today: $todayDate")
            
            val filtered = notifications.filter { notification ->
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = notification.createdAt
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val notificationDate = "$day/${month + 1}/$year"
                println("🔍 DEBUG: NotificationList - Comparing: $notificationDate == $todayDate")
                notificationDate == todayDate
            }
            println("🔍 DEBUG: NotificationList - Today's notifications count: ${filtered.size}")
            filtered
        }
        
        if (filteredNotifications.isEmpty()) {
            // Hiển thị empty state
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                item { 
                    EmptyNotificationState(
                        message = if (selectedDate != null) 
                            "Không có thông báo nào cho ngày $selectedDate" 
                        else 
                            "Không có thông báo nào cho ngày hôm nay"
                    )
                }
            }
        } else {
            // Hiển thị notifications đã lọc
            val displayDate = selectedDate ?: run {
                val today = java.util.Calendar.getInstance()
                val todayYear = today.get(java.util.Calendar.YEAR)
                val todayMonth = today.get(java.util.Calendar.MONTH)
                val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
                "$todayDay/${todayMonth + 1}/$todayYear"
            }
            
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                // Date header
                item {
                    DateHeader(date = displayDate)
                }
                
                // Notifications for this date
                items(filteredNotifications.sortedByDescending { it.createdAt }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF666666),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
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
fun EmptyNotificationState(message: String = "Không có thông báo nào.") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có thông báo nào",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bạn sẽ nhận được thông báo khi có cập nhật về đặt sân, trận đấu và đánh giá",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun getNotificationIcon(type: String): ImageVector {
    return when (type) {
        "BOOKING_CREATED", "BOOKING_SUCCESS", "BOOKING_CONFIRMED" -> Icons.Default.Event
        "BOOKING_CANCELLED", "BOOKING_CANCELLED_BY_OWNER" -> Icons.Default.AlarmOff
        "OPPONENT_JOINED", "OPPONENT_SEARCH" -> Icons.Default.Person
        "MATCH_RESULT" -> Icons.Default.SportsSoccer
        "FIELD_UPDATED" -> Icons.Default.Update
        "REVIEW_ADDED", "REVIEW_REPLY" -> Icons.Default.Notifications
        "PAYMENT_SUCCESS" -> Icons.Default.CheckCircle
        "PAYMENT_FAILED" -> Icons.Default.Notifications
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
                    read = false,
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
                    read = true,
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyNotificationState()
        }
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
