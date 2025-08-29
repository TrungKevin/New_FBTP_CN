# 💰 Khắc Phục Vấn Đề Hiển Thị Giá - CourtService

## 🚨 **Vấn Đề Được Báo Cáo**

**"Tại sao price khi được lưu không hiển thị lên theo từng khoảng giờ được"**

**Biểu hiện**: Cột giá "Giá (₫/30')" hoàn toàn trống, mặc dù dữ liệu đã được lưu vào Firebase.

## 🔍 **Nguyên Nhân Có Thể**

### **1. Mapping Logic Sai**
- Dữ liệu từ Firebase không được map đúng cách sang UI model
- Price field bị mất trong quá trình mapping
- Description format không khớp với logic mapping

### **2. State Management**
- Local state `pricingRules` không được cập nhật đúng cách
- UI không re-render khi state thay đổi
- LaunchedEffect không được trigger

### **3. Data Flow**
- Dữ liệu được lưu vào Firebase nhưng không được load về
- Mapping từ `PricingRule` sang `CourtPricingRule` bị lỗi
- Price value bị convert sai

## 🔧 **Đã Khắc Phục**

### **1. Cải Thiện Mapping Logic**
```kotlin
// Đảm bảo price không bao giờ trống
val mappedPrice = if (rule.price > 0) rule.price.toString() else ""

// Debug logs chi tiết
println("  - Original Price: ${rule.price}")
println("  - Mapped Price: $mappedPrice")
```

### **2. Thêm Debug Logs Chi Tiết**
```kotlin
// Debug: Kiểm tra local state sau khi cập nhật
println("🔍 DEBUG: Kiểm tra local state sau khi cập nhật:")
println("  - localPricingRules.size: ${localPricingRules.size}")
println("  - localPricingRules.isEmpty: ${localPricingRules.isEmpty()}")
localPricingRules.forEachIndexed { index, rule ->
    println("  - [$index] price: '${rule.price}' (length: ${rule.price.length})")
}
```

### **3. Cải Thiện UI Rendering**
```kotlin
// Debug: In ra thông tin về rule để kiểm tra
LaunchedEffect(rule) {
    println("🔍 DEBUG: Rendering price for rule [$index]:")
    println("  - rule: $rule")
    println("  - rule.price: '${rule.price}'")
    println("  - rule.price.isNotEmpty(): ${rule.price.isNotEmpty()}")
    println("  - rule.price.length: ${rule.price.length}")
}

// Hiển thị giá với visual feedback
Text(
    text = if (rule.price.isNotEmpty()) "${rule.price} ₫/30'" else "Chưa có giá",
    // ... styling với background color khác nhau
)
```

### **4. Debug Rendering Table**
```kotlin
// Debug: Kiểm tra dữ liệu pricingRules trước khi render
LaunchedEffect(pricingRules) {
    println("🔍 DEBUG: Rendering pricing table:")
    println("  - pricingRules.size: ${pricingRules.size}")
    println("  - pricingRules.isEmpty: ${pricingRules.isEmpty()}")
    pricingRules.forEachIndexed { index, rule ->
        println("  - [$index] $rule")
    }
}
```

## 🧪 **Cách Test Để Khắc Phục**

### **Bước 1: Test Lưu Dữ Liệu**
```
1. Vào CourtService component
2. Click nút ✏️ (Edit)
3. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
4. Click nút 💾 (Save)
```

### **Bước 2: Kiểm Tra Debug Logs - Lưu Dữ Liệu**
```
Trong Logcat, filter "DEBUG", tìm:
💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase
📊 Input pricing rules: 6 items
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
🔍 DEBUG: Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h
💾 DEBUG: Dữ liệu sẽ lưu vào Firebase:
📊 Pricing Rules sẽ lưu: 6 items
  [0] PricingRule:
    - price: 55000
🚀 DEBUG: Gửi lệnh lưu dữ liệu vào Firebase...
✅ Đã gửi lệnh lưu dữ liệu vào Firebase
```

### **Bước 3: Kiểm Tra Debug Logs - Load Dữ Liệu**
```
Sau khi lưu thành công, kiểm tra:
🔄 DEBUG: LaunchedEffect triggered
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🔍 DEBUG: PricingRule từ Firebase:
  - price: 55000
🔄 DEBUG: Mapping rule:
  - Original Price: 55000
  - Mapped Price: 55000
🎯 DEBUG: Tạo CourtPricingRule: CourtPricingRule(..., price=55000)
🔄 DEBUG: Cập nhật local state với 6 pricing rules
✅ Đã map 6 pricing rules thành công
🔍 DEBUG: Kiểm tra local state sau khi cập nhật:
  - localPricingRules.size: 6
  - localPricingRules.isEmpty: false
  - [0] price: '55000' (length: 5)
```

