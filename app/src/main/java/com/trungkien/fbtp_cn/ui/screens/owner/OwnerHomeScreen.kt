package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.model.FieldImages
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeHeader
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeMyFieldsSection
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeQuickActions
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSearchBar
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSummary
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSummaryCard
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeUpcomingBookings
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldUiState
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import androidx.compose.runtime.LaunchedEffect
import com.trungkien.fbtp_cn.viewmodel.BookingViewModel
import com.trungkien.fbtp_cn.viewmodel.BookingEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    onNavigateToFieldDetail: (String) -> Unit,
    onNavigateToAddField: () -> Unit,
    onNavigateToFieldList: () -> Unit = {},
    onNavigateToBookingList: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    modifier: Modifier = Modifier,
    fieldViewModel: FieldViewModel? = null // NHẬN VIEWMODEL TỪ PARENT
) {
    val authViewModel: AuthViewModel = viewModel()
    val localFieldViewModel: FieldViewModel = fieldViewModel ?: viewModel() // SỬ DỤNG VIEWMODEL TỪ PARENT
    val user = authViewModel.currentUser.collectAsState().value
    val bookingViewModel: BookingViewModel = viewModel()
    val bookingUi = bookingViewModel.uiState.collectAsState().value
    val uiState by localFieldViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        if (user == null) authViewModel.fetchProfile()
    }
    // Load bookings for owner to drive today's summary
    LaunchedEffect(user?.userId) {
        user?.userId?.let { ownerId ->
            bookingViewModel.handle(BookingEvent.LoadByOwner(ownerId))
        }
    }
    
    // 🔥 KHÔNG CẦN LOAD DỮ LIỆU TẠI ĐÂY NỮA - ĐÃ ĐƯỢC XỬ LÝ TẠI OWNERMAINSCREEN
    // Chỉ sử dụng dữ liệu từ parent ViewModel
    
    val fields = uiState.fields // Sử dụng dữ liệu thực từ Firebase
    val bookings = bookingUi.ownerBookings
    
    // Debug logging để kiểm tra việc load dữ liệu
    LaunchedEffect(fields, uiState.isLoading, uiState.error) {
        println("DEBUG: 🏠 OwnerHomeScreen - fields count: ${fields.size}")
        println("DEBUG: 🏠 OwnerHomeScreen - isLoading: ${uiState.isLoading}")
        println("DEBUG: 🏠 OwnerHomeScreen - error: ${uiState.error}")
        println("DEBUG: 🏠 OwnerHomeScreen - fieldViewModel from parent: ${fieldViewModel != null}")
        if (fields.isNotEmpty()) {
            println("DEBUG: 🏠 OwnerHomeScreen - first field: ${fields.first().name}")
        }
    }
    
    // Tạo summary động dựa trên dữ liệu thực từ Firebase
    val summary by remember(fields, bookings) { 
        val today = java.time.LocalDate.now()
        fun toLocalDateFromCreated(millis: Long): java.time.LocalDate =
            java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()

        val todayStr = today.toString()
        val todayBookings = bookings.filter { b ->
            // Match either stored date string or createdAt's local date to be robust
            b.date == todayStr || runCatching { toLocalDateFromCreated(b.createdAt) == today }.getOrDefault(false)
        }
        val newCount = todayBookings.count { it.status == "PENDING" }
        val confirmedCount = todayBookings.count { it.status == "PAID" }
        val canceledCount = todayBookings.count { it.status == "CANCELLED" }
        val revenue = todayBookings.filter { it.status == "PAID" }.sumOf { it.totalPrice }
        mutableStateOf(
            HomeSummary(
                newBookings = newCount,
                confirmed = confirmedCount,
                canceled = canceledCount,
                revenueToday = revenue,
                totalFields = fields.size
            )
        )
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val (showSheet, setShowSheet) = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showSheet) { // Hiển thị ModalBottomSheet khi showSheet là true
        ModalBottomSheet(// ModalBottomSheet để hiển thị lịch đặt sân
            onDismissRequest = { setShowSheet(false) }, // Đóng sheet khi người dùng chạm ra ngoài
            sheetState = sheetState, // Trạng thái của sheet
            scrimColor = Color.Black.copy(alpha = 0.32f),
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface // Màu nền của sheet
        ) {
            HomeUpcomingBookings( // Hiển thị danh sách các lịch đặt sân sắp tới
                bookings = bookings,
                onSeeAll = { setShowSheet(false) }
            )
        }
    }

    LazyColumn(// dùng để hiển thị danh sách các thành phần
        modifier = modifier.fillMaxSize(),
      //  contentPadding = PaddingValues(16.dp), // Padding xung quanh nội dung
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {// màn hình đầu tiên hiển thị tiêu đề, avatar và nút lịch
            HomeHeader(
                ownerName = user?.name ?: "",
                onCalendarClick = {
                    coroutineScope.launch { setShowSheet(true) }
                },
                avatarUrl = user?.avatarUrl
            )
        }
        item {
            HomeQuickActions(
                onManageFields = onNavigateToFieldList,
                onBookingList = onNavigateToBookingList,
                onAddField = onNavigateToAddField,
                onStatistics = onNavigateToStats
            )
        }
        item { HomeSummaryCard(summary = summary) } // Display summary card
        if (fields.isNotEmpty()) {
            item {
                HomeMyFieldsSection( // Display my fields section
                    fields = fields,
                    onFieldClick = { f -> onNavigateToFieldDetail(f.fieldId) },
                    ownerAvatarUrl = user?.avatarUrl
                )
            }
        }
    }
}

