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
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.*
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.PricingRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }

    // Service quantities map (serviceId -> qty)
    var servicesQuantity by remember { mutableStateOf(mapOf<String, Int>()) }
    var showServicePicker by remember { mutableStateOf(false) }

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
    val hours = selectedSlots.size
    
    // ✅ FIX: Tính giá chính xác theo từng slot đã chọn
    val fieldTotal = if (hours > 0 && uiState.pricingRules.isNotEmpty()) {
        val totalPrice = selectedSlots.sumOf { slot ->
            val price = calculatePriceForTimeSlot(
                timeSlot = slot,
                selectedDate = selectedDate,
                pricingRules = uiState.pricingRules
            )
            price ?: basePricePerHour.toLong()
        }
        totalPrice.toInt()
    } else {
        basePricePerHour * hours
    }
    val grandTotal = fieldTotal + servicesTotal
    
    // ✅ FIX: Load field data và slots khi component được tạo
    LaunchedEffect(fieldId) {
        fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
        fieldViewModel.handleEvent(FieldEvent.LoadPricingRules(fieldId))
        fieldViewModel.handleEvent(FieldEvent.LoadFieldServices(fieldId))
    }
    
    // ✅ FIX: Load slots khi thay đổi ngày
    LaunchedEffect(selectedDate, fieldId) {
        fieldViewModel.handleEvent(FieldEvent.LoadSlotsByFieldIdAndDate(fieldId, selectedDate.toString()))
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
                    Button(onClick = onConfirmBooking, enabled = hours > 0) { Text("Xác nhận đặt") }
                }
            }
        }
    ) { innerPadding ->
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
                        onDateChange = { selectedDate = it },
                        field = field
                    )
                    
                    // ✅ FIX: Sử dụng field data thật cho BookingTimeSlotGrid
                    BookingTimeSlotGrid(
                        selectedDate = selectedDate, 
                        selected = selectedSlots, 
                        onToggle = { slot ->
                            selectedSlots = selectedSlots.toMutableSet().apply { 
                                if (contains(slot)) remove(slot) else add(slot) 
                            }.toSet()
                        },
                        field = field,
                        fieldViewModel = fieldViewModel
                    )
                } ?: run {
                    // Fallback nếu không có field data
                    BookingDatePicker(selectedDate = selectedDate, onDateChange = { selectedDate = it })
                    BookingTimeSlotGrid(selectedDate = selectedDate, selected = selectedSlots, onToggle = { slot ->
                        selectedSlots = selectedSlots.toMutableSet().apply { if (contains(slot)) remove(slot) else add(slot) }.toSet()
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
                servicesTotal = servicesTotal
            )

            // Extra spacer so the last card is not hidden behind bottom bar
            Spacer(modifier = Modifier.height(16.dp))
        }
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


