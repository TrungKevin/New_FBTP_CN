# 📊 Phân tích Logic Bảng Giá Sân & Dịch Vụ Bổ Sung

## 🎯 **Tổng quan hệ thống:**

### **1. Cấu trúc chính:**
- **`CourtService.kt`**: Component chính quản lý cả Bảng giá sân và Dịch vụ bổ sung
- **`FieldServiceManager.kt`**: Component con quản lý riêng Dịch vụ bổ sung
- **`CourtPricingRule`**: Model UI cho bảng giá sân
- **`FieldServiceItem`**: Model UI cho dịch vụ bổ sung

### **2. Luồng dữ liệu:**
```
Firebase → ViewModel → UI State → Components → User Input → Firebase
```

## 🔧 **BẢNG GIÁ SÂN - Logic chi tiết:**

### **1. Model dữ liệu:**
```kotlin
data class CourtPricingRule(
    val id: String = "",                    // ruleId từ Firebase
    val dayOfWeek: String = "",            // T2 - T6, T7 - CN, Ngày lễ
    val timeSlot: String = "",             // 5h - 12h, 12h - 18h, 18h - 24h
    val price: String = "",                // Giá tiền (string để dễ edit)
    val dayType: String = "",              // WEEKDAY, WEEKEND, HOLIDAY
    val slots: Int = 1,                    // Số khe giờ
    val minutes: Int = 30,                 // Thời gian mỗi khe (phút)
    val calcMode: String = "CEIL_TO_RULE", // Cách tính giá
    val description: String = "",          // Mô tả quy tắc giá
    val isActive: Boolean = true           // Trạng thái hoạt động
)
```

### **2. Cấu trúc bảng giá:**
- **6 khung giờ cố định**: 3 cho T2-T6, 3 cho T7-CN
- **3 khung thời gian**: 5h-12h, 12h-18h, 18h-24h
- **Hiển thị**: Luôn hiển thị đủ 6 dòng, kể cả khi chưa có giá

### **3. Logic hiển thị:**
```kotlin
// Tạo 6 khung giờ cố định
repeat(6) { index ->
    val dayOfWeek = (if (index < 3) "T2 - T6" else "T7 - CN").trim()
    val timeSlot = when (index % 3) {
        0 -> "5h - 12h"
        1 -> "12h - 18h"
        2 -> "18h - 24h"
        else -> "5h - 12h"
    }.trim()
    
    // Tìm rule tương ứng trong state
    val existingRule = pricingRules.find { 
        it.dayOfWeek.trim() == dayOfWeek && it.timeSlot.trim() == timeSlot 
    }
}
```

### **4. Logic chỉnh sửa:**
```kotlin
// Edit mode: TextField để nhập giá
BasicTextField(
    value = existingRule?.price ?: "",
    onValueChange = { newPrice ->
        if (existingRule != null) {
            // Cập nhật rule hiện có
            val index = pricingRules.indexOfFirst { rule ->
                rule.dayOfWeek.trim() == dayOfWeek && 
                rule.timeSlot.trim() == timeSlot
            }
            if (index != -1) {
                val updatedRules = pricingRules.toMutableList()
                updatedRules[index] = existingRule.copy(price = newPrice)
                pricingRules = updatedRules.toList()
            }
        } else {
            // Tạo rule mới
            val newRule = CourtPricingRule(
                id = System.currentTimeMillis().toString(),
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                price = newPrice,
                dayType = if (dayOfWeek == "T2 - T6") "WEEKDAY" else "WEEKEND"
            )
            pricingRules = pricingRules + newRule
        }
    }
)
```

### **5. Logic hiển thị giá:**
```kotlin
val displayText = when {
    existingRule?.price?.isNotEmpty() == true && existingRule.price != "0" -> {
        "${existingRule.price} ₫"
    }
    existingRule?.price == "0" -> {
        "0 ₫"
    }
    else -> {
        "Chưa có giá"
    }
}
```

## 🛍️ **BẢNG DỊCH VỤ BỔ SUNG - Logic chi tiết:**

### **1. Model dữ liệu:**
```kotlin
data class FieldServiceItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val isActive: Boolean = true
)
```

### **2. Cấu trúc danh mục:**
- **3 danh mục cố định**: "Nước đóng chai", "Thuê dụng cụ", "Dịch vụ khác"
- **Hiển thị**: Theo từng danh mục riêng biệt
- **Template**: Có sẵn một số dịch vụ mẫu

