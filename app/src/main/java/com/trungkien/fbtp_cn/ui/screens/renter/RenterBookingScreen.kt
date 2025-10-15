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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.ui.components.renter.bookinghis.RenterBookingCard
import com.trungkien.fbtp_cn.ui.components.renter.bookinghis.RenterBookingDetailSheet
import com.trungkien.fbtp_cn.ui.components.renter.bookinghis.RenterBookingHeader
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterBookingScreen(
    modifier: Modifier = Modifier,
    bookings: List<Booking> = MockData.mockBookings,
    bookingViewModel: com.trungkien.fbtp_cn.viewmodel.BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: com.trungkien.fbtp_cn.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedBookingId by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        containerColor = Color.White
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Header with calendar filter
            RenterBookingHeader(
                selectedDateLabel = selectedDate,
                onCalendarClick = {
                    showDatePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            )

            val myUid = authViewModel.currentUser.collectAsState().value?.userId
            val bookingUi = bookingViewModel.uiState.collectAsState().value
            LaunchedEffect(myUid) {
                myUid?.let { 
                    println("🔍 DEBUG: RenterBookingScreen - Loading bookings for userId: $it")
                    bookingViewModel.handle(com.trungkien.fbtp_cn.viewmodel.BookingEvent.LoadMine(it)) 
                }
            }
            
            // Debug logs for booking data
            LaunchedEffect(bookingUi.myBookings) {
                println("🔍 DEBUG: RenterBookingScreen - Booking data updated:")
                println("  - Total bookings: ${bookingUi.myBookings.size}")
                bookingUi.myBookings.forEachIndexed { index, booking ->
                    println("  [$index] bookingId: ${booking.bookingId}, type: ${booking.bookingType}, status: ${booking.status}, date: ${booking.date}")
                }
            }
            
            // ✅ Cải thiện logic lọc: Ẩn booking đã qua ngày hiện tại khi không có filter ngày
            val today = java.time.LocalDate.now().toString() // Format: "2024-01-15"
            val allBookings = bookingUi.myBookings.ifEmpty { bookings }
            
            val filtered = if (selectedDate != null) {
                // Khi có filter ngày cụ thể, hiển thị tất cả booking của ngày đó
                allBookings.filter { it.date == selectedDate }
            } else {
                // Khi không có filter, chỉ hiển thị booking từ hôm nay trở đi
                allBookings.filter { it.date >= today }
            }
            
            // ✅ Hiển thị PENDING/PAID trong mục sắp diễn ra; DONE/CANCELLED trong mục đã hoàn thành
            val upcoming = filtered.filter { it.status.equals("PENDING", true) || it.status.equals("PAID", true) }
            val completed = filtered.filter { it.status.equals("DONE", true) || it.status.equals("CANCELLED", true) }

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
                            onDetailClick = { b -> selectedBookingId = if (selectedBookingId == b.bookingId) null else b.bookingId }
                        )
                        if (selectedBookingId == booking.bookingId) {
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
                            onDetailClick = { b -> selectedBookingId = if (selectedBookingId == b.bookingId) null else b.bookingId }
                        )
                        if (selectedBookingId == booking.bookingId) {
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (selectedDate != null) {
                                        "Không có lịch đặt nào vào ngày $selectedDate"
                                    } else {
                                        "Chưa có lịch đặt nào từ hôm nay"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedDate == null) {
                                    Text(
                                        text = "Sử dụng bộ lọc ngày để xem lịch đặt cũ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.let { dateString ->
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            selectedDate = selectedLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDatePicker = false
                        selectedDate = null // Reset filter
                    }
                ) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
                bookingId = "booking_1",
                renterId = "renter1",
                ownerId = "owner1",
                fieldId = "field_001",
                date = "2024-01-15",
                startAt = "18:00",
                endAt = "19:00",
                slotsCount = 1,
                minutes = 60,
                basePrice = 170000,
                servicePrice = 25000,
                totalPrice = 195000,
                status = "PAID"
            ),
            Booking(
                bookingId = "booking_2",
                renterId = "renter2",
                ownerId = "owner2",
                fieldId = "field_002",
                date = "2024-01-10",
                startAt = "20:00",
                endAt = "21:00",
                slotsCount = 1,
                minutes = 60,
                basePrice = 120000,
                servicePrice = 0,
                totalPrice = 120000,
                status = "DONE"
            )
        )
        RenterBookingScreen(modifier = Modifier.fillMaxSize(), bookings = previewBookings)
    }
}


