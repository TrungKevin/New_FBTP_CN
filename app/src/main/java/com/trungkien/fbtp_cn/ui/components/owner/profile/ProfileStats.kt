package com.trungkien.fbtp_cn.ui.components.owner.profile

import android.R.attr.icon
import android.R.attr.label
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ProfileStats(
    totalFields: Int,
    totalBookings: Int,
    totalRevenue: Int,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onViewDetails),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header với nút xem chi tiết
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thống kê tổng quan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Thống kê
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Tổng số sân
                StatItem(
                    icon = painterResource(id = R.drawable.stadium),
                    value = totalFields.toString(),
                    label = "Sân",
                    color = Color(0xFF00C853)
                )
                
                // Tổng số đặt sân
                StatItem(
                    icon = painterResource(id = R.drawable.event),
                    value = totalBookings.toString(),
                    label = "Đặt sân",
                    color = Color(0xFF2196F3)
                )
                
                // Tổng doanh thu
                StatItem(
                    icon = painterResource(id = R.drawable.money),
                    value = "${String.format("%,d", totalRevenue)}",
                    label = "Doanh thu",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF263238)
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575)
        )
    }
}

@Preview
@Composable
fun ProfileStatsPreview() {
    FBTP_CNTheme {
        ProfileStats(
            totalFields = 2,
            totalBookings = 15,
            totalRevenue = 2500000,
            onViewDetails = {}
        )
    }
}
