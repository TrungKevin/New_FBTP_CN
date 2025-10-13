package com.trungkien.fbtp_cn.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.ui.components.notification.NotificationScreenContent
import com.trungkien.fbtp_cn.ui.components.notification.NotificationHeader
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.runtime.LaunchedEffect
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SimpleNotificationScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToField: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFieldDetail: (fieldId: String, initialTab: String) -> Unit = { _, _ -> },
    userId: String = "",
    modifier: Modifier = Modifier
) {
    // State dữ liệu thật từ Firestore
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    val repo = remember { NotificationRepository() }
    val scope = rememberCoroutineScope()

    // Subscribe realtime theo userId
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            repo.listenNotificationsByUser(userId).collectLatest { list ->
                notifications = list
            }
        } else {
            notifications = emptyList()
        }
    }

    Scaffold(
        topBar = {
            NotificationHeader(
                onBackClick = onBackClick,
                unreadCount = notifications.count { !it.isRead },
                onMarkAllAsRead = {
                    if (userId.isNotBlank()) {
                        scope.launch { repo.markAllAsRead(userId) }
                    }
                }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        NotificationScreenContent(
            notifications = notifications,
            onItemClick = { notification ->
                scope.launch { repo.markAsRead(notification.notificationId) }

                // Điều hướng theo loại
                when (notification.type) {
                    "BOOKING_CREATED", "BOOKING_SUCCESS", "BOOKING_CANCELLED" -> {
                        onNavigateToBooking()
                    }
                    "FIELD_UPDATED" -> {
                        onNavigateToField()
                    }
                    "REVIEW_ADDED" -> {
                        val fieldId = notification.data.fieldId
                        if (!fieldId.isNullOrBlank()) {
                            onNavigateToFieldDetail(fieldId, "reviews")
                        } else {
                            onNavigateToProfile()
                        }
                    }
                    "OPPONENT_JOINED", "MATCH_RESULT" -> {
                        onNavigateToBooking()
                    }
                    else -> onNavigateToHome()
                }
            },
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SimpleNotificationScreenPreview() {
    FBTP_CNTheme {
        SimpleNotificationScreen(onBackClick = {})
    }
}
