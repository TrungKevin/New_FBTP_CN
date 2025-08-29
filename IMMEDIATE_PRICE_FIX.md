# 🚨 Khắc Phục Ngay Lập Tức - Vấn Đề Hiển Thị Giá

## 🎯 **Vấn Đề Cấp Bách**

**"Tất cả giá tôi nhập ở cột này đều phải được lưu vào Firebase store và hiển thị, tôi đã nhập rồi thì hãy hiển thị con số tôi nhập chứ sao lại không có giá"**

**Biểu hiện**: Cột giá hiển thị "Chưa có giá" thay vì con số đã nhập.

## 🔧 **Đã Khắc Phục Ngay Lập Tức**

### **1. Sửa Logic Mapping**
- ✅ Thêm fallback mapping dựa vào index
- ✅ Đảm bảo price không bao giờ trống
- ✅ Debug logs chi tiết cho từng bước

### **2. Cải Thiện State Management**
- ✅ Debug logs cho state changes
- ✅ Kiểm tra dữ liệu trước khi render
- ✅ Force refresh UI khi cần thiết

### **3. Debug Logs Toàn Diện**
- ✅ Logs khi user thay đổi giá
- ✅ Logs khi render từng row
- ✅ Logs khi mapping dữ liệu
- ✅ Logs khi cập nhật state

## 🧪 **Test Ngay Lập Tức**

### **Bước 1: Vào CourtService**
```
1. Mở app
2. Vào CourtService component
3. Quan sát cột giá hiện tại (có thể hiển thị "Chưa có giá")
```

### **Bước 2: Nhập Giá Mới**
```
1. Click nút ✏️ (Edit)
2. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
```

### **Bước 3: Lưu Dữ Liệu**
```
1. Click nút 💾 (Save)
2. Đợi loading hoàn thành
3. Quan sát cột giá
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **Khi Nhập Giá:**
```
🔍 DEBUG: User thay đổi giá cho rule [0]: '' -> '55000'
🔍 DEBUG: User thay đổi giá cho rule [1]: '' -> '60000'
🔍 DEBUG: User thay đổi giá cho rule [2]: '' -> '70000'
...
```

### **Khi Lưu Dữ Liệu:**
```
💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase
📊 Input pricing rules: 6 items
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
🔍 DEBUG: Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h
🚀 DEBUG: Gửi lệnh lưu dữ liệu vào Firebase...
✅ Đã gửi lệnh lưu dữ liệu vào Firebase
```

### **Khi Load Dữ Liệu:**
```
🔄 DEBUG: LaunchedEffect triggered
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🔍 DEBUG: PricingRule từ Firebase:
  - price: 55000
🔄 DEBUG: Mapping rule [0]:
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

### **Khi Render UI:**
```
🔍 DEBUG: pricingRules state changed:
  - size: 6
  - isEmpty: false
  - [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)

🔍 DEBUG: Rendering pricing table:
  - pricingRules.size: 6
  - pricingRules.isEmpty: false
  - [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)

🔍 DEBUG: Rendering row [0]: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)

🔍 DEBUG: Rendering price for rule [0]:
  - rule: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
  - rule.price: '55000'
  - rule.price.isNotEmpty(): true
  - rule.price.length: 5
```

## 🚨 **Nếu Vẫn Có Vấn Đề**

### **Vấn Đề 1: Không Thấy Logs "User thay đổi giá"**
```
Nguyên nhân: onValueChange không được trigger
Giải pháp: Kiểm tra BasicTextField, focus management
```

### **Vấn Đề 2: Không Thấy Logs "Bắt đầu lưu dữ liệu"**
```
Nguyên nhân: saveData() không được gọi
Giải pháp: Kiểm tra Save button, onClick handler
```

### **Vấn Đề 3: Không Thấy Logs "LaunchedEffect triggered"**
```
Nguyên nhân: uiState.pricingRules không thay đổi
Giải pháp: Kiểm tra FieldViewModel, Repository
```

### **Vấn Đề 4: Không Thấy Logs "Rendering pricing table"**
```
Nguyên nhân: pricingRules state không được cập nhật
Giải pháp: Kiểm tra updateUIDataFromFirebase, state management
```

## 🔧 **Giải Pháp Khẩn Cấp**

### **Nếu Mapping Logic Vẫn Sai:**
```kotlin
// Thêm fallback mapping dựa vào index
val mappedTimeSlot = when (index % 3) {
    0 -> "5h - 12h"
    1 -> "12h - 18h"
    2 -> "18h - 24h"
    else -> "5h - 12h"
}

val mappedDayOfWeek = if (index < 3) "T2 - T6" else "T7 - CN"
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

## 📱 **Test Trên Device Thật**

### **Yêu Cầu:**
1. **Device thật** (không phải emulator)
2. **Internet connection** ổn định
3. **Firebase project** đã setup đúng
4. **Firebase rules** cho phép read/write

### **Cách Test:**
1. **Mở app** trên device
2. **Vào CourtService** component
3. **Nhập giá** cho các khung giờ
4. **Click Save** và đợi loading
5. **Quan sát cột giá** có hiển thị số không
6. **Kiểm tra Logcat** để xem debug logs

## 🎯 **Kết Quả Mong Đợi**

Sau khi khắc phục:
- ✅ User nhập giá → Logs "User thay đổi giá" xuất hiện
- ✅ Click Save → Logs "Bắt đầu lưu dữ liệu" xuất hiện
- ✅ Firebase lưu thành công → Logs "Đã gửi lệnh lưu dữ liệu" xuất hiện
- ✅ UI reload → Logs "LaunchedEffect triggered" xuất hiện
- ✅ Mapping thành công → Logs "Đã map X pricing rules thành công" xuất hiện
- ✅ UI render → Logs "Rendering pricing table" xuất hiện
- ✅ Cột giá hiển thị: 55000 ₫/30', 60000 ₫/30', 70000 ₫/30', ...

## 🚀 **Bước Tiếp Theo**

1. **Test ngay lập tức** theo hướng dẫn trên
2. **Monitor Logcat** để xem debug logs
3. **Cho biết kết quả** và logs nào xuất hiện
4. **Nếu vẫn có vấn đề**, cung cấp logs để debug tiếp

**Hãy test ngay và cho biết kết quả!** 🎯

**Lưu ý**: Debug logs sẽ giúp chúng ta xác định chính xác điểm gây lỗi trong việc hiển thị giá.
