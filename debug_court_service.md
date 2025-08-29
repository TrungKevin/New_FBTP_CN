# 🐛 Debug CourtService - Phân Tích Vấn Đề

## 🔍 **Vấn Đề Được Báo Cáo**

**"Khi nhập thông tin bảng giá sân thì tất cả phải được lưu vào Firebase store, hiển thị ngược lại lên bảng này để owner thấy và quản lý tại sao cột giá không hiển thị lên những dữ liệu được lưu"**

## 🧐 **Phân Tích Code Hiện Tại**

### **1. Flow Lưu Dữ Liệu**
```kotlin
// 1. User nhập giá → CourtPricingRule.price = "55000"
// 2. Click Save → saveData() được gọi
// 3. Convert UI model → Firebase model
// 4. Gửi lên Firebase qua FieldViewModel
// 5. Firebase lưu thành công
// 6. uiState.success được set
// 7. LaunchedEffect(uiState.success) trigger
// 8. loadFieldData() được gọi
// 9. Firebase trả về dữ liệu
// 10. updateUIDataFromFirebase() được gọi
// 11. UI được cập nhật
```

### **2. Flow Load Dữ Liệu**
```kotlin
// 1. LaunchedEffect(field.fieldId) trigger
// 2. loadFieldData() được gọi
// 3. FieldEvent.LoadPricingRulesByFieldId(fieldId)
// 4. Repository query Firebase
// 5. Firebase trả về List<PricingRule>
// 6. uiState.pricingRules được cập nhật
// 7. LaunchedEffect(uiState.pricingRules, ...) trigger
// 8. updateUIDataFromFirebase() được gọi
// 9. Mapping PricingRule → CourtPricingRule
// 10. Local state được cập nhật
// 11. UI hiển thị dữ liệu
```

## 🚨 **Các Điểm Có Thể Gây Lỗi**

### **Điểm 1: Mapping Logic**
```kotlin
// Trong updateUIDataFromFirebase()
val mappedTimeSlot = when {
    rule.description.contains("5h - 12h", ignoreCase = true) -> "5h - 12h"
    rule.description.contains("12h - 18h", ignoreCase = true) -> "12h - 18h"
    rule.description.contains("18h - 24h", ignoreCase = true) -> "18h - 24h"
    else -> {
        // Nếu không tìm thấy trong description, tạo khung giờ mặc định
        val hours = rule.minutes / 60
        val startHour = 5
        val endHour = startHour + hours
        "${startHour}h - ${endHour}h"
    }
}
```

**Vấn đề có thể**: Description không chứa khung giờ chính xác

### **Điểm 2: Description Format**
```kotlin
// Trong saveData()
description = "Giá ${rule.dayOfWeek} - ${rule.timeSlot}"
// Kết quả: "Giá T2 - T6 - 5h - 12h"
```

**Vấn đề có thể**: Format description không khớp với logic mapping

### **Điểm 3: State Management**
```kotlin
// Trong updateUIDataFromFirebase()
localPricingRules.clear()
localPricingRules.addAll(newPricingRules)
```

**Vấn đề có thể**: Local state không được cập nhật đúng cách

## 🔧 **Giải Pháp Debug**

### **Giải Pháp 1: Thêm Debug Logs**
```kotlin
// Trong updateUIDataFromFirebase()
firebasePricingRules.forEach { rule ->
    println("🔍 DEBUG: PricingRule từ Firebase:")
    println("  - ruleId: ${rule.ruleId}")
    println("  - fieldId: ${rule.fieldId}")
    println("  - dayType: ${rule.dayType}")
    println("  - description: ${rule.description}")
    println("  - price: ${rule.price}")
    println("  - minutes: ${rule.minutes}")
}
```

### **Giải Pháp 2: Kiểm Tra Description Format**
```kotlin
// Trong saveData()
val description = "Giá ${rule.dayOfWeek} - ${rule.timeSlot}"
println("🔍 DEBUG: Tạo description: $description")
```

### **Giải Pháp 3: Kiểm Tra Mapping Result**
```kotlin
// Trong updateUIDataFromFirebase()
val newPricingRules = firebasePricingRules.map { rule ->
    // ... mapping logic ...
    val result = CourtPricingRule(...)
    println("🔍 DEBUG: Mapping result: $result")
    result
}
```

## 🧪 **Test Cases Để Debug**

### **Test Case 1: Kiểm Tra Description Format**
```
1. Lưu dữ liệu với giá: 55000
2. Kiểm tra Firebase console
3. Xem description có format: "Giá T2 - T6 - 5h - 12h" không
```

### **Test Case 2: Kiểm Tra Mapping Logic**
```
1. Load dữ liệu từ Firebase
2. Kiểm tra logcat
3. Xem mapping có đúng không:
   - "Giá T2 - T6 - 5h - 12h" → timeSlot = "5h - 12h"
   - "Giá T2 - T6 - 12h - 18h" → timeSlot = "12h - 18h"
```

### **Test Case 3: Kiểm Tra State Update**
```
1. Sau khi mapping
2. Kiểm tra localPricingRules có dữ liệu không
3. Kiểm tra UI có hiển thị không
```

## 🎯 **Kết Luận**

Vấn đề có thể nằm ở:

1. **Description format** không khớp với mapping logic
2. **Mapping logic** không xử lý đúng các trường hợp
3. **State management** không cập nhật đúng cách
4. **Firebase data** không được load đúng cách

**Giải pháp**: Thêm debug logs để theo dõi toàn bộ flow và xác định chính xác điểm gây lỗi.
