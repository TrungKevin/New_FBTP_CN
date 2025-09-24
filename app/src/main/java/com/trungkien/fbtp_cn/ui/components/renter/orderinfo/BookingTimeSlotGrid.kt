package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.trungkien.fbtp_cn.model.PricingRule

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingTimeSlotGrid(
    selectedDate: LocalDate,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    field: com.trungkien.fbtp_cn.model.Field? = null,
    fieldViewModel: com.trungkien.fbtp_cn.viewmodel.FieldViewModel? = null,
    startHour: Int = 6,
    endHour: Int = 22,
    modifier: Modifier = Modifier,
    // ✅ NEW: Thêm các tham số cho logic đối thủ
    onConsecutiveSelection: (List<String>) -> Unit = {}, // Callback khi chọn khung giờ liên tiếp
    waitingOpponentSlots: Set<String> = emptySet(), // Các khung giờ đang tìm đối thủ (màu vàng)
    lockedSlots: Set<String> = emptySet(), // Các khung giờ đã có đối thủ (màu đỏ)
    bookedStartTimes: Set<String> = emptySet(), // ✅ NEW: Các khung giờ đã được đặt từ bookings
    waitingOpponentTimes: Set<String> = emptySet(), // ✅ NEW: từ bookings (SOLO)
    lockedOpponentTimes: Set<String> = emptySet() // ✅ NEW: từ bookings (DUO)
) {
    // ✅ FIX: Sử dụng field data thật nếu có
    val (actualStartHour, actualEndHour, isOpen24h) = if (field != null) {
        val start = field.openHours.start.split(":")[0].toInt()
        val end = field.openHours.end.split(":")[0].toInt()
        Triple(start, end, field.openHours.isOpen24h)
    } else {
        Triple(startHour, endHour, false)
    }
    
    // ✅ FIX: Tạo slots dựa trên giờ hoạt động thật
    val slots = if (isOpen24h) {
        // Nếu mở 24h, tạo slots từ 00:00 đến 23:30
        (0..23).flatMap { hour ->
            listOf(0, 30).map { minute ->
                String.format("%02d:%02d", hour, minute)
            }
        }
    } else {
        // Tạo slots từ startHour đến endHour, cách nhau 30 phút
        val startHalfHour = actualStartHour * 2
        val endHalfHour = actualEndHour * 2
        // Generate slots as half-open interval [start, end): exclude end boundary time
        (startHalfHour until endHalfHour).map { halfHour ->
            val hour = halfHour / 2
            val minute = if (halfHour % 2 == 0) 0 else 30
            String.format("%02d:%02d", hour, minute)
        }
    }
    
    // ✅ FIX: Lấy slots từ Firebase nếu có fieldViewModel
    val uiState by fieldViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val firebaseSlots = uiState?.slots ?: emptyList()
    
    // ✅ FIX: Lấy pricing rules để hiển thị giá
    val pricingRules = uiState?.pricingRules ?: emptyList()

    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Khung giờ ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(slots) { slot ->
                    val hour = slot.substring(0,2).toInt()
                    // Slot đã được generate theo giờ mở cửa, coi như khả dụng để chọn
                    val isAvailable = true
                    val isSelected = selected.contains(slot)
                    
                    // ✅ FIX: Kiểm tra slot đã được đặt (từ "slots" hoặc từ "bookings")
                    val isBooked = bookedStartTimes.contains(slot) || firebaseSlots.any { firebaseSlot ->
                        firebaseSlot.startAt == slot && firebaseSlot.isBooked
                    }
                    
                    // ✅ FIX: Kiểm tra slot đang tìm đối thủ hoặc đã có đối thủ cho ngày cụ thể
                    val isWaitingOpponentForThisDate = waitingOpponentSlots.contains(slot) || waitingOpponentTimes.contains(slot)
                    val isLockedForThisDate = lockedSlots.contains(slot) || lockedOpponentTimes.contains(slot)
                    
                    // ✅ FIX: Tính giá chính xác theo ngày và khung giờ giống TimeSlots
                    val price = calculatePriceForTimeSlot(
                        timeSlot = slot,
                        selectedDate = selectedDate,
                        pricingRules = pricingRules
                    )
                    val priceText = if (price != null && price > 0) "${String.format("%,d", price)}₫" else ""

                    val bg = when {
                        isLockedForThisDate -> Color.Red.copy(alpha = 0.2f)
                        isWaitingOpponentForThisDate -> Color(0xFFFFD700).copy(alpha = 0.3f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val fg = when {
                        isLockedForThisDate || isBooked -> Color.Red
                        isWaitingOpponentForThisDate -> Color(0xFFB8860B)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (priceText.isNotEmpty()) 48.dp else 36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(
                                if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .let { 
                                if (!isBooked && !isLockedForThisDate) {
                                    it.then(
                                        Modifier.clickable {
                                            // ✅ FIX: Đơn giản hóa - chỉ gọi onToggle
                                            onToggle(slot)
                                            
                                            // ✅ NEW: Kiểm tra selection liên tiếp sau khi onToggle
                                            val currentSelected = if (isSelected) {
                                                selected - slot
                                            } else {
                                                selected + slot
                                            }
                                            
                                            // Gọi callback nếu có selection liên tiếp
                                            if (currentSelected.size > 1) {
                                                val consecutiveSlots = getConsecutiveSlots(currentSelected, slots)
                                                if (consecutiveSlots.isNotEmpty()) {
                                                    onConsecutiveSelection(consecutiveSlots)
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    it
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = slot, style = MaterialTheme.typography.labelSmall, color = fg)
                            if (priceText.isNotEmpty() && !isBooked) {
                                Text(
                                    text = priceText, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = fg.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ FIX: Hàm tính giá dựa trên PricingRules giống TimeSlots
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
    
    println("💰 DEBUG: BookingTimeSlotGrid - Price calculation for $timeSlot on ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}:")
    println("  - dayType: $dayType")
    println("  - timeSlotType: $timeSlotType")
    println("  - matchingRule: ${matchingRule?.price ?: "Not found"}")
    
    return matchingRule?.price
}

// ✅ NEW: Hàm kiểm tra có thể chọn liên tiếp không
private fun canSelectConsecutive(
    newSlot: String,
    currentSelected: Set<String>,
    allSlots: List<String>
): Boolean {
    if (currentSelected.isEmpty()) return true // Slot đầu tiên luôn có thể chọn
    
    val sortedSelected = currentSelected.sorted()
    val newSlotIndex = allSlots.indexOf(newSlot)
    
    // Kiểm tra xem slot mới có liền kề với slot đã chọn không
    return sortedSelected.any { selectedSlot ->
        val selectedIndex = allSlots.indexOf(selectedSlot)
        kotlin.math.abs(newSlotIndex - selectedIndex) == 1
    }
}

// ✅ NEW: Hàm lấy danh sách các slot liên tiếp
private fun getConsecutiveSlots(
    selected: Set<String>,
    allSlots: List<String>
): List<String> {
    if (selected.size <= 1) return selected.toList()
    
    val sortedSelected = selected.sorted()
    val consecutiveGroups = mutableListOf<List<String>>()
    var currentGroup = mutableListOf<String>()
    
    for (i in sortedSelected.indices) {
        val currentSlot = sortedSelected[i]
        val currentIndex = allSlots.indexOf(currentSlot)
        
        if (currentGroup.isEmpty()) {
            currentGroup.add(currentSlot)
        } else {
            val lastSlot = currentGroup.last()
            val lastIndex = allSlots.indexOf(lastSlot)
            
            if (currentIndex - lastIndex == 1) {
                // Liên tiếp
                currentGroup.add(currentSlot)
            } else {
                // Không liên tiếp, lưu group hiện tại và bắt đầu group mới
                if (currentGroup.size > 1) {
                    consecutiveGroups.add(currentGroup.toList())
                }
                currentGroup = mutableListOf(currentSlot)
            }
        }
    }
    
    // Thêm group cuối cùng
    if (currentGroup.size > 1) {
        consecutiveGroups.add(currentGroup.toList())
    }
    
    // Trả về group lớn nhất
    return consecutiveGroups.maxByOrNull { it.size } ?: emptyList()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
private fun BookingTimeSlotGridPreview() {
    val selectedSlots = setOf("08:00", "10:00")
    BookingTimeSlotGrid(selectedDate = LocalDate.now(), selected = emptySet(), onToggle = {})
}


