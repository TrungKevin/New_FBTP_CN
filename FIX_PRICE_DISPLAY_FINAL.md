# 🚨 Khắc Phục Cuối Cùng - Vấn Đề Hiển Thị Giá

## 🎯 **Vấn Đề Cuối Cùng**

**"Dữ liệu đã có thông tin sao cột giá chưa hiển thị"**

**Phân tích**: Dữ liệu đã có trong Firebase (như bạn đã chụp màn hình), nhưng cột giá vẫn không hiển thị. Điều này có nghĩa là có vấn đề với việc **load dữ liệu từ Firebase** hoặc **mapping dữ liệu** từ `PricingRule` sang `CourtPricingRule`.

## 🔍 **Nguyên Nhân Có Thể**

### **1. Dữ Liệu Firebase Không Được Load**
- `uiState.pricingRules` trống
- `FieldEvent.LoadPricingRulesByFieldId` không được gọi
- Firebase connection failed

### **2. Mapping Logic Bị Lỗi**
- `PricingRule` → `CourtPricingRule` mapping sai
- `price` field không được map đúng
- `description` parsing sai

### **3. State Management Bị Lỗi**
- `localPricingRules` không được cập nhật
- `LaunchedEffect` không được trigger
- UI state không sync với local state

### **4. UI Rendering Bị Lỗi**
- `pricingRules` state trống
- Text component không render đúng
- Conditional rendering sai

## 🔧 **Đã Khắc Phục Cuối Cùng**

### **1. Debug Logs Toàn Diện**
- ✅ Logs khi LaunchedEffect triggered
- ✅ Logs khi mapping từ Firebase
- ✅ Logs khi cập nhật local state
- ✅ Logs khi render UI

### **2. Logic Mapping Cải Thiện**
- ✅ Xử lý tốt hơn việc mapping price
- ✅ Debug logs chi tiết cho từng bước mapping
- ✅ Kiểm tra type và giá trị của price

### **3. State Management Cải Thiện**
- ✅ Debug logs cho state changes
- ✅ Kiểm tra dữ liệu trước và sau khi cập nhật
- ✅ Force refresh UI khi cần thiết

## 🧪 **Test Cuối Cùng**

### **Bước 1: Vào CourtService**
```
1. Mở app
2. Vào CourtService component
3. Quan sát cột giá hiện tại (có thể hiển thị "Chưa có giá")
4. Mở Logcat để theo dõi debug logs
```

### **Bước 2: Quan Sát Debug Logs**
```
1. Quan sát logs "LaunchedEffect triggered"
2. Quan sát logs "Cập nhật dữ liệu từ Firebase"
3. Quan sát logs "Pricing Rules từ Firebase: X items"
4. Quan sát logs "Mapping rule [X]"
5. Quan sát logs "Tạo CourtPricingRule"
6. Quan sát logs "Cập nhật local state với X pricing rules"
7. Quan sát logs "Đã map X pricing rules thành công"
```

### **Bước 3: Quan Sát State Changes**
```
1. Quan sát logs "pricingRules state changed"
2. Quan sát logs "Rendering pricing table"
3. Quan sát logs "Rendering row [X]"
4. Quan sát logs "Rendering price for rule [X]"
5. Quan sát logs "Rendering price cell [X]"
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **Khi LaunchedEffect Triggered:**
```
🔄 DEBUG: LaunchedEffect triggered - pricingRules: 6, fieldServices: 0, refreshTrigger: 1
🔍 DEBUG: uiState.pricingRules content:
  [0] PricingRule: ruleId=B457auFAESzchvKJN9j1, dayType=WEEKDAY, description=Giá T2 - T6 - 12h - 18h, price=2
  [1] PricingRule: ruleId=BSXVo24w1exhMq40Ufq9, dayType=WEEKDAY, description=Giá T2 - T6 - 5h - 12h, price=1
  ...
```

### **Khi Mapping Từ Firebase:**
```
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🔍 DEBUG: PricingRule từ Firebase:
  - ruleId: B457auFAESzchvKJN9j1
  - fieldId: HNwo0FideMqG7PusJzOd
  - dayType: WEEKDAY
  - description: Giá T2 - T6 - 12h - 18h
  - price: 2
  - minutes: 30
🔄 DEBUG: Mapping rule [0]:
  - Original: dayType=WEEKDAY, description=Giá T2 - T6 - 12h - 18h
  - Mapped: dayOfWeek=T2 - T6, timeSlot=12h - 18h
  - Original Price: 2 (type: Long)
  - Mapped Price: '2' (length: 1)
  - Price > 0: true
  - Price == 0: false
🎯 DEBUG: Tạo CourtPricingRule: CourtPricingRule(id=B457auFAESzchvKJN9j1, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=2, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 12h - 18h, isActive=true)
🔍 DEBUG: CourtPricingRule.price: '2' (isEmpty: false, length: 1)
```

### **Khi Cập Nhật Local State:**
```
🔄 DEBUG: Cập nhật local state với 6 pricing rules
🔍 DEBUG: Trước khi cập nhật local state:
  - localPricingRules.size: 0
  - localPricingRules.isEmpty: true
✅ Đã map 6 pricing rules thành công
🔍 DEBUG: Sau khi cập nhật local state:
  - localPricingRules.size: 6
  - localPricingRules.isEmpty: false
📊 CourtPricingRule [0]: dayOfWeek=T2 - T6, timeSlot=12h - 18h, price='2' (isEmpty: false, length: 1)
📊 CourtPricingRule [1]: dayOfWeek=T2 - T6, timeSlot=5h - 12h, price='1' (isEmpty: false, length: 1)
...
🔍 DEBUG: Kiểm tra local state sau khi cập nhật:
  - localPricingRules.size: 6
  - localPricingRules.isEmpty: false
  - [0] price: '2' (length: 1, isEmpty: false)
  - [1] price: '1' (length: 1, isEmpty: false)
