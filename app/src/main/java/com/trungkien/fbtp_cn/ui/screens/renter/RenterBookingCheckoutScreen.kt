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
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.*
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
    
    // ✅ NEW: Timer để delay hiện dialog đối thủ
    var opponentDialogTimer by remember { mutableStateOf<Job?>(null) }
    
    // ✅ FIX: Lấy trạng thái đối thủ cho ngày hiện tại
    val waitingOpponentSlots = waitingOpponentSlotsByDate[selectedDate.toString()] ?: emptySet()
    val lockedSlots = lockedSlotsByDate[selectedDate.toString()] ?: emptySet()
    
    // ✅ NEW: Tập slot thực sự dùng để tính toán (bao gồm slot đang chọn + chờ đối thủ + đã có đối thủ)
    val effectiveSlots: Set<String> = remember(selectedSlots, waitingOpponentSlots, lockedSlots) {
        (selectedSlots + waitingOpponentSlots + lockedSlots).toSet()
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
    
    // ✅ FIX: Tính số giờ dựa trên số phút (mỗi slot = 30 phút)
    val totalMinutes = effectiveSlots.size * 30
    val hours = if (totalMinutes > 0) totalMinutes / 60.0 else 0.0
    
    // ✅ DEBUG: Log để kiểm tra tính toán
    LaunchedEffect(selectedSlots, hours, fieldTotal) {
        println("🔄 DEBUG: Calculation update:")
        println("  - selectedSlots: $selectedSlots (size: ${selectedSlots.size})")
        println("  - totalMinutes: $totalMinutes")
        println("  - hours: $hours")
        println("  - fieldTotal: $fieldTotal")
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
                                    bookingViewModel.handle(
                                        BookingEvent.Create(
                                            renterId = renterId,
                                            ownerId = ownerId,
                                            fieldId = fieldId,
                                            date = selectedDate.toString(),
                                            consecutiveSlots = effectiveSlots.sorted(),
                                            bookingType = if (lockedSlots.isNotEmpty()) "DUO" else "SOLO",
                                            hasOpponent = lockedSlots.isNotEmpty(),
                                            opponentId = null,
                                            opponentName = null,
                                            opponentAvatar = null,
                                            basePrice = fieldTotal.toLong(),
                                            serviceLines = serviceLines,
                                            notes = notes.ifBlank { null }
                                        )
                                    )
                                    // Loading UI sẽ hiển thị qua bookingUi.isLoading composable ở phía trên
                                }
                            }
                        }, 
                        enabled = effectiveSlots.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (effectiveSlots.isNotEmpty()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { 
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
                    BookingTimeSlotGrid(
                        selectedDate = selectedDate, 
                        selected = selectedSlots, 
                        onToggle = { slot ->
                            // ✅ FIX: Chỉ cập nhật trạng thái khung giờ cho ngày hiện tại
                            val currentDateKey = selectedDate.toString()
                            val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
                            val newSlots = if (currentSlots.contains(slot)) {
                                currentSlots - slot
                            } else {
                                currentSlots + slot
                            }
                            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
                            println("🔄 DEBUG: Toggle slot: $slot")
                            println("🔄 DEBUG: Current slots before: $currentSlots")
                            println("🔄 DEBUG: New slots after: $newSlots")
                            println("🔄 DEBUG: Updated slots for $currentDateKey: $newSlots")
                            println("🔄 DEBUG: All slots by date: $selectedSlotsByDate")
                        },
                        field = field,
                        fieldViewModel = fieldViewModel,
                        // ✅ NEW: Thêm logic đối thủ với delay 3 giây
                        onConsecutiveSelection = { slots ->
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
                        },
                        waitingOpponentSlots = waitingOpponentSlots,
                        lockedSlots = lockedSlots,
                        bookedStartTimes = fieldViewModel.uiState.collectAsState().value.bookedStartTimes,
                        waitingOpponentTimes = fieldViewModel.uiState.collectAsState().value.waitingOpponentTimes,
                        lockedOpponentTimes = fieldViewModel.uiState.collectAsState().value.lockedOpponentTimes
                    )
                } ?: run {
                    // Fallback nếu không có field data
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
                        }
                    )
                    BookingTimeSlotGrid(selectedDate = selectedDate, selected = selectedSlots, onToggle = { slot ->
                        // ✅ FIX: Chỉ cập nhật trạng thái khung giờ cho ngày hiện tại
                        val currentDateKey = selectedDate.toString()
                        val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
                        val newSlots = if (currentSlots.contains(slot)) {
                            currentSlots - slot
                        } else {
                            currentSlots + slot
                        }
                        selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
                        println("🔄 DEBUG: Updated slots for $currentDateKey: $newSlots")
                        println("🔄 DEBUG: All slots by date: $selectedSlotsByDate")
                    })
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
            // ✅ FIX: Đã có đối thủ - chỉ cập nhật trạng thái cho ngày hiện tại
            val currentDateKey = selectedDate.toString()
            val currentLockedSlots = lockedSlotsByDate[currentDateKey] ?: emptySet()
            val newLockedSlots = currentLockedSlots + consecutiveSlots.toSet()
            lockedSlotsByDate = lockedSlotsByDate + (currentDateKey to newLockedSlots)
            
            val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
            val newSlots = currentSlots - consecutiveSlots.toSet()
            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
            consecutiveSlots = emptyList()
            println("✅ DEBUG: User has opponent - slots locked for $currentDateKey: $consecutiveSlots")
        },
        onNoOpponent = {
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
            // ✅ FIX: Xác nhận tìm đối thủ - chỉ cập nhật trạng thái cho ngày hiện tại
            val currentDateKey = selectedDate.toString()
            val currentWaitingSlots = waitingOpponentSlotsByDate[currentDateKey] ?: emptySet()
            val newWaitingSlots = currentWaitingSlots + consecutiveSlots.toSet()
            waitingOpponentSlotsByDate = waitingOpponentSlotsByDate + (currentDateKey to newWaitingSlots)
            
            val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
            val newSlots = currentSlots - consecutiveSlots.toSet()
            selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to newSlots)
            consecutiveSlots = emptyList()
            println("✅ DEBUG: User confirmed finding opponent - slots waiting for $currentDateKey: $consecutiveSlots")
        }
    )
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


