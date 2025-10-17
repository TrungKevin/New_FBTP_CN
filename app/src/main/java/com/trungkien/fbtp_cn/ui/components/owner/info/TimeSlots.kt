package com.trungkien.fbtp_cn.ui.components.owner.info

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.Slot
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeSlots(
    field: Field,
    modifier: Modifier = Modifier,
    fieldViewModel: FieldViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    // ✅ FIX: Lấy giờ hoạt động từ Field.openHours
    val startHour = field.openHours.start.split(":")[0].toInt()
    val endHour = field.openHours.end.split(":")[0].toInt()
    val isOpen24h = field.openHours.isOpen24h
    
    println("🕐 DEBUG: TimeSlots - Field operating hours:")
    println("  - start: ${field.openHours.start} (hour: $startHour)")
    println("  - end: ${field.openHours.end} (hour: $endHour)")
    println("  - isOpen24h: $isOpen24h")
    
    // ✅ FIX: Tạo danh sách ngày (7 ngày từ hôm nay)
    val dates = remember {
        (0..6).map { LocalDate.now().plusDays(it.toLong()) }
    }
    
    // ✅ FIX: Tạo time slots dựa trên giờ hoạt động thực tế
    val timeSlots = remember(startHour, endHour, isOpen24h) {
        val slots = mutableListOf<String>()
        
        if (isOpen24h) {
            // Nếu mở 24h, tạo slots từ 00:00 đến 23:30
            for (hour in 0..23) {
                for (minute in listOf(0, 30)) {
                    slots.add(String.format("%02d:%02d", hour, minute))
                }
            }
        } else {
            // Tạo slots từ startHour đến endHour, cách nhau 30 phút
            val startHalfHour = startHour * 2
            val endHalfHour = endHour * 2
            
            for (halfHour in startHalfHour..endHalfHour) {
                val hour = halfHour / 2
                val minute = if (halfHour % 2 == 0) 0 else 30
                slots.add(String.format("%02d:%02d", hour, minute))
            }
        }
        
        println("🕐 DEBUG: Generated ${slots.size} time slots:")
        slots.forEachIndexed { index, slot ->
            println("  [$index] $slot")
        }
        
        slots
    }
    
    // ✅ FIX: Load slots từ Firebase cho ngày được chọn
    var pricingRules by remember { mutableStateOf(emptyList<PricingRule>()) }
    
    // Observe UI state từ ViewModel
    val uiState by fieldViewModel.uiState.collectAsState()
    
    // Load slots và pricing rules khi thay đổi ngày
    LaunchedEffect(selectedDate, field.fieldId) {
        println("🔄 DEBUG: Loading data for date: ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
        loadSlotsForDate(field.fieldId, selectedDate, fieldViewModel)
        loadPricingRules(field.fieldId, fieldViewModel)
        // ✅ NEW: bật realtime cập nhật set màu khi matches thay đổi trong ngày này
        fieldViewModel.startRealtimeSlotsForDate(field.fieldId, selectedDate.toString())
    }
    
    // Cập nhật slots và pricing rules từ Firebase
    LaunchedEffect(uiState.pricingRules, uiState.slots) {
        pricingRules = uiState.pricingRules
        println("💰 DEBUG: Loaded ${pricingRules.size} pricing rules")
        println("🕐 DEBUG: Loaded ${uiState.slots.size} slots")
    }
    
        // ✅ FIX: Sử dụng LazyColumn để có thể cuộn xuống xem tất cả khe giờ
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Khung giờ hoạt động",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            // ✅ FIX: Hiển thị thông tin giờ hoạt động
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Thông tin giờ hoạt động",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• Giờ mở cửa: ${field.openHours.start}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• Giờ đóng cửa: ${field.openHours.end}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "• Khoảng cách giữa các khe: 30 phút",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    if (isOpen24h) {
                        Text(
                            text = "• Mở cửa 24/24",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Chọn ngày",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(dates) { date ->
                    DateSelector(
                        date = date,
                        isSelected = date == selectedDate,
                        onDateClick = { selectedDate = date }
                    )
                }
            }
        }
        
        if (selectedDate != null) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Khung giờ ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            item {
                // ✅ FIX: Hiển thị time slots với thông tin từ Firebase - sử dụng LazyVerticalGrid để hiển thị đầy đủ
                TimeGrid(
                    timeSlots = timeSlots,
                    slotsFromFirebase = uiState.slots,
                    pricingRules = pricingRules,
                    selectedDate = selectedDate,
                    startHour = startHour,
                    endHour = endHour,
                    isOpen24h = isOpen24h
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateSelector(
    date: LocalDate,
    isSelected: Boolean,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else 
                    MaterialTheme.colorScheme.surface
            )
            .clickable { onDateClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi")),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimeGrid(
    timeSlots: List<String>,
    slotsFromFirebase: List<Slot>,
    pricingRules: List<PricingRule>,
    selectedDate: LocalDate,
    startHour: Int,
    endHour: Int,
    isOpen24h: Boolean,
    modifier: Modifier = Modifier
) {
    // ✅ FIX: Sử dụng LazyVerticalGrid với chiều cao phù hợp để hiển thị đầy đủ
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 800.dp), // ✅ FIX: Giới hạn chiều cao để có thể scroll
        contentPadding = PaddingValues(bottom = 16.dp) // ✅ FIX: Thêm padding bottom để dễ scroll
    ) {
        items(timeSlots) { timeSlot ->
            // ✅ FIX: Tìm slot tương ứng từ Firebase
            val slotFromFirebase = slotsFromFirebase.find { slot ->
                slot.startAt == timeSlot
            }
            
            // ✅ FIX: Tính giá dựa trên PricingRules
            val price = calculatePriceForTimeSlot(
                timeSlot = timeSlot,
                selectedDate = selectedDate,
                pricingRules = pricingRules
            )
            
            TimeSlotItem(
                time = timeSlot,
                isAvailable = isTimeSlotAvailable(timeSlot, startHour, endHour, isOpen24h),
                isBooked = slotFromFirebase?.isBooked == true,
                price = price
            )
        }
    }
}

