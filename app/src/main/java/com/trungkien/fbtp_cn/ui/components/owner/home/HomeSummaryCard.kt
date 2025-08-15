package com.trungkien.fbtp_cn.ui.components.owner.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.CommonShadows

data class HomeSummary(
    val newBookings: Int,
    val confirmed: Int,
    val canceled: Int,
    val revenueToday: Long
)

@Composable
fun HomeSummaryCard(summary: HomeSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CommonShadows.Card),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.event), // Replace with your icon
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Tổng quan hôm nay",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Đơn mới",
                    value = summary.newBookings.toString(),
                    color = Color(0xFF4CAF50),
                    icon = "📝"
                )

                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Xác nhận",
                    value = summary.confirmed.toString(),
                    color = Color(0xFF2196F3),
                    icon = "✅"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Đã hủy",
                    value = summary.canceled.toString(),
                    color = Color(0xFFF44336),
                    icon = "❌"
                )

                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Doanh thu",
                    value = "${"%,d".format(summary.revenueToday)}đ",
                    color = Color(0xFFFF9800),
                    icon = "💰",
                    isRevenue = true
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier,
    isRevenue: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CommonShadows.Badge),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isRevenue) 12.sp else 16.sp
                ),
                color = color
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeSummaryCard() {
    FBTP_CNTheme {
        HomeSummaryCard(
            summary = HomeSummary(
                newBookings = 12,
                confirmed = 28,
                canceled = 3,
                revenueToday = 2500000
            )
        )
    }
}