```

### **Khi State Changed:**
```
🔍 DEBUG: pricingRules state changed:
  - size: 6
  - isEmpty: false
  - [0] CourtPricingRule(id=B457auFAESzchvKJN9j1, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=2, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 12h - 18h, isActive=true)
    - price: '2' (isEmpty: false, length: 1)
  - [1] CourtPricingRule(id=BSXVo24w1exhMq40Ufq9, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=1, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 5h - 12h, isActive=true)
    - price: '1' (isEmpty: false, length: 1)
```

### **Khi Render UI:**
```
🔍 DEBUG: Rendering pricing table:
  - pricingRules.size: 6
  - pricingRules.isEmpty: false
  - [0] CourtPricingRule(id=B457auFAESzchvKJN9j1, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=2, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 12h - 18h, isActive=true)
    - price: '2' (isEmpty: false, length: 1)
✅ DEBUG: pricingRules có 6 items, bắt đầu render

🔍 DEBUG: Rendering row [0]: CourtPricingRule(id=B457auFAESzchvKJN9j1, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=2, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 12h - 18h, isActive=true)
  - price: '2' (isEmpty: false, length: 1)

🔍 DEBUG: Rendering price for rule [0]:
  - rule: CourtPricingRule(id=B457auFAESzchvKJN9j1, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=2, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 12h - 18h, isActive=true)
  - rule.price: '2'
  - rule.price.isNotEmpty(): true
  - rule.price.length: 1

🔍 DEBUG: Rendering price cell [0]: price='2', isEmpty=false
```

## 🚨 **Nếu Vẫn Có Vấn Đề**

### **Vấn Đề 1: Không Thấy Logs "LaunchedEffect triggered"**
```
Nguyên nhân: uiState.pricingRules không thay đổi
Giải pháp: Kiểm tra FieldViewModel, Repository
```

### **Vấn Đề 2: Không Thấy Logs "Cập nhật dữ liệu từ Firebase"**
```
Nguyên nhân: updateUIDataFromFirebase không được gọi
Giải pháp: Kiểm tra LaunchedEffect, state management
```

### **Vấn Đề 3: Không Thấy Logs "Mapping rule [X]"**
```
Nguyên nhân: firebasePricingRules trống
Giải pháp: Kiểm tra Firebase connection, data loading
```

### **Vấn Đề 4: Không Thấy Logs "Cập nhật local state"**
```
Nguyên nhân: Mapping logic bị lỗi
Giải pháp: Kiểm tra CourtPricingRule creation
```

### **Vấn Đề 5: Không Thấy Logs "pricingRules state changed"**
```
Nguyên nhân: Local state không được cập nhật
Giải pháp: Kiểm tra SnapshotStateList update
```

### **Vấn Đề 6: Không Thấy Logs "Rendering pricing table"**
```
Nguyên nhân: pricingRules state trống
Giải pháp: Kiểm tra state propagation
```

## 🔧 **Giải Pháp Khẩn Cấp**

### **Nếu Dữ Liệu Không Được Load:**
```kotlin
// Force refresh data
var refreshTrigger by remember { mutableStateOf(0) }

LaunchedEffect(refreshTrigger) {
    loadFieldData(field.fieldId, fieldViewModel)
}

// Thêm nút refresh manual
IconButton(onClick = { refreshTrigger++ }) {
    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
}
```

### **Nếu Mapping Logic Bị Lỗi:**
```kotlin
// Debug mapping logic
val mappedPrice = when {
    rule.price > 0 -> rule.price.toString()
    rule.price == 0L -> ""
    else -> ""
}

println("🔍 DEBUG: Price mapping: ${rule.price} -> '$mappedPrice'")
```

### **Nếu State Management Bị Lỗi:**
```kotlin
// Force state update
var forceUpdate by remember { mutableStateOf(0) }

LaunchedEffect(forceUpdate) {
    // Force UI to re-render
}

// Thêm nút force update
IconButton(onClick = { forceUpdate++ }) {
    Icon(Icons.Default.Refresh, contentDescription = "Force Update")
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
3. **Quan sát cột giá** có hiển thị số không
4. **Kiểm tra Logcat** để xem debug logs
5. **So sánh logs** với logs mong đợi ở trên

## 🎯 **Kết Quả Mong Đợi**

Sau khi khắc phục:
- ✅ LaunchedEffect triggered → Logs "LaunchedEffect triggered" xuất hiện
- ✅ Firebase data loaded → Logs "Cập nhật dữ liệu từ Firebase" xuất hiện
- ✅ Mapping thành công → Logs "Mapping rule [X]" xuất hiện
- ✅ Local state updated → Logs "Cập nhật local state" xuất hiện
- ✅ State changed → Logs "pricingRules state changed" xuất hiện
- ✅ UI render → Logs "Rendering pricing table" xuất hiện
- ✅ Cột giá hiển thị: 2 ₫/30', 1 ₫/30', 3 ₫/30', ...

## 🚀 **Bước Tiếp Theo**

1. **Test ngay lập tức** theo hướng dẫn trên
2. **Monitor Logcat** để xem debug logs
3. **So sánh logs** với logs mong đợi
4. **Cho biết kết quả** và logs nào xuất hiện
5. **Nếu vẫn có vấn đề**, cung cấp logs để debug tiếp

**Hãy test ngay và cho biết kết quả!** 🎯

**Lưu ý**: Debug logs sẽ giúp chúng ta xác định chính xác điểm gây lỗi trong việc hiển thị giá. Bây giờ tôi đã thêm rất nhiều debug logs để theo dõi toàn bộ flow từ khi load dữ liệu từ Firebase đến khi render UI.
