package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import java.time.LocalTime
import androidx.compose.runtime.saveable.rememberSaveable

// ✅ STRICT FIX: Helper function to strictly identify Renter A
private fun Booking.isRenterAStrict(): Boolean =
    matchSide?.trim()?.equals("A", ignoreCase = true) == true

// ✅ CRITICAL FIX: Single place to define what belongs to the Bookings tab
private fun Booking.isForBookingsTab(): Boolean =
    bookingType.equals("DUO", true) &&
    createdWithOpponent == true &&     // must be chosen at creation time
    isRenterAStrict()                  // strictly renter A

private enum class BookingStatusFilter(val label: String) {
    All("Tất cả"),
    Pending("Chờ xác nhận"),
    Confirmed("Đã xác nhận"),
    Canceled("Đã hủy"),
    Finished("Đã kết thúc")
}

private enum class MainTab(val label: String) {
    Bookings("Đặt sân"),
    Matches("Trận đấu")
}

private enum class MatchStatusFilter(val label: String) {
    All("Tất cả"),
    Waiting("Đang chờ"), // WAITING_OPPONENT
    Full("Đã ghép đôi"),  // FULL
    Confirmed("Đã xác nhận"), // CONFIRMED
    Cancelled("Đã hủy"), // CANCELLED
    Finished("Đã kết thúc") // endAt < now for today, or ngày chọn < hôm nay
}