### **3. Logic hiển thị:**
```kotlin
val serviceCategories = listOf("Nước đóng chai", "Thuê dụng cụ", "Dịch vụ khác")

serviceCategories.forEach { category ->
    val categoryServices = services.filter { it.category == category }
    
    Card {
        Text(text = category) // Header danh mục
        
        // Hiển thị danh sách dịch vụ
        categoryServices.forEach { service ->
            ServiceRow(service = service, ...)
        }
        
        // Row thêm dịch vụ mới (edit mode)
        if (isEditMode) {
            AddServiceRow(category = category, ...)
        }
    }
}
```

### **4. Logic chỉnh sửa dịch vụ:**
```kotlin
@Composable
private fun ServiceRow(
    service: FieldServiceItem,
    isEditMode: Boolean,
    onServiceUpdated: (FieldServiceItem) -> Unit,
    onServiceDeleted: () -> Unit
) {
    if (isEditMode) {
        // TextField tên dịch vụ
        BasicTextField(
            value = service.name,
            onValueChange = { newName ->
                onServiceUpdated(service.copy(name = newName))
            }
        )
        
        // TextField giá dịch vụ
        BasicTextField(
            value = service.price,
            onValueChange = { newPrice ->
                onServiceUpdated(service.copy(price = newPrice))
            }
        )
        
        // Nút xóa
        IconButton(onClick = onServiceDeleted) {
            Icon(Icons.Default.Delete, ...)
        }
    } else {
        // View mode: Hiển thị thông tin
        Text(text = service.name.ifEmpty { "Chưa có dịch vụ" })
        Text(text = if (service.price.isNotEmpty()) "${service.price} ₫" else "")
    }
}
```

### **5. Logic thêm dịch vụ mới:**
```kotlin
@Composable
private fun AddServiceRow(
    category: String,
    onServiceAdded: (FieldServiceItem) -> Unit
) {
    var newServiceName by remember { mutableStateOf("") }
    var newServicePrice by remember { mutableStateOf("") }
    
    BasicTextField(
        value = newServiceName,
        onValueChange = { newName ->
            newServiceName = newName
            if (newName.isNotEmpty()) {
                val newService = FieldServiceItem(
                    id = System.currentTimeMillis().toString(),
                    name = newName,
                    price = "",
                    category = category,
                    isActive = true
                )
                onServiceAdded(newService)
                newServiceName = ""
            }
        }
    )
    
    BasicTextField(
        value = newServicePrice,
        onValueChange = { newPrice ->
            newServicePrice = newPrice
            // Logic cập nhật giá sẽ được xử lý riêng
        }
    )
}
```

## 🔄 **STATE MANAGEMENT:**

### **1. CourtService State:**
```kotlin
var isEditMode by remember { mutableStateOf(false) }
var pricingRules by remember { mutableStateOf(emptyList<CourtPricingRule>()) }
var refreshTrigger by remember { mutableStateOf(0) }
var validationErrors by remember { mutableStateOf(listOf<String>()) }
```

### **2. FieldServiceManager State:**
```kotlin
var services by remember { mutableStateOf(emptyList<FieldServiceItem>()) }
var refreshTrigger by remember { mutableStateOf(0) }
var validationErrors by remember { mutableStateOf(listOf<String>()) }
```

### **3. Firebase Integration:**
```kotlin
// Load data từ Firebase
LaunchedEffect(field.fieldId) {
    loadFieldData(field.fieldId, fieldViewModel)
    refreshTrigger++
}

// Observe UI state
val uiState by fieldViewModel.uiState.collectAsState()

// Cập nhật khi có thay đổi từ Firebase
LaunchedEffect(uiState.pricingRules, uiState.fieldServices, refreshTrigger) {
    val (newPricingRules, _) = updateUIDataFromFirebase(...)
    pricingRules = newPricingRules.toList()
}
```

## 💾 **SAVE LOGIC:**

