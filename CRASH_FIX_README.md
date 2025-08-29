# 🚨 Khắc Phục Vấn Đề CourtService - Cột Giá Không Hiển Thị

## 🎯 **Vấn Đề Được Báo Cáo**

**"Khi nhập thông tin bảng giá sân thì tất cả phải được lưu vào Firebase store, hiển thị ngược lại lên bảng này để owner thấy và quản lý tại sao cột giá không hiển thị lên những dữ liệu được lưu"**

## 🔧 **Đã Sửa**

### **1. Thêm Debug Logs Chi Tiết**
- ✅ Debug logs trong `updateUIDataFromFirebase()`
- ✅ Debug logs trong `saveData()`
- ✅ Debug logs cho mapping logic

### **2. Sửa Logic Mapping**
- ✅ Thêm support cho format "5h-12h" (không có dấu cách)
- ✅ Cải thiện logic tìm kiếm khung giờ trong description
- ✅ Debug logs cho từng bước mapping

### **3. Cải Thiện Description Format**
- ✅ Đảm bảo description format: "Giá T2 - T6 - 5h - 12h"
- ✅ Debug logs cho description được tạo

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

### **Bước 2: Kiểm Tra Debug Logs**
```
Trong Logcat, filter "DEBUG", tìm các keywords:
- "Bắt đầu lưu dữ liệu vào Firebase"
- "Tạo PricingRule với description"
- "Gửi lệnh lưu dữ liệu vào Firebase"
- "Cập nhật bảng giá và dịch vụ thành công!"
```

### **Bước 3: Kiểm Tra Firebase Console**
```
1. Vào Firebase Console
2. Chọn project
3. Vào Firestore Database
4. Kiểm tra collection "pricing_rules"
5. Xem có 6 documents mới không
6. Kiểm tra description format: "Giá T2 - T6 - 5h - 12h"
```

### **Bước 4: Kiểm Tra Load Dữ Liệu**
```
Sau khi lưu thành công, kiểm tra Logcat:
- "LaunchedEffect triggered"
- "Cập nhật dữ liệu từ Firebase"
- "Có dữ liệu pricing rules, mapping..."
- "Mapping rule:"
- "Tạo CourtPricingRule:"
- "Cập nhật local state với X pricing rules"
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **Khi Lưu Dữ Liệu:**
```
💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase
📊 Input pricing rules: 6 items
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
  [1] CourtPricingRule(id=2, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=60000)
  ...
🔍 DEBUG: Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h
💾 DEBUG: Dữ liệu sẽ lưu vào Firebase:
📊 Pricing Rules sẽ lưu: 6 items
  [0] PricingRule:
    - ruleId: 
    - fieldId: field_001
    - dayType: WEEKDAY
    - description: Giá T2 - T6 - 5h - 12h
    - price: 55000
    - minutes: 30
🚀 DEBUG: Gửi lệnh lưu dữ liệu vào Firebase...
✅ Đã gửi lệnh lưu dữ liệu vào Firebase
```

### **Khi Load Dữ Liệu:**
```
🔄 DEBUG: LaunchedEffect triggered - pricingRules: 6, fieldServices: 0, refreshTrigger: 1
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🛍️ Field Services từ Firebase: 0 items
🔍 DEBUG: PricingRule từ Firebase:
  - ruleId: abc123
  - fieldId: field_001
  - dayType: WEEKDAY
  - description: Giá T2 - T6 - 5h - 12h
  - price: 55000
  - minutes: 30
✅ Có dữ liệu pricing rules, mapping...
🔄 DEBUG: Mapping rule:
  - Original: dayType=WEEKDAY, description=Giá T2 - T6 - 5h - 12h
  - Mapped: dayOfWeek=T2 - T6, timeSlot=5h - 12h
  - Price: 55000
🎯 DEBUG: Tạo CourtPricingRule: CourtPricingRule(id=abc123, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
🔄 DEBUG: Cập nhật local state với 6 pricing rules
✅ Đã map 6 pricing rules thành công
📊 CourtPricingRule [0]: dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000
📊 CourtPricingRule [1]: dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=60000
...
```

## 🚨 **Nếu Vẫn Có Vấn Đề**

### **Vấn Đề 1: Dữ Liệu Không Được Lưu**
```
Kiểm tra:
- Firebase connection
- Firebase rules (permission)
- Network connection
- Validation logic
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
// Thêm fallback mapping
val mappedTimeSlot = when {
    rule.description.contains("5h - 12h", ignoreCase = true) -> "5h - 12h"
    rule.description.contains("12h - 18h", ignoreCase = true) -> "12h - 18h"
    rule.description.contains("18h - 24h", ignoreCase = true) -> "18h - 24h"
    rule.description.contains("5h-12h", ignoreCase = true) -> "5h - 12h"
    rule.description.contains("12h-18h", ignoreCase = true) -> "12h - 18h"
    rule.description.contains("18h-24h", ignoreCase = true) -> "18h - 24h"
    // Thêm fallback dựa vào minutes
    rule.minutes == 30 -> "5h - 12h" // Default cho 30 phút
    else -> "5h - 12h" // Fallback cuối cùng
}
```

### **Nếu State Management Vẫn Sai:**
```kotlin
// Force refresh UI
var refreshTrigger by remember { mutableStateOf(0) }

LaunchedEffect(uiState.pricingRules, uiState.fieldServices, refreshTrigger) {
    updateUIDataFromFirebase(uiState.pricingRules, uiState.fieldServices, pricingRules, services)
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
- ✅ Cột giá hiển thị đúng giá đã nhập
- ✅ Owner có thể quản lý bảng giá dễ dàng
- ✅ Debug logs rõ ràng để troubleshoot

Hãy test theo hướng dẫn trên và cho biết kết quả! 🚀
