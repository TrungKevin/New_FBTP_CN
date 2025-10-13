package com.trungkien.fbtp_cn.ui.components.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.model.NotificationData
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun NotificationScreenContent(
    notifications: List<Notification>,
    onItemClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    NotificationList(
        notifications = notifications,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
fun NotificationScreen(
    notifications: List<Notification>,
    onBackClick: () -> Unit,
    onItemClick: (Notification) -> Unit,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadCount = notifications.count { !it.isRead }
    
    Scaffold(
        topBar = {
            NotificationHeader(
                onBackClick = onBackClick,
                unreadCount = unreadCount,
                onMarkAllAsRead = onMarkAllAsRead
            )
        },
        containerColor = Color(0xFFF0F0F0) // Background color cho toàn bộ screen
    ) { paddingValues ->
        NotificationScreenContent(
            notifications = notifications,
            onItemClick = onItemClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    FBTP_CNTheme {
        val sampleNotifications = listOf(
            Notification(
                notificationId = "1",
                toUserId = "user1",
                type = "BOOKING_CREATED",
                title = "Đặt sân mới!",
                body = "Nguyễn Văn A đã đặt sân Toan-SPort vào lúc 18:00 ngày 15/01/2024.",
                data = NotificationData(bookingId = "booking1", fieldId = "field1"),
                isRead = false,
                createdAt = System.currentTimeMillis() - 3600000
            ),
            Notification(
                notificationId = "2",
                toUserId = "user1",
                type = "REVIEW_ADDED",
                title = "Đánh giá mới!",
                body = "Bạn nhận được đánh giá 5 sao từ Nguyễn Văn B cho sân Toan-SPort.",
                data = NotificationData(fieldId = "field1"),
                isRead = true,
                createdAt = System.currentTimeMillis() - 7200000
            ),
            Notification(
                notificationId = "3",
                toUserId = "user1",
                type = "OPPONENT_JOINED",
                title = "Có đối thủ tham gia!",
                body = "Nguyễn Văn D đã tham gia trận đấu tại sân Toan-SPort vào lúc 19:00 ngày 17/01/2024.",
                data = NotificationData(matchId = "match1", fieldId = "field1"),
                isRead = false,
                createdAt = System.currentTimeMillis() - 10800000
            )
        )

        NotificationScreen(
            notifications = sampleNotifications,
            onBackClick = {},
            onItemClick = {},
            onMarkAllAsRead = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenContentPreview() {
    FBTP_CNTheme {
        val sampleNotifications = listOf(
            Notification(
                notificationId = "1",
                toUserId = "user1",
                type = "BOOKING_CREATED",
                title = "Đặt sân mới!",
                body = "Nguyễn Văn A đã đặt sân Toan-SPort vào lúc 18:00 ngày 15/01/2024.",
                data = NotificationData(bookingId = "booking1", fieldId = "field1"),
                isRead = false,
                createdAt = System.currentTimeMillis() - 3600000
            ),
            Notification(
                notificationId = "2",
                toUserId = "user1",
                type = "BOOKING_CANCELLED",
                title = "Đặt sân bị hủy!",
                body = "Nguyễn Văn C đã hủy đặt sân Toan-SPort vào lúc 20:00 ngày 16/01/2024.",
                data = NotificationData(bookingId = "booking3", fieldId = "field1"),
                isRead = false,
                createdAt = System.currentTimeMillis() - 7200000
            )
        )

        NotificationScreenContent(
            notifications = sampleNotifications,
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenEmptyPreview() {
    FBTP_CNTheme {
        NotificationScreen(
            notifications = emptyList(),
            onBackClick = {},
            onItemClick = {},
            onMarkAllAsRead = {}
        )
    }
}