### **1. Bảng giá sân:**
```kotlin
private fun saveData(
    fieldId: String, 
    pricingRules: List<CourtPricingRule>, 
    fieldServices: List<FieldService>, 
    fieldViewModel: FieldViewModel
) {
    // Lọc chỉ những pricing rules có giá
    val pricingRulesWithPrice = pricingRules.filter { rule ->
        rule.price.isNotEmpty() && rule.price != "0"
    }
    
    // Chuyển đổi sang Firebase PricingRule
    val newPricingRules = pricingRulesWithPrice.map { rule ->
        PricingRule(
            ruleId = rule.id.ifEmpty { "" },
            fieldId = fieldId,
            dayType = rule.dayType.ifEmpty { 
                when (rule.dayOfWeek) {
                    "T2 - T6" -> "WEEKDAY"
                    "T7 - CN" -> "WEEKEND"
                    else -> "WEEKDAY"
                }
            },
            price = rule.price.toLongOrNull() ?: 0L,
            description = rule.description
        )
    }
    
    // Gửi lệnh lưu
    fieldViewModel.handleEvent(FieldEvent.UpdateFieldPricingAndServices(fieldId, newPricingRules, fieldServices))
}
```

### **2. Dịch vụ bổ sung:**
```kotlin
private fun saveFieldServices(
    fieldId: String,
    services: List<FieldServiceItem>,
    fieldViewModel: FieldViewModel
) {
    // Lọc chỉ những service có tên và giá
    val servicesToSave = services.filter { 
        it.name.isNotEmpty() && it.price.isNotEmpty() && it.isActive 
    }
    
    // Chuyển đổi sang Firebase FieldService
    val newFieldServices = servicesToSave.map { service ->
        FieldService(
            fieldServiceId = service.id.ifEmpty { "" },
            fieldId = fieldId,
            name = service.name,
            price = service.price.toLongOrNull() ?: 0L,
            billingType = when (service.category) {
                "Nước đóng chai" -> "PER_UNIT"
                "Thuê dụng cụ" -> "FLAT_PER_BOOKING"
                "Dịch vụ khác" -> "PER_UNIT"
                else -> "PER_UNIT"
            },
            description = "Dịch vụ: ${service.name} - Danh mục: ${service.category}",
            isAvailable = service.isActive
        )
    }
    
    // Gửi lệnh lưu
    fieldViewModel.handleEvent(FieldEvent.UpdateFieldServices(fieldId, newFieldServices))
}
```

## ⚠️ **CÁC VẤN ĐỀ HIỆN TẠI:**

### **1. Bảng giá sân:**
- ✅ **Logic hiển thị**: Hoạt động tốt với 6 khung giờ cố định
- ✅ **Logic chỉnh sửa**: Cập nhật real-time khi user nhập
- ✅ **Logic lưu**: Chuyển đổi đúng format Firebase
- ⚠️ **Vấn đề**: Có thể có vấn đề với việc reload từ Firebase

### **2. Dịch vụ bổ sung:**
- ✅ **Logic hiển thị**: Theo danh mục rõ ràng
- ✅ **Logic chỉnh sửa**: Real-time updates
- ✅ **Logic thêm mới**: Tự động thêm khi nhập tên
- ⚠️ **Vấn đề**: Logic thêm dịch vụ có thể chưa hoàn chỉnh

### **3. State Management:**
- ✅ **Firebase integration**: Load và save đúng cách
- ✅ **UI state**: Reactive với thay đổi từ Firebase
- ⚠️ **Vấn đề**: Có thể có timing issues với refreshTrigger

## 🎯 **KHUYẾN NGHỊ CẢI THIỆN:**

### **1. Bảng giá sân:**
- Thêm validation cho giá (chỉ cho phép số)
- Thêm format giá (dấu phẩy ngăn cách hàng nghìn)
- Cải thiện UX khi chưa có giá

### **2. Dịch vụ bổ sung:**
- Hoàn thiện logic thêm dịch vụ mới
- Thêm validation cho tên và giá
- Cải thiện UX khi thêm dịch vụ

### **3. State Management:**
- Tối ưu hóa refreshTrigger logic
- Thêm loading states
- Cải thiện error handling

## 📋 **TEST CASES:**

### **1. Bảng giá sân:**
- [ ] Hiển thị đúng 6 khung giờ
- [ ] Chỉnh sửa giá real-time
- [ ] Lưu thành công vào Firebase
- [ ] Reload từ Firebase đúng cách

### **2. Dịch vụ bổ sung:**
- [ ] Hiển thị theo 3 danh mục
- [ ] Chỉnh sửa tên và giá
- [ ] Thêm dịch vụ mới
- [ ] Xóa dịch vụ
- [ ] Lưu thành công vào Firebase

### **3. Integration:**
- [ ] Edit mode hoạt động đúng
- [ ] Save button hoạt động cho cả hai
- [ ] Validation errors hiển thị đúng
- [ ] Loading states hoạt động