// tạo hàm kiểm tra booking đã kết thúc
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
    onMatchClick: (String) -> Unit = {},
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
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Bookings) }
    var selectedFilter by rememberSaveable { mutableStateOf(BookingStatusFilter.All) }
    var selectedMatchFilter by rememberSaveable { mutableStateOf(MatchStatusFilter.All) }
    var showDatePicker by remember { mutableStateOf(false) }
    // ✅ Mặc định xem lịch theo ngày hôm nay; người dùng có thể chuyển qua bộ lọc phạm vi
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showRangeMenu by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(RecentRangeFilter.All) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    
    // Capture callback for navigation
    val matchClickCallback = remember(onMatchClick) { onMatchClick }
    
    // Function to handle match click
    fun handleMatchClick(matchId: String) {
        matchClickCallback(matchId)
    }

    val filtered = remember(selectedFilter, selectedDate, selectedRange, allBookings, selectedTab) {
        println("🔍 DEBUG: Starting filter process - selectedTab: ${selectedTab.name}, allBookings size: ${allBookings.size}")
        
        var list = allBookings
        // Range filter
        selectedRange.days?.let { days ->
            val cutoff = LocalDate.now().minusDays(days)
            list = list.filter { b ->
                try { LocalDate.parse(b.date) >= cutoff } catch (_: Exception) { true }
            }
            println("🔍 DEBUG: After range filter (${days} days): ${list.size}")
        }
        // Date filter
        selectedDate?.let { d ->
            val ds = d.toString()
            list = list.filter { it.date == ds }
            println("🔍 DEBUG: After date filter ($ds): ${list.size}")
        }
        
        // ✅ FIX: Tách logic theo tab
        println("🔍 DEBUG: Selected tab: ${selectedTab.name}")
        println("🔍 DEBUG: Current tab is: ${if (selectedTab == MainTab.Bookings) "ĐẶT SÂN" else "TRẬN ĐẤU"}")
        when (selectedTab) {
            MainTab.Bookings -> {
                println("🔍 DEBUG: Processing MainTab.Bookings - input list size: ${list.size}")
                println("🔍 DEBUG: ⚠️ USING STRICT FILTERING LOGIC WITH ABSOLUTE DENYLIST ⚠️")
                
                // ✅ STRICT FILTER: Absolute denylist for any non-A or null side
                list = list.filter { booking ->
                    // Absolute denylist for any non-A or null side
                    val isSideBOrNull = booking.matchSide == null ||
                                        booking.matchSide.equals("B", true)
                    if (isSideBOrNull) {
                        println("🔍 BookingsTab STRICT DENY -> id=${booking.bookingId}, side='${booking.matchSide}' (REJECTED)")
                        return@filter false
                    }

                    val show = booking.isForBookingsTab() && !isBookingFinished(booking, selectedDate)
                    // Optional debug
                    println("🔍 BookingsTab strict -> id=${booking.bookingId}, side=${booking.matchSide}, createdWithOpponent=${booking.createdWithOpponent}, show=$show")
                    show
                }

                // Then apply status filter on this already restricted list
                list = when (selectedFilter) {
                    BookingStatusFilter.All -> list.filter { !isBookingFinished(it, selectedDate) }
                    BookingStatusFilter.Pending -> list.filter { it.status.equals("PENDING", true) && !isBookingFinished(it, selectedDate) }
                    BookingStatusFilter.Confirmed -> list.filter { (it.status.equals("PAID", true) || it.status.equals("CONFIRMED", true)) && !isBookingFinished(it, selectedDate) }
                    BookingStatusFilter.Canceled -> list.filter { it.status.equals("CANCELLED", true) }
                    BookingStatusFilter.Finished -> list.filter { (it.status.equals("PAID", true) || it.status.equals("CONFIRMED", true)) && isBookingFinished(it, selectedDate) }
                }
            }
            MainTab.Matches -> {
                // Tab "Trận đấu": Hiển thị tất cả bookings (cả chưa có đối thủ và đã có đối thủ)
                // Không filter theo opponentMode nữa, hiển thị tất cả
                println("🔍 DEBUG: Tab 'Trận đấu' - showing all bookings: ${list.size}")
                // Status filter cho tất cả bookings
                list = when (selectedFilter) {
                    BookingStatusFilter.All -> list
                    BookingStatusFilter.Pending -> list.filter { it.status == "PENDING" }
                    BookingStatusFilter.Confirmed -> list.filter { it.status == "PAID" || it.status == "CONFIRMED" }
                    BookingStatusFilter.Canceled -> list.filter { it.status == "CANCELLED" }
                    BookingStatusFilter.Finished -> list.filter { isBookingFinished(it, selectedDate) }
                }
            }
        }
        
        println("🔍 DEBUG: Final filtered list size: ${list.size}")
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
                                    // ✅ Khi chọn phạm vi (tuần/tháng/...), bỏ chọn ngày đơn lẻ để phạm vi có hiệu lực
                                    selectedDate = null
                                    showRangeMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Header thống kê tổng hợp theo bộ lọc ngày/phạm vi, gộp cả Đặt sân và Trận đấu (từ allBookings)
        BookingStatsHeader(
            bookings = allBookings,
            selectedDate = selectedDate,
            selectedRange = selectedRange,
            ownerId = user?.userId
        )

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
                // Matches content with status filter
                Column(modifier = Modifier.fillMaxSize()) {
                    BookingFilterBar(
                        options = MatchStatusFilter.values().map { it.label },
                        selected = selectedMatchFilter.label,
                        onSelectedChange = { label ->
                            selectedMatchFilter = MatchStatusFilter.values().first { it.label == label }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OwnerMatchesContent(
                        selectedDate = selectedDate,
                        selectedStatus = selectedMatchFilter,
                        onMatchClick = onMatchClick,  // <-- Truyền callback từ parameter của OwnerBookingListScreen
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Render DatePicker dialog at root
    OwnerBookingDatePicker(
        show = showDatePicker,
        onDismiss = { showDatePicker = false },
        onSelected = { ld ->
            // ✅ Khi chọn ngày, ưu tiên ngày và reset phạm vi về "Tất cả"
            selectedDate = ld
            selectedRange = RecentRangeFilter.All
        }
    )
}

@Composable
private fun OwnerMatchesContent(
    selectedDate: LocalDate?,
    selectedStatus: MatchStatusFilter,
    onMatchClick: (String) -> Unit,  // <-- Thêm parameter này
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    val bookingRepo = remember { BookingRepository() }
    val scope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<Match>>(emptyList()) }
    // ✅ NEW: Danh sách matches dùng cho kiểm tra overlap (không bị ảnh hưởng bởi filter tab)
    var matchedForOverlap by remember { mutableStateOf<List<Match>>(emptyList()) }
    // ✅ NEW: Không mutate list trong items{} để tránh crash compose; dùng bộ lọc/override cục bộ
    var removedWaitingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var matchStatusOverride by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var waitingBookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var listeners by remember { mutableStateOf<List<com.google.firebase.firestore.ListenerRegistration>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user, selectedDate, selectedStatus) {
        if (user != null) {
            isLoading = true
            try {
                // Load matches for all fields owned by this user
                val fieldRepo = FieldRepository()
                val fields = fieldRepo.getFieldsByOwnerId(user.userId).getOrNull() ?: emptyList()
                println("🔍 DEBUG: OwnerMatchesContent - ownerId=${user.userId}, fields=${fields.map { it.fieldId }}")
                
                val allMatches = mutableListOf<Match>()
                val allWaitingBookings = mutableListOf<Booking>()
                
                // Clear old listeners when filter/date changes
                listeners.forEach { it.remove() }
                listeners = emptyList()
                
                fields.forEach { field ->
                    val dateStr = selectedDate?.toString() ?: LocalDate.now().toString()
                    println("🔍 DEBUG: listen matches field=${field.fieldId}, date=$dateStr, filter=${selectedStatus.name}")
                    
                    // Listen to matches
                    val matchListener = bookingRepo.listenMatchesByFieldDate(
                        fieldId = field.fieldId,
                        date = dateStr,
                        onChange = { fieldMatches ->
                            println("✅ DEBUG: listenMatchesByFieldDate → field=${field.fieldId} size=${fieldMatches.size}")
                            
                            // ✅ DEBUG: Log tất cả matches từ Firebase
                            fieldMatches.forEach { match ->
                                println("    - Raw match ${match.rangeKey}: status=${match.status}, time=${match.startAt}-${match.endAt}")
                            }
                            
                            // Remove old matches for this field and add new ones
                            allMatches.removeAll { it.fieldId == field.fieldId }
                            matchedForOverlap = matchedForOverlap.filter { it.fieldId != field.fieldId }
                            
                            // ✅ Dùng danh sách đầy đủ cho hiển thị, nhưng chỉ FULL/CONFIRMED để overlap
                            val matchedForDisplay = when (selectedStatus) {
                                MatchStatusFilter.Cancelled -> fieldMatches.filter { it.status == "CANCELLED" }
                                MatchStatusFilter.Full -> fieldMatches.filter { it.status == "FULL" && !isFinished(it) }
                                MatchStatusFilter.Confirmed -> fieldMatches.filter { it.status == "CONFIRMED" && !isFinished(it) }
                                // Đã kết thúc: chỉ những trận đã CONFIRMED và đã qua thời gian
                                MatchStatusFilter.Finished -> fieldMatches.filter { it.status == "CONFIRMED" && isFinished(it) }
                                MatchStatusFilter.Waiting -> emptyList() // Không hiển thị WAITING ở danh sách match
                                MatchStatusFilter.All -> fieldMatches.filter { (it.status == "FULL" || it.status == "CONFIRMED") && !isFinished(it) }
                            }

                            // ✅ Lưu cho overlap-check (chỉ FULL/CONFIRMED)
                            val overlapPool = fieldMatches.filter { it.status == "FULL" || it.status == "CONFIRMED" }
                            matchedForOverlap = matchedForOverlap + overlapPool

                            val filtered = matchedForDisplay
                            
                            println("✅ DEBUG: filtered(${selectedStatus.name}) size=${filtered.size}")
                            allMatches.addAll(filtered)
                            matches = allMatches

                            // ✅ Re-filter waitingBookings ngay khi matches thay đổi
                            val currentMatches = matchedForOverlap.toList()
                            waitingBookings = waitingBookings.filter { booking ->
                                val overlapped = currentMatches.any { m ->
                                    m.fieldId == booking.fieldId && m.date == booking.date &&
                                        isTimeOverlap(booking.startAt, booking.endAt, m.startAt, m.endAt)
                                }
                                val finished = isBookingFinished(booking, selectedDate)
                                !overlapped && !finished
                            }
                            println("🔄 DEBUG: Re-filtered waitingBookings after matches update: ${waitingBookings.size}")
                        },
                        onError = { _ -> }
                    )
                    listeners = listeners + matchListener
                    
                     // Listen to waiting bookings (chưa có đối thủ) - sử dụng listenBookingsByOwner
                     val bookingListener = bookingRepo.listenBookingsByOwner(
                         ownerId = user.userId,
                         onChange = { allOwnerBookings ->
                             println("✅ DEBUG: listenBookingsByOwner → size=${allOwnerBookings.size}")
                             // Remove old bookings for this field and add new ones
                             allWaitingBookings.removeAll { it.fieldId == field.fieldId }
                             // Filter theo fieldId và date
                             val fieldBookings = allOwnerBookings.filter { booking ->
                                 booking.fieldId == field.fieldId && booking.date == dateStr
                             }
                             
                            // ✅ CRITICAL FIX: Logic filtering chính xác để loại bỏ booking khi đã có match
                            // Ghi chú: Ở phía trên chúng ta đã lọc bookings theo field của owner
                            // nên không cần ràng buộc ownerId ở đây nữa (tránh ẩn thẻ khi booking.ownerId bị lưu sai).
                            val waitingOnly = fieldBookings.filter { booking ->
                                val isSolo = booking.bookingType == "SOLO"
                                val hasNoOpponent = booking.hasOpponent == false
                                val isPending = booking.status == "PENDING"
                                
                                // ✅ KEY: Loại bỏ nếu khung giờ đã có match FULL/CONFIRMED trùng
                                val hasOverlappingMatch = matchedForOverlap.any { match ->
                                    val sameField = match.fieldId == booking.fieldId
                                    val sameDate = match.date == booking.date
                                    val timeOverlap = isTimeOverlap(booking.startAt, booking.endAt, match.startAt, match.endAt)
                                    
                                    println("    🔍 Checking overlap for booking ${booking.bookingId} vs match ${match.rangeKey}:")
                                    println("      - sameField: $sameField (${match.fieldId} == ${booking.fieldId})")
                                    println("      - sameDate: $sameDate (${match.date} == ${booking.date})")
                                    println("      - timeOverlap: $timeOverlap (${booking.startAt}-${booking.endAt} vs ${match.startAt}-${match.endAt})")
                                    
                                    sameField && sameDate && timeOverlap
                                }
                                
                                val shouldShow = isSolo && hasNoOpponent && isPending && !hasOverlappingMatch
                                
                                println("    📋 Booking ${booking.bookingId} (${booking.renterId}):")
                                println("      - isSolo: $isSolo")
                                println("      - hasNoOpponent: $hasNoOpponent")
                                println("      - isPending: $isPending")
                                println("      - hasOverlappingMatch: $hasOverlappingMatch")
                                println("      - shouldShow: $shouldShow")
                                
                                shouldShow
                            }
                             
                             // ✅ DEBUG: Log chi tiết để debug
                             println("🔍 DEBUG: Field ${field.fieldId}, Date $dateStr:")
                             println("  - Total fieldBookings: ${fieldBookings.size}")
                             println("  - WaitingOnly (after match check): ${waitingOnly.size}")
                             println("  - AllMatches count: ${allMatches.size}")
                             waitingOnly.forEach { booking ->
                                 println("    - Waiting booking ${booking.bookingId}: status=${booking.status}, time=${booking.startAt}-${booking.endAt}")
                             }
                             allMatches.forEach { match ->
                                 println("    - Match ${match.rangeKey}: status=${match.status}, time=${match.startAt}-${match.endAt}")
                             }
                             
                            // Filter theo status: chỉ hiển thị ở đúng bộ lọc của nó và "Tất cả"
                            val filtered = when (selectedStatus) {
                                // All: chỉ các booking SOLO-PENDING chưa kết thúc
                                MatchStatusFilter.All -> waitingOnly.filter { !isBookingFinished(it, selectedDate) }
                                // Waiting: chỉ PENDING chưa kết thúc
                                MatchStatusFilter.Waiting -> waitingOnly.filter { it.status == "PENDING" && !isBookingFinished(it, selectedDate) }
                                // Full/Confirmed không áp dụng cho waiting bookings
                                MatchStatusFilter.Full -> emptyList()
                                MatchStatusFilter.Confirmed -> emptyList()
                                // Cancelled: lấy trực tiếp từ bookings theo field cho trạng thái CANCELLED (không áp ràng buộc isPending)
                                MatchStatusFilter.Cancelled -> fieldBookings.filter { it.bookingType == "SOLO" && it.status == "CANCELLED" }
                                // Finished: chỉ booking đã kết thúc theo thời gian (PENDING)
                                MatchStatusFilter.Finished -> waitingOnly.filter { isBookingFinished(it, selectedDate) }
                            }
                             
                             println("🔍 DEBUG: Filtered waiting bookings for ${selectedStatus.name}: ${filtered.size}")
                             allWaitingBookings.addAll(filtered)
                             waitingBookings = allWaitingBookings
                         },
                         onError = { _ -> }
                     )
                    listeners = listeners + bookingListener
                }
                
                // ✅ FIX: Tạo listeners cho matches cho từng field
                fields.forEach { field ->
                    val dateStr = selectedDate?.toString() ?: LocalDate.now().toString()
                    println("🔍 DEBUG: listen matches field=${field.fieldId}, date=$dateStr, filter=${selectedStatus.name}")
                    
                    val matchListener = bookingRepo.listenMatchesByFieldDate(
                        fieldId = field.fieldId,
                        date = dateStr,
                        onChange = { fieldMatches ->
                            println("✅ DEBUG: listenMatchesByFieldDate → field=${field.fieldId} size=${fieldMatches.size}")
                            // Remove old matches for this field and add new ones
                            allMatches.removeAll { it.fieldId == field.fieldId }
                            // Chỉ lấy matches đã ghép đôi (FULL hoặc CONFIRMED)
                            val matchedOnly = fieldMatches.filter { match ->
                                match.status == "FULL" || match.status == "CONFIRMED"
                            }
                            // Hiển thị theo filter trạng thái
                            val filtered = when (selectedStatus) {
                                MatchStatusFilter.All -> matchedOnly.filter { !isFinished(it) }
                                MatchStatusFilter.Waiting -> emptyList()
                                MatchStatusFilter.Full -> matchedOnly.filter { it.status == "FULL" && !isFinished(it) }
                                MatchStatusFilter.Confirmed -> matchedOnly.filter { it.status == "CONFIRMED" && !isFinished(it) }
                                MatchStatusFilter.Cancelled -> matchedOnly.filter { it.status == "CANCELLED" && !isFinished(it) }
                                // Đã kết thúc: chỉ CONFIRMED và đã qua thời gian
                                MatchStatusFilter.Finished -> matchedOnly.filter { it.status == "CONFIRMED" && isFinished(it) }
                            }
                            println("✅ DEBUG: filtered(${selectedStatus.name}) size=${filtered.size}")
                            allMatches.addAll(filtered)
                            matches = allMatches

                            // ✅ Re-filter waitingBookings ngay khi matches thay đổi (listener thứ 2)
                            val currentMatches = matchedForOverlap.toList()
                            waitingBookings = waitingBookings.filter { booking ->
                                val overlapped = currentMatches.any { m ->
                                    m.fieldId == booking.fieldId && m.date == booking.date &&
                                        isTimeOverlap(booking.startAt, booking.endAt, m.startAt, m.endAt)
                                }
                                val finished = isBookingFinished(booking, selectedDate)
                                !overlapped && !finished
                            }
                            println("🔄 DEBUG: Re-filtered waitingBookings (2nd) after matches update: ${waitingBookings.size}")
                        },
                        onError = { _ -> }
                    )
                    listeners = listeners + matchListener
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
        } else if (matches.isEmpty() && waitingBookings.isEmpty()) {
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
                        text = "Chưa có trận đấu hoặc đặt sân chờ đối thủ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Tạo danh sách hiển thị an toàn (tránh mutate trực tiếp trong Lazy items)
            val waitingToDisplay = remember(waitingBookings, removedWaitingIds) {
                waitingBookings.filter { it.bookingId !in removedWaitingIds }
            }
            val matchesToDisplay = remember(matches, matchStatusOverride) {
                matches.map { m -> matchStatusOverride[m.rangeKey]?.let { s -> m.copy(status = s) } ?: m }
            }
            
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Hiển thị waiting bookings trước (chưa có đối thủ) - Card như hình 1
                items(waitingToDisplay, key = { it.bookingId }) { booking ->
                    val finished = isBookingFinished(booking, selectedDate)
                    WaitingBookingCard(
                        booking = booking,
                        onClick = { /* Handle booking click */ },
                        // ✅ Optimistic update: remove from waiting list ngay khi xác nhận/hủy
                        onConfirm = if (booking.status != "CANCELLED" && !finished) {
                            {
                                // Đánh dấu loại bỏ để UI không crash trong quá trình đo của LazyList
                                removedWaitingIds = removedWaitingIds + booking.bookingId
                                // Thực hiện cập nhật thật lên server
                                scope.launch { bookingRepo.updateBookingStatus(booking.bookingId, "PAID") }
                            }
                        } else null,
                        onCancel = if (booking.status != "CANCELLED" && !finished) {
                            {
                                removedWaitingIds = removedWaitingIds + booking.bookingId
                                scope.launch { bookingRepo.updateBookingStatus(booking.bookingId, "CANCELLED") }
                            }
                        } else null,
                        onSuggestTime = if (!finished) {
                            {
                                // TODO: Xử lý gợi ý khung giờ khác
                            }
                        } else null
                    )
                }
                
                // Hiển thị matches (đã ghép đôi) - Card như hình 2
                items(matchesToDisplay, key = { it.rangeKey }) { match ->
                    val finished = isFinished(match)
                    OwnerMatchCard(
                        match = match,
                        onClick = { 
                            // Navigate to match detail - sử dụng callback đã truyền
                            onMatchClick(match.rangeKey)
                        },
                        // ✅ Optimistic update: cập nhật trạng thái local ngay khi bấm
                        onConfirm = if (match.status != "CANCELLED" && !finished) {
                            {
                                // Ghi override trạng thái để UI phản hồi tức thời, không mutate list gốc trong items
                                matchStatusOverride = matchStatusOverride + (match.rangeKey to "CONFIRMED")
                                // Nếu tab hiện tại không bao gồm CONFIRMED, re-filter sẽ ẩn nó sau khi recomposition
                                scope.launch { bookingRepo.updateMatchStatus(match.rangeKey, "CONFIRMED") }
                            }
                        } else null,
                        onCancel = if (match.status != "CANCELLED" && !finished) {
                            {
                                matchStatusOverride = matchStatusOverride + (match.rangeKey to "CANCELLED")
                                scope.launch { bookingRepo.updateMatchStatus(match.rangeKey, "CANCELLED") }
                            }
                        } else null
                    )
                }
            }
        }
    }
}

private fun isFinished(match: Match): Boolean {
    return try {
        val matchDate = LocalDate.parse(match.date)
        val end = LocalTime.parse(match.endAt)
        val today = LocalDate.now()
        if (matchDate.isBefore(today)) return true
        if (matchDate.isAfter(today)) return false
        // cùng ngày hôm nay: kết thúc nếu endAt < thời điểm hiện tại
        val now = LocalTime.now()
        return end.isBefore(now)
    } catch (_: Exception) { false }
}

private fun isBookingFinished(booking: Booking, selectedDate: LocalDate?): Boolean {
    return try {
        val bookingDate = LocalDate.parse(booking.date)
        val end = LocalTime.parse(booking.endAt)
        val today = LocalDate.now()
        if (bookingDate.isBefore(today)) return true
        if (bookingDate.isAfter(today)) return false
        val now = LocalTime.now()
        return end.isBefore(now)
    } catch (_: Exception) { false }
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
    selectedDate: LocalDate?,
    selectedRange: RecentRangeFilter,
    ownerId: String?,
    modifier: Modifier = Modifier
) {
    val bookingRepo = remember { BookingRepository() }
    val scope = rememberCoroutineScope()
    var headerMatches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var matchListeners by remember { mutableStateOf<List<com.google.firebase.firestore.ListenerRegistration>>(emptyList()) }

    // ✅ IMPROVED: Lắng nghe matches cho tất cả các field của owner để thống kê chính xác
    LaunchedEffect(ownerId, selectedDate, selectedRange) {
        // Clear listeners cũ
        matchListeners.forEach { it.remove() }
        matchListeners = emptyList()
        
        if (ownerId != null) {
            val fields = FieldRepository().getFieldsByOwnerId(ownerId).getOrNull().orEmpty()
            val allMatches = mutableListOf<Match>()
            
            fields.forEach { field ->
                // Nếu có selectedDate, chỉ lắng nghe ngày đó
                // Nếu có selectedRange, lắng nghe tất cả các ngày trong phạm vi
                val datesToListen = when {
                    selectedDate != null -> listOf(selectedDate.toString())
                    selectedRange.days != null -> {
                        val today = LocalDate.now()
                        (0 until selectedRange.days.toInt()).map { 
                            today.minusDays(it.toLong()).toString() 
                        }
                    }
                    else -> listOf(LocalDate.now().toString()) // Mặc định hôm nay
                }
                
                datesToListen.forEach { dateStr ->
                    val listener = bookingRepo.listenMatchesByFieldDate(
                        fieldId = field.fieldId,
                        date = dateStr,
                        onChange = { fieldMatches ->
                            println("📊 DEBUG: StatsHeader - Field ${field.fieldId}, Date $dateStr: ${fieldMatches.size} matches")
                            
                            // Remove old matches for this field+date combination
                            allMatches.removeAll { it.fieldId == field.fieldId && it.date == dateStr }
                            allMatches.addAll(fieldMatches)
                            headerMatches = allMatches.toList()
                            
                            println("📊 DEBUG: StatsHeader - Total matches: ${headerMatches.size}")
                        },
                        onError = { error ->
                            println("📊 ERROR: StatsHeader listener failed: $error")
                        }
                    )
                    matchListeners = matchListeners + listener
                }
            }
        } else {
            headerMatches = emptyList()
        }
    }

    // ✅ IMPROVED: Áp dụng bộ lọc ngày/phạm vi cho thống kê với debug
    var filteredBookings = bookings
        .filter { b -> ownerId == null || b.ownerId == ownerId }
    
    println("📊 DEBUG: StatsHeader - Initial bookings: ${bookings.size}, filtered by owner: ${filteredBookings.size}")
    
    selectedRange.days?.let { days ->
        val cutoff = LocalDate.now().minusDays(days)
        val beforeRange = filteredBookings.size
        filteredBookings = filteredBookings.filter { b ->
            try { LocalDate.parse(b.date) >= cutoff } catch (_: Exception) { true }
        }
        println("📊 DEBUG: StatsHeader - After range filter (${days} days): $beforeRange -> ${filteredBookings.size}")
    }
    
    selectedDate?.let { d ->
        val ds = d.toString()
        val beforeDate = filteredBookings.size
        filteredBookings = filteredBookings.filter { it.date == ds }
        println("📊 DEBUG: StatsHeader - After date filter ($ds): $beforeDate -> ${filteredBookings.size}")
    }

    val isFinished: (Booking) -> Boolean = { b ->
        try {
            val bookingDate = LocalDate.parse(b.date)
            val end = LocalTime.parse(b.endAt)
            val today = LocalDate.now()
            if (bookingDate.isBefore(today)) true
            else if (bookingDate.isAfter(today)) false
            else end.isBefore(LocalTime.now())
        } catch (_: Exception) { false }
    }

    val isMatchFinished: (Match) -> Boolean = { m ->
        try {
            val matchDate = LocalDate.parse(m.date)
            val end = LocalTime.parse(m.endAt)
            val today = LocalDate.now()
            if (matchDate.isBefore(today)) true
            else if (matchDate.isAfter(today)) false
            else end.isBefore(LocalTime.now())
        } catch (_: Exception) { false }
    }

    // ✅ STRICT FIX: Tính toán thống kê với strict Renter A rule
    // Chờ xác nhận: PENDING bookings (Renter A) + FULL matches chưa kết thúc
    val pendingFromBookings = filteredBookings.count { booking ->
        val isPending = booking.status.equals("PENDING", true)
        val notFinished = !isFinished(booking)
        val result = isPending && booking.isForBookingsTab() && notFinished
        
        if (result) {
            println("📊 DEBUG: Pending booking: ${booking.bookingId} (${booking.renterId})")
        }
        result
    }
    
    val pendingFromMatches = headerMatches.count { match ->
        val isFull = match.status.equals("FULL", true)
        val notFinished = !isMatchFinished(match)
        val result = isFull && notFinished
        
        if (result) {
            println("📊 DEBUG: Pending match: ${match.rangeKey}")
        }
        result
    }
    val pendingCount = pendingFromBookings + pendingFromMatches
    
    // Đã xác nhận: PAID/CONFIRMED bookings (Renter A) + CONFIRMED matches chưa kết thúc
    val confirmedFromBookings = filteredBookings.count { booking ->
        val s = booking.status.uppercase()
        val isConfirmed = s == "PAID" || s == "CONFIRMED"
        val notFinished = !isFinished(booking)
        val result = isConfirmed && booking.isForBookingsTab() && notFinished
        
        if (result) {
            println("📊 DEBUG: Confirmed booking: ${booking.bookingId} (${booking.renterId})")
        }
        result
    }
    
    val confirmedFromMatches = headerMatches.count { match ->
        val isConfirmed = match.status.equals("CONFIRMED", true)
        val notFinished = !isMatchFinished(match)
        val result = isConfirmed && notFinished
        
        if (result) {
            println("📊 DEBUG: Confirmed match: ${match.rangeKey}")
        }
        result
    }
    val confirmedCount = confirmedFromBookings + confirmedFromMatches
    
    // Đã hủy: CANCELLED bookings (Renter A) + CANCELLED matches
    val cancelledFromBookings = filteredBookings.count { booking ->
        val isCancelled = booking.status.equals("CANCELLED", true)
        val result = isCancelled && booking.isForBookingsTab()
        
        if (result) {
            println("📊 DEBUG: Cancelled booking: ${booking.bookingId} (${booking.renterId})")
        }
        result
    }
    
    val cancelledFromMatches = headerMatches.count { match ->
        val isCancelled = match.status.equals("CANCELLED", true)
        val result = isCancelled
        
        if (result) {
            println("📊 DEBUG: Cancelled match: ${match.rangeKey}")
        }
        result
    }
    val cancelledCount = cancelledFromBookings + cancelledFromMatches
    
    // ✅ STRICT FIX: Doanh thu - chỉ tính các trận đã XÁC NHẬN và đã KẾT THÚC
    val revenueFromBookings = filteredBookings
        .asSequence()
        .filter { booking ->
            val statusOk = booking.status.equals("PAID", true) || booking.status.equals("CONFIRMED", true)
            val isFinished = isFinished(booking)
            val result = statusOk && booking.isForBookingsTab() && isFinished
            
            if (result) {
                println("📊 DEBUG: Revenue booking: ${booking.bookingId} - ${booking.totalPrice}đ")
            }
            result
        }
        .sumOf { it.totalPrice }
        
    val revenueFromMatches = headerMatches
        .asSequence()
        .filter { match ->
            val isConfirmed = match.status.equals("CONFIRMED", true)
            val isFinished = isMatchFinished(match)
            val result = isConfirmed && isFinished
            
            if (result) {
                println("📊 DEBUG: Revenue match: ${match.rangeKey} - ${match.totalPrice}đ")
            }
            result
        }
        .sumOf { it.totalPrice }
        
    val totalRevenue = revenueFromBookings + revenueFromMatches
    
    // ✅ DEBUG: Log tổng kết thống kê
    println("📊 DEBUG: StatsHeader Summary:")
    println("  - Pending: $pendingFromBookings bookings + $pendingFromMatches matches = $pendingCount")
    println("  - Confirmed: $confirmedFromBookings bookings + $confirmedFromMatches matches = $confirmedCount")
    println("  - Cancelled: $cancelledFromBookings bookings + $cancelledFromMatches matches = $cancelledCount")
    println("  - Revenue: ${revenueFromBookings}đ bookings + ${revenueFromMatches}đ matches = ${totalRevenue}đ")

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
                color = Color(0xFF2E7D32)
            )
            StatItem(
                title = "Đã hủy",
                value = cancelledCount.toString(),
                color = Color(0xFFF44336)
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
    // ✅ Xác định đã kết thúc theo thời gian thực tế (ngày/giờ)
    val isFinishedByTime = try {
        val bDate = java.time.LocalDate.parse(booking.date)
        val end = java.time.LocalTime.parse(booking.endAt)
        val today = java.time.LocalDate.now()
        val now = java.time.LocalTime.now()
        bDate.isBefore(today) || (bDate.isEqual(today) && end.isBefore(now))
    } catch (_: Exception) { false }

    // Màu sắc trạng thái đồng bộ với OwnerMatchCard
    val statusColor = when {
        // Hiển thị giống tab Trận đấu: màu chữ đậm, nền mờ (alpha 0.12)
        isFinishedByTime && (booking.status == "PAID" || booking.status == "CONFIRMED") -> Color(0xFF2E7D32)
        booking.status == "CONFIRMED" || booking.status == "PAID" -> Color(0xFF2E7D32)
        booking.status == "PENDING" -> Color(0xFFFF9800)
        booking.status == "CANCELLED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val statusText = when {
        isFinishedByTime && (booking.status == "PAID" || booking.status == "CONFIRMED") -> "ĐÃ KẾT THÚC"
        booking.status == "CONFIRMED" || booking.status == "PAID" -> "ĐÃ XÁC NHẬN"
        booking.status == "PENDING" -> "ĐANG CHỜ"
        booking.status == "CANCELLED" -> "ĐÃ HỦY"
        else -> booking.status
    }

    val statusIcon = when {
        isFinishedByTime -> "⌛"
        booking.status == "PAID" || booking.status == "CONFIRMED" -> "✓"
        booking.status == "PENDING" -> "⏱"
        booking.status == "CANCELLED" -> "✕"
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
                            text = statusText,
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

            // Action buttons cho pending bookings với design mới (ẩn nếu trận đã kết thúc)
            if (booking.status == "PENDING" && !isFinishedByTime) {
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
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Hủy",
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


@Composable
private fun WaitingBookingCard(
    booking: Booking,
    onClick: () -> Unit = {},
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onSuggestTime: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fieldRepo = remember { FieldRepository() }
    val userRepo = remember { UserRepository() }
    
    var fieldName by remember(booking.fieldId) { mutableStateOf<String?>(null) }
    var renterName by remember(booking.renterId) { mutableStateOf<String?>(null) }
    var renterPhone by remember(booking.renterId) { mutableStateOf<String?>(null) }
    var renterAvatarUrl by remember(booking.renterId) { mutableStateOf<String?>(null) }
    
    LaunchedEffect(booking.fieldId) {
        fieldRepo.getFieldById(booking.fieldId).onSuccess { field ->
            fieldName = field?.name
        }
    }
    
    LaunchedEffect(booking.renterId) {
        userRepo.getUserById(
            userId = booking.renterId,
            onSuccess = { user ->
                renterName = user.name
                renterPhone = user.phone
                renterAvatarUrl = user.avatarUrl
            },
            onError = { _ ->
                // Handle error silently
            }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header với tên sân và trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fieldName ?: "Sân không xác định",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Status chip
                val statusColor = when (booking.status) {
                    "PENDING" -> Color(0xFFFF9800) // Màu cam cho "ĐANG CHỜ ĐỐI THỦ"
                    "PAID", "CONFIRMED" -> Color(0xFF4CAF50)
                    "CANCELLED" -> Color(0xFFF44336)
                    else -> Color(0xFFFF9800) // Mặc định là cam cho "ĐANG CHỜ ĐỐI THỦ"
                }
                
                val statusText = when (booking.status) {
                    "PENDING" -> "ĐANG CHỜ ĐỐI THỦ"
                    "PAID", "CONFIRMED" -> "ĐÃ GHÉP ĐÔI"
                    "CANCELLED" -> "ĐÃ HỦY"
                    else -> "ĐANG CHỜ ĐỐI THỦ"
                }
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Thông tin booking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = booking.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${booking.startAt} - ${booking.endAt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "71 duong 10", // TODO: Lấy từ field
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Thông tin người đặt với UI đẹp hơn
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Người tham gia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar với border đẹp
                        Card(
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val data = renterAvatarUrl.orEmpty()
                                var rendered = false
                                if (data.isNotBlank()) {
                                    // Ưu tiên decode base64 (strip data URI + whitespace)
                                    val decodedBmp = try {
                                        val base = if (data.startsWith("data:image")) data.substringAfter(",") else data
                                        val compact = base.replace("\n", "").replace("\r", "").trim()
                                        val bytes = Base64.decode(compact, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (_: Exception) { null }
                                    if (decodedBmp != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = decodedBmp.asImageBitmap(),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        rendered = true
                                    } else if (data.startsWith("http", true) || data.startsWith("data:image", true)) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(data)
                                                .crossfade(true)
                                                .allowHardware(false)
                                                .build(),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        rendered = true
                                    }
                                }
                                if (!rendered) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Người đặt sân",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = renterName ?: "Đang tải...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = renterPhone ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Text "Đang tìm kiếm đối thủ"
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = "🔍 Đang tìm kiếm đối thủ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF9800),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons với UI đẹp hơn - 3 button bằng nhau
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onConfirm != null) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Xác nhận",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFF44336)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Hủy",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (onSuggestTime != null) {
                    OutlinedButton(
                        onClick = onSuggestTime,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF9E9E9E)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF9E9E9E)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Gợi ý",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ✅ CRITICAL FIX: Helper function để kiểm tra time overlap
private fun isTimeOverlap(bookingStartAt: String, bookingEndAt: String, matchStartAt: String, matchEndAt: String): Boolean {
    // Kiểm tra xem booking và match có overlap về thời gian không
    // Booking: 18:00-19:00, Match: 18:30-19:30 -> overlap = true
    // Booking: 18:00-19:00, Match: 19:00-20:00 -> overlap = false (không overlap)
    
    return bookingStartAt < matchEndAt && bookingEndAt > matchStartAt
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewOwnerBookingListScreen() {
    FBTP_CNTheme {
        OwnerBookingListScreen(onBookingClick = { /* Preview callback */ })
    }
}