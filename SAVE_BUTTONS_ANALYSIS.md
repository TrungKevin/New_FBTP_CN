# 🔘 Phân tích 2 Button Save - Bảng Giá Sân & Dịch Vụ Bổ Sung

## 🎯 **Tổng quan hiện tại:**

Hiện tại có **2 button save riêng biệt**:
1. **Button Save Bảng Giá Sân** (trong `CourtService.kt`)
2. **Button Save Dịch Vụ Bổ Sung** (trong `FieldServiceManager.kt`)

## 🔧 **BUTTON SAVE BẢNG GIÁ SÂN:**

### **1. Vị trí và UI:**
```kotlin
// Trong CourtService.kt - Header section
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
```

### **2. Logic hoạt động:**
```kotlin
// 1. Validate dữ liệu bảng giá
val errors = validateData(pricingRules)

// 2. Nếu không có lỗi, lưu dữ liệu
if (errors.isEmpty()) {
    saveData(field.fieldId, pricingRules, uiState.fieldServices, fieldViewModel)
} else {
    // 3. Nếu có lỗi, hiển thị validation errors
    validationErrors = errors
}
```

### **3. Function saveData:**
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

### **4. Validation logic:**
```kotlin
private fun validateData(pricingRules: List<CourtPricingRule>): List<String> {
    val errors = mutableListOf<String>()
    
    // Validate chỉ những rule có giá
    val rulesWithPrice = pricingRules.filter { rule -> 
        rule.price.isNotEmpty() && rule.price != "0" 
    }
    
    rulesWithPrice.forEach { rule ->
        if (rule.price.toLongOrNull() == null) {
            errors.add("Giá không hợp lệ cho ${rule.dayOfWeek} - ${rule.timeSlot}: ${rule.price}")
        } else if (rule.price.toLong() <= 0) {
            errors.add("Giá phải lớn hơn 0 cho ${rule.dayOfWeek} - ${rule.timeSlot}")
        }
    }
    
    // Kiểm tra có ít nhất một pricing rule có giá
    if (rulesWithPrice.isEmpty()) {
        errors.add("Vui lòng nhập ít nhất một mức giá cho sân")
    }
    
    return errors
}
```

## 🛍️ **BUTTON SAVE DỊCH VỤ BỔ SUNG:**

### **1. Vị trí và UI:**
```kotlin
// Trong FieldServiceManager.kt - Bottom section
if (isEditMode) {
    Button(
        onClick = {
            println("💾 DEBUG: FieldServiceManager - Save button được click!")
            val errors = validateServices(services)
            if (errors.isEmpty()) {
                saveFieldServices(fieldId, services, fieldViewModel)
                validationErrors = emptyList()
            } else {
                validationErrors = errors
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text("Lưu Dịch Vụ", color = MaterialTheme.colorScheme.onPrimary)
    }
}
```

### **2. Logic hoạt động:**
```kotlin
// 1. Validate dữ liệu dịch vụ
val errors = validateServices(services)

// 2. Nếu không có lỗi, lưu dữ liệu
if (errors.isEmpty()) {
    saveFieldServices(fieldId, services, fieldViewModel)
    validationErrors = emptyList()
} else {
    // 3. Nếu có lỗi, hiển thị validation errors
    validationErrors = errors
}
```

### **3. Function saveFieldServices:**
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

### **4. Validation logic:**
```kotlin
private fun validateServices(services: List<FieldServiceItem>): List<String> {
    val errors = mutableListOf<String>()
    
    // Validate chỉ những service có tên và đang active
    val servicesWithName = services.filter { it.name.isNotEmpty() && it.isActive }
    
    servicesWithName.forEach { service ->
        if (service.price.isEmpty()) {
            errors.add("Giá không được để trống cho dịch vụ: ${service.name}")
        } else if (service.price.toLongOrNull() == null) {
            errors.add("Giá không hợp lệ cho dịch vụ ${service.name}: ${service.price}")
        } else if (service.price.toLong() <= 0) {
            errors.add("Giá phải lớn hơn 0 cho dịch vụ: ${service.name}")
        }
    }
    
    // Kiểm tra có ít nhất một dịch vụ
    if (servicesWithName.isEmpty()) {
        errors.add("Vui lòng nhập ít nhất một dịch vụ")
    }
    
    return errors
}
```

## ⚠️ **VẤN ĐỀ HIỆN TẠI:**

