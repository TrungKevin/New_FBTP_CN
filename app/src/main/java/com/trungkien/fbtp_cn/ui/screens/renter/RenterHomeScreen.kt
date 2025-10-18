package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.home.*
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.trungkien.fbtp_cn.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.BookingViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.BookingEvent
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.ui.components.renter.fieldsearch.SearchResultField
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.repository.ReviewRepository
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import androidx.compose.runtime.LaunchedEffect

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterHomeScreen(
    modifier: Modifier = Modifier,
    onFieldClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = viewModel()
    val fieldViewModel: FieldViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    LaunchedEffect(Unit) {
        if (user == null) authViewModel.fetchProfile()
    }
    val fieldUi = fieldViewModel.uiState.collectAsState().value
    val bookingUi = bookingViewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        fieldViewModel.handleEvent(FieldEvent.LoadAllFields)
        authViewModel.currentUser.collect { u ->
            u?.userId?.let { bookingViewModel.handle(BookingEvent.LoadMine(it)) }
        }
    }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        if (fieldUi.isLoading || bookingUi.isLoading) {
            LoadingDialog(message = "Đang tải dữ liệu...")
        }
        // Header với search bar
        RenterHomeHeader(
            onSearchClick = onSearchClick,
            onMapClick = onMapClick,
            renterName = user?.name ?: ""
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Map preview nhỏ
        RenterMapPreview(
            onMapClick = onMapClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sân nổi bật (lọc theo rating từ 3.0 đến 5.0) và map sang SearchResultField để tái dùng UI/logic
        val baseFields: List<Field> = fieldUi.allFields.ifEmpty { fieldUi.fields }
        val ownerRepo = remember { UserRepository() }
        val reviewRepo = remember { ReviewRepository() }
        var ownerMap by remember { mutableStateOf<Map<String, com.trungkien.fbtp_cn.model.User>>(emptyMap()) }
        var reviewMap by remember { mutableStateOf<Map<String, com.trungkien.fbtp_cn.model.ReviewSummary>>(emptyMap()) }

        LaunchedEffect(baseFields) {
            // Load owner + reviews sơ bộ (best effort)
            val owners = mutableMapOf<String, com.trungkien.fbtp_cn.model.User>()
            val reviews = mutableMapOf<String, com.trungkien.fbtp_cn.model.ReviewSummary>()
            baseFields.forEach { f ->
                try {
                    ownerRepo.getUserById(
                        userId = f.ownerId,
                        onSuccess = { u -> owners[f.ownerId] = u },
                        onError = { }
                    )
                } catch (_: Exception) {}
                try {
                    reviewRepo.getReviewSummary(f.fieldId).getOrNull()?.let { reviews[f.fieldId] = it }
                } catch (_: Exception) {}
            }
            ownerMap = owners
            reviewMap = reviews
        }

        val featuredItems: List<SearchResultField> = baseFields
            .filter { field ->
                val rating = reviewMap[field.fieldId]?.averageRating ?: field.averageRating
                rating >= 3f
            }
            .sortedByDescending { field -> reviewMap[field.fieldId]?.averageRating ?: field.averageRating }
            .take(10)
            .map { f ->
                val owner = ownerMap[f.ownerId]
                val rating = reviewMap[f.fieldId]?.averageRating ?: f.averageRating
                val totalReviews = reviewMap[f.fieldId]?.totalReviews ?: f.totalReviews
                SearchResultField(
                    id = f.fieldId,
                    name = f.name,
                    type = f.sports.firstOrNull() ?: "",
                    price = "", // có thể điền theo pricing rules nếu cần
                    location = f.address,
                    rating = rating,
                    distance = "",
                    isAvailable = f.active,
                    imageUrl = f.images.mainImage.ifEmpty { null },
                    ownerName = owner?.name ?: "Chủ sân",
                    ownerAvatarUrl = owner?.avatarUrl,
                    ownerPhone = owner?.phone ?: "",
                    fieldImages = f.images,
                    address = f.address,
                    openHours = "${f.openHours.start} - ${f.openHours.end}",
                    amenities = f.amenities,
                    totalReviews = totalReviews,
                    contactPhone = f.contactPhone,
                    description = f.description
                )
            }

        RenterFeaturedFields(
            items = featuredItems,
            onFieldClick = onFieldClick,
            onBookClick = onBookClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lịch sử đặt sân gần đây (chỉ hiển thị booking của hôm nay và hôm qua)
        val fieldsById = (fieldUi.allFields.ifEmpty { fieldUi.fields }).associateBy { it.fieldId }
        
        // Lọc booking theo ngày (hôm nay và hôm qua)
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayStr = today.toString() // Format: yyyy-MM-dd
        val yesterdayStr = yesterday.toString() // Format: yyyy-MM-dd
        
        val recentBookings: List<Booking> = bookingUi.myBookings
            .filter { booking -> 
                booking.date == todayStr || booking.date == yesterdayStr
            }
            .sortedByDescending { it.createdAt }
            .take(5)
        RenterRecentBookings(
            bookings = recentBookings,
            fieldsById = fieldsById,
            onFieldClick = onFieldClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick actions
        RenterQuickActions(
            onSearchClick = onSearchClick,
            onMapClick = onMapClick,
            onHistoryClick = onHistoryClick,
            onProfileClick = onProfileClick
        )
        
        Spacer(modifier = Modifier.height(100.dp)) // Space cho bottom navigation
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun RenterHomeScreenPreview() {
    FBTP_CNTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RenterHomeScreen(
                onFieldClick = { fieldId ->
                    // Preview callback
                },
                onSearchClick = {
                    // Preview callback
                },
                onMapClick = {
                    // Preview callback
                },
                onHistoryClick = {
                    // Preview callback
                },
                onProfileClick = {
                    // Preview callback
                }
            )
        }
    }
}
