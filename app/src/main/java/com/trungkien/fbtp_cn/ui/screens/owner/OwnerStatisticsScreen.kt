package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.owner.stats.StatsFilterBar as CommonStatsFilterBar
import com.trungkien.fbtp_cn.ui.components.owner.stats.SummaryRow as CommonSummaryRow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.OwnerStatsViewModel
import com.trungkien.fbtp_cn.viewmodel.TimeRange
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel

private enum class TimeRange { Today, Week, Month, Year }

@Composable
fun OwnerStatisticsScreen(
    modifier: Modifier = Modifier
) {
    val authVm: AuthViewModel = viewModel()
    val statsVm: OwnerStatsViewModel = viewModel()
    val ui = statsVm.ui.collectAsState().value

    // range hiện tại
    var selectedRange by remember { mutableStateOf(TimeRange.Week) }

    // đồng bộ filter -> VM
    LaunchedEffect(selectedRange) { statsVm.setRange(selectedRange) }

    // load dữ liệu khi có ownerId
    val ownerId = authVm.currentUser.collectAsState().value?.userId
    // Đảm bảo có profile để lấy ownerId
    LaunchedEffect(Unit) {
        if (ownerId == null) authVm.fetchProfile()
    }
    LaunchedEffect(ownerId, ui.range) {
        if (ownerId != null) statsVm.load(ownerId)
    }

    val revenueValues: List<Int> = (ui.stats?.chartRevenueByBucket ?: emptyList()).map { (it / 1000L).toInt() }
    val bookingValues: List<Int> = ui.stats?.chartBookingsByBucket ?: emptyList()

    val totalRevenue = (ui.stats?.totalRevenue ?: 0L).toInt()
    val totalBookings = ui.stats?.totalBookings ?: 0
    val cancelRate = ui.stats?.let { s ->
        if (s.totalBookings == 0) 0 else (s.totalCancelled * 100 / s.totalBookings)
    } ?: 0

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
                CommonStatsFilterBar(
                    selected = selectedRange,
                    onRangeSelected = { selectedRange = it as TimeRange },
                    labels = listOf(
                        TimeRange.Today to "Hôm nay",
                        TimeRange.Week to "Tuần",
                        TimeRange.Month to "Tháng",
                        TimeRange.Year to "Năm"
                    )
                )
            }

            item {
                CommonSummaryRow(
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
                    values = if (revenueValues.isEmpty()) List(if (selectedRange==TimeRange.Year) 12 else if (selectedRange==TimeRange.Today) 12 else 7){0} else revenueValues
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
                    values = if (bookingValues.isEmpty()) List(if (selectedRange==TimeRange.Year) 12 else if (selectedRange==TimeRange.Today) 12 else 7){0} else bookingValues
                )
            }

            item {
                val top = ui.stats?.topFields?.map { TopField(it.name, it.bookings, it.revenue.toInt()) } ?: emptyList()
                TopFieldsCard(items = top)
            }
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
