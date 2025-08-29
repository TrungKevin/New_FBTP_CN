# 🔧 Test Model Cải Tiến - Lưu và Hiển Thị Dữ Liệu

## 🎯 **Mục Tiêu Cải Tiến**

**"Chỉnh sửa model để tất cả thông tin được nhập lưu vào Firebase và phải hiển thị lại được ngay bảng"**

## 🔍 **Model Đã Cải Tiến**

### **1. CourtPricingRule (Model UI)**
```kotlin
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
    val isActive: Boolean = true           // Trạng thái hoạt động
)
```

### **2. PricingRule (Model Firebase)**
```kotlin
data class PricingRule(
    val ruleId: String,                    // ID duy nhất của quy tắc giá
    val fieldId: String,                   // ID sân (liên kết với bảng FIELDS)
    val dayType: String,                   // Loại ngày: "WEEKDAY" | "WEEKEND" | "HOLIDAY"
    val slots: Int,                        // Số khe giờ (ví dụ: 2 khe = 1 giờ)
    val minutes: Int,                      // Thời gian mỗi khe (phút)
    val price: Long,                       // Giá tiền/30' trong khoảng thời gian này (VNĐ)
    val calcMode: String,                  // Cách tính: "CEIL_TO_RULE" | "LINEAR"
    val effectiveFrom: Long? = null,       // Thời điểm có hiệu lực từ (timestamp)
    val effectiveTo: Long? = null,         // Thời điểm hết hiệu lực (timestamp)
    val description: String = "",          // Mô tả quy tắc giá
    val isActive: Boolean = true           // Trạng thái hoạt động
)
```

## 🔄 **Flow Mapping Dữ Liệu**

### **1. Từ UI → Firebase (Khi Save)**
```
CourtPricingRule → PricingRule
├── id → ruleId (giữ nguyên nếu có, để trống nếu mới)
├── dayOfWeek → description (T2 - T6 → "Giá T2 - T6 - 5h - 12h")
├── timeSlot → description (5h - 12h → "Giá T2 - T6 - 5h - 12h")
├── price → price (string → long)
├── dayType → dayType (WEEKDAY/WEEKEND)
├── slots → slots (1)
├── minutes → minutes (30)
├── calcMode → calcMode (CEIL_TO_RULE)
├── description → description
└── isActive → isActive (true)
```

### **2. Từ Firebase → UI (Khi Load)**
```
PricingRule → CourtPricingRule
├── ruleId → id
├── dayType → dayOfWeek (WEEKDAY → "T2 - T6", WEEKEND → "T7 - CN")
├── description → timeSlot (parse từ description)
├── price → price (long → string)
├── dayType → dayType
├── slots → slots
├── minutes → minutes
├── calcMode → calcMode
├── description → description
└── isActive → isActive
```

## 🧪 **Test Chi Tiết**

### **Bước 1: Vào CourtService**
```
1. Mở app
2. Vào CourtService component
3. Quan sát cột giá hiện tại (có thể hiển thị "Chưa có giá")
4. Mở Logcat để theo dõi debug logs
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
3. Quan sát Logcat để xem logs "User thay đổi giá"
```

### **Bước 3: Click Save Button**
```
1. Click nút 💾 (Save)
2. Quan sát Logcat để xem logs "Save button được click"
3. Quan sát logs "Trước khi lưu, pricingRules có X items"
4. Quan sát logs "Bắt đầu lưu dữ liệu vào Firebase"
```

### **Bước 4: Quan Sát Quá Trình Lưu**
```
1. Quan sát logs "Tạo PricingRule từ CourtPricingRule"
2. Quan sát logs "Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h"
3. Quan sát logs "Kiểm tra dữ liệu trước khi gửi"
4. Quan sát logs "Pricing rules có giá > 0: 6"
5. Quan sát logs "Gửi lệnh lưu dữ liệu vào Firebase"
6. Quan sát logs "Đã gửi lệnh lưu dữ liệu vào Firebase"
7. Quan sát logs "Đang chờ Firebase xử lý..."
```

### **Bước 5: Quan Sát Kết Quả Firebase**
```
1. Đợi Firebase xử lý (có thể mất vài giây)
2. Quan sát logs từ Firebase:
   - Nếu thành công: "Firebase trả về thành công"
   - Nếu lỗi: "Firebase trả về lỗi"
3. Quan sát logs "Bắt đầu reload data từ Firebase"
4. Quan sát logs "Đã tăng refreshTrigger: X"
```

