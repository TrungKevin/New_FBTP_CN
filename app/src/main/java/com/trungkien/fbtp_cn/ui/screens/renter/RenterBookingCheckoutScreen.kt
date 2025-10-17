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
                        // Chỉ hiển thị màu vàng khi booking còn hiệu lực
                        booking.status.equals("PENDING", true) && booking.opponentMode == "WAITING_OPPONENT" -> {
                            waiting.addAll(slots)
                            slots.forEach { slot ->
                                slotToOwner[slot] = booking.renterId
                                println("  - slotToOwner[$slot] = ${booking.renterId}")
                            }
                        }
                        booking.status.equals("CONFIRMED", true) || booking.status.equals("PAID", true) -> {
                            locked.addAll(slots)
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
                        enabled = effectiveSlots.isNotEmpty(),
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
        // Hiển thị loading khi đang tạo booking
        if (bookingUi.isLoading) {
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
                            
                            // Handle click rules with priority: locked(red) → toast; waiting(yellow) → join; booked(grey) → toast; else toggle
                            if (lockedSlots.contains(slot)) {
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
                        bookedStartTimes = fieldViewModel.uiState.collectAsState().value.bookedStartTimes,
                        waitingOpponentTimes = fieldViewModel.uiState.collectAsState().value.waitingOpponentTimes,
                        lockedOpponentTimes = fieldViewModel.uiState.collectAsState().value.lockedOpponentTimes
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

            BookingServicesPicker(
                servicesTotal = servicesTotal,
                selectedServices = servicesQuantity,
                allServices = allServices,
                onAddServicesClick = { showServicePicker = true }
            )

            BookingNotes(notes = notes, onNotesChange = { notes = it })

            BookingSummaryCard(
                hours = hours,
                pricePerHour = basePricePerHour,
                servicesTotal = servicesTotal,
                // ✅ NEW: Truyền thêm thông tin chi tiết
                fieldTotal = fieldTotal,
                averagePricePerHour = averagePricePerHour
            )

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
            onConfirm = {
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
                                notes = m.notes
                            )
                            val createResult = bookingRepo.createMatchIfMissing(ensure)
                            if (createResult.isFailure) {
                                println("❌ ERROR: Failed to create match: ${createResult.exceptionOrNull()?.message}")
                                return@launch
                            } else {
                                println("✅ DEBUG: Match created/verified successfully")
                            }
                            
                            // ✅ FIX: Only call joinOpponent if createMatchIfMissing succeeded
                            val joinResult = bookingRepo.joinOpponent(
                                matchId = m.rangeKey,
                                renterId = renterId,
                                ownerId = uiState.currentField?.ownerId ?: "",
                                basePrice = basePrice,
                                serviceLines = emptyList(),
                                notes = notes.ifBlank { null }
                            )
                            
                            if (joinResult.isFailure) {
                                println("❌ ERROR: Failed to join opponent: ${joinResult.exceptionOrNull()?.message}")
                                return@launch
                            } else {
                                println("✅ DEBUG: Successfully joined opponent, bookingId: ${joinResult.getOrNull()}")
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
                            
                            if (bookingMode == "HAS_OPPONENT") {
                                // Trường hợp join làm đối thủ → chuyển đỏ
                                val newWaitingSlots = currentWaitingSlots - selectedSlots
                                val newLockedSlots = currentLockedSlots + selectedSlots
                                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
                                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                            } else if (bookingMode == "FIND_OPPONENT") {
                                // Trường hợp tìm đối thủ → giữ vàng, đảm bảo không bị chuyển đỏ
                                val newWaitingSlots = currentWaitingSlots + selectedSlots
                                val newLockedSlots = currentLockedSlots - selectedSlots
                                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
                                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
                            } else {
                                // Mặc định an toàn: không đổi màu
                                waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to currentWaitingSlots)
                                lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to currentLockedSlots)
                            }
                            
                            // ✅ NEW: Lưu lại lựa chọn vừa xác nhận để tiếp tục hiển thị tổng
                            recentConfirmedSlotsByDate = recentConfirmedSlotsByDate + (currentDateKey to selectedSlots)
                            // Không cần giữ border xanh nữa: UI có thể xóa selection nếu muốn,
                            // nhưng tổng vẫn dựa vào recentConfirmedSlotsByDate
                            
                            // Reload field data để cập nhật UI
                            fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
                            
                            println("✅ DEBUG: Match completed - only consecutive slots with same userId updated: $selectedSlots")
                            println("✅ DEBUG: Moved from WAITING_OPPONENT to FULL: $selectedSlots")
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


