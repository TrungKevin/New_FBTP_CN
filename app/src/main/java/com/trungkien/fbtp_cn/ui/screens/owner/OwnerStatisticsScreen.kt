package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

private enum class TimeRange {
    Today, Week, Month, Year
}

@Composable
fun OwnerStatisticsScreen(
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(TimeRange.Week) }

    // Fake data generator based on selected range
    val revenueValues: List<Int> = when (selectedRange) {
        TimeRange.Today -> listOf(10, 15, 7, 20, 18, 25, 12, 14, 9, 22, 17, 13)
        TimeRange.Week -> listOf(120, 80, 160, 90, 140, 180, 110)
        TimeRange.Month -> List(30) { (60..180).random() }
        TimeRange.Year -> List(12) { (1200..3000).random() }
    }

    val bookingValues: List<Int> = when (selectedRange) {
        TimeRange.Today -> listOf(1, 2, 0, 3, 2, 4, 1, 2, 1, 3, 2, 1)
        TimeRange.Week -> listOf(2, 1, 4, 2, 3, 5, 3)
        TimeRange.Month -> List(30) { (1..6).random() }
        TimeRange.Year -> List(12) { (20..60).random() }
    }

    val totalRevenue = revenueValues.sum() * if (selectedRange == TimeRange.Year) 1000 else 10000
    val totalBookings = bookingValues.sum()
    val cancelRate = 3 // Placeholder

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatsFilterBar(
                    selected = selectedRange,
                    onRangeSelected = { selectedRange = it }
                )
            }

            item {
                SummaryRow(
                    totalRevenue = totalRevenue,
                    totalBookings = totalBookings,
                    cancelRate = cancelRate
                )
            }

            item {
                RevenueChartCard(
                    title = when (selectedRange) {
                        TimeRange.Today -> "Doanh thu theo giờ hôm nay"
                        TimeRange.Week -> "Doanh thu 7 ngày gần đây"
                        TimeRange.Month -> "Doanh thu 30 ngày gần đây"
                        TimeRange.Year -> "Doanh thu 12 tháng gần đây"
                    },
                    values = revenueValues
                )
            }

            item {
                BookingsChartCard(
                    title = when (selectedRange) {
                        TimeRange.Today -> "Lượt đặt theo giờ hôm nay"
                        TimeRange.Week -> "Lượt đặt sân 7 ngày gần đây"
                        TimeRange.Month -> "Lượt đặt sân 30 ngày gần đây"
                        TimeRange.Year -> "Lượt đặt sân 12 tháng gần đây"
                    },
                    values = bookingValues
                )
            }

            item {
                TopFieldsCard(
                    items = listOf(
                        TopField(name = "Sân A", bookings = 8, revenue = 1_200_000),
                        TopField(name = "Sân B", bookings = 5, revenue = 800_000),
                        TopField(name = "Sân C", bookings = 3, revenue = 500_000),
                    )
                )
            }
        }
    }
}

@Composable
private fun StatsFilterBar(
    selected: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title on its own line
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

        // Buttons on a row below the title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                TimeRange.Today to "Hôm nay",
                TimeRange.Week to "Tuần",
                TimeRange.Month to "Tháng",
                TimeRange.Year to "Năm"
            )
            items.forEach { (range, label) ->
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

@Composable
private fun SummaryRow(
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
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier,
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
                androidx.compose.material3.Icon(
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
private fun RevenueChartCard(//đùng để hiển thị biểu đồ doanh thu của các khoảng thời gian khác nhau
    title: String, // tiêu đề của biểu đồ
    values: List<Int>, // danh sách các giá trị doanh thu để hiển thị trên biểu đồ
    modifier: Modifier = Modifier // để tùy chỉnh giao diện của biểu đồ
) {
    ElevatedCard( // sử dụng ElevatedCard để tạo thẻ có độ nổi
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),// màu nền của thẻ
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp), // độ cao của thẻ
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Icon( // sử dụng Icon để hiển thị biểu tượng doanh thu
                    painter = painterResource(id = R.drawable.bartchar),
                    contentDescription = null,
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(18.dp)
                )
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            }
            Spacer(modifier = Modifier.height(16.dp))
            SimpleBarChart(values = values, barColor = Color(0xFF00C853))
        }
    }
}

@Composable
private fun BookingsChartCard(// đùng để hiển thị biểu đồ lượt đặt sân của các khoảng thời gian khác nhau
    title: String,
    values: List<Int>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.schedule),
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(18.dp)
                )
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Scale bookings for visualization when numbers are small
            val scaledValues = if (values.maxOrNull() ?: 0 <= 10) values.map { it * 30 } else values
            SimpleBarChart(values = scaledValues, barColor = Color(0xFF2196F3), maxValue = maxOf(200, scaledValues.maxOrNull() ?: 1))
        }
    }
}

@Composable
private fun SimpleBarChart( // đùng để vẽ biểu đồ cột đơn giản
    values: List<Int>,
    barColor: Color,
    maxValue: Int = values.maxOrNull() ?: 1,
    modifier: Modifier = Modifier
) {
    // Simple bar chart without external library
    val normalized = values.map { it.toFloat() / (maxValue.takeIf { it > 0 } ?: 1) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        normalized.forEach { ratio ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((20 + (ratio * 100)).dp)
                    .background(barColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((ratio * 100).dp)
                        .background(barColor, shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

private data class TopField(
    val name: String,
    val bookings: Int,
    val revenue: Int
)

@Composable
private fun TopFieldsCard(// đùng để hiển thị danh sách các sân nổi bật
    items: List<TopField>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Sân nổi bật", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF00C853).copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = R.drawable.stadium),
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF263238))
                            Text(text = "${item.bookings} lượt đặt", fontSize = 12.sp, color = Color(0xFF757575))
                        }
                    }
                    Text(text = String.format("%,d", item.revenue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnerStatisticsScreenPreview() {
    FBTP_CNTheme {
        OwnerStatisticsScreen()
    }
}
