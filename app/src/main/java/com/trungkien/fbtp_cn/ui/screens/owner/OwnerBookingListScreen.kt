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
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingEmptyState
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingFilterBar
import com.trungkien.fbtp_cn.ui.components.owner.booking.BookingDetailManage
import com.trungkien.fbtp_cn.ui.components.owner.booking.OwnerMatchCard
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.BookingViewModel
import com.trungkien.fbtp_cn.viewmodel.BookingEvent
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.repository.BookingRepository
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class BookingStatusFilter(val label: String) {
    All("Tất cả"),
    Pending("Chờ xác nhận"),
    Confirmed("Đã xác nhận"),
    Canceled("Đã hủy")
}

private enum class MainTab(val label: String) {
    Bookings("Đặt sân"),
    Matches("Trận đấu")
}

private enum class RecentRangeFilter(val label: String, val days: Long?) {
    All("Tất cả", null),
    Week("1 tuần gần đây", 7),
    Month("1 tháng gần đây", 30),
    Month3("3 tháng gần đây", 90),
    Month6("6 tháng gần đây", 180)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerBookingListScreen(
    onBookingClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    val ui = bookingViewModel.uiState.collectAsState().value
    LaunchedEffect(user) {
        if (user == null) authViewModel.fetchProfile() else {
            bookingViewModel.handle(BookingEvent.LoadByOwner(user.userId))
        }
    }
    val allBookings = ui.ownerBookings
    var selectedTab by remember { mutableStateOf(MainTab.Bookings) }
    var selectedFilter by remember { mutableStateOf(BookingStatusFilter.All) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showRangeMenu by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(RecentRangeFilter.All) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    val filtered = remember(selectedFilter, selectedDate, selectedRange, allBookings) {
        var list = allBookings
        // Range filter
        selectedRange.days?.let { days ->
            val cutoff = LocalDate.now().minusDays(days)
            list = list.filter { b ->
                try { LocalDate.parse(b.date) >= cutoff } catch (_: Exception) { true }
            }
        }
        // Date filter
        selectedDate?.let { d ->
            val ds = d.toString()
            list = list.filter { it.date == ds }
        }
        // Status filter
        list = when (selectedFilter) {
            BookingStatusFilter.All -> list
            BookingStatusFilter.Pending -> list.filter { it.status == "PENDING" }
            BookingStatusFilter.Confirmed -> list.filter { it.status == "PAID" }
            BookingStatusFilter.Canceled -> list.filter { it.status == "CANCELLED" }
        }
        list
    }

    // Nếu có booking được chọn, hiển thị màn hình chi tiết
    selectedBooking?.let { booking ->
        BookingDetailManage(
            booking = booking,
            onConfirm = {
                booking.bookingId.let { id ->
                    bookingViewModel.handle(BookingEvent.UpdateStatus(id, "PAID"))
                }
                selectedBooking = null
            },
            onCancel = {
                booking.bookingId.let { id ->
                    bookingViewModel.handle(BookingEvent.UpdateStatus(id, "CANCELLED"))
                }
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
                Box {
                    IconButton(onClick = { showRangeMenu = true }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Bộ lọc",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showRangeMenu,
                        onDismissRequest = { showRangeMenu = false }
                    ) {
                        RecentRangeFilter.values().forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.label) },
                                onClick = {
                                    selectedRange = opt
                                    showRangeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Header với thống kê (dựa trên danh sách đã lọc)
        BookingStatsHeader(bookings = filtered)

        // Main tab selector
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            MainTab.values().forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content based on selected tab
        when (selectedTab) {
            MainTab.Bookings -> {
                // Status filter chips for bookings
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
                                                bookingViewModel.handle(BookingEvent.UpdateStatus(booking.bookingId, "PAID"))
                                            }
                                            "reject" -> {
                                                bookingViewModel.handle(BookingEvent.UpdateStatus(booking.bookingId, "CANCELLED"))
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
            MainTab.Matches -> {
                // Matches content
                OwnerMatchesContent(
                    selectedDate = selectedDate,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Render DatePicker dialog at root
    OwnerBookingDatePicker(
        show = showDatePicker,
        onDismiss = { showDatePicker = false },
        onSelected = { ld -> selectedDate = ld }
    )
}

@Composable
private fun OwnerMatchesContent(
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    val bookingRepo = remember { BookingRepository() }
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user, selectedDate) {
        if (user != null) {
            isLoading = true
            try {
                // Load matches for all fields owned by this user
                val fieldRepo = FieldRepository()
                val fields = fieldRepo.getFieldsByOwnerId(user.userId).getOrNull() ?: emptyList()
                
                val allMatches = mutableListOf<Match>()
                fields.forEach { field ->
                    val dateStr = selectedDate?.toString() ?: LocalDate.now().toString()
                    bookingRepo.listenMatchesByFieldDate(
                        fieldId = field.fieldId,
                        date = dateStr,
                        onChange = { fieldMatches ->
                            // Remove old matches for this field and add new ones
                            allMatches.removeAll { it.fieldId == field.fieldId }
                            // CHỈ thêm các match đã được ghép đôi (status = "FULL")
                            allMatches.addAll(fieldMatches.filter { it.status == "FULL" })
                            matches = allMatches.toList()
                        },
                        onError = { _ -> }
                    )
                }
            } catch (_: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (matches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Chưa có trận đấu nào đã ghép đôi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(matches, key = { it.rangeKey }) { match ->
                    OwnerMatchCard(
                        match = match,
                        onClick = { /* Handle match click */ }
                    )
                }
            }
        }
    }
}

// Date picker dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerBookingDatePicker(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelected: (LocalDate?) -> Unit
) {
    if (!show) return
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                val date = millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                onSelected(date)
                onDismiss()
            }) { Text("Chọn") }
        },
        dismissButton = {
            TextButton(onClick = {
                onSelected(null)
                onDismiss()
            }) { Text("Xóa") }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun BookingStatsHeader(
    bookings: List<Booking>,
    modifier: Modifier = Modifier
) {
    val pendingCount = bookings.count { it.status == "PENDING" }
    val confirmedCount = bookings.count { it.status == "PAID" }
    val totalRevenue = bookings.filter { it.status == "PAID" }.sumOf { it.totalPrice }

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
    val context = LocalContext.current
    // Load thực tế: tên sân, thông tin renter
    var fieldName by remember(booking.fieldId) { mutableStateOf<String?>(null) }
    var renterName by remember(booking.renterId) { mutableStateOf<String?>(null) }
    var renterPhone by remember(booking.renterId) { mutableStateOf<String?>(null) }
    val userRepo = remember { UserRepository() }
    val fieldRepo = remember { FieldRepository() }
    LaunchedEffect(booking.fieldId) {
        try {
            fieldRepo.getFieldById(booking.fieldId).getOrNull()?.let { f ->
                fieldName = f.name
            }
        } catch (_: Exception) {}
    }
    LaunchedEffect(booking.renterId) {
        try {
            userRepo.getUserById(
                userId = booking.renterId,
                onSuccess = { u ->
                    renterName = u.name
                    renterPhone = u.phone
                },
                onError = { }
            )
        } catch (_: Exception) {}
    }

    fun formatVnCurrency(amount: Long): String {
        return "${String.format("%,d", amount).replace(',', '.')}đ"
    }
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
                        text = "Sân ${fieldName ?: booking.fieldId}",
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
                // Avatar renter - tham khảo cách render từ RenterReviewCard
                val avatarData by produceState(initialValue = "", key1 = booking.renterId) {
                    if (booking.renterId.isNotBlank()) {
                        UserRepository().getUserById(
                            booking.renterId,
                            onSuccess = { u -> value = u.avatarUrl ?: "" },
                            onError = { _ -> value = "" }
                        )
                    } else value = ""
                }

                val rendered = if (avatarData.isNotBlank()) {
                    val decoded = try {
                        val base = if (avatarData.startsWith("data:image")) avatarData.substringAfter(",") else avatarData
                        val bytes = Base64.decode(base, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) { null }
                    if (decoded != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decoded.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        true
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (avatarData.startsWith("http") || avatarData.startsWith("data:image")) avatarData else "data:image/jpeg;base64,$avatarData")
                                .allowHardware(false)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        true
                    }
                } else false

                if (!rendered) {
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
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = renterName ?: booking.renterId,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = renterPhone ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = formatVnCurrency(booking.totalPrice),
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