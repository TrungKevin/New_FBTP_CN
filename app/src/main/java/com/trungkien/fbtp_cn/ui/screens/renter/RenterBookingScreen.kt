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
            
            // ✅ CRITICAL FIX: Debug logs với renterId để đảm bảo chỉ hiển thị booking đúng account
            LaunchedEffect(bookingUi.myBookings, myUid) {
                println("🔍 DEBUG: RenterBookingScreen - Booking data updated:")
                println("  - Current userId: $myUid")
                println("  - Total bookings: ${bookingUi.myBookings.size}")
                bookingUi.myBookings.forEachIndexed { index, booking ->
                    val isMatching = booking.renterId == myUid
                    println("  [$index] bookingId: ${booking.bookingId}, renterId: ${booking.renterId}, matches: $isMatching, type: ${booking.bookingType}, status: ${booking.status}, date: ${booking.date}")
                    if (!isMatching) {
                        println("    ⚠️ WARNING: Booking renterId (${booking.renterId}) không khớp với userId hiện tại ($myUid)")
                    }
                }
            }
            
            // ✅ CRITICAL FIX: Chỉ dùng myBookings từ ViewModel, KHÔNG fallback về MockData
            // Điều này đảm bảo chỉ hiển thị booking của account hiện tại
            val today = java.time.LocalDate.now().toString() // Format: "2024-01-15"
            val allBookings = bookingUi.myBookings
            
            // ✅ CRITICAL FIX: Đảm bảo chỉ hiển thị booking của account hiện tại
            // Nếu myUid null, không hiển thị booking nào (user chưa đăng nhập)
            val userBookings = if (myUid != null) {
                allBookings.filter { booking ->
                    booking.renterId == myUid
                }
            } else {
                emptyList() // Không hiển thị gì nếu chưa đăng nhập
            }
            
            // ✅ FIX: Logic lọc ngày - Chỉ hiển thị booking của ngày hôm nay khi không có filter
            val filteredByDate = if (selectedDate != null) {
                // Khi có filter ngày cụ thể, hiển thị tất cả booking của ngày đó
                userBookings.filter { it.date == selectedDate }
            } else {
                // ✅ FIX: Khi không có filter, CHỈ hiển thị booking của ngày hôm nay (không hiển thị ngày tương lai hoặc quá khứ)
                userBookings.filter { it.date == today }
            }
            
            // ✅ CRITICAL FIX: Loại bỏ booking duplicate
            // Vấn đề: Khi renter B join opponent, hệ thống có thể tạo duplicate booking
            // Logic: Group theo fieldId + date + startAt + endAt + renterId (không phụ thuộc vào matchId)
            // Vì matchId có thể null ở booking đầu tiên, sau đó mới được assign
            val filtered = if (myUid != null) {
                filteredByDate
                    .groupBy { booking ->
                        // ✅ FIX: Group theo fieldId + date + startAt + endAt + renterId
                        // Không dùng matchId vì nó có thể thay đổi sau khi match được tạo
                        "${booking.fieldId}_${booking.date}_${booking.startAt}_${booking.endAt}_${booking.renterId}"
                    }
                    .values
                    .mapNotNull { group ->
                        // Nếu có nhiều booking trong group (duplicate) → ưu tiên booking có matchId, nếu không thì giữ mới nhất
                        if (group.size > 1) {
                            println("⚠️ WARNING: Found ${group.size} duplicate bookings, filtering:")
                            group.forEach { b ->
                                println("    - bookingId: ${b.bookingId}, createdAt: ${b.createdAt}, matchId: ${b.matchId}, matchSide: ${b.matchSide}")
                            }
                            // ✅ Ưu tiên: 1) Booking có matchId (đã được assign vào match), 2) Booking mới nhất
                            val withMatchId = group.filter { it.matchId != null && it.matchId.isNotBlank() }
                            val selected = if (withMatchId.isNotEmpty()) {
                                withMatchId.maxByOrNull { it.createdAt } // Giữ booking có matchId và mới nhất
                            } else {
                                group.maxByOrNull { it.createdAt } // Nếu không có matchId, giữ booking mới nhất
                            }
                            println("    ✅ Selected booking: ${selected?.bookingId}, matchId: ${selected?.matchId}")
                            selected
                        } else {
                            group.firstOrNull()
                        }
                    }
            } else {
                filteredByDate
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
                                        "Không có lịch đặt nào trong ngày hôm nay"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedDate == null) {
                                    Text(
                                        text = "Sử dụng bộ lọc ngày để xem lịch đặt các ngày trước đó hoặc ngày tương lai",
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


