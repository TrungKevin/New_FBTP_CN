package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.model.FieldService
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import com.trungkien.fbtp_cn.ui.components.owner.info.FieldServiceManager

@Composable
fun CourtService(
    field: Field, 
    modifier: Modifier = Modifier,
    fieldViewModel: FieldViewModel = viewModel()
) {
    var isEditMode by remember { mutableStateOf(false) }
    
    // ✅ FIX: State cho bảng giá sân - Sử dụng List immutable để force recompose
    var pricingRules by remember { mutableStateOf(emptyList<CourtPricingRule>()) }
    
    // State cho danh sách dịch vụ - Không còn cần thiết vì đã chuyển sang FieldServiceManager
    // var services by remember { mutableStateOf(emptyList<CourtServiceItem>()) }
    

    
    // State để force refresh UI khi cần thiết
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // State cho validation
    var validationErrors by remember { mutableStateOf(listOf<String>()) }
    
    // Focus management
    val focusManager = LocalFocusManager.current
    
    // Khởi tạo dữ liệu ban đầu
    LaunchedEffect(field.fieldId) {
        println("🚀 DEBUG: Bắt đầu load data cho field: ${field.fieldId}")
        loadFieldData(field.fieldId, fieldViewModel)
        refreshTrigger++
    }
    
    // Observe UI state
    val uiState by fieldViewModel.uiState.collectAsState()
    
    // Hiển thị loading dialog khi đang lưu
    if (uiState.isLoading) {
        LoadingDialog()
    }
    
    // Hiển thị thông báo thành công
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            println("✅ DEBUG: Firebase trả về thành công: $success")
            
            // Tự động tắt edit mode khi lưu thành công
            isEditMode = false
            
            // Reload data từ Firebase để hiển thị dữ liệu mới
            println("🔄 DEBUG: Bắt đầu reload data từ Firebase...")
            loadFieldData(field.fieldId, fieldViewModel)
            
            // Force refresh UI
            refreshTrigger++
            
            // Clear validation errors
            validationErrors = emptyList()
        }
    }
    
    // Hiển thị thông báo lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            println("❌ DEBUG: Firebase trả về lỗi: $error")
            validationErrors = listOf("Lỗi Firebase: $error")
        }
    }
    
    // Cập nhật dữ liệu khi có thay đổi từ Firebase
    LaunchedEffect(uiState.pricingRules, uiState.fieldServices, refreshTrigger) {
        println("🔄 DEBUG: LaunchedEffect triggered - pricingRules: ${uiState.pricingRules.size}, fieldServices: ${uiState.fieldServices.size}, refreshTrigger: $refreshTrigger")
        
        // ✅ DEBUG: Kiểm tra raw data từ Firebase
        println("🔍 DEBUG: Raw Firebase data:")
        println("  - uiState.pricingRules.size: ${uiState.pricingRules.size}")
        if (uiState.pricingRules.isEmpty()) {
            println("⚠️ WARNING: Không có pricing rules nào từ Firebase!")
            println("🔍 DEBUG: Field ID đang query: ${field.fieldId}")
            println("🔍 DEBUG: Field name: ${field.name}")
            println("🔍 DEBUG: Field sports: ${field.sports}")
        } else {
            uiState.pricingRules.forEachIndexed { index, rule ->
                println("    [$index] ruleId: '${rule.ruleId}', fieldId: '${rule.fieldId}', price: ${rule.price}, description: '${rule.description}', dayType: '${rule.dayType}'")
            }
        }
        println("  - uiState.fieldServices.size: ${uiState.fieldServices.size}")
        // Services không còn cần thiết vì đã chuyển sang FieldServiceManager
        
        // ✅ FIX: Cập nhật state local từ Firebase data với new instances
        val (newPricingRules, _) = updateUIDataFromFirebase(uiState.pricingRules, uiState.fieldServices, pricingRules, emptyList())
        
        println("🔍 DEBUG: updateUIDataFromFirebase returned:")
        println("  - newPricingRules.size: ${newPricingRules.size}")
        newPricingRules.forEachIndexed { index, rule ->
            println("  - [$index] ${rule.dayOfWeek} - ${rule.timeSlot}: '${rule.price}' (isEmpty: ${rule.price.isEmpty()})")
        }
        
        pricingRules = newPricingRules.toList()
        // services không còn cần thiết vì đã chuyển sang FieldServiceManager
        
        println("🔍 DEBUG: After set localPricingRules: size=${pricingRules.size}, prices=${pricingRules.map { it.price }}")
        
        // Debug: Kiểm tra state sau khi cập nhật
        println("🔍 DEBUG: State sau khi cập nhật:")
        println("  - pricingRules.size: ${pricingRules.size}")
        println("  - pricingRules với giá: ${pricingRules.filter { it.price.isNotEmpty() }.size}")
        pricingRules.forEachIndexed { index, rule ->
            println("  - [$index] ${rule.dayOfWeek} - ${rule.timeSlot}: '${rule.price}' (isEmpty: ${rule.price.isEmpty()})")
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .clickable { 
                focusManager.clearFocus()
            }
    ) {
        // Header với nút chỉnh sửa và refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BẢNG GIÁ & DỊCH VỤ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row {
                // Nút refresh
                IconButton(
                    onClick = { 
                        loadFieldData(field.fieldId, fieldViewModel)
                        refreshTrigger++
                    }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Làm mới",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (!isEditMode) {
                    // Nút chỉnh sửa
                    IconButton(
                        onClick = { 
                            isEditMode = true
                            validationErrors = emptyList()
                        }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Nút lưu và hủy
                    IconButton(
                        onClick = { 
                            println("💾 DEBUG: Save button được click!")
                            
                            // Validate dữ liệu trước khi lưu
                            val errors = validateData(pricingRules)
                            if (errors.isEmpty()) {
                                saveData(field.fieldId, pricingRules, uiState.fieldServices, fieldViewModel)
                            } else {
                                validationErrors = errors
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Lưu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            isEditMode = false
                            validationErrors = emptyList()
                            loadFieldData(field.fieldId, fieldViewModel)
                        }
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Hủy",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hiển thị validation errors
        if (validationErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Lỗi",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vui lòng sửa các lỗi sau:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    validationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // BẢNG GIÁ SÂN
        Text(
            text = "BẢNG GIÁ SÂN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // ✅ FIX: Bảng giá sân - Force recompose
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
        ) {
            // Header của bảng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Thứ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Khung giờ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Giá (₫/30')",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Dữ liệu bảng giá - Luôn hiển thị 6 khung giờ với dữ liệu từ state
            repeat(6) { index ->
                val dayOfWeek = (if (index < 3) "T2 - T6" else "T7 - CN").trim()
                val timeSlot = when (index % 3) {
                    0 -> "5h - 12h"
                    1 -> "12h - 18h"
                    2 -> "18h - 24h"
                    else -> "5h - 12h"
                }.trim()
                
                // ✅ FIX: Tìm rule tương ứng trong state với normalized strings
                val existingRule = pricingRules.find { 
                    it.dayOfWeek.trim() == dayOfWeek && it.timeSlot.trim() == timeSlot 
                }
                
                // ✅ FIX: DEBUG: Kiểm tra rule tìm được với normalized strings
                println("🔍 DEBUG: UI find: day='$dayOfWeek', time='$timeSlot', found=${existingRule != null}, price='${existingRule?.price}'")
                println("  - dayOfWeek: '$dayOfWeek', timeSlot: '$timeSlot'")
                if (existingRule == null) {
                    println("  - Available rules (normalized):")
                    pricingRules.forEachIndexed { i, rule ->
                        println("    [$i] '${rule.dayOfWeek.trim()}' - '${rule.timeSlot.trim()}' : '${rule.price}'")
                    }
                    println("  - pricingRules.size: ${pricingRules.size}")
                    println("  - pricingRules.isEmpty: ${pricingRules.isEmpty()}")
                } else {
                    println("  - Found rule: $dayOfWeek - $timeSlot - '${existingRule.price}'")
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayOfWeek,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = timeSlot,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    if (isEditMode) {
                        BasicTextField(
                            value = existingRule?.price ?: "",
                            onValueChange = { newPrice ->
                                try {
                                    println("🔍 DEBUG: onValueChange cho $dayOfWeek - $timeSlot với giá: '$newPrice'")
                                    println("  - existingRule: $existingRule")
                                    println("  - dayOfWeek: '$dayOfWeek', timeSlot: '$timeSlot'")
                                    println("  - pricingRules.size trước: ${pricingRules.size}")
                                    
                                    if (existingRule != null) {
                                        // ✅ FIX: Sử dụng synchronized để tránh race condition
                                        synchronized(pricingRules) {
                                            // ✅ FIX: Cập nhật rule hiện có - Tìm index bằng cách so sánh dayOfWeek và timeSlot với trim()
                                            val index = pricingRules.indexOfFirst { rule ->
                                                rule.dayOfWeek.trim() == dayOfWeek && 
                                                rule.timeSlot.trim() == timeSlot
                                            }
                                            println("  - Cập nhật rule tại index: $index")
                                            println("  - pricingRules.size: ${pricingRules.size}")
                                            
                                            // ✅ FIX: Thêm validation mạnh mẽ để tránh IndexOutOfBoundsException
                                            if (index != -1 && index >= 0 && index < pricingRules.size) {
                                                val updatedRules = pricingRules.toMutableList()
                                                // ✅ FIX: Double check index sau khi tạo MutableList
                                                if (index < updatedRules.size) {
                                                    updatedRules[index] = existingRule.copy(
                                                        dayOfWeek = dayOfWeek,
                                                        timeSlot = timeSlot,
                                                        price = newPrice
                                                    )
                                                    pricingRules = updatedRules.toList() // ✅ FIX: Force new instance
                                                    println("  - Đã cập nhật rule tại index: $index với giá: '$newPrice'")
                                                } else {
                                                    println("  - ❌ ERROR: Index $index vượt quá size ${updatedRules.size}")
                                                }
                                            } else {
                                            // ✅ FIX: Nếu không tìm thấy index, tạo rule mới với trim() để nhất quán
                                            println("  - Không tìm thấy index, tạo rule mới cho: $dayOfWeek - $timeSlot")
                                            val newRule = existingRule.copy(
                                                dayOfWeek = dayOfWeek,
                                                timeSlot = timeSlot,
                                                price = newPrice
                                            )
                                            pricingRules = pricingRules + newRule // ✅ FIX: Force new instance
                                            println("  - Đã thêm rule mới: $newRule")
                                        }
                                    }
                                } else {
                                    // ✅ FIX: Tạo rule mới nếu không tìm thấy với trim() để nhất quán
                                    synchronized(pricingRules) {
                                        println("  - Tạo rule mới cho: $dayOfWeek - $timeSlot")
                                        val newRule = CourtPricingRule(
                                            id = (System.currentTimeMillis()).toString(), // ✅ FIX: Unique ID
                                            dayOfWeek = dayOfWeek,
                                            timeSlot = timeSlot,
                                            price = newPrice,
                                            dayType = if (dayOfWeek == "T2 - T6") "WEEKDAY" else "WEEKEND",
                                            minutes = 30,
                                            description = "Giá $dayOfWeek - $timeSlot"
                                        )
                                        pricingRules = pricingRules + newRule // ✅ FIX: Force new instance
                                        println("  - Đã thêm rule mới: $newRule")
                                    }
                                }
                                
                                println("  - pricingRules.size sau: ${pricingRules.size}")
                                println("  - pricingRules hiện tại (normalized):")
                                pricingRules.forEachIndexed { i, rule ->
                                    println("    [$i] ${rule.dayOfWeek.trim()} - ${rule.timeSlot.trim()}: '${rule.price}'")
                                }
                                } catch (e: Exception) {
                                    println("❌ ERROR: Exception trong onValueChange: ${e.message}")
                                    println("  - Stack trace: ${e.stackTraceToString()}")
                                    // Không làm gì để tránh crash, chỉ log lỗi
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center
                            )
                        )
                    } else {
                        // ✅ FIX: Hiển thị giá từ state hoặc "Chưa có giá"
                        val displayText = when {
                            existingRule?.price?.isNotEmpty() == true && existingRule.price != "0" -> {
                                "${existingRule.price} ₫"
                            }
                            existingRule?.price == "0" -> {
                                "0 ₫"  // Hiển thị giá 0 thay vì "Chưa có giá"
                            }
                            else -> {
                                "Chưa có giá"
                            }
                        }
                        
                        // DEBUG: Kiểm tra logic hiển thị
                        println("🔍 DEBUG: Hiển thị cho $dayOfWeek - $timeSlot")
                        println("  - existingRule: $existingRule")
                        println("  - dayOfWeek: '$dayOfWeek', timeSlot: '$timeSlot'")
                        println("  - existingRule?.price: '${existingRule?.price}'")
                        println("  - existingRule?.price?.isNotEmpty(): ${existingRule?.price?.isNotEmpty()}")
                        println("  - existingRule?.price != '0': ${existingRule?.price != "0"}")
                        println("  - displayText: '$displayText'")
                        
                        Text(
                            text = displayText,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = when {
                                existingRule?.price?.isNotEmpty() == true && existingRule.price != "0" -> MaterialTheme.colorScheme.onSurface
                                existingRule?.price == "0" -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sử dụng FieldServiceManager để quản lý dịch vụ
        FieldServiceManager(
            fieldId = field.fieldId,
            fieldViewModel = fieldViewModel,
            isEditMode = isEditMode
        )
    }
}

// ==================== MODEL MỚI CHO UI ====================

/**
 * Model mới cho bảng giá - Dễ hiển thị và chỉnh sửa
 * Mapping chính xác với PricingRule từ Firebase
 */
data class CourtPricingRule(
    val id: String = "",                    // ruleId từ Firebase
    val dayOfWeek: String = "",            // T2 - T6, T7 - CN, Ngày lễ
    val timeSlot: String = "",             // 5h - 12h, 12h - 18h, 18h - 24h
    val price: String = "",                // Giá tiền (string để dễ edit)
    
    // Thông tin bổ sung để mapping chính xác
    val dayType: String = "",              // WEEKDAY, WEEKEND, HOLIDAY
    val slots: Int = 1,                    // Số khe giờ
    val minutes: Int = 30,                 // Thời gian mỗi khe (phút)
    val calcMode: String = "CEIL_TO_RULE", // Cách tính giá
    val description: String = "",          // Mô tả quy tắc giá
    val active: Boolean = true           // Trạng thái hoạt động
)

/**
 * Model mới cho dịch vụ - Dễ hiển thị và chỉnh sửa
 */
data class CourtServiceItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = ""
)

// ==================== HELPER FUNCTIONS ====================

/**
 * Load dữ liệu từ Firebase
 */
private fun loadFieldData(fieldId: String, fieldViewModel: FieldViewModel) {
    println("🔄 DEBUG: Loading field data for fieldId: $fieldId")
    println("🔍 DEBUG: Field details:")
    println("  - Field ID: $fieldId")
    // Không thể truy cập field object trong function này
    // println("  - Field name: ${field.name}")
    // println("  - Field sports: ${field.sports}")
    // println("  - Field owner: ${field.ownerId}")
    
    try {
        fieldViewModel.handleEvent(FieldEvent.LoadPricingRulesByFieldId(fieldId))
        fieldViewModel.handleEvent(FieldEvent.LoadFieldServicesByFieldId(fieldId))
        println("✅ DEBUG: Đã gửi lệnh load dữ liệu từ Firebase")
        println("  - LoadPricingRulesByFieldId($fieldId)")
        println("  - LoadFieldServicesByFieldId($fieldId)")
    } catch (e: Exception) {
        println("❌ ERROR: Lỗi khi gửi lệnh load data: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Cập nhật UI data từ Firebase data
 */
private fun updateUIDataFromFirebase(
    firebasePricingRules: List<PricingRule>,
    firebaseFieldServices: List<FieldService>,
    localPricingRules: List<CourtPricingRule>,
    localServices: List<CourtServiceItem>
): Pair<List<CourtPricingRule>, List<CourtServiceItem>> {
    println("🔄 DEBUG: Cập nhật dữ liệu từ Firebase")
    println("📊 Pricing Rules từ Firebase: ${firebasePricingRules.size} items")
    println("🛍️ Field Services từ Firebase: ${firebaseFieldServices.size} items")
    
    // ✅ FIX: Tạo template đầy đủ với 6 khung giờ cố định
    val templateRules = createEmptyPricingRules().toMutableList()
    
    // Cập nhật pricing rules từ Firebase
    if (firebasePricingRules.isNotEmpty()) {
        println("✅ Có dữ liệu pricing rules, mapping...")
        
        firebasePricingRules.forEach { rule ->
            println("🔍 DEBUG: Xử lý rule: ${rule.ruleId} - ${rule.description} - Giá: ${rule.price}")
            
            // ✅ FIX: Mapping chính xác dựa trên dayType và description
            val mappedRule = mapFirebaseRuleToUI(rule)
            
            // Tìm template rule tương ứng và cập nhật
            val templateIndex = templateRules.indexOfFirst { 
                it.dayOfWeek == mappedRule.dayOfWeek && it.timeSlot == mappedRule.timeSlot 
            }
            
            if (templateIndex != -1) {
                templateRules[templateIndex] = mappedRule
                println("✅ Cập nhật template rule [$templateIndex] với giá: '${mappedRule.price}'")
            } else {
                println("⚠️ Không tìm thấy template rule tương ứng, thêm mới")
                templateRules.add(mappedRule)
            }
        }
    } else {
        println("⚠️ Không có dữ liệu pricing rules từ Firebase")
    }
    
    println("🔍 DEBUG: Kết quả mapping:")
    templateRules.forEachIndexed { index, rule ->
        println("  [$index] ${rule.dayOfWeek} - ${rule.timeSlot}: '${rule.price}' (isEmpty: ${rule.price.isEmpty()})")
    }
    
    return Pair(templateRules, emptyList())
}

/**
 * Map Firebase PricingRule sang UI CourtPricingRule
 */
private fun mapFirebaseRuleToUI(rule: PricingRule): CourtPricingRule {
    // ✅ FIX: Mapping chính xác dựa trên dayType
    val mappedDayOfWeek = when (rule.dayType) {
        "WEEKDAY" -> "T2 - T6"
        "WEEKEND" -> "T7 - CN"
        "HOLIDAY" -> "Ngày lễ"
        else -> "T2 - T6" // Fallback
    }
    
    // ✅ FIX: Mapping chính xác dựa trên description hoặc minutes
    val mappedTimeSlot = when {
        rule.description.contains("5h") || rule.description.contains("5h-12h") || rule.description.contains("5h - 12h") -> "5h - 12h"
        rule.description.contains("12h") || rule.description.contains("12h-18h") || rule.description.contains("12h - 18h") -> "12h - 18h"
        rule.description.contains("18h") || rule.description.contains("18h-24h") || rule.description.contains("18h - 24h") -> "18h - 24h"
        rule.minutes <= 180 -> "5h - 12h"      // ≤ 3 giờ
        rule.minutes <= 360 -> "12h - 18h"     // ≤ 6 giờ
        else -> "18h - 24h"                    // > 6 giờ
    }
    
    return CourtPricingRule(
        id = rule.ruleId,
        dayOfWeek = mappedDayOfWeek,
        timeSlot = mappedTimeSlot,
        price = rule.price.toString(), // ✅ FIX: Luôn chuyển đổi sang string
        dayType = rule.dayType,
        slots = rule.slots,
        minutes = rule.minutes,
        calcMode = rule.calcMode,
        description = rule.description,
        active = rule.active
    )
}

/**
 * Tạo pricing rules mẫu trống
 */
private fun createEmptyPricingRules(): List<CourtPricingRule> {
    println("🔧 DEBUG: Tạo pricing rules mẫu trống")
    
    val emptyRules = listOf(
        // T2 - T6 (Thứ 2 đến Thứ 6)
        CourtPricingRule(
            id = "1", 
            dayOfWeek = "T2 - T6", 
            timeSlot = "5h - 12h", 
            price = "",
            dayType = "WEEKDAY",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T2 - T6 - 5h - 12h",
            active = true
        ),
        CourtPricingRule(
            id = "2", 
            dayOfWeek = "T2 - T6", 
            timeSlot = "12h - 18h", 
            price = "",
            dayType = "WEEKDAY",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T2 - T6 - 12h - 18h",
            active = true
        ),
        CourtPricingRule(
            id = "3", 
            dayOfWeek = "T2 - T6", 
            timeSlot = "18h - 24h", 
            price = "",
            dayType = "WEEKDAY",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T2 - T6 - 18h - 24h",
            active = true
        ),
        
        // T7 - CN (Thứ 7 và Chủ nhật)
        CourtPricingRule(
            id = "4", 
            dayOfWeek = "T7 - CN", 
            timeSlot = "5h - 12h", 
            price = "",
            dayType = "WEEKEND",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T7 - CN - 5h - 12h",
            active = true
        ),
        CourtPricingRule(
            id = "5", 
            dayOfWeek = "T7 - CN", 
            timeSlot = "12h - 18h", 
            price = "",
            dayType = "WEEKEND",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T7 - CN - 12h - 18h",
            active = true
        ),
        CourtPricingRule(
            id = "6", 
            dayOfWeek = "T7 - CN", 
            timeSlot = "18h - 24h", 
            price = "",
            dayType = "WEEKEND",
            slots = 1,
            minutes = 30,
            calcMode = "CEIL_TO_RULE",
            description = "Giá T7 - CN - 18h - 24h",
            active = true
        )
    )
    
    println("🔧 DEBUG: Đã tạo ${emptyRules.size} pricing rules mẫu:")
    emptyRules.forEachIndexed { index, rule ->
        println("  - [$index] $rule")
    }
    
    return emptyRules
}

/**
 * Tạo services mẫu trống
 */
private fun createEmptyServices(): List<CourtServiceItem> {
    return listOf(
        // Banh
        CourtServiceItem(id = "1", name = "", price = "", category = "Banh"),
        CourtServiceItem(id = "2", name = "", price = "", category = "Banh"),
        
        // Nước đóng chai
        CourtServiceItem(id = "3", name = "Sting", price = "12000", category = "Nước đóng chai"),
        CourtServiceItem(id = "4", name = "Revie", price = "15000", category = "Nước đóng chai"),
        CourtServiceItem(id = "5", name = "", price = "", category = "Nước đóng chai"),
        
        // Phí Thuê Vợt
        CourtServiceItem(id = "6", name = "", price = "", category = "Phí Thuê Vợt")
    )
}

/**
 * Lưu dữ liệu vào Firebase
 */
private fun saveData(
    fieldId: String,
    pricingRules: List<CourtPricingRule>,
    fieldServices: List<FieldService>,
    fieldViewModel: FieldViewModel
) {
    println("💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase")
    println("📊 Input pricing rules: ${pricingRules.size} items")
    println("🛍️ Input field services: ${fieldServices.size} items")
    println("🏟️ Field ID: $fieldId")
    
    pricingRules.forEachIndexed { index, rule ->
        println("  [$index] $rule")
    }
    
    println("🛍️ DEBUG: Field services đầu vào:")
    fieldServices.forEachIndexed { index, service ->
        println("  [$index] ${service.name}: ${service.price} ₫ (ID: ${service.fieldServiceId})")
    }
    
    // ✅ FIX: Lọc chỉ những pricing rules có giá
    val pricingRulesWithPrice = pricingRules.filter { rule ->
        rule.price.isNotEmpty() && rule.price != "0"
    }
    
    println("💰 DEBUG: Pricing rules có giá: ${pricingRulesWithPrice.size} items")
    pricingRulesWithPrice.forEachIndexed { index, rule ->
        println("  [$index] ${rule.dayOfWeek} - ${rule.timeSlot}: ${rule.price} ₫")
    }
    
    // ✅ FIX: Tạo danh sách pricing rules mới - chỉ lưu những rule có giá
    val newPricingRules = pricingRulesWithPrice.map { rule ->
        println("🔍 DEBUG: Tạo PricingRule từ CourtPricingRule: $rule")
        
        // Sử dụng thông tin đầy đủ từ CourtPricingRule
        val description = if (rule.description.isNotEmpty()) rule.description else "Giá ${rule.dayOfWeek} - ${rule.timeSlot}"
        println("🔍 DEBUG: Tạo PricingRule với description: $description")
        
        PricingRule(
            ruleId = rule.id.ifEmpty { "" }, // Sử dụng id hiện tại nếu có, nếu không để Firebase tự tạo
            fieldId = fieldId,
            dayType = rule.dayType.ifEmpty { 
                when (rule.dayOfWeek) {
                    "T2 - T6" -> "WEEKDAY"
                    "T7 - CN" -> "WEEKEND"
                    else -> "WEEKDAY"
                }
            },
            slots = rule.slots,
            minutes = rule.minutes,
            price = rule.price.toLongOrNull() ?: 0L,
            calcMode = rule.calcMode.ifEmpty { "CEIL_TO_RULE" },
            effectiveFrom = null, // Có thể thêm sau
            effectiveTo = null,   // Có thể thêm sau
            description = description,
            active = rule.active
        )
    }
    
    // ✅ FIX: Giữ nguyên field services hiện có từ Firebase
    val newFieldServices = fieldServices.map { service ->
        // Giữ nguyên service hiện có, chỉ cập nhật fieldId nếu cần
        service.copy(fieldId = fieldId)
    }
    
    println("💾 DEBUG: Dữ liệu sẽ lưu vào Firebase:")
    println("📊 Pricing Rules sẽ lưu: ${newPricingRules.size} items")
    newPricingRules.forEachIndexed { index, rule ->
        println("  [$index] PricingRule:")
        println("    - ruleId: ${rule.ruleId}")
        println("    - fieldId: ${rule.fieldId}")
        println("    - dayType: ${rule.dayType}")
        println("    - description: ${rule.description}")
        println("    - price: ${rule.price}")
        println("    - minutes: ${rule.minutes}")
    }
    println("🛍️ Field Services sẽ lưu (giữ nguyên từ Firebase): ${newFieldServices.size} items")
    newFieldServices.forEachIndexed { index, service ->
        println("  [$index] FieldService:")
        println("    - fieldServiceId: ${service.fieldServiceId}")
        println("    - fieldId: ${service.fieldId}")
        println("    - name: ${service.name}")
        println("    - price: ${service.price}")
        println("    - billingType: ${service.billingType}")
    }
    
    // ✅ FIX: Kiểm tra xem có dữ liệu để lưu không
    if (newPricingRules.isEmpty()) {
        println("⚠️ WARNING: Không có pricing rules nào để lưu!")
        println("💡 HINT: Hãy nhập giá cho ít nhất một khung giờ trước khi lưu")
        return
    }
    
    // Lưu tất cả dữ liệu mới vào Firebase
    println("🚀 DEBUG: Gửi lệnh lưu dữ liệu vào Firebase...")
    println("🔍 DEBUG: Kiểm tra dữ liệu trước khi gửi:")
    println("  - fieldId: $fieldId")
    println("  - newPricingRules.size: ${newPricingRules.size}")
    println("  - newFieldServices.size: ${newFieldServices.size}")
    
    // Kiểm tra xem có pricing rules nào có giá không
    val pricingRulesWithPriceFinal = newPricingRules.filter { it.price > 0 }
    println("💰 DEBUG: Pricing rules có giá > 0: ${pricingRulesWithPriceFinal.size}")
    pricingRulesWithPriceFinal.forEachIndexed { index, rule ->
        println("  [$index] Giá: ${rule.price} ₫ - ${rule.description}")
    }
    
    // Kiểm tra xem có field services nào có tên và giá không
    val fieldServicesWithData = newFieldServices.filter { it.name.isNotEmpty() && it.price > 0 }
    println("🛍️ DEBUG: Field services từ Firebase (giữ nguyên): ${fieldServicesWithData.size}")
    fieldServicesWithData.forEachIndexed { index, service ->
        println("  [$index] ${service.name}: ${service.price} ₫ (ID: ${service.fieldServiceId})")
    }
    
    // ✅ FIX: Gửi lệnh lưu dữ liệu vào Firebase
    fieldViewModel.handleEvent(FieldEvent.UpdateFieldPricingAndServices(fieldId, newPricingRules, newFieldServices))
    
    println("✅ Đã gửi lệnh lưu dữ liệu vào Firebase")
    println("⏳ DEBUG: Đang chờ Firebase xử lý...")
}

/**
 * Validate dữ liệu trước khi lưu
 */
private fun validateData(pricingRules: List<CourtPricingRule>): List<String> {
    val errors = mutableListOf<String>()
    
    // ✅ FIX: Validate pricing rules - chỉ validate những rule có giá
    val rulesWithPrice = pricingRules.filter { rule -> 
        rule.price.isNotEmpty() && rule.price != "0" 
    }
    
    println("🔍 DEBUG: Validation - Rules có giá: ${rulesWithPrice.size} items")
    rulesWithPrice.forEachIndexed { index, rule ->
        println("  [$index] ${rule.dayOfWeek} - ${rule.timeSlot}: ${rule.price} ₫")
    }
    
    rulesWithPrice.forEachIndexed { index, rule ->
        if (rule.price.isEmpty()) {
            errors.add("Giá không được để trống cho ${rule.dayOfWeek} - ${rule.timeSlot}")
        } else if (rule.price.toLongOrNull() == null) {
            errors.add("Giá không hợp lệ cho ${rule.dayOfWeek} - ${rule.timeSlot}: ${rule.price}")
        } else if (rule.price.toLong() <= 0) {
            errors.add("Giá phải lớn hơn 0 cho ${rule.dayOfWeek} - ${rule.timeSlot}")
        }
    }
    
    // ✅ FIX: Kiểm tra xem có ít nhất một pricing rule có giá không
    if (rulesWithPrice.isEmpty()) {
        errors.add("Vui lòng nhập ít nhất một mức giá cho sân")
        println("⚠️ WARNING: Không có pricing rules nào có giá để validate")
    } else {
        println("✅ DEBUG: Có ${rulesWithPrice.size} pricing rules có giá để validate")
    }
    
    return errors
}
