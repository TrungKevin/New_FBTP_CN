package com.trungkien.fbtp_cn.ui.components.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun NotificationBell(
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val buttonSize = if (compact) 24.dp else 48.dp
    val iconSize = if (compact) 20.dp else 24.dp
    val badgeSize = if (compact) 14.dp else 20.dp
    val fontSize = if (compact) 9.sp else 10.sp

    Box(
        modifier = modifier
    ) {
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(buttonSize)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Thông báo",
                tint = Color(0xFF00C853),
                modifier = Modifier.size(iconSize)
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    color = Color.White,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationBellPreview() {
    FBTP_CNTheme {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            NotificationBell(unreadCount = 5, onNotificationClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationBellLargeCountPreview() {
    FBTP_CNTheme {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            NotificationBell(unreadCount = 12, onNotificationClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationBellCompactPreview() {
    FBTP_CNTheme {
        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            NotificationBell(unreadCount = 8, onNotificationClick = {}, compact = true)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationBellNoCountPreview() {
    FBTP_CNTheme {
        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            NotificationBell(unreadCount = 0, onNotificationClick = {})
        }
    }
}
