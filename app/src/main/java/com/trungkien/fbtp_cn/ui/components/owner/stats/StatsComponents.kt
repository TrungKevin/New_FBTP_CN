package com.trungkien.fbtp_cn.ui.components.owner.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R

@Composable
fun SummaryCard(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = title, fontSize = 12.sp, color = Color(0xFF757575))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
        }
    }
}

@Composable
fun SummaryRow(
    totalRevenue: Int,
    totalBookings: Int,
    cancelRate: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            icon = painterResource(id = R.drawable.money),
            title = "Doanh thu",
            value = String.format("%,d", totalRevenue),
            accent = Color(0xFFFF9800)
        )
        SummaryCard(
            icon = painterResource(id = R.drawable.schedule),
            title = "Đặt sân",
            value = totalBookings.toString(),
            accent = Color(0xFF2196F3)
        )
        SummaryCard(
            icon = painterResource(id = R.drawable.payments),
            title = "Hủy (%)",
            value = "$cancelRate%",
            accent = Color(0xFFE91E63)
        )
    }
}

@Composable
fun StatsFilterBar(
    selected: Enum<*>,
    onRangeSelected: (Enum<*>) -> Unit,
    labels: List<Pair<Enum<*>, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .background(color = Color(0xFFE8F5E8), shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Khoảng thời gian",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00C853),
                letterSpacing = 0.2.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEach { (range, label) ->
                val isSelected = selected == range
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { onRangeSelected(range) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF00C853) else Color.White,
                            contentColor = if (isSelected) Color.White else Color(0xFF00C853)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF00C853).copy(alpha = if (isSelected) 0f else 0.7f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}


