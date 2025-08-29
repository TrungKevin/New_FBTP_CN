# 🏟️ CourtService - Hệ Thống CRUD Hoàn Chỉnh Cho Owner

## 🎯 **Tổng Quan**

`CourtService` đã được tối ưu hóa để trở thành một hệ thống CRUD (Create, Read, Update, Delete) hoàn chỉnh cho phép **Owner** quản lý bảng giá sân và danh sách dịch vụ một cách dễ dàng và trực quan.

## ✨ **Tính Năng Mới**

### **1. 🔄 Nút Refresh (Làm Mới)**
- **Vị trí**: Góc phải header, bên cạnh nút Edit
- **Chức năng**: Làm mới dữ liệu từ Firebase
- **Khi nào dùng**: Khi muốn đồng bộ dữ liệu mới nhất từ server

### **2. 🎨 Model Mới Cho UI**
- **`CourtPricingRule`**: Model tối ưu cho bảng giá
- **`CourtServiceItem`**: Model tối ưu cho dịch vụ
- **Ưu điểm**: Dễ hiển thị, dễ chỉnh sửa, tách biệt khỏi model Firebase

### **3. 🧹 Code Cleaner**
- Tách biệt logic UI và business logic
- Helper functions rõ ràng, dễ bảo trì
- Xử lý state management tốt hơn

## 🎮 **Cách Sử Dụng**

### **Bước 1: Xem Dữ Liệu Hiện Tại**
```
1. Vào CourtService component
2. Dữ liệu tự động load từ Firebase
3. Hiển thị bảng giá và danh sách dịch vụ
4. Nếu chưa có dữ liệu → hiển thị template trống
```

### **Bước 2: Làm Mới Dữ Liệu**
```
1. Click nút 🔄 (Refresh) 
2. Dữ liệu được reload từ Firebase
3. UI tự động cập nhật
```

### **Bước 3: Vào Chế Độ Chỉnh Sửa**
```
1. Click nút ✏️ (Edit)
2. Giao diện chuyển sang edit mode
3. Các input fields xuất hiện
```

### **Bước 4: Chỉnh Sửa Bảng Giá**
```
1. **Khung giờ**: Click vào ô khung giờ để sửa
2. **Giá**: Click vào ô giá để nhập giá mới
3. **Format giá**: Nhập số (ví dụ: 55000, 60000, 70000)
4. **Đơn vị**: Giá được tính theo ₫/30 phút
```

### **Bước 5: Quản Lý Dịch Vụ**
```
1. **Thêm dịch vụ**: Click nút ➕ để mở dialog
2. **Chỉnh sửa**: Click vào tên hoặc giá để sửa
3. **Xóa**: Click nút 🗑️ để xóa dịch vụ
```

### **Bước 6: Lưu Thay Đổi**
```
1. Click nút 💾 (Save)
2. Loading dialog hiển thị
3. Dữ liệu được gửi lên Firebase
4. Tự động thoát edit mode khi thành công
5. UI hiển thị dữ liệu mới
```

## 🗄️ **Cấu Trúc Dữ Liệu**

### **CourtPricingRule (UI Model)**
```kotlin
data class CourtPricingRule(
    val id: String = "",           // ID từ Firebase
    val dayOfWeek: String = "",    // "T2 - T6" | "T7 - CN"
    val timeSlot: String = "",     // "5h - 12h" | "12h - 18h" | "18h - 24h"
    val price: String = ""         // Giá dạng string (dễ edit)
)
```

### **CourtServiceItem (UI Model)**
```kotlin
data class CourtServiceItem(
    val id: String = "",           // ID từ Firebase
    val name: String = "",         // Tên dịch vụ
    val price: String = "",        // Giá dạng string (dễ edit)
    val category: String = ""      // Danh mục dịch vụ
)
```

### **Mapping Với Firebase Models**
```kotlin
// PricingRule → CourtPricingRule
PricingRule(
    dayType = "WEEKDAY" → dayOfWeek = "T2 - T6"
    description = "Giá T2 - T6 - 5h - 12h" → timeSlot = "5h - 12h"
    price = 55000L → price = "55000"
)

// FieldService → CourtServiceItem
FieldService(
    name = "Sting" → name = "Sting"
    price = 12000L → price = "12000"
    billingType = "PER_UNIT" → category = "Nước đóng chai"
)
```

## 🔧 **Logic Hoạt Động**

### **1. Load Data Flow**
```
LaunchedEffect(field.fieldId) 
    ↓
loadFieldData(fieldId, fieldViewModel)
    ↓
FieldEvent.LoadPricingRulesByFieldId(fieldId)
FieldEvent.LoadFieldServicesByFieldId(fieldId)
    ↓
Firebase trả về data
    ↓
updateUIDataFromFirebase() 
    ↓
UI được cập nhật
```

### **2. Save Data Flow**
```
Click Save Button
    ↓
saveData(fieldId, pricingRules, services, fieldViewModel)
    ↓
Convert UI models → Firebase models
    ↓
FieldEvent.UpdateFieldPricingAndServices()
    ↓
Firebase lưu data
    ↓
uiState.success được set
    ↓
LaunchedEffect(uiState.success) trigger
    ↓
Reload data từ Firebase
    ↓
UI hiển thị data mới
```