### **Bước 4: Kiểm Tra Debug Logs - UI Rendering**
```
Khi UI render, kiểm tra:
🔍 DEBUG: Rendering pricing table:
  - pricingRules.size: 6
  - pricingRules.isEmpty: false
  - [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)

🔍 DEBUG: Rendering price for rule [0]:
  - rule: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
  - rule.price: '55000'
  - rule.price.isNotEmpty(): true
  - rule.price.length: 5
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **Nếu Dữ Liệu Không Được Lưu:**
```
❌ Không thấy logs "Bắt đầu lưu dữ liệu vào Firebase"
❌ Không thấy logs "Gửi lệnh lưu dữ liệu vào Firebase"
❌ Không thấy logs "Đã gửi lệnh lưu dữ liệu vào Firebase"
```

### **Nếu Dữ Liệu Được Lưu Nhưng Không Load:**
```
❌ Không thấy logs "LaunchedEffect triggered"
❌ Không thấy logs "Cập nhật dữ liệu từ Firebase"
❌ Không thấy logs "Có dữ liệu pricing rules, mapping..."
```

### **Nếu Mapping Sai:**
```
❌ Không thấy logs "Mapping rule:"
❌ Không thấy logs "Tạo CourtPricingRule:"
❌ Không thấy logs "Cập nhật local state với X pricing rules"
```

### **Nếu State Không Cập Nhật:**
```
❌ Không thấy logs "Rendering pricing table:"
❌ Không thấy logs "Rendering price for rule [X]:"
❌ localPricingRules.size = 0 hoặc isEmpty = true
```

## 🚨 **Nếu Vẫn Có Vấn Đề**

### **Vấn Đề 1: Dữ Liệu Không Được Lưu**
```
Kiểm tra:
- Firebase connection
- Firebase rules (permission)
- Network connection
- Validation logic trong saveData()
```

### **Vấn Đề 2: Dữ Liệu Được Lưu Nhưng Không Load**
```
Kiểm tra:
- FieldViewModel.loadPricingRulesByFieldId()
- Repository.getPricingRulesByFieldId()
- Firebase query
- uiState.pricingRules
```

### **Vấn Đề 3: Mapping Sai**
```
Kiểm tra:
- Description format trong Firebase
- Logic mapping trong updateUIDataFromFirebase()
- dayType vs dayOfWeek mapping
- timeSlot extraction
- Price conversion
```

### **Vấn Đề 4: State Không Cập Nhật**
```
Kiểm tra:
- LaunchedEffect triggers
- Local state updates
- UI recomposition
- Compose state management
```

## 🔧 **Giải Pháp Thêm**

### **Nếu Mapping Logic Vẫn Sai:**
```kotlin
// Thêm fallback mapping dựa vào index
val mappedTimeSlot = when (index) {
    0 -> "5h - 12h"
    1 -> "12h - 18h"
    2 -> "18h - 24h"
    3 -> "5h - 12h"
    4 -> "12h - 18h"
    5 -> "18h - 24h"
    else -> "5h - 12h"
}

val mappedDayOfWeek = when (index) {
    0, 1, 2 -> "T2 - T6"
    3, 4, 5 -> "T7 - CN"
    else -> "T2 - T6"
}
```

### **Nếu State Management Vẫn Sai:**
```kotlin
// Force refresh UI
var refreshTrigger by remember { mutableStateOf(0) }

LaunchedEffect(uiState.pricingRules, uiState.fieldServices, refreshTrigger) {
    updateUIDataFromFirebase(uiState.pricingRules, uiState.fieldServices, pricingRules, services)
}

// Thêm nút refresh manual
IconButton(onClick = { refreshTrigger++ }) {
    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
}
```

## 📝 **Ghi Chú Quan Trọng**

1. **Test trên device thật** để đảm bảo Firebase connection
2. **Monitor Logcat** để theo dõi toàn bộ flow
3. **Kiểm tra Firebase Console** để xác nhận dữ liệu được lưu
4. **Test nhiều lần** để đảm bảo consistency
5. **Kiểm tra network connection** trước khi test

## 🎯 **Kết Quả Mong Đợi**

Sau khi khắc phục:
- ✅ Dữ liệu được lưu vào Firebase
- ✅ UI tự động reload với dữ liệu mới
- ✅ Cột giá hiển thị đúng giá đã nhập (55000 ₫/30', 60000 ₫/30', ...)
- ✅ Owner có thể quản lý bảng giá dễ dàng
- ✅ Debug logs rõ ràng để troubleshoot

## 🚀 **Bước Tiếp Theo**

1. **Test theo hướng dẫn trên**
2. **Monitor Logcat** để xem debug logs
3. **Cho biết kết quả** và logs nào xuất hiện
4. **Nếu vẫn có vấn đề**, cung cấp logs để debug tiếp

Hãy test và cho biết kết quả! 🎯