private fun mockFields(): List<Field> = listOf(
    Field(
        fieldId = "1",
        ownerId = "mockOwnerId",
        name = "POC Pickleball",
        address = "25 Tú Xương, TP. Thủ Đức",
        geo = GeoLocation(),
        sports = listOf("Pickleball"),
        images = FieldImages(
            mainImage = "",
            image1 = "",
            image2 = "",
            image3 = ""
        ),
        slotMinutes = 30,
        openHours = OpenHours(
            start = "05:00",
            end = "23:00",
            isOpen24h = false
        ),
        amenities = listOf("PARKING", "EQUIPMENT"),
        description = "Sân Pickleball chất lượng cao",
        contactPhone = "0926666357",
        averageRating = 4.5f,
        totalReviews = 12,
        isActive = true
    ),
    Field(
        fieldId = "2",
        ownerId = "mockOwnerId",
        name = "Sân Cầu Lông ABC",
        address = "Quận 1, TP.HCM",
        geo = GeoLocation(),
        sports = listOf("BADMINTON"),
        images = FieldImages(
            mainImage = "",
            image1 = "",
            image2 = "",
            image3 = ""
        ),
        slotMinutes = 30,
        openHours = OpenHours(
            start = "06:00",
            end = "22:00",
            isOpen24h = false
        ),
        amenities = listOf("PARKING", "SHOWER"),
        description = "Sân cầu lông chuyên nghiệp",
        contactPhone = "0901234567",
        averageRating = 4.2f,
        totalReviews = 8,
        isActive = false
    )
)

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
        date = "2024-01-15",
        startAt = "15:00",
        endAt = "16:00",
        slotsCount = 1,
        minutes = 60,
        basePrice = 120000,
        servicePrice = 0,
        totalPrice = 120000,
        status = "CANCELLED"
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOwnerHomeScreen() {
    FBTP_CNTheme {
        OwnerHomeScreen(
            onNavigateToFieldDetail = { /* Preview callback */ },
            onNavigateToAddField = { /* Preview callback */ },
            onNavigateToFieldList = { /* Preview callback */ },
            onNavigateToBookingList = { /* Preview callback */ },
            onNavigateToStats = { /* Preview callback */ }
        )
    }
}




