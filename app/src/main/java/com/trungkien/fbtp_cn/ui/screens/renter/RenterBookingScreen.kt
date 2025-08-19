package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.ui.components.renter.booking.RenterBookingCard
import com.trungkien.fbtp_cn.ui.components.renter.booking.RenterBookingDetailSheet
import com.trungkien.fbtp_cn.ui.components.renter.booking.RenterBookingHeader
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterBookingScreen(
    modifier: Modifier = Modifier,
    bookings: List<Booking> = MockData.mockBookings
) {
    var selectedBookingId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White
    ) { inner ->
        var selectedDate by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Header with calendar filter
            RenterBookingHeader(
                selectedDateLabel = selectedDate,
                onCalendarClick = {
                    // TODO: Open date picker
                    // For preview/demo: toggle between two dates
                    selectedDate = if (selectedDate == null) "2024-01-15" else null
                },
                modifier = Modifier.fillMaxWidth()
            )

            val allBookings = bookings
            val filtered = selectedDate?.let { d -> allBookings.filter { it.date == d } } ?: allBookings
            val upcoming = filtered.filter { it.status.equals("Confirmed", true) || it.status.equals("Upcoming", true) }
            val completed = filtered.filter { it.status.equals("Completed", true) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (upcoming.isNotEmpty()) {
                    item {
                        Text(
                            text = "Sắp diễn ra (${upcoming.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(upcoming) { booking ->
                        RenterBookingCard(
                            booking = booking,
                            onDetailClick = { b -> selectedBookingId = if (selectedBookingId == b.id) null else b.id }
                        )
                        if (selectedBookingId == booking.id) {
                            RenterBookingDetailSheet(
                                booking = booking,
                                onClose = { selectedBookingId = null }
                            )
                        }
                    }
                }

                if (completed.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        Text(
                            text = "Đã hoàn thành (${completed.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(completed) { booking ->
                        RenterBookingCard(
                            booking = booking,
                            onDetailClick = { b -> selectedBookingId = if (selectedBookingId == b.id) null else b.id }
                        )
                        if (selectedBookingId == booking.id) {
                            RenterBookingDetailSheet(
                                booking = booking,
                                onClose = { selectedBookingId = null }
                            )
                        }
                    }
                }

                if (upcoming.isEmpty() && completed.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa có lịch đặt nào",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RenterBookingScreenPreview() {
    FBTP_CNTheme {
        // Provide static preview data to ensure content renders in Studio
        val previewBookings = listOf(
            Booking(
                id = "booking_1", fieldId = "field_001", fieldName = "Court 1 - Tennis",
                timeRange = "18:00 - 19:00", status = "Confirmed",
                date = "2024-01-15", totalPrice = 195000, fieldPrice = 170000
            ),
            Booking(
                id = "booking_2", fieldId = "field_002", fieldName = "Court 2 - Badminton",
                timeRange = "20:00 - 21:00", status = "Completed",
                date = "2024-01-10", totalPrice = 120000, fieldPrice = 120000
            )
        )
        RenterBookingScreen(modifier = Modifier.fillMaxSize(), bookings = previewBookings)
    }
}