@Composable
private fun TimeSlotItem(
    time: String,
    isAvailable: Boolean,
    isBooked: Boolean,
    price: Long?,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isBooked -> Color.Red.copy(alpha = 0.3f)
        !isAvailable -> Color.Gray.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }
    
    val textColor = when {
        isBooked -> Color.Red
        !isAvailable -> Color.Gray
        else -> MaterialTheme.colorScheme.primary
    }
    
    // ✅ FIX: Tăng kích thước để dễ nhìn và tương tác
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp) // ✅ FIX: Tăng chiều cao để hiển thị giá rõ ràng hơn
            .clip(RoundedCornerShape(8.dp)) // ✅ FIX: Tăng border radius
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp), // ✅ FIX: Tăng padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium, // ✅ FIX: Tăng font size
                fontWeight = FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            // ✅ FIX: Hiển thị giá nếu có
            if (price != null && price > 0) {
                Text(
                    text = "${price}₫",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), // ✅ FIX: Tăng font size
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun isTimeSlotAvailable(timeSlot: String, startHour: Int, endHour: Int, isOpen24h: Boolean): Boolean {
    if (isOpen24h) return true
    
    val hour = timeSlot.split(":")[0].toInt()
    return hour in startHour until endHour
}

// ✅ FIX: Hàm tính giá dựa trên PricingRules
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
    
    println("💰 DEBUG: Price calculation for $timeSlot on ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}:")
    println("  - dayType: $dayType")
    println("  - timeSlotType: $timeSlotType")
    println("  - matchingRule: ${matchingRule?.price ?: "Not found"}")
    
    return matchingRule?.price
}

// ✅ FIX: Hàm load slots từ Firebase
private fun loadSlotsForDate(fieldId: String, date: LocalDate, fieldViewModel: FieldViewModel) {
    val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    println("🔄 DEBUG: Loading slots for fieldId: $fieldId, date: $dateString")
    
    fieldViewModel.handleEvent(FieldEvent.LoadSlotsByFieldIdAndDate(fieldId, dateString))
}

// ✅ FIX: Hàm load pricing rules
private fun loadPricingRules(fieldId: String, fieldViewModel: FieldViewModel) {
    println("🔄 DEBUG: Loading pricing rules for fieldId: $fieldId")
    fieldViewModel.handleEvent(FieldEvent.LoadPricingRulesByFieldId(fieldId))
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun TimeSlotsPreview() {
    FBTP_CNTheme {
        // Tạo mock Field để preview
        val mockField = Field(
            fieldId = "preview_field",
            name = "Sân Tennis 1",
            openHours = com.trungkien.fbtp_cn.model.OpenHours(
                start = "08:00",
                end = "22:00",
                isOpen24h = false
            )
        )
        
        TimeSlots(field = mockField)
    }
}