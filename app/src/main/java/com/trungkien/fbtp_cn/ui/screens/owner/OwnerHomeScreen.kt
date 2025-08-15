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
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeHeader
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeMyFieldsSection
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeQuickActions
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSearchBar
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSummary
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSummaryCard
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeUpcomingBookings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    onNavigateToFieldDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fields = remember { mockFields() }
    val bookings = remember { mockBookings() }
    val summary by remember { mutableStateOf(HomeSummary(2, 5, 1, 1250000)) }
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
                ownerName = "Kien",
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
                onAddField = { /* TODO */ },
                onStatistics = { /* TODO: Navigate to stats */ }
            )
        }
        item { HomeSummaryCard(summary = summary) } // Display summary card
        if (fields.isNotEmpty()) {
                    item {
            HomeMyFieldsSection( // Display my fields section
                fields = fields,
                onFieldClick = { f -> onNavigateToFieldDetail(f.id) }
            )
        }
        }
    }
}

private fun mockFields(): List<Field> = listOf(
    Field(
        id = "1",
        name = "POC Pickleball",
        type = "Pickleball",
        price = 150000,
        imageUrl = "",
        status = "Available",
        isAvailable = true,
        address = "25 Tú Xương, TP. Thủ Đức",
        operatingHours = "05:00 - 23:00",
        contactPhone = "0926666357",
        distance = "835m"
    ),
    Field(
        id = "2",
        name = "Sân Cầu Lông ABC",
        type = "Cầu Lông",
        price = 120000,
        imageUrl = "",
        status = "Booked",
        isAvailable = false,
        address = "Quận 1, TP.HCM",
        operatingHours = "06:00 - 22:00",
        contactPhone = "0901234567",
        distance = "1.2km"
    )
)

private fun mockBookings(): List<Booking> = listOf(
    Booking("b1", "1", "POC Pickleball", "08:00 - 09:00", "Chờ xác nhận"),
    Booking("b2", "1", "POC Pickleball", "10:00 - 11:00", "Đã xác nhận"),
    Booking("b3", "2", "Sân Cầu Lông ABC", "15:00 - 16:00", "Đã hủy")
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOwnerHomeScreen() {
    FBTP_CNTheme {
        OwnerHomeScreen(
            onNavigateToFieldDetail = { /* Preview callback */ }
        )
    }
}