### **Bước 6: Quan Sát Dữ Liệu Được Load**
```
1. Quan sát logs "LaunchedEffect triggered"
2. Quan sát logs "Cập nhật dữ liệu từ Firebase"
3. Quan sát logs "Pricing Rules từ Firebase: X items"
4. Quan sát logs "Mapping rule [X]"
5. Quan sát logs "Tạo CourtPricingRule"
6. Quan sát logs "Cập nhật local state với X pricing rules"
7. Quan sát logs "Đã map X pricing rules thành công"
```

### **Bước 7: Quan Sát UI Render**
```
1. Quan sát logs "pricingRules state changed"
2. Quan sát logs "Rendering pricing table"
3. Quan sát logs "Rendering row [X]"
4. Quan sát logs "Rendering price for rule [X]"
5. Quan sát logs "Rendering price cell [X]"
6. Quan sát cột giá có hiển thị số không
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **Khi Nhập Giá:**
```
🔍 DEBUG: User thay đổi giá cho rule [0]: '' -> '55000'
💰 DEBUG: Giá mới: '55000' (length: 5, isEmpty: false)
✅ DEBUG: Giá hợp lệ: true
✅ DEBUG: Đã cập nhật pricingRules[0].price = '55000'
```

### **Khi Save:**
```
💾 DEBUG: Save button được click!
📊 DEBUG: Trước khi lưu, pricingRules có 6 items:
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 5h - 12h, isActive=true)
```

### **Khi Tạo PricingRule:**
```
🔍 DEBUG: Tạo PricingRule từ CourtPricingRule: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 5h - 12h, isActive=true)
🔍 DEBUG: Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h
```

### **Khi Load Dữ Liệu:**
```
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🔍 DEBUG: PricingRule từ Firebase:
  - ruleId: rule_123
  - fieldId: field_456
  - dayType: WEEKDAY
  - description: Giá T2 - T6 - 5h - 12h
  - price: 55000
  - minutes: 30
🔄 DEBUG: Mapping rule [0]:
  - Original: dayType=WEEKDAY, description=Giá T2 - T6 - 5h - 12h
  - Mapped: dayOfWeek=T2 - T6, timeSlot=5h - 12h
  - Original Price: 55000
  - Mapped Price: 55000
🎯 DEBUG: Tạo CourtPricingRule: CourtPricingRule(id=rule_123, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000, dayType=WEEKDAY, slots=1, minutes=30, calcMode=CEIL_TO_RULE, description=Giá T2 - T6 - 5h - 12h, isActive=true)
```

## 🎯 **Kết Quả Mong Đợi**

### **Sau Khi Cải Tiến Model:**
- ✅ User nhập giá → Logs "User thay đổi giá" xuất hiện
- ✅ Click Save → Logs "Save button được click" xuất hiện
- ✅ Tạo PricingRule → Logs "Tạo PricingRule từ CourtPricingRule" xuất hiện
- ✅ Lưu Firebase → Logs "Firebase trả về thành công" xuất hiện
- ✅ Load dữ liệu → Logs "Cập nhật dữ liệu từ Firebase" xuất hiện
- ✅ Mapping thành công → Logs "Tạo CourtPricingRule" xuất hiện
- ✅ UI render → Logs "Rendering pricing table" xuất hiện
- ✅ Cột giá hiển thị: 55000 ₫/30', 60000 ₫/30', 70000 ₫/30', ...

### **Dữ Liệu Được Lưu Đầy Đủ:**
- ✅ `ruleId`: Giữ nguyên nếu có, để trống nếu mới
- ✅ `fieldId`: ID sân chính xác
- ✅ `dayType`: WEEKDAY/WEEKEND chính xác
- ✅ `slots`: 1 (mỗi khe 30 phút)
- ✅ `minutes`: 30 (30 phút mỗi khe)
- ✅ `price`: Giá tiền chính xác
- ✅ `calcMode`: CEIL_TO_RULE
- ✅ `description`: Mô tả chi tiết (Giá T2 - T6 - 5h - 12h)
- ✅ `isActive`: true

## 🚀 **Bước Tiếp Theo**

1. **Test ngay lập tức** theo hướng dẫn trên
2. **Monitor Logcat** để xem debug logs
3. **Cho biết kết quả** và logs nào xuất hiện
4. **Nếu vẫn có vấn đề**, cung cấp logs để debug tiếp

**Hãy test ngay và cho biết kết quả!** 🎯

**Lưu ý**: Bây giờ model đã được cải tiến để có đầy đủ thông tin mapping giữa UI và Firebase. Điều này sẽ đảm bảo dữ liệu được lưu và hiển thị chính xác.
