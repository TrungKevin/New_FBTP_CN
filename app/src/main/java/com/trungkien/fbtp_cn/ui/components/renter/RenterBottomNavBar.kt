package com.trungkien.fbtp_cn.ui.components.renter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterBottomNavBar(
    currentScreen: RenterNavScreen,
    onTabSelected: (RenterNavScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RenterNavScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(screen) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = getIconForNavItem(screen)
                        ),
                        contentDescription = getTitleForNavItem(screen),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = getTitleForNavItem(screen),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun getIconForNavItem(screen: RenterNavScreen): Int {
    return when (screen) {
        RenterNavScreen.Home -> R.drawable.menu
        RenterNavScreen.Search -> R.drawable.stadium
        RenterNavScreen.Map -> R.drawable.map
        RenterNavScreen.Booking -> R.drawable.event
        RenterNavScreen.Profile -> R.drawable.hoso
    }
}

private fun getTitleForNavItem(screen: RenterNavScreen): String {
    return when (screen) {
        RenterNavScreen.Home -> "Trang chủ"
        RenterNavScreen.Search -> "Tìm kiếm"
        RenterNavScreen.Map -> "Bản đồ"
        RenterNavScreen.Booking -> "Đặt sân"
        RenterNavScreen.Profile -> "Hồ sơ"
    }
}

enum class RenterNavScreen {
    Home,      // Trang chủ với featured fields, map preview
    Search,    // Tìm kiếm sân với filter
    Map,       // Màn hình bản đồ đầy đủ
    Booking,   // Lịch sử đặt sân, quản lý booking
    Profile    // Hồ sơ cá nhân, đánh giá
}

@Preview
@Composable
fun RenterBottomAppBarPreview() {
    FBTP_CNTheme {
        RenterBottomNavBar(
            currentScreen = RenterNavScreen.Home,
            onTabSelected = {}
        )
    }
}

