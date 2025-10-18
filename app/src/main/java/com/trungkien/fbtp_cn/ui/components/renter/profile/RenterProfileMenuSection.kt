package com.trungkien.fbtp_cn.ui.components.renter.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterProfileMenuSection(
    onMyBookings: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(text = "Menu chính", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            Spacer(Modifier.height(16.dp))

            MenuItem(
                icon = painterResource(id = R.drawable.event),
                title = "Lịch đặt của tôi",
                subtitle = "Xem các đặt sân đã lên lịch",
                onClick = onMyBookings,
                color = Color(0xFF2196F3),
                isVectorIcon = false
            )

            Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))

            MenuItem(
                icon = Icons.Default.Notifications,
                title = "Thông báo",
                subtitle = "Cài đặt thông báo",
                onClick = onNotifications,
                color = Color(0xFF9C27B0),
                isVectorIcon = true
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: Any, // Painter hoặc ImageVector
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    color: Color,
    isVectorIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isVectorIcon && icon is ImageVector) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            } else if (!isVectorIcon && icon is Painter) {
                Icon(painter = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF263238))
            Text(text = subtitle, fontSize = 14.sp, color = Color(0xFF757575))
        }
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go", tint = Color(0xFFBDBDBD), modifier = Modifier.size(20.dp))
    }
}

@Preview
@Composable
private fun RenterProfileMenuSectionPreview() {
    FBTP_CNTheme {
        RenterProfileMenuSection(onMyBookings = {}, onNotifications = {})
    }
}


