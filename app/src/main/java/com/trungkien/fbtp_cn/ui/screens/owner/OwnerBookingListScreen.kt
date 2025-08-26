package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingEmptyState
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingFilterBar
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingDetailManage
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

private enum class BookingStatusFilter(val label: String) {
    All("Tất cả"),
    Pending("Chờ xác nhận"),
    Confirmed("Đã xác nhận"),
    Canceled("Đã hủy")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingListScreen(
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allBookings = remember { mockBookings() }
    var selectedFilter by remember { mutableStateOf(BookingStatusFilter.All) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    val filtered = remember(selectedFilter, allBookings) {
        when (selectedFilter) {
            BookingStatusFilter.All -> allBookings
            BookingStatusFilter.Pending -> allBookings.filter { it.status == "PENDING" }
            BookingStatusFilter.Confirmed -> allBookings.filter { it.status == "PAID" }
            BookingStatusFilter.Canceled -> allBookings.filter { it.status == "CANCELLED" }
        }
    }

    // Nếu có booking được chọn, hiển thị màn hình chi tiết
    selectedBooking?.let { booking ->
        BookingDetailManage(
            booking = booking,
            onConfirm = {
                // TODO: Xử lý xác nhận đặt sân
                selectedBooking = null
            },
            onCancel = {
                // TODO: Xử lý hủy đặt sân
                selectedBooking = null
            },
            onSuggestTime = {
                // TODO: Xử lý gợi ý khung giờ khác
            },
            onBack = {
                selectedBooking = null
            }
        )
        return
    }

    // Sử dụng Column thay vì Scaffold để giảm khoảng cách với TopAppBar
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // Đồng nhất màu với TopAppBar
    ) {
        // Header với tiêu đề và actions - giảm padding top để gần TopAppBar hơn
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Giảm vertical padding từ 16dp xuống 8dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quản lý đặt sân",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showDatePicker = true }
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Chọn ngày",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { /* bộ lọc nâng cao */ }
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Bộ lọc",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Header với thống kê
        BookingStatsHeader(bookings = allBookings)

        // Status filter chips
        BookingFilterBar(
            options = BookingStatusFilter.values().map { it.label },
            selected = selectedFilter.label,
            onSelectedChange = { label ->
                selectedFilter = BookingStatusFilter.values().first { it.label == label }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Booking list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    BookingEmptyState(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(filtered, key = { it.bookingId }) { booking ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EnhancedBookingListItem(
                            booking = booking,
                            onBookingClick = {
                                selectedBooking = booking
                            },
                            onActionClick = { action ->
                                when (action) {
                                    "approve" -> {
                                        // TODO: Xử lý xác nhận
                                    }
                                    "reject" -> {
                                        // TODO: Xử lý từ chối
                                    }
                                }
                            }
                        )
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun BookingStatsHeader(
    bookings: List<Booking>,
    modifier: Modifier = Modifier
) {
    val pendingCount = bookings.count { it.status == "PENDING" }
    val confirmedCount = bookings.count { it.status == "PAID" }
    val totalRevenue = bookings.filter { it.status == "PAID" }.size * 150000 // Mock revenue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                title = "Chờ xác nhận",
                value = pendingCount.toString(),
                color = Color(0xFFFF9800)
            )
            StatItem(
                title = "Đã xác nhận",
                value = confirmedCount.toString(),
                color = Color(0xFF4CAF50)
            )
            StatItem(
                title = "Doanh thu",
                value = "${String.format("%,d", totalRevenue)}đ",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedBookingListItem(
    booking: Booking,
    onBookingClick: (Booking) -> Unit,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (booking.status) {
        "PAID" -> Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFF9800)
        "CANCELLED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val statusIcon = when (booking.status) {
        "PAID" -> "✓"
        "PENDING" -> "⏱"
        "CANCELLED" -> "✕"
        else -> "•"
    }

    Card(
        onClick = { onBookingClick(booking) },
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header với tên sân và trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sân ${booking.fieldId}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Thời gian
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.schedule),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${booking.startAt} - ${booking.endAt}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Status badge
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = statusIcon,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                        Text(
                            text = booking.status,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer info với design đồng bộ
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                // Avatar với design đồng bộ
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nguyễn Văn A", // Mock customer name
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "0909 123 456", // Mock phone
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "150.000đ", // Mock price
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Action buttons cho pending bookings với design mới
            if (booking.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Button xác nhận với style từ BookingDetailManage
                    Button(
                        onClick = { onActionClick("approve") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Xác nhận",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Button từ chối
                        OutlinedButton(
                            onClick = { onActionClick("reject") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            ),
                            border = BorderStroke(2.dp, Color(0xFFF44336)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Từ chối",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Button gợi ý với design mới
                        OutlinedButton(
                            onClick = { onActionClick("suggest") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.schedule),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Gợi ý",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Enhanced mock data with more details
private fun mockBookings(): List<Booking> = listOf(
    Booking(
        bookingId = "b1",
        renterId = "renter1",
        ownerId = "owner1",
        fieldId = "1",
        date = "2024-01-15",
        startAt = "08:00",
        endAt = "09:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 150000,
        servicePrice = 0,
        totalPrice = 150000,
        status = "PENDING"
    ),
    Booking(
        bookingId = "b2",
        renterId = "renter2",
        ownerId = "owner1",
        fieldId = "1",
        date = "2024-01-15",
        startAt = "10:00",
        endAt = "11:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 150000,
        servicePrice = 0,
        totalPrice = 150000,
        status = "PAID"
    ),
    Booking(
        bookingId = "b3",
        renterId = "renter3",
        ownerId = "owner2",
        fieldId = "2",
        date = "2024-01-16",
        startAt = "15:00",
        endAt = "16:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 120000,
        servicePrice = 0,
        totalPrice = 120000,
        status = "CANCELLED"
    ),
    Booking(
        bookingId = "b4",
        renterId = "renter4",
        ownerId = "owner2",
        fieldId = "2",
        date = "2024-01-20",
        startAt = "19:00",
        endAt = "20:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 120000,
        servicePrice = 0,
        totalPrice = 120000,
        status = "PENDING"
    ),
    Booking(
        bookingId = "b5",
        renterId = "renter5",
        ownerId = "owner3",
        fieldId = "3",
        date = "2024-01-15",
        startAt = "14:00",
        endAt = "15:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 180000,
        servicePrice = 0,
        totalPrice = 180000,
        status = "PAID"
    ),
    Booking(
        bookingId = "b6",
        renterId = "renter6",
        ownerId = "owner3",
        fieldId = "3",
        date = "2024-01-16",
        startAt = "09:00",
        endAt = "10:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 180000,
        servicePrice = 0,
        totalPrice = 180000,
        status = "PENDING"
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewOwnerBookingListScreen() {
    FBTP_CNTheme {
        OwnerBookingListScreen(onBookingClick = { /* Preview callback */ })
    }
}