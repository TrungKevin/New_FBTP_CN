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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldUiState
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    onNavigateToFieldDetail: (String) -> Unit,
    onNavigateToAddField: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val fieldViewModel: FieldViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    val uiState by fieldViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        if (user == null) authViewModel.fetchProfile()
    }
    
    // Load fields khi user có sẵn
    LaunchedEffect(user?.userId) {
        user?.userId?.let { ownerId ->
            fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
        }
    }
    
    // Auto-reload fields khi có sân mới được thêm
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            if (success.contains("Thêm sân thành công")) {
                user?.userId?.let { ownerId ->
                    fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
                }
            }
        }
    }
    
    val fields = uiState.fields // Sử dụng dữ liệu thực từ Firebase
    val bookings = remember { mockBookings() }
    
    // Tạo summary động dựa trên dữ liệu thực từ Firebase
    val summary by remember(fields) { 
        mutableStateOf(
            HomeSummary(
                newBookings = 2, // TODO: Lấy từ Firebase
                confirmed = 5,    // TODO: Lấy từ Firebase  
                canceled = 1,     // TODO: Lấy từ Firebase
                revenueToday = 1250000, // TODO: Lấy từ Firebase
                totalFields = fields.size // Số lượng sân thực từ Firebase
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
                }
            )
        }
        item { HomeSearchBar(onSearch = { /* TODO: filter */ }) }
        item {
            HomeQuickActions(
                onManageFields = { /* TODO: Navigate to field list */ },
                onBookingList = { /* TODO: Navigate to booking list */ },
                onAddField = onNavigateToAddField,
                onStatistics = { /* TODO: Navigate to stats */ }
            )
        }
        item { HomeSummaryCard(summary = summary) } // Display summary card
        if (fields.isNotEmpty()) {
            item {
                HomeMyFieldsSection( // Display my fields section
                    fields = fields,
                    onFieldClick = { f -> onNavigateToFieldDetail(f.fieldId) }
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
            onNavigateToAddField = { /* Preview callback */ }
        )
    }
}




