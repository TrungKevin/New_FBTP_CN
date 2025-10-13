package com.trungkien.fbtp_cn.ui.components.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHeader(
    onBackClick: () -> Unit,
    unreadCount: Int,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "Thông báo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        actions = {
            if (unreadCount > 0) {
                TextButton(onClick = onMarkAllAsRead) {
                    Text("Đánh dấu đã đọc tất cả")
                }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun NotificationHeaderPreview() {
    FBTP_CNTheme {
        NotificationHeader(
            onBackClick = {},
            unreadCount = 5,
            onMarkAllAsRead = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationHeaderNoUnreadPreview() {
    FBTP_CNTheme {
        NotificationHeader(
            onBackClick = {},
            unreadCount = 0,
            onMarkAllAsRead = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationHeaderLargeCountPreview() {
    FBTP_CNTheme {
        NotificationHeader(
            onBackClick = {},
            unreadCount = 99,
            onMarkAllAsRead = {}
        )
    }
}