### **3. Edit Mode Flow**
```
Click Edit Button
    ↓
isEditMode = true
    ↓
UI hiển thị input fields
    ↓
User chỉnh sửa data
    ↓
Local state được cập nhật
    ↓
Click Save → Lưu vào Firebase
    ↓
Click Cancel → Reload data gốc
```

## 📊 **Template Dữ Liệu Mặc Định**

### **Bảng Giá (6 khung giờ)**
```
┌─────────┬─────────────┬─────────────┐
│ Thứ     │ Khung giờ   │ Giá (₫/30')│
├─────────┼─────────────┼─────────────┤
│ T2 - T6 │ 5h - 12h    │ [trống]    │
│ T2 - T6 │ 12h - 18h   │ [trống]    │
│ T2 - T6 │ 18h - 24h   │ [trống]    │
│ T7 - CN │ 5h - 12h    │ [trống]    │
│ T7 - CN │ 12h - 18h   │ [trống]    │
│ T7 - CN │ 18h - 24h   │ [trống]    │
└─────────┴─────────────┴─────────────┘
```

### **Dịch Vụ (4 danh mục)**
```
📦 Banh
   ├─ [trống] - [trống]
   └─ [trống] - [trống]

🥤 Nước đóng chai
   ├─ Sting - 12000 ₫
   ├─ Revie - 15000 ₫
   └─ [trống] - [trống]

🏸 Phí Thuê Vợt
   └─ [trống] - [trống]

🔧 Dịch vụ khác
   └─ [trống] - [trống]
```

## 🧪 **Test Cases**

### **Test Case 1: Tạo Bảng Giá Mới**
```
1. Vào edit mode
2. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
3. Click Save
4. Kiểm tra: UI hiển thị giá mới
```

### **Test Case 2: Thêm Dịch Vụ Mới**
```
1. Vào edit mode
2. Click nút ➕
3. Nhập: Tên = "Banh tennis", Giá = "180000"
4. Click "Thêm"
5. Kiểm tra: Dịch vụ xuất hiện trong danh mục "Banh"
```

### **Test Case 3: Chỉnh Sửa Dịch Vụ**
```
1. Vào edit mode
2. Click vào tên dịch vụ "Sting"
3. Sửa thành "Sting Energy"
4. Click Save
5. Kiểm tra: Tên được cập nhật
```

### **Test Case 4: Xóa Dịch Vụ**
```
1. Vào edit mode
2. Click nút 🗑️ bên cạnh dịch vụ
3. Click Save
4. Kiểm tra: Dịch vụ biến mất
```

### **Test Case 5: Refresh Data**
```
1. Click nút 🔄
2. Kiểm tra: Dữ liệu được reload từ Firebase
3. Kiểm tra: UI hiển thị dữ liệu mới nhất
```

## 🚨 **Xử Lý Lỗi**

### **1. Lỗi Mạng**
```
- Hiển thị error message trong uiState.error
- User có thể retry bằng nút refresh
```

### **2. Lỗi Validation**
```
- Kiểm tra dữ liệu trước khi gửi lên Firebase
- Chỉ lưu các rule có giá hợp lệ
- Chỉ lưu các service có tên và giá
```

### **3. Lỗi Firebase**
```
- Hiển thị error message cụ thể
- Tự động retry khi cần thiết
```

## 🔍 **Debug Logs**

### **Load Data**
```
🚀 DEBUG: Bắt đầu load data cho field: field_001
🔄 DEBUG: Loading field data for fieldId: field_001
🔄 DEBUG: LaunchedEffect triggered
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🛍️ Field Services từ Firebase: 3 items
✅ Có dữ liệu pricing rules, mapping...
🔄 Mapping: 30 phút -> 5h - 12h, WEEKDAY -> T2 - T6
💰 Giá từ Firebase: 55000
🎯 Tạo CourtPricingRule: CourtPricingRule(...)
✅ Đã map 6 pricing rules thành công
```

### **Save Data**
```
💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase
📊 Pricing Rules sẽ lưu: 6 items
  [0] PricingRule(...)
  [1] PricingRule(...)
🛍️ Field Services sẽ lưu: 3 items
  [0] FieldService(...)
✅ Đã gửi lệnh lưu dữ liệu vào Firebase
✅ Cập nhật bảng giá và dịch vụ thành công!
🔄 DEBUG: Loading field data for fieldId: field_001
```

## 🎉 **Kết Luận**

CourtService mới cung cấp:

✅ **Hệ thống CRUD hoàn chỉnh**
✅ **Giao diện thân thiện với owner**
✅ **Model tối ưu cho UI**
✅ **Xử lý state management tốt**
✅ **Debug logs chi tiết**
✅ **Error handling đầy đủ**
✅ **Auto-sync với Firebase**
✅ **Performance tối ưu**

Owner giờ đây có thể dễ dàng quản lý bảng giá và dịch vụ của sân một cách trực quan và hiệu quả! 🚀