### **1. 2 Button Save riêng biệt:**
- ❌ **UX không tốt**: User phải click 2 lần để lưu cả 2 bảng
- ❌ **Không đồng bộ**: Có thể lưu bảng giá nhưng quên lưu dịch vụ
- ❌ **Confusing**: Không rõ ràng về thứ tự lưu

### **2. Logic không thống nhất:**
- ❌ **Bảng giá**: Lưu cả pricing rules và field services
- ❌ **Dịch vụ**: Chỉ lưu field services
- ❌ **Overlap**: Có thể ghi đè dữ liệu lẫn nhau

### **3. State Management issues:**
- ❌ **Separate validation**: Mỗi button có validation riêng
- ❌ **Separate error handling**: Error messages không thống nhất
- ❌ **Separate success handling**: Success feedback không đồng bộ

## 🎯 **KHUYẾN NGHỊ CẢI THIỆN:**

### **1. Gộp thành 1 Button Save duy nhất:**
```kotlin
// Button save duy nhất cho cả 2 bảng
Button(
    onClick = {
        println("💾 DEBUG: Unified Save button được click!")
        
        // Validate cả 2 bảng
        val pricingErrors = validateData(pricingRules)
        val serviceErrors = validateServices(services)
        val allErrors = pricingErrors + serviceErrors
        
        if (allErrors.isEmpty()) {
            // Lưu cả 2 bảng cùng lúc
            saveAllData(field.fieldId, pricingRules, services, fieldViewModel)
        } else {
            validationErrors = allErrors
        }
    },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("Lưu Bảng Giá & Dịch Vụ", color = MaterialTheme.colorScheme.onPrimary)
}
```

### **2. Function saveAllData thống nhất:**
```kotlin
private fun saveAllData(
    fieldId: String,
    pricingRules: List<CourtPricingRule>,
    services: List<FieldServiceItem>,
    fieldViewModel: FieldViewModel
) {
    // Chuyển đổi pricing rules
    val newPricingRules = pricingRules.filter { 
        it.price.isNotEmpty() && it.price != "0" 
    }.map { rule ->
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
    
    // Chuyển đổi field services
    val newFieldServices = services.filter { 
        it.name.isNotEmpty() && it.price.isNotEmpty() && it.isActive 
    }.map { service ->
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
    
    // Gửi lệnh lưu cả 2 bảng
    fieldViewModel.handleEvent(FieldEvent.UpdateFieldPricingAndServices(fieldId, newPricingRules, newFieldServices))
}
```

### **3. Validation thống nhất:**
```kotlin
private fun validateAllData(
    pricingRules: List<CourtPricingRule>,
    services: List<FieldServiceItem>
): List<String> {
    val errors = mutableListOf<String>()
    
    // Validate pricing rules
    val pricingErrors = validateData(pricingRules)
    errors.addAll(pricingErrors)
    
    // Validate services
    val serviceErrors = validateServices(services)
    errors.addAll(serviceErrors)
    
    return errors
}
```

## 📋 **IMPLEMENTATION PLAN:**

### **1. Bước 1: Tạo unified save function**
- [ ] Tạo `saveAllData` function
- [ ] Tạo `validateAllData` function
- [ ] Test logic chuyển đổi dữ liệu

### **2. Bước 2: Cập nhật UI**
- [ ] Xóa button save riêng biệt
- [ ] Thêm button save duy nhất
- [ ] Cập nhật validation error display

### **3. Bước 3: Cập nhật FieldServiceManager**
- [ ] Xóa button save trong FieldServiceManager
- [ ] Truyền services data lên parent component
- [ ] Cập nhật callback mechanism

### **4. Bước 4: Test và validation**
- [ ] Test save cả 2 bảng
- [ ] Test validation errors
- [ ] Test success feedback
- [ ] Test data consistency

## 🎉 **LỢI ÍCH SAU KHI CẢI THIỆN:**

### **1. UX tốt hơn:**
- ✅ **1 click save**: User chỉ cần click 1 lần
- ✅ **Clear feedback**: Thông báo rõ ràng về trạng thái
- ✅ **Consistent behavior**: Hành vi thống nhất

### **2. Data consistency:**
- ✅ **Atomic save**: Lưu cả 2 bảng cùng lúc
- ✅ **No data loss**: Không bị mất dữ liệu
- ✅ **Proper validation**: Validate đầy đủ trước khi lưu

### **3. Maintainability:**
- ✅ **Single responsibility**: 1 function cho 1 nhiệm vụ
- ✅ **Easier testing**: Dễ test hơn
- ✅ **Better error handling**: Xử lý lỗi tập trung
