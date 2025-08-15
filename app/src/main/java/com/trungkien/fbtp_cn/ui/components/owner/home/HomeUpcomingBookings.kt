package com.trungkien.fbtp_cn.ui.components.owner.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange

import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.model.Booking
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun HomeUpcomingBookings(
    bookings: List<Booking>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Lịch sắp tới",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            TextButton(onClick = onSeeAll) {
                Text(
                    text = "Xem tất cả",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bookings List
        if (bookings.isEmpty()) {
            EmptyBookingsCard()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                bookings.take(3).forEach { booking ->
                    BookingCard(booking = booking)
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val statusColor = when (booking.status) {
        "Đã xác nhận" -> Color(0xFF4CAF50)
        "Chờ xác nhận" -> Color(0xFFFF9800)
        "Đã hủy" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.fieldName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.schedule), // Sử dụng icon event
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = booking.timeRange,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBookingsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Chưa có lịch đặt sân nào",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeUpcomingBookings() {
    FBTP_CNTheme {
        HomeUpcomingBookings(
            bookings = listOf(
                Booking(
                    id = "b1",
                    fieldId = "1",
                    fieldName = "POC Pickleball Court A",
                    timeRange = "08:00 - 09:00",
                    status = "Chờ xác nhận"
                ),
                Booking(
                    id = "b2",
                    fieldId = "1",
                    fieldName = "POC Pickleball Court B",
                    timeRange = "10:00 - 11:00",
                    status = "Đã xác nhận"
                )
            ),
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyHomeUpcomingBookings() {
    FBTP_CNTheme {
        HomeUpcomingBookings(
            bookings = emptyList(),
            onSeeAll = {}
        )
    }
}