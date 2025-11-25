package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.*
import com.trungkien.fbtp_cn.ui.components.renter.dialogs.OpponentConfirmationDialog
import com.trungkien.fbtp_cn.ui.components.renter.dialogs.OpponentDialogUtils
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.BookingViewModel
import com.trungkien.fbtp_cn.viewmodel.BookingEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.model.ServiceLine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog

// ✅ NEW: Function để kiểm tra 2 slots có liền nhau không
fun isConsecutiveSlot(slot1: String, slot2: String): Boolean {
    val time1 = slot1.split(":")
    val hour1 = time1[0].toInt()
    val minute1 = time1[1].toInt()
    
    val time2 = slot2.split(":")
    val hour2 = time2[0].toInt()
    val minute2 = time2[1].toInt()
    
    val totalMinutes1 = hour1 * 60 + minute1
    val totalMinutes2 = hour2 * 60 + minute2
    
    return kotlin.math.abs(totalMinutes2 - totalMinutes1) == 30
}

// ✅ NEW: Function để kiểm tra khe giờ đã qua thời gian hiện tại chưa
@RequiresApi(Build.VERSION_CODES.O)
fun isTimeSlotPassed(selectedDate: LocalDate, slot: String): Boolean {
    val now = java.time.LocalDateTime.now()
    
    // Parse slot time (format: "HH:mm")
    val slotParts = slot.split(":")
    val slotHour = slotParts[0].toInt()
    val slotMinute = slotParts[1].toInt()
    
    // Tạo LocalDateTime cho khe giờ được chọn
    val slotDateTime = selectedDate.atTime(slotHour, slotMinute)
    
    // So sánh với thời gian hiện tại
    return slotDateTime.isBefore(now)
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterBookingCheckoutScreen(
    fieldId: String,
    basePricePerHour: Int,
    onBackClick: () -> Unit,
    onConfirmBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ FIX: Sử dụng FieldViewModel để lấy dữ liệu thật
    val fieldViewModel: FieldViewModel = viewModel()
    val uiState by fieldViewModel.uiState.collectAsState()
    val bookingViewModel: BookingViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val bookingUi by bookingViewModel.uiState.collectAsState()
    val currentUser = authViewModel.currentUser.collectAsState().value
    val bookingRepo = remember { com.trungkien.fbtp_cn.repository.BookingRepository() }
    val userRepo = remember { com.trungkien.fbtp_cn.repository.UserRepository() }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedDate by remember { 
        val today = LocalDate.now()
        println("🔄 DEBUG: Initializing selectedDate to: ${today.toString()}")
        mutableStateOf(today) 
    }
    // ✅ FIX: Quản lý trạng thái khung giờ riêng biệt cho từng ngày
    var selectedSlotsByDate by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    var notes by remember { mutableStateOf("") }
    // ✅ NEW: Loading state cho renter B khi lưu
    var isSavingForB by remember { mutableStateOf(false) }
    
    // ✅ FIX: Lấy selectedSlots cho ngày hiện tại
    val selectedSlots = selectedSlotsByDate[selectedDate.toString()] ?: emptySet()
    
    // ✅ DEBUG: Log để kiểm tra selectedSlots
    LaunchedEffect(selectedSlots) {
        println("🔄 DEBUG: selectedSlots changed: $selectedSlots")
        println("🔄 DEBUG: selectedSlotsByDate: $selectedSlotsByDate")
    }

    // Service quantities map (serviceId -> qty)
    var servicesQuantity by remember { mutableStateOf(mapOf<String, Int>()) }
    var showServicePicker by remember { mutableStateOf(false) }
    
    // ✅ NEW: State cho logic đối thủ - cũng quản lý theo từng ngày
    var showOpponentDialog by remember { mutableStateOf(false) }
    var showFindOpponentDialog by remember { mutableStateOf(false) }
    var consecutiveSlots by remember { mutableStateOf(listOf<String>()) }
    var waitingOpponentSlotsByDate by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    var lockedSlotsByDate by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    // ✅ NEW: Giữ lại các slot vừa xác nhận để vẫn hiển thị tổng sau confirm
    var recentConfirmedSlotsByDate by remember { mutableStateOf(mapOf<String, Set<String>>()) }
    // Map slot -> match & owner for WAITING_OPPONENT
    var waitingSlotToMatch by remember { mutableStateOf<Map<String, com.trungkien.fbtp_cn.model.Match>>(emptyMap()) }
    var waitingSlotOwner by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    // Join dialog state
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinMatch: com.trungkien.fbtp_cn.model.Match? by remember { mutableStateOf(null) }
    var opponentName by remember { mutableStateOf("") }
    // ✅ NEW: Trạng thái sau khi renter B join để bắt buộc lưu trước khi đặt nếu có thay đổi
    var joinedMatchIdForB by remember { mutableStateOf<String?>(null) }
    var hasSavedPostJoinForB by remember { mutableStateOf(false) }
    // ✅ NEW: Ghi nhận chế độ đặt: HAS_OPPONENT hoặc FIND_OPPONENT
    var bookingMode by remember { mutableStateOf("") }
    
    // ✅ NEW: Timer để delay hiện dialog đối thủ
    var opponentDialogTimer by remember { mutableStateOf<Job?>(null) }
    
    // ✅ NEW: Function để xử lý logic join khi user khác click vào slot WAITING_OPPONENT
    fun proceedWithJoinLogic(slot: String, date: String, fieldId: String, currentUserId: String?) {
        println("🎯 DEBUG: Proceeding with join logic for slot: $slot")
        // ✅ Join flow nghĩa là đang có đối thủ → chuyển sang chế độ HAS_OPPONENT
        bookingMode = "HAS_OPPONENT"
        // Không toast. Luôn hiển thị viền xanh + bắt đầu countdown 3s để show dialog
        val currentDateKey = date
        val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
        if (!currentSlots.contains(slot)) {
            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to (currentSlots + slot))
        }

        opponentDialogTimer?.cancel()
        showJoinDialog = false

        // Lấy thông tin match/opponent và tự động chọn tất cả khung giờ của match
        val cachedMatch = waitingSlotToMatch[slot]
        if (cachedMatch != null) {
            println("🎯 DEBUG: Found cached match: ${cachedMatch.rangeKey}")
            joinMatch = cachedMatch
            val firstId = cachedMatch.participants.firstOrNull()?.renterId
            if (!firstId.isNullOrEmpty()) {
                userRepo.getUserById(firstId, onSuccess = { u -> opponentName = u.name }, onError = { opponentName = "" })
            }
            
            // ✅ FIX: Chỉ chọn slots liền nhau có cùng userId với slot được click
            val matchSlots = generateTimeSlots(cachedMatch.startAt, cachedMatch.endAt)
            println("🎯 DEBUG: Generated match slots: $matchSlots")
            val clickedSlotOwnerId = cachedMatch.participants.firstOrNull()?.renterId
            println("🎯 DEBUG: Clicked slot owner ID: $clickedSlotOwnerId")
            
            // Kiểm tra từng slot xem có cùng userId và liền nhau không
            val validSlots = mutableSetOf<String>()
            
            // Sử dụng runBlocking để đảm bảo tất cả async operations hoàn thành
            runBlocking {
                matchSlots.forEach { slotToCheck ->
                    // Kiểm tra từ waitingSlotOwner map trước
                    val slotOwnerId = waitingSlotOwner[slotToCheck]
                    println("🎯 DEBUG: Checking slot $slotToCheck, owner from map: $slotOwnerId")
                    
                    if (slotOwnerId == clickedSlotOwnerId) {
                        validSlots.add(slotToCheck)
                        println("🎯 DEBUG: ✅ Slot $slotToCheck has same owner, adding to valid slots")
                    } else if (slotOwnerId == null) {
                        // Nếu map không có data, kiểm tra từ database
                        val bookingResult = bookingRepo.findWaitingBookingBySlot(fieldId, date, slotToCheck)
                        bookingResult.onSuccess { booking ->
                            if (booking != null && booking.renterId == clickedSlotOwnerId) {
                                validSlots.add(slotToCheck)
                                println("🎯 DEBUG: ✅ Slot $slotToCheck has same owner (from DB), adding to valid slots")
                            } else {
                                println("🎯 DEBUG: ❌ Slot $slotToCheck has different owner (from DB), skipping")
                            }
                        }
                    } else {
                        println("🎯 DEBUG: ❌ Slot $slotToCheck has different owner ($slotOwnerId), skipping")
                    }
                }
            }
            
            // ✅ FIX: Chỉ chọn các slots liền nhau có cùng userId với slot được click
            val consecutiveSlots = mutableSetOf<String>()
            consecutiveSlots.add(slot) // Luôn bao gồm slot được click
            
            // Tìm các slots liền nhau về phía trước và sau có cùng userId
            val sortedSlots = validSlots.sorted()
            val clickedIndex = sortedSlots.indexOf(slot)
            
            if (clickedIndex >= 0) {
                // Thêm các slots liền nhau về phía trước
                for (i in clickedIndex - 1 downTo 0) {
                    val prevSlot = sortedSlots[i]
                    if (isConsecutiveSlot(prevSlot, sortedSlots[i + 1])) {
                        consecutiveSlots.add(prevSlot)
                        println("🎯 DEBUG: Added previous consecutive slot: $prevSlot")
                    } else {
                        break
                    }
                }
                
                // Thêm các slots liền nhau về phía sau
                for (i in clickedIndex + 1 until sortedSlots.size) {
                    val nextSlot = sortedSlots[i]
                    if (isConsecutiveSlot(sortedSlots[i - 1], nextSlot)) {
                        consecutiveSlots.add(nextSlot)
                        println("🎯 DEBUG: Added next consecutive slot: $nextSlot")
                    } else {
                        break
                    }
                }
            }
            
            println("🎯 DEBUG: Consecutive slots with same userId: $consecutiveSlots")
            val newSlots = currentSlots + consecutiveSlots
            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
            
            // ✅ FIX: Hiển thị viền xanh ngay lập tức, không delay
            println("🎯 DEBUG: Consecutive slots selected immediately with green border: $consecutiveSlots")
            
            // ✅ NEW: Delay 3 giây trước khi hiển thị OpponentConfirmationDialog
            opponentDialogTimer = CoroutineScope(Dispatchers.Main).launch {
                println("🎯 DEBUG: Starting 3-second timer for OpponentConfirmationDialog")
                delay(3000) // 3 giây
                val stillSelected = (selectedSlotsByDate[selectedDate.toString()] ?: emptySet()).contains(slot)
                println("🎯 DEBUG: After 3 seconds, stillSelected: $stillSelected")
                if (stillSelected) {
                    println("🎯 DEBUG: Showing OpponentConfirmationDialog")
                    showJoinDialog = true
                } else {
                    println("🎯 DEBUG: Slot no longer selected, not showing dialog")
                }
            }
        } else {
            println("🎯 DEBUG: No cached match, fetching from database")
            // Fetch từ database
            CoroutineScope(Dispatchers.IO).launch {
                val bookingResult = bookingRepo.findWaitingBookingBySlot(fieldId, date, slot)
                bookingResult.onSuccess { booking ->
                    if (booking != null) {
                        println("🎯 DEBUG: Found booking from database: ${booking.bookingId}")
                        joinMatch = com.trungkien.fbtp_cn.model.Match(
                            rangeKey = booking.matchId ?: "",
                            fieldId = booking.fieldId,
                            date = booking.date,
                            startAt = booking.startAt,
                            endAt = booking.endAt,
                            capacity = 2,
                            occupiedCount = 1,
                            participants = listOf(
                                com.trungkien.fbtp_cn.model.MatchParticipant(
                                    bookingId = booking.bookingId,
                                    renterId = booking.renterId,
                                    side = "A"
                                )
                            ),
                            price = booking.basePrice,
                            totalPrice = booking.totalPrice,
                            status = "WAITING_OPPONENT"
                        )
                        
                        val firstId = booking.renterId
                        if (!firstId.isNullOrEmpty()) {
                            userRepo.getUserById(firstId, onSuccess = { u -> opponentName = u.name }, onError = { opponentName = "" })
                        }
                        
                        // ✅ FIX: Chỉ chọn slots liền nhau có cùng userId với slot được click
                        val matchSlots = generateTimeSlots(booking.startAt, booking.endAt)
                        println("🎯 DEBUG: Generated match slots from DB: $matchSlots")
                        val clickedSlotOwnerId = booking.renterId
                        println("🎯 DEBUG: Clicked slot owner ID from DB: $clickedSlotOwnerId")
                        
                        // Kiểm tra từng slot xem có cùng userId và liền nhau không
                        val validSlots = mutableSetOf<String>()
                        
                        // Sử dụng runBlocking để đảm bảo tất cả async operations hoàn thành
                        runBlocking {
                            matchSlots.forEach { slotToCheck ->
                                // Kiểm tra từ waitingSlotOwner map trước
                                val slotOwnerId = waitingSlotOwner[slotToCheck]
                                println("🎯 DEBUG: Checking slot $slotToCheck, owner from map: $slotOwnerId")
                                
                                if (slotOwnerId == clickedSlotOwnerId) {
                                    validSlots.add(slotToCheck)
                                    println("🎯 DEBUG: ✅ Slot $slotToCheck has same owner, adding to valid slots")
                                } else if (slotOwnerId == null) {
                                    // Nếu map không có data, kiểm tra từ database
                                    val bookingResult = bookingRepo.findWaitingBookingBySlot(fieldId, date, slotToCheck)
                                    bookingResult.onSuccess { booking ->
                                        if (booking != null && booking.renterId == clickedSlotOwnerId) {
                                            validSlots.add(slotToCheck)
                                            println("🎯 DEBUG: ✅ Slot $slotToCheck has same owner (from DB), adding to valid slots")
                                        } else {
                                            println("🎯 DEBUG: ❌ Slot $slotToCheck has different owner (from DB), skipping")
                                        }
                                    }
                                } else {
                                    println("🎯 DEBUG: ❌ Slot $slotToCheck has different owner ($slotOwnerId), skipping")
                                }
                            }
                        }
                        
                        // ✅ FIX: Chỉ chọn các slots liền nhau có cùng userId với slot được click
                        val consecutiveSlots = mutableSetOf<String>()
                        consecutiveSlots.add(slot) // Luôn bao gồm slot được click
                        
                        // Tìm các slots liền nhau về phía trước và sau có cùng userId
                        val sortedSlots = validSlots.sorted()
                        val clickedIndex = sortedSlots.indexOf(slot)
                        
                        if (clickedIndex >= 0) {
                            // Thêm các slots liền nhau về phía trước
                            for (i in clickedIndex - 1 downTo 0) {
                                val prevSlot = sortedSlots[i]
                                if (isConsecutiveSlot(prevSlot, sortedSlots[i + 1])) {
                                    consecutiveSlots.add(prevSlot)
                                    println("🎯 DEBUG: Added previous consecutive slot: $prevSlot")
                                } else {
                                    break
                                }
                            }
                            
                            // Thêm các slots liền nhau về phía sau
                            for (i in clickedIndex + 1 until sortedSlots.size) {
                                val nextSlot = sortedSlots[i]
                                if (isConsecutiveSlot(sortedSlots[i - 1], nextSlot)) {
                                    consecutiveSlots.add(nextSlot)
                                    println("🎯 DEBUG: Added next consecutive slot: $nextSlot")
                                } else {
                                    break
                                }
                            }
                        }
                        
                        println("🎯 DEBUG: Consecutive slots with same userId (from DB): $consecutiveSlots")
                        val newSlots = currentSlots + consecutiveSlots
                        selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
                        
                        // ✅ FIX: Hiển thị viền xanh ngay lập tức, không delay
                        println("🎯 DEBUG: Consecutive slots selected immediately with green border (from DB): $consecutiveSlots")
                        
                        // ✅ NEW: Delay 3 giây trước khi hiển thị OpponentConfirmationDialog
                        CoroutineScope(Dispatchers.Main).launch {
                            println("🎯 DEBUG: Starting 3-second timer for OpponentConfirmationDialog (from DB)")
                            delay(3000) // 3 giây
                            val stillSelected = (selectedSlotsByDate[selectedDate.toString()] ?: emptySet()).contains(slot)
                            println("🎯 DEBUG: After 3 seconds (from DB), stillSelected: $stillSelected")
                            if (stillSelected) {
                                println("🎯 DEBUG: Showing OpponentConfirmationDialog (from DB)")
                                showJoinDialog = true
                            } else {
                                println("🎯 DEBUG: Slot no longer selected, not showing dialog (from DB)")
                            }
                        }
                    } else {
                        println("🎯 DEBUG: No booking found in database for slot: $slot")
                        println("🎯 DEBUG: Slot should be FREE (white), not WAITING_OPPONENT (yellow)")
                        println("🎯 DEBUG: Data inconsistency detected - ViewModel has data but DB doesn't")
                        // ✅ FIX: Không tạo mock data, chỉ log để debug
                        // Slot này thực sự là FREE, không phải WAITING_OPPONENT
                        // Cần kiểm tra tại sao waitingTimesFromVm có data nhưng DB không có
                    }
                }.onFailure { error ->
                    println("❌ ERROR: Failed to fetch booking from database: ${error.message}")
                }
            }
        }
    }
    
    // ✅ FIX: Lấy trạng thái đối thủ cho ngày hiện tại
    val waitingOpponentSlots = waitingOpponentSlotsByDate[selectedDate.toString()] ?: emptySet()
    val lockedSlots = lockedSlotsByDate[selectedDate.toString()] ?: emptySet()
    
    // ✅ DEBUG: Log trạng thái slots
    println("🎯 DEBUG: Current slot states for ${selectedDate.toString()}:")
    println("  - waitingOpponentSlots: $waitingOpponentSlots")
    println("  - lockedSlots: $lockedSlots")
    println("  - waitingTimesFromVm: ${fieldViewModel.uiState.collectAsState().value.waitingOpponentTimes}")
    println("  - bookedStartTimes: ${fieldViewModel.uiState.collectAsState().value.bookedStartTimes}")
    println("  - lockedOpponentTimes: ${fieldViewModel.uiState.collectAsState().value.lockedOpponentTimes}")
    
    // ✅ DEBUG: Kiểm tra data consistency và sync nếu cần
    val vmWaitingTimes = fieldViewModel.uiState.collectAsState().value.waitingOpponentTimes
    val vmLockedTimes = fieldViewModel.uiState.collectAsState().value.lockedOpponentTimes
    val effectiveLockedSlots = remember(lockedSlots, vmLockedTimes) {
        lockedSlots + vmLockedTimes
    }
    
    if (vmWaitingTimes.isNotEmpty() && waitingOpponentSlots.isEmpty()) {
        println("⚠️ WARNING: Data inconsistency detected!")
        println("  - ViewModel waitingOpponentTimes: $vmWaitingTimes")
        println("  - Local waitingOpponentSlots: $waitingOpponentSlots")
        println("  - Syncing ViewModel data to local state...")
        
        // ✅ FIX: Sync data từ ViewModel vào local state
        val currentDateKey = selectedDate.toString()
        waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to vmWaitingTimes.toSet())
        println("✅ DEBUG: Synced waitingOpponentSlots: ${vmWaitingTimes.toSet()}")
    }
    
    if (vmLockedTimes.isNotEmpty() && lockedSlots.isEmpty()) {
        println("⚠️ WARNING: Locked times inconsistency detected!")
        println("  - ViewModel lockedOpponentTimes: $vmLockedTimes")
        println("  - Local lockedSlots: $lockedSlots")
        println("  - Syncing ViewModel data to local state...")
        
        // ✅ FIX: Sync data từ ViewModel vào local state
        val currentDateKey = selectedDate.toString()
        val waitingToday = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
        val recentToday = recentConfirmedSlotsByDate[currentDateKey] ?: emptySet()
        // Không sync các slot mà chính user vừa đặt ở trạng thái WAITING
        val syncLocked = vmLockedTimes.toSet() - waitingToday - recentToday
        lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to syncLocked)
        println("✅ DEBUG: Synced lockedSlots (filtered): $syncLocked")
    }
    
    // ✅ FIX: Tính tổng cho renter hiện tại = chỉ slots đang chọn, không gộp với waiting slots
    val effectiveSlots: Set<String> = remember(selectedSlots, recentConfirmedSlotsByDate, selectedDate) {
        val currentDateKey = selectedDate.toString()
        val recent = recentConfirmedSlotsByDate[currentDateKey] ?: emptySet()
        // Ưu tiên slots đang chọn; nếu user vừa confirm và selection bị reset bởi UI, vẫn dùng recent
        val base = if (selectedSlots.isNotEmpty()) selectedSlots else recent
        base.toSet()
    }

    // ✅ FIX: Lấy field services thật từ Firebase
    val allServices = uiState.fieldServices.map { service ->
        RenterServiceItem(
            id = service.serviceId ?: service.fieldServiceId,
            name = service.name,
            price = service.price.toInt()
        )
    }

    val servicesTotal = servicesQuantity.entries.sumOf { entry ->
        val price = allServices.firstOrNull { it.id == entry.key }?.price ?: 0
        price * entry.value
    }

    // ✅ NEW: Reset cờ lưu sau join khi có thay đổi ở ghi chú/dịch vụ (chỉ áp dụng khi B đã join)
    LaunchedEffect(joinedMatchIdForB, notes, servicesQuantity) {
        if (joinedMatchIdForB != null) {
            hasSavedPostJoinForB = false
        }
    }

    // ✅ FIX: Load booking data for ownership check
    val currentDate = selectedDate.toString()
    
    LaunchedEffect(fieldId, currentDate) {
        println("🎯 DEBUG: LaunchedEffect triggered for fieldId: $fieldId, date: $currentDate")
        
        try {
            println("🎯 DEBUG: Starting booking data load...")
            val result = bookingRepo.getBookingsByFieldAndDate(fieldId, currentDate)
            result.onSuccess { bookings ->
                println("🎯 DEBUG: Booking loading for date $currentDate:")
                println("  - Total bookings found: ${bookings.size}")
                
                val waiting = mutableSetOf<String>()
                val locked = mutableSetOf<String>()
                val slotToOwner = mutableMapOf<String, String>()
                
                bookings.forEach { booking ->
                    println("🎯 DEBUG: Processing booking:")
                    println("  - bookingId: ${booking.bookingId}")
                    println("  - renterId: ${booking.renterId}")
                    println("  - status: ${booking.status}")
                    println("  - opponentMode: ${booking.opponentMode}")
                    println("  - startAt: ${booking.startAt}, endAt: ${booking.endAt}")
                    
                    // Generate slots for this booking
                    val slots = generateTimeSlots(booking.startAt, booking.endAt)
                    println("  - generated slots: $slots")
                    
                    when {
                        // ✅ FIXED: Kiểm tra cả booking status và match status
                        booking.status.equals("PENDING", true) && booking.opponentMode == "WAITING_OPPONENT" -> {
                            // Trường hợp solo booking đang chờ đối thủ
                            waiting.addAll(slots)
                            slots.forEach { slot ->
                                slotToOwner[slot] = booking.renterId
                                println("  - slotToOwner[$slot] = ${booking.renterId}")
                            }
                        }
                        booking.status.equals("CONFIRMED", true) || booking.status.equals("PAID", true) -> {
                            // Trường hợp booking đã được xác nhận/thanh toán
                            locked.addAll(slots)
                        }
                        booking.status.equals("PENDING", true) && booking.hasOpponent == true -> {
                            // ✅ NEW: Trường hợp booking có đối thủ (match FULL) - chuyển sang màu đỏ
                            locked.addAll(slots)
                            println("  - Added to LOCKED (hasOpponent=true): $slots")
                        }
                    }
                }
                
                println("🎯 DEBUG: Final slotToOwner map:")
                println("  - slotToOwner: $slotToOwner")
                println("  - waiting slots: $waiting")
                println("  - locked slots: $locked")
                
                // Update UI state
                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDate to waiting)
                lockedSlotsByDate = lockedSlotsByDate + (currentDate to locked)
                waitingSlotOwner = slotToOwner
                
                println("🎯 DEBUG: UI state updated successfully")
            }.onFailure { error ->
                println("❌ ERROR: Failed to load bookings: ${error.message}")
            }
        } catch (e: Exception) {
            println("❌ ERROR: Exception in LaunchedEffect: ${e.message}")
        }
    }
    // ✅ FIX: Tính giá chính xác theo từng slot đã chọn
    val fieldTotal = if (effectiveSlots.isNotEmpty()) {
        val totalPrice = effectiveSlots.sorted().sumOf { slot ->
            val price = if (uiState.pricingRules.isNotEmpty()) {
                calculatePriceForTimeSlot(
                    timeSlot = slot,
                    selectedDate = selectedDate,
                    pricingRules = uiState.pricingRules
                )
            } else {
                // ✅ FIX: Sử dụng giá mặc định khi chưa có pricing rules
                basePricePerHour.toLong()
            }
            price ?: basePricePerHour.toLong()
        }
        totalPrice.toInt()
    } else {
        0 // Không có slot nào được chọn
    }
    
    // ✅ FIX: Quy ước số mốc -> số giờ theo yêu cầu:
    // 2 mốc = 0.5 giờ; 3 mốc = 1.0 giờ; 4 mốc = 1.5 giờ; 5 mốc = 2.0 giờ; ...
    // Công thức tổng quát: hours = max(0, (count - 1)) * 0.5
    val slotCount = effectiveSlots.size
    val hours = ((slotCount - 1).coerceAtLeast(0)) * 0.5
    
    // ✅ DEBUG: Log để kiểm tra tính toán
    LaunchedEffect(selectedSlots, hours, fieldTotal, effectiveSlots, currentUser?.userId) {
        println("🔄 DEBUG: Calculation update:")
        println("  - currentUserId: ${currentUser?.userId}")
        println("  - selectedSlots: $selectedSlots (size: ${selectedSlots.size})")
        println("  - effectiveSlots: $effectiveSlots (size: ${effectiveSlots.size})")
        println("  - slotCount: $slotCount")
        println("  - hours: $hours")
        println("  - fieldTotal: $fieldTotal")
        println("  - waitingOpponentSlots: $waitingOpponentSlots")
        println("  - waitingSlotOwner: $waitingSlotOwner")
    }
    
    // ✅ NEW: Tính giá trung bình mỗi giờ để hiển thị
    val averagePricePerHour = if (hours > 0) (fieldTotal / hours).toInt() else basePricePerHour
    
    val grandTotal = fieldTotal + servicesTotal
    
    // ✅ FIX: Load field data khi component được tạo
    LaunchedEffect(fieldId) {
        fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
        fieldViewModel.handleEvent(FieldEvent.LoadPricingRules(fieldId))
        fieldViewModel.handleEvent(FieldEvent.LoadFieldServices(fieldId))
    }
    
    // ✅ NEW: Load slots CHỈ khi có ngày cụ thể được chọn
    LaunchedEffect(selectedDate, fieldId) {
        if (fieldId.isNotEmpty()) {
            println("🔄 DEBUG: LaunchedEffect triggered - Loading slots for field: $fieldId, date: ${selectedDate.toString()}")
            fieldViewModel.handleEvent(FieldEvent.LoadSlotsByFieldIdAndDate(fieldId, selectedDate.toString()))
            // ✅ NEW: bật realtime cập nhật set màu theo ngày
            fieldViewModel.startRealtimeSlotsForDate(fieldId, selectedDate.toString())
        } else {
            println("⚠️ DEBUG: LaunchedEffect triggered but fieldId is empty: '$fieldId'")
        }
    }

    if (showServicePicker) {
        BookingServicesPickerSheet(
            services = allServices,
            initial = servicesQuantity,
            onDismiss = { showServicePicker = false },
            onConfirm = { map ->
                servicesQuantity = map
                showServicePicker = false
            }
        )
    }

    // Khi tạo booking thành công -> điều hướng ra danh sách đặt lịch
    LaunchedEffect(bookingUi.lastCreatedId) {
        if (bookingUi.lastCreatedId != null) {
            onConfirmBooking()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tổng: ${String.format("%,d", grandTotal)}₫", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
                            if (effectiveSlots.isNotEmpty()) {
                                val renterId = currentUser?.userId
                                val ownerId = uiState.currentField?.ownerId.orEmpty()
                                val serviceLines: List<ServiceLine> = servicesQuantity.entries.mapNotNull { (id, qty) ->
                                    val svc = allServices.firstOrNull { it.id == id }
                                    svc?.let {
                                        ServiceLine(
                                            serviceId = it.id,
                                            name = it.name,
                                            billingType = "UNIT",
                                            price = it.price.toLong(),
                                            quantity = qty,
                                            lineTotal = (it.price * qty).toLong()
                                        )
                                    }
                                }
                                // ✅ Renter B post-join: đảm bảo có Booking B và cập nhật Match
                                if (joinedMatchIdForB != null && !renterId.isNullOrEmpty() && ownerId.isNotEmpty()) {
                                    println("🛑 BLOCK: renter B post-join confirm → ensure Booking B and update Match")
                                    println("🔁 DEBUG: Post-join save → matchId=${joinedMatchIdForB}, renterId=$renterId, ownerId=$ownerId, services=${serviceLines.size}, notes='${notes}'")
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        try {
                                            isSavingForB = true
                                            val ensured = bookingRepo.ensureBookingForRenterB(
                                                matchId = joinedMatchIdForB!!,
                                                renterBId = renterId,
                                                ownerId = ownerId,
                                                basePrice = fieldTotal.toLong()
                                            )
                                            if (ensured.isFailure) {
                                                println("❌ ERROR: ensureBookingForRenterB failed → ${ensured.exceptionOrNull()?.message}")
                                                isSavingForB = false
                                                return@launch
                                            }
                                            val bookingBId = ensured.getOrNull()
                                            println("✅ DEBUG: ensureBookingForRenterB success → bookingId=$bookingBId")

                                            val result = bookingRepo.updateRenterBInMatch(
                                                matchId = joinedMatchIdForB!!,
                                                renterId = renterId,
                                                serviceLines = serviceLines,
                                                notes = notes
                                            )
                                            if (result.isSuccess) {
                                                println("✅ DEBUG: Post-join save completed (Booking B ensured, B notes/services updated)")
                                                hasSavedPostJoinForB = true
                                                // ✅ UPDATE UI: chuyển các slots đã chọn từ VÀNG → ĐỎ ngay lập tức
                                                val currentDateKey = selectedDate.toString()
                                                val currentWaiting = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
                                                val currentLocked = lockedSlotsByDate[currentDateKey] ?: emptySet()
                                                // Một số flow dùng effectiveSlots thay vì selectedSlots → ưu tiên effectiveSlots nếu có
                                                val toLock = if (effectiveSlots.isNotEmpty()) effectiveSlots.toSet() else selectedSlots
                                                val newWaiting = currentWaiting - toLock
                                                val newLocked = currentLocked + toLock
                                                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaiting)
                                                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLocked)
                                                println("🔄 STATE CHANGE (B confirm): moved $toLock from YELLOW → RED")
                                                // ✅ UX: Điều hướng về lịch sử đặt sân (tab Booking)
                                                isSavingForB = false
                                                onConfirmBooking()
                                            } else {
                                                println("❌ ERROR: Post-join save failed → ${result.exceptionOrNull()?.message}")
                                                isSavingForB = false
                                            }
                                        } catch (e: Exception) {
                                            println("❌ EXCEPTION: Post-join save → ${e.message}")
                                            isSavingForB = false
                                        }
                                    }
                                    return@Button
                                }
                                if (!renterId.isNullOrEmpty() && ownerId.isNotEmpty()) {
                                    println("🔍 DEBUG: RenterBookingCheckoutScreen - Button clicked:")
                                    println("  - renterId: $renterId")
                                    println("  - ownerId: $ownerId")
                                    println("  - fieldId: $fieldId")
                                    println("  - date: ${selectedDate.toString()}")
                                    println("  - effectiveSlots: $effectiveSlots")
                                    println("  - bookingMode: $bookingMode")
                                    println("  - bookingType: ${if (bookingMode == "HAS_OPPONENT") "DUO" else "SOLO"}")
                                    println("  - hasOpponent: ${bookingMode == "HAS_OPPONENT"}")
                                    
                                    bookingViewModel.handle(
                                        BookingEvent.Create(
                                            renterId = renterId,
                                            ownerId = ownerId,
                                            fieldId = fieldId,
                                            date = selectedDate.toString(),
                                            consecutiveSlots = effectiveSlots.sorted(),
                                            // ✅ Determine type strictly by bookingMode, not by presence of locked slots on the day
                                            bookingType = if (bookingMode == "HAS_OPPONENT") "DUO" else "SOLO",
                                            hasOpponent = bookingMode == "HAS_OPPONENT",
                                            opponentId = null,
                                            opponentName = null,
                                            opponentAvatar = null,
                                            basePrice = fieldTotal.toLong(),
                                            serviceLines = serviceLines,
                                            // ✅ LOGIC PHÂN BIỆT 2 TRƯỜNG HỢP:
                                            // 1. HAS_OPPONENT (bookingMode = "HAS_OPPONENT"): 
                                            //    - Renter đặt khe giờ với đối thủ sẵn có
                                            //    - TẤT CẢ dữ liệu (notes, serviceLines) → lưu vào Booking
                                            //    - notes → Booking.notes
                                            //    - serviceLines → Booking.serviceLines
                                            //    - KHÔNG tạo Match
                                            // 2. FIND_OPPONENT (bookingMode = "FIND_OPPONENT"):
                                            //    - Renter A đặt khe giờ chưa có đối thủ → tạo Match
                                            //    - TẤT CẢ dữ liệu (notes, serviceLines) → lưu vào Match, KHÔNG lưu vào Booking
                                            //    - notes → Match.noteA (KHÔNG lưu vào Booking.notes)
                                            //    - serviceLines → Match.serviceLinesA (KHÔNG lưu vào Booking.serviceLines)
                                            notes = notes.ifBlank { null },
                                            matchSide = "A", // ✅ CRITICAL FIX: Renter A always has matchSide="A" regardless of opponent choice
                                            createdWithOpponent = bookingMode == "HAS_OPPONENT" // ✅ CRITICAL FIX: immutable origin flag
                                        )
                                    )
                                    // Loading UI sẽ hiển thị qua bookingUi.isLoading composable ở phía trên
                                }
                            }
                        }, 
                        // ✅ Enable when there are slots to submit for this renter (selected/recent or own waiting slots)
                        enabled = run {
                            val baseEnabled = effectiveSlots.isNotEmpty()
                            // Nếu B đã join và có nhập ghi chú/chọn dịch vụ nhưng CHƯA bấm lưu -> không cho đặt
                            val isB = joinedMatchIdForB != null
                            val hasAnyInput = notes.isNotBlank() || servicesQuantity.values.any { it > 0 }
                            val requireSave = isB && hasAnyInput && !hasSavedPostJoinForB
                            baseEnabled && !requireSave
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (effectiveSlots.isNotEmpty()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { 
                        // ✅ FIX: Chỉ cập nhật trạng thái cho slots thực sự được chọn
                        if (bookingMode == "FIND_OPPONENT" && selectedSlots.isNotEmpty()) {
                            val currentDateKey = selectedDate.toString()
                            val currentWaiting = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
                            // ✅ FIX: Chỉ thêm các slots được chọn chưa có trong waiting
                            val slotsToAdd = selectedSlots.filter { !currentWaiting.contains(it) }
                            if (slotsToAdd.isNotEmpty()) {
                                val newWaiting = currentWaiting + slotsToAdd
                                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaiting)
                                // Gán owner cho các slot vàng
                                val me = currentUser?.userId
                                val owners = waitingSlotOwner.toMutableMap()
                                slotsToAdd.forEach { s -> if (me != null) owners[s] = me }
                                waitingSlotOwner = owners
                                // Lưu lại để tổng không mất
                                val recent = recentConfirmedSlotsByDate[currentDateKey] ?: emptySet()
                                recentConfirmedSlotsByDate = recentConfirmedSlotsByDate + (currentDateKey to (recent + slotsToAdd))
                                println("✅ DEBUG: Added selected slots to waiting: $slotsToAdd")
                            }
                        }
                        Text("Xác nhận đặt") 
                    }
                }
            }
        }
    ) { innerPadding ->
        // Hiển thị loading khi đang tạo booking (A) hoặc đang lưu B
        if (bookingUi.isLoading || isSavingForB) {
            LoadingDialog(message = "Đang tạo đặt lịch...")
        }
        // ✅ FIX: FocusManager để ẩn bàn phím khi click ra ngoài
        val focusManager: FocusManager = LocalFocusManager.current
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // ✅ FIX: Click ra ngoài để ẩn bàn phím
                    focusManager.clearFocus()
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ FIX: Hiển thị loading nếu chưa có field data
            if (uiState.isLoading && uiState.currentField == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // ✅ FIX: Sử dụng field data thật cho BookingDatePicker
                uiState.currentField?.let { field ->
                    BookingDatePicker(
                        selectedDate = selectedDate, 
                        onDateChange = { newDate ->
                            println("🔄 DEBUG: Date changed from ${selectedDate.toString()} to ${newDate.toString()}")
                            selectedDate = newDate
                            // ✅ FIX: Debug log để xem trạng thái khung giờ của ngày mới
                            val newDateKey = newDate.toString()
                            val slotsForNewDate = selectedSlotsByDate[newDateKey] ?: emptySet()
                            val waitingSlotsForNewDate = waitingOpponentSlotsByDate[newDateKey] ?: emptySet()
                            val lockedSlotsForNewDate = lockedSlotsByDate[newDateKey] ?: emptySet()
                            println("🔄 DEBUG: Date changed to $newDateKey")
                            println("  - Selected slots: $slotsForNewDate")
                            println("  - Waiting opponent slots: $waitingSlotsForNewDate")
                            println("  - Locked slots: $lockedSlotsForNewDate")
                        },
                        field = field
                    )
                    
                    // ✅ FIX: Sử dụng field data thật cho BookingTimeSlotGrid
                    val fvState = fieldViewModel.uiState.collectAsState().value
                    val bookedTimes = fvState.bookedStartTimes
                    val waitingTimesFromVm = fvState.waitingOpponentTimes
                    BookingTimeSlotGrid(
                        selectedDate = selectedDate, 
                        selected = selectedSlots, 
                        onToggle = { slot ->
                            println("🎯 DEBUG: Slot clicked: $slot")
                            println("🎯 DEBUG: lockedSlots: $lockedSlots")
                            println("🎯 DEBUG: waitingOpponentSlots: $waitingOpponentSlots")
                            println("🎯 DEBUG: waitingTimesFromVm: $waitingTimesFromVm")
                            
                            // ✅ NEW: Kiểm tra khe giờ đã qua thời gian hiện tại - ưu tiên cao nhất
                            if (isTimeSlotPassed(selectedDate, slot)) {
                                println("🎯 DEBUG: Time slot has passed - showing toast")
                                OpponentDialogUtils.showTimeSlotPassedToast(context)
                                return@BookingTimeSlotGrid
                            }
                            
                            // Handle click rules with priority: locked(red) → toast; waiting(yellow) → join; booked(grey) → toast; else toggle
                            if (effectiveLockedSlots.contains(slot)) {
                                println("🎯 DEBUG: Slot is locked - showing toast")
                                OpponentDialogUtils.showSlotBookedToast(context)
                                return@BookingTimeSlotGrid
                            }
                            if (waitingOpponentSlots.contains(slot) || waitingTimesFromVm.contains(slot)) {
                                println("🎯 DEBUG: Clicked on WAITING_OPPONENT slot: $slot")
                                println("🎯 DEBUG: waitingOpponentSlots: $waitingOpponentSlots")
                                println("🎯 DEBUG: waitingTimesFromVm: $waitingTimesFromVm")
                                println("🎯 DEBUG: waitingSlotOwner map before check: $waitingSlotOwner")
                                
                                val ownerId = waitingSlotOwner[slot]
                                val currentUserId = currentUser?.userId
                                println("🎯 DEBUG: Slot ownership check:")
                                println("  - ownerId from map: $ownerId")
                                println("  - currentUserId: $currentUserId")
                                println("  - waitingSlotOwner map: $waitingSlotOwner")
                                
                                // ✅ FIX: Kiểm tra ownership từ database nếu map rỗng
                                if (ownerId == null && waitingSlotOwner.isEmpty()) {
                                    println("🎯 DEBUG: waitingSlotOwner map is empty, checking database for ownership")
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val bookingResult = bookingRepo.findWaitingBookingBySlot(
                                            fieldId = fieldId,
                                            date = selectedDate.toString(),
                                            slot = slot
                                        )
                                        bookingResult.onSuccess { booking ->
                                            if (booking != null) {
                                                val dbOwnerId = booking.renterId
                                                println("🎯 DEBUG: Found booking owner from DB: $dbOwnerId")
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    if (dbOwnerId == currentUserId) {
                                                        println("🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot (from DB)")
                                                        OpponentDialogUtils.showOwnSlotToast(context)
                                                    } else {
                                                        println("🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot (from DB) - proceeding with join logic")
                                                        // Proceed with join logic
                                                        proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
                                                    }
                                                }
                                            } else {
                                                println("🎯 DEBUG: No booking found in database for slot: $slot")
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    // Slot này thực sự là FREE, không phải WAITING_OPPONENT
                                                    println("🎯 DEBUG: Slot should be FREE (white), not WAITING_OPPONENT (yellow)")
                                                }
                                            }
                                        }.onFailure { error ->
                                            println("❌ ERROR: Failed to check ownership from database: ${error.message}")
                                            CoroutineScope(Dispatchers.Main).launch {
                                                // Fallback: treat as other's slot
                                                proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
                                            }
                                        }
                                    }
                                    return@BookingTimeSlotGrid
                                }
                                
                                if (ownerId != null && ownerId == currentUserId) {
                                    println("🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot")
                                    OpponentDialogUtils.showOwnSlotToast(context)
                                    return@BookingTimeSlotGrid
                                } else {
                                    println("🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot - proceeding with join logic")
                                    // ✅ FIX: Chỉ gọi proceedWithJoinLogic khi là slot của user khác
                                    proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
                                }
                                return@BookingTimeSlotGrid
                            }
                            if (bookedTimes.contains(slot)) {
                                OpponentDialogUtils.showSlotBookedToast(context)
                                return@BookingTimeSlotGrid
                            }
                            // ✅ FIX: Chỉ cập nhật trạng thái khung giờ cho ngày hiện tại
                            val currentDateKey = selectedDate.toString()
                            val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
                            val newSlots = if (currentSlots.contains(slot)) {
                                currentSlots - slot
                            } else {
                                currentSlots + slot
                            }
                            // ✅ NEW: Hủy timer join dialog nếu click vào slot khác
                            opponentDialogTimer?.cancel()
                            showJoinDialog = false
                            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
                            println("🔄 DEBUG: Toggle slot: $slot")
                            println("🔄 DEBUG: Current slots before: $currentSlots")
                            println("🔄 DEBUG: New slots after: $newSlots")
                            println("🔄 DEBUG: Updated slots for $currentDateKey: $newSlots")
                            println("🔄 DEBUG: All slots by date: $selectedSlotsByDate")
                        },
                        field = field,
                        fieldViewModel = fieldViewModel,
                        // ✅ NEW: Logic đối thủ chỉ cho khung giờ trống (không phải WAITING_OPPONENT)
                        onConsecutiveSelection = { slots ->
                            // Chỉ hiển thị OpponentSelectionDialog nếu tất cả slots đều là khung giờ trống
                            val allSlotsAreEmpty = slots.all { slot ->
                                !waitingOpponentSlots.contains(slot) && 
                                !waitingTimesFromVm.contains(slot) &&
                                !lockedSlots.contains(slot) &&
                                !bookedTimes.contains(slot)
                            }
                            
                            if (allSlotsAreEmpty) {
                                consecutiveSlots = slots
                                if (slots.size > 1) {
                                    // ✅ FIX: Hủy timer cũ nếu có
                                    opponentDialogTimer?.cancel()
                                    
                                    // ✅ FIX: Tạo timer mới với delay 3 giây
                                    opponentDialogTimer = CoroutineScope(Dispatchers.Main).launch {
                                        delay(3000) // 3 giây
                                        showOpponentDialog = true
                                    }
                                } else {
                                    // ✅ FIX: Hủy timer nếu không có khung giờ liên tiếp
                                    opponentDialogTimer?.cancel()
                                    showOpponentDialog = false
                                }
                            }
                        },
                        waitingOpponentSlots = waitingOpponentSlots,
                        lockedSlots = lockedSlots,
                        bookedStartTimes = fieldViewModel.uiState.collectAsState().value.bookedStartTimes, // dùng để tô xám các slot đã được đặt
                        waitingOpponentTimes = fieldViewModel.uiState.collectAsState().value.waitingOpponentTimes,// dùng để tô vàng các slot chờ đối thủ
                        lockedOpponentTimes = fieldViewModel.uiState.collectAsState().value.lockedOpponentTimes // dùng để tô đỏ các slot có đối thủ
                    )
                } ?: run {
                    // ✅ FIX: Chỉ hiển thị fallback UI khi không có field data
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BookingDatePicker(
                            selectedDate = selectedDate, 
                            onDateChange = { newDate ->
                                println("🔄 DEBUG: Date changed from ${selectedDate.toString()} to ${newDate.toString()}")
                                selectedDate = newDate
                                // ✅ FIX: Debug log để xem trạng thái khung giờ của ngày mới
                                val newDateKey = newDate.toString()
                                val slotsForNewDate = selectedSlotsByDate[newDateKey] ?: emptySet()
                                val waitingSlotsForNewDate = waitingOpponentSlotsByDate[newDateKey] ?: emptySet()
                                val lockedSlotsForNewDate = lockedSlotsByDate[newDateKey] ?: emptySet()
                                println("🔄 DEBUG: Date changed to $newDateKey")
                                println("  - Selected slots: $slotsForNewDate")
                                println("  - Waiting opponent slots: $waitingSlotsForNewDate")
                                println("  - Locked slots: $lockedSlotsForNewDate")
                            },
                            field = null
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Không có dữ liệu sân",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val actorSide = if (joinMatch != null) "B" else "A"
            BookingServicesPicker(
                servicesTotal = servicesTotal,
                selectedServices = servicesQuantity,
                allServices = allServices,
                onAddServicesClick = { showServicePicker = true },
                actorSide = actorSide
            )

            BookingNotes(notes = notes, onNotesChange = { notes = it }, actorSide = actorSide)

            BookingSummaryCard(
                hours = hours,
                pricePerHour = basePricePerHour,
                servicesTotal = servicesTotal,
                // ✅ NEW: Truyền thêm thông tin chi tiết
                fieldTotal = fieldTotal,
                averagePricePerHour = averagePricePerHour
            )

            // ✅ NEW: Nút lưu ghi chú/dịch vụ cho renter B sau khi đã join
            if (joinedMatchIdForB != null) {
                Button(
                    onClick = {
                        val computedServices: List<ServiceLine> = servicesQuantity.entries.mapNotNull { (id, qty) ->
                            val svc = allServices.firstOrNull { it.id == id }
                            svc?.let {
                                ServiceLine(
                                    serviceId = it.id,
                                    name = it.name,
                                    billingType = "UNIT",
                                    price = it.price.toLong(),
                                    quantity = qty,
                                    lineTotal = (it.price * qty).toLong()
                                )
                            }
                        }
                        val notesToSave = notes
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            val res = bookingRepo.updateOpponentDetails(
                                matchId = joinedMatchIdForB!!,
                                renterId = currentUser?.userId ?: "",
                                notes = notesToSave,
                                serviceLines = computedServices
                            )
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                if (res.isSuccess) {
                                    android.widget.Toast.makeText(context, "Lưu ghi chú/dịch vụ (B) thành công", android.widget.Toast.LENGTH_SHORT).show()
                                    hasSavedPostJoinForB = true
                                } else {
                                    android.widget.Toast.makeText(context, "Lỗi khi lưu: ${res.exceptionOrNull()?.message ?: "Không xác định"}", android.widget.Toast.LENGTH_SHORT).show()
                                    hasSavedPostJoinForB = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Lưu ghi chú/dịch vụ (đối thủ B)")
                }

                // Gợi ý cần lưu trước khi đặt nếu có thay đổi
                val hasAnyInput = notes.isNotBlank() || servicesQuantity.values.any { it > 0 }
                if (hasAnyInput && !hasSavedPostJoinForB) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bạn đã nhập/đổi dịch vụ. Hãy bấm 'Lưu ghi chú/dịch vụ (đối thủ B)' trước khi đặt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Extra spacer so the last card is not hidden behind bottom bar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // ✅ NEW: Dialog hỏi có đối thủ hay không
    OpponentSelectionDialog(
        isVisible = showOpponentDialog,
        onDismiss = { showOpponentDialog = false },
        onHasOpponent = {
            bookingMode = "HAS_OPPONENT"
            // ✅ FIX: Đã có đối thủ - chỉ cập nhật trạng thái cho slots được chọn
            val currentDateKey = selectedDate.toString()
            val currentLockedSlots = lockedSlotsByDate[currentDateKey] ?: emptySet()
            val slotsToAdd = selectedSlots.filter { !currentLockedSlots.contains(it) }
            if (slotsToAdd.isNotEmpty()) {
                val newLockedSlots = currentLockedSlots + slotsToAdd
                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                // ✅ NEW: Lưu lại slots vừa xác nhận để "Tổng tạm tính" không bị mất
                recentConfirmedSlotsByDate = recentConfirmedSlotsByDate + (currentDateKey to slotsToAdd.toSet())
                
                // ✅ FIX: Clear selected slots sau khi confirm
                selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to emptySet())
                println("✅ DEBUG: User has opponent - slots locked for $currentDateKey: $slotsToAdd")
            }
        },
        onNoOpponent = {
            bookingMode = "FIND_OPPONENT"
            // Chưa có đối thủ - hiển thị dialog tìm đối thủ
            showFindOpponentDialog = true
        }
    )
    
    // ✅ NEW: Dialog xác nhận tìm đối thủ
    FindOpponentDialog(
        isVisible = showFindOpponentDialog,
        selectedSlots = consecutiveSlots,
        onDismiss = { showFindOpponentDialog = false },
        onConfirm = {
            // ✅ FIX: Xác nhận tìm đối thủ - chỉ cập nhật trạng thái cho slots được chọn
            println("🔍 DEBUG: FindOpponentDialog.onConfirm called:")
            println("  - selectedSlots: $selectedSlots")
            println("  - currentUser: ${currentUser?.userId}")
            println("  - selectedDate: ${selectedDate.toString()}")
            
            bookingMode = "FIND_OPPONENT"
            println("🔍 DEBUG: bookingMode set to: $bookingMode")
            
            val currentDateKey = selectedDate.toString()
            val currentWaitingSlots = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
            // ✅ FIX: Chỉ thêm các slots được chọn chưa có trong waiting
            val slotsToAdd = selectedSlots.filter { !currentWaitingSlots.contains(it) }
            if (slotsToAdd.isNotEmpty()) {
                val newWaitingSlots = currentWaitingSlots + slotsToAdd
                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
                println("🔍 DEBUG: Updated waitingOpponentSlotsByDate: $waitingOpponentSlotsByDate")
                
                // ✅ NEW: Đảm bảo các slot này KHÔNG bị đỏ – loại khỏi locked ngay lập tức
                val currentLockedSlots = lockedSlotsByDate[currentDateKey] ?: emptySet()
                val newLockedSlots = currentLockedSlots - slotsToAdd
                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                println("🔍 DEBUG: Updated lockedSlotsByDate: $lockedSlotsByDate")
                
                // ✅ NEW: Gán owner cho các slot WAITING_OPPONENT vừa tạo để tổng tạm tính nhận diện là của user hiện tại
                val newOwners = waitingSlotOwner.toMutableMap()
                val me = currentUser?.userId
                slotsToAdd.forEach { s -> if (me != null) newOwners[s] = me }
                waitingSlotOwner = newOwners
                println("🔍 DEBUG: Updated waitingSlotOwner: $waitingSlotOwner")
                
                // ✅ NEW: Lưu lại các slot vừa xác nhận để "Tổng tạm tính" vẫn hiển thị
                recentConfirmedSlotsByDate = recentConfirmedSlotsByDate + (currentDateKey to slotsToAdd.toSet())
                println("🔍 DEBUG: Updated recentConfirmedSlotsByDate: $recentConfirmedSlotsByDate")
                
                // ✅ FIX: Clear selected slots sau khi confirm
                selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to emptySet())
                println("✅ DEBUG: User confirmed finding opponent - slots waiting for $currentDateKey: $slotsToAdd")
                println("✅ DEBUG: FindOpponentDialog.onConfirm completed successfully")
            }
        }
    )

    // Prompt join opponent for yellow slots of other users (custom styled box)
    if (showJoinDialog && joinMatch != null) {
        OpponentConfirmationDialog(
            isVisible = true,
            opponentName = opponentName.ifBlank { "người chơi" },
            timeSlot = "${joinMatch!!.startAt} - ${joinMatch!!.endAt}",
            date = joinMatch!!.date,
            onConfirm = { // ✅ Renter B xác nhận join, notes và services lấy từ BookingNotes và BookingServicesPicker
                println("🔍 DEBUG: ========== OpponentConfirmationDialog.onConfirm - Renter B joining ==========")
                println("🔍 DEBUG: Current state values:")
                println("  - notes state: '$notes'")
                println("  - servicesQuantity state: $servicesQuantity")
                println("  - allServices count: ${allServices.size}")
                
                val m = joinMatch!!
                val basePrice = uiState.pricingRules.firstOrNull()?.price?.toLong() ?: basePricePerHour.toLong()
                currentUser?.userId?.let { renterId ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        // ✅ NEW: Đảm bảo document Match tồn tại trước khi join
                        try {
                            val ensure = com.trungkien.fbtp_cn.model.Match(
                                rangeKey = m.rangeKey,
                                fieldId = m.fieldId,
                                date = m.date,
                                startAt = m.startAt,
                                endAt = m.endAt,
                                capacity = 2,
                                occupiedCount = m.occupiedCount,
                                participants = m.participants,
                                price = m.price,
                                totalPrice = m.totalPrice,
                                status = m.status,
                                matchType = m.matchType,
                                // chuyển đổi sang cấu trúc mới
                                notes = listOf(m.notes.getOrNull(0), m.notes.getOrNull(1)),
                                serviceLinesBySide = mapOf(
                                    "A" to (m.serviceLinesBySide["A"] ?: emptyList()),
                                    "B" to (m.serviceLinesBySide["B"] ?: emptyList())
                                )
                            )
                            val createResult = bookingRepo.createMatchIfMissing(ensure)
                            if (createResult.isFailure) {
                                println("❌ ERROR: Failed to create match: ${createResult.exceptionOrNull()?.message}")
                                return@launch
                            } else {
                                println("✅ DEBUG: Match created/verified successfully")
                            }
                            
                            // ✅ Logic 2 - FIND_OPPONENT: Renter B join vào match của Renter A
                            // - TẤT CẢ dữ liệu (notes, serviceLines) → lưu vào Match, KHÔNG tạo Booking B
                            // - notes → Match.notes[1] (lấy từ BookingNotes component - state 'notes')
                            // - serviceLines → Match.serviceLinesBySide["B"] (lấy từ BookingServicesPicker - state 'servicesQuantity')
                            // ✅ FIX: Tính toán serviceLines từ servicesQuantity và allServices (Renter B chọn dịch vụ trong BookingServicesPicker)
                            println("🔍 DEBUG: Calculating serviceLines from servicesQuantity:")
                            servicesQuantity.forEach { (id, qty) ->
                                println("  - serviceId: $id, quantity: $qty")
                            }
                            
                            val serviceLines: List<ServiceLine> = servicesQuantity.entries.mapNotNull { (id, qty) ->
                                val svc = allServices.firstOrNull { it.id == id }
                                svc?.let {
                                    ServiceLine(
                                        serviceId = it.id,
                                        name = it.name,
                                        billingType = "UNIT",
                                        price = it.price.toLong(),
                                        quantity = qty,
                                        lineTotal = (it.price * qty).toLong()
                                    )
                                }
                            }
                            println("🔍 DEBUG: Renter B joining - serviceLines count: ${serviceLines.size}")
                            if (serviceLines.isNotEmpty()) {
                                serviceLines.forEachIndexed { index, service ->
                                    println("  [$index] ${service.name} (id: ${service.serviceId}): qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
                                }
                            } else {
                                println("⚠️ WARNING: serviceLines is EMPTY - Renter B did not select any services")
                            }
                            
                            // ✅ FIX: Sử dụng notes từ BookingNotes component (Renter B nhập ghi chú trong BookingNotes, state 'notes')
                            val renterBNotes = notes // Pass raw string so empty string clears noteB
                            println("🔍 DEBUG: Renter B notes from BookingNotes component:")
                            println("  - notes state (raw): '$notes'")
                            println("  - renterBNotes (raw, will be saved as-is): '$renterBNotes'")
                            println("🔍 DEBUG: =================================================================")
                            
                            val joinResult = bookingRepo.joinOpponent(
                                matchId = m.rangeKey,
                                renterId = renterId,
                                ownerId = uiState.currentField?.ownerId ?: "",
                                basePrice = basePrice,
                                serviceLines = serviceLines, // ✅ ServiceLines của Renter B từ BookingServicesPicker → Match.serviceLinesB
                                notes = renterBNotes // ✅ Notes của Renter B từ BookingNotes component → Match.noteB (empty string clears)
                            )
                            
                            if (joinResult.isFailure) {
                                println("❌ ERROR: Failed to join opponent: ${joinResult.exceptionOrNull()?.message}")
                                return@launch
                            } else {
                                val joinedId = joinResult.getOrNull()
                                println("✅ DEBUG: Successfully joined opponent, matchId: ${joinedId}")
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    joinedMatchIdForB = joinedId
                                }
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Exception in match creation/joining: ${e.message}")
                            return@launch
                        }
                        
                        // ✅ FIX: Cập nhật trạng thái chỉ các slots liền nhau có cùng userId đã được chọn
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            val currentDateKey = selectedDate.toString()
                            val selectedSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
                            println("🎯 DEBUG: Selected slots to update status: $selectedSlots")
                            
                            val currentWaitingSlots = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
                            val currentLockedSlots = lockedSlotsByDate[currentDateKey] ?: emptySet()
                            
                            // ✅ FIXED LOGIC: Chỉ chuyển đổi trạng thái khi thực sự cần thiết
                            // 
                            // 🎯 LOGIC TRẠNG THÁI MÀU SẮC KHE GIỜ:
                            // 
                            // 1. 🟦 TRẮNG (FREE): Khe giờ trống, có thể đặt
                            // 2. 🟨 VÀNG (WAITING_OPPONENT): Renter A đặt solo, đang chờ đối thủ  
                            // 3. 🟥 ĐỎ (LOCKED_FULL): Renter A + Renter B đã match, đã đặt đầy đủ
                            //
                            // 🔄 CHUYỂN ĐỔI TRẠNG THÁI:
                            // - Renter A chọn "Chưa có đối thủ" → Chuyển từ TRẮNG sang VÀNG
                            // - Renter B join vào slot VÀNG → Chuyển từ VÀNG sang ĐỎ
                            // - Sau khi xác nhận đặt → GIỮ NGUYÊN màu sắc hiện tại
                            // - Chỉ thay đổi màu khi có hành động hủy sân:
                            //   + Renter A hủy solo → VÀNG về TRẮNG
                            //   + Renter A hoặc B hủy trong match FULL → ĐỎ về VÀNG  
                            //   + Owner hủy cả match → ĐỎ về TRẮNG
                            when (bookingMode) {
                                "HAS_OPPONENT" -> {
                                    // Trường hợp renter B join vào slot vàng của renter A → chuyển đỏ
                                    val newWaitingSlots = currentWaitingSlots - selectedSlots
                                    val newLockedSlots = currentLockedSlots + selectedSlots
                                    waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
                                    lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                                    println("🔄 STATE CHANGE: HAS_OPPONENT - Moved $selectedSlots from YELLOW to RED")
                                }
                                "FIND_OPPONENT" -> {
                                    // Trường hợp renter A đặt solo → chuyển vàng và GIỮ NGUYÊN sau khi confirm
                                    val newWaitingSlots = currentWaitingSlots + selectedSlots
                                    val newLockedSlots = currentLockedSlots - selectedSlots
                                    waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
                                    lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                                    println("🔄 STATE CHANGE: FIND_OPPONENT - Moved $selectedSlots to YELLOW and KEEP IT")
                                }
                                else -> {
                                    // Mặc định: KHÔNG thay đổi trạng thái màu
                                    waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to currentWaitingSlots)
                                    lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to currentLockedSlots)
                                    println("🔄 STATE CHANGE: DEFAULT - NO COLOR CHANGE for $selectedSlots")
                                }
                            }
                            
                            // ✅ FIXED LOGIC: Sau khi xác nhận đặt, GIỮ NGUYÊN trạng thái màu sắc
                            // Không cần thay đổi trạng thái màu sau khi confirm, chỉ cần lưu lại để hiển thị tổng
                            recentConfirmedSlotsByDate = recentConfirmedSlotsByDate + (currentDateKey to selectedSlots)
                            
                            // ✅ CRITICAL: KHÔNG thay đổi trạng thái màu sắc sau khi xác nhận
                            // Trạng thái màu sẽ được giữ nguyên:
                            // - FIND_OPPONENT: Giữ màu vàng (WAITING_OPPONENT)
                            // - HAS_OPPONENT: Giữ màu đỏ (LOCKED_FULL)
                            // - Chỉ thay đổi màu khi có hành động hủy sân
                            println("✅ CONFIRMATION: Keeping color state unchanged after confirmation")
                            println("✅ CONFIRMATION: FIND_OPPONENT slots remain YELLOW")
                            println("✅ CONFIRMATION: HAS_OPPONENT slots remain RED")
                            
                            // ✅ CRITICAL: KHÔNG reload field data sau khi join để giữ nguyên trạng thái màu
                            // fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId)) // ❌ REMOVED: Gây reset trạng thái màu
                            println("✅ CONFIRMATION: NOT reloading field data to preserve color state")
                        }
                    }
                }
                showJoinDialog = false
            },
            onCancel = { showJoinDialog = false }
        )
    }
}

// ✅ FIX: Hàm tính giá dựa trên PricingRules giống TimeSlots
@RequiresApi(Build.VERSION_CODES.O)
private fun calculatePriceForTimeSlot(
    timeSlot: String,
    selectedDate: LocalDate,
    pricingRules: List<PricingRule>
): Long? {
    if (pricingRules.isEmpty()) return null
    
    // Xác định loại ngày (WEEKDAY/WEEKEND) - Sử dụng Calendar
    val calendar = java.util.Calendar.getInstance()
    calendar.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ..., 7=Saturday
    val dayType = when (dayOfWeek) {
        java.util.Calendar.SUNDAY, java.util.Calendar.SATURDAY -> "WEEKEND" // Chủ nhật, Thứ 7
        else -> "WEEKDAY" // Thứ 2-6
    }
    
    // Xác định khung giờ dựa trên timeSlot
    val hour = timeSlot.split(":")[0].toInt()
    val timeSlotType = when {
        hour in 5..11 -> "5h - 12h"
        hour in 12..17 -> "12h - 18h"
        hour in 18..23 -> "18h - 24h"
        else -> "5h - 12h" // Fallback
    }
    
    // Tìm pricing rule phù hợp
    val matchingRule = pricingRules.find { rule ->
        rule.dayType == dayType && 
        rule.description.contains(timeSlotType)
    }
    
    println("💰 DEBUG: RenterBookingCheckoutScreen - Price calculation for $timeSlot on ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}:")
    println("  - dayType: $dayType")
    println("  - timeSlotType: $timeSlotType")
    println("  - matchingRule: ${matchingRule?.price ?: "Not found"}")
    
    return matchingRule?.price
}

// ✅ NEW: Function để generate time slots từ startAt đến endAt
fun generateTimeSlots(startAt: String, endAt: String): List<String> {
    val slots = mutableListOf<String>()
    val startHour = startAt.substring(0, 2).toInt()
    val startMinute = startAt.substring(3, 5).toInt()
    val endHour = endAt.substring(0, 2).toInt()
    val endMinute = endAt.substring(3, 5).toInt()
    
    var currentHour = startHour
    var currentMinute = startMinute
    
    // ✅ FIX: Include endAt slot by using <= instead of <
    while (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute)) {
        val timeSlot = String.format("%02d:%02d", currentHour, currentMinute)
        slots.add(timeSlot)
        
        currentMinute += 30
        if (currentMinute >= 60) {
            currentMinute = 0
            currentHour++
        }
    }
    
    return slots
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun RenterBookingCheckoutScreenPreview() {
    FBTP_CNTheme {
        RenterBookingCheckoutScreen(
            fieldId = "field1",
            basePricePerHour = 150000,
            onBackClick = {},
            onConfirmBooking = {}
        )
    }
}


