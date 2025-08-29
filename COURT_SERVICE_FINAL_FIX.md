# 🎯 **CourtService - Sửa Lỗi Hiển Thị Giá Cuối Cùng**

## 🚨 **Vấn Đề Đã Sửa**

**Trước đây**: Khi nhập giá và lưu vào Firebase, dữ liệu được lưu thành công nhưng không hiển thị lại trên UI. Luôn hiển thị "Chưa có giá".

**Nguyên nhân chính**: 
1. **Logic mapping sai**: Code bỏ qua giá = 0 với điều kiện `if (rule.price > 0)`
2. **Template trống được tạo lại**: Mỗi lần reload đều tạo template mới, mất dữ liệu đã nhập
3. **State management không đồng bộ**: Local state không được cập nhật đúng cách từ Firebase

## ✅ **Giải Pháp Đã Áp Dụng**

### **1. Sửa Logic Mapping**
```kotlin
// ❌ TRƯỚC: Bỏ qua giá = 0
val priceToSet = if (rule.price > 0) rule.price.toString() else ""

// ✅ SAU: Luôn lấy giá từ Firebase
val priceToSet = rule.price.toString()
```

### **2. Sửa Template Logic**
```kotlin
// ❌ TRƯỚC: Tạo template mới mỗi lần reload
localPricingRules.clear()
localPricingRules.addAll(createEmptyPricingRules())

// ✅ SAU: Sử dụng template cố định và cập nhật dữ liệu
val templateRules = createEmptyPricingRules().toMutableList()
// Map dữ liệu từ Firebase vào template
firebasePricingRules.forEach { rule ->
    // Tìm template rule tương ứng và cập nhật giá
    val templateIndex = templateRules.indexOfFirst { ... }
    if (templateIndex != -1) {
        templateRules[templateIndex] = templateRules[templateIndex].copy(
            price = rule.price.toString() // Luôn lấy giá, không bỏ qua
        )
    }
}
```

### **3. Sửa State Management**
```kotlin
// ✅ Cập nhật local state với template đã được cập nhật
localPricingRules.clear()
localPricingRules.addAll(templateRules)
```

## 🔧 **Các Thay Đổi Chính**

### **1. Model Mới**
- **`CourtPricingRule`**: Model UI cho bảng giá, dễ hiển thị và chỉnh sửa
- **`CourtServiceItem`**: Model UI cho dịch vụ, dễ quản lý

### **2. Logic Mapping Hoàn Chỉnh**
- **Template cố định**: 6 khung giờ (T2-T6 × 3, T7-CN × 3)
- **Mapping chính xác**: Dữ liệu từ Firebase được map vào đúng vị trí template
- **Không bỏ sót giá**: Tất cả giá từ Firebase đều được hiển thị

### **3. UI/UX Cải Thiện**
- **Bảng giá**: Hiển thị đầy đủ 6 khung giờ với giá thực tế
- **Bảng dịch vụ**: Phân loại theo danh mục (Banh, Nước, Vợt, Khác)
- **Edit mode**: Chỉnh sửa trực tiếp trên UI
- **Validation**: Kiểm tra dữ liệu trước khi lưu

## 🧪 **Test Cases**

### **Test Case 1: Nhập Giá Mới**
1. Mở app → Đăng nhập owner → Vào sân → Chọn "Bảng giá & Dịch vụ"
2. Click "Chỉnh sửa" → Nhập giá cho T2-T6 5h-12h: "50000"
3. Click "Lưu" → Đợi Firebase xử lý
4. **Kết quả mong đợi**: Giá "50000 ₫" hiển thị thay vì "Chưa có giá"

### **Test Case 2: Sửa Giá Cũ**
1. Trong edit mode → Sửa giá T2-T6 5h-12h từ "50000" thành "60000"
2. Click "Lưu" → Đợi Firebase xử lý
3. **Kết quả mong đợi**: Giá "60000 ₫" hiển thị chính xác

### **Test Case 3: Thêm Dịch Vụ Mới**
1. Trong edit mode → Thêm dịch vụ mới vào danh mục "Banh"
2. Nhập tên: "Banh Wilson" → Giá: "15000"
3. Click "Lưu" → Đợi Firebase xử lý
4. **Kết quả mong đợi**: Dịch vụ mới hiển thị trong danh mục "Banh"

## 🔍 **Debug Logs**

Khi test, bạn sẽ thấy các log sau trong console:

```
🚀 DEBUG: Bắt đầu load data cho field: [fieldId]
🔄 DEBUG: Loading field data for fieldId: [fieldId]
✅ DEBUG: Đã gửi lệnh load dữ liệu từ Firebase
🔄 DEBUG: LaunchedEffect triggered - pricingRules: X, fieldServices: Y, refreshTrigger: Z
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: X items
🛍️ Field Services từ Firebase: Y items
✅ Có dữ liệu pricing rules, mapping...
🔍 DEBUG: Xử lý rule: [ruleId] - [description] - Giá: [price]
🔄 Mapping: [minutes] phút -> [timeSlot], [dayType] -> [dayOfWeek]
💰 Giá từ Firebase: [price]
✅ Cập nhật template rule [index] với giá: '[price]' (rule.price: [price])
✅ Đã map X pricing rules thành công
💰 DEBUG: Pricing rules có giá: X/Y
  💰 [index] Giá: '[price]' - [dayOfWeek] - [timeSlot]
```

## 🎉 **Kết Quả Cuối Cùng**

### ✅ **Đã Sửa Xong:**
1. **Giá hiển thị chính xác** sau khi lưu vào Firebase
2. **Bảng dịch vụ đầy đủ** với 4 danh mục
3. **Template cố định** 6 khung giờ không bị mất dữ liệu
4. **State management đồng bộ** giữa Firebase và UI
5. **Validation system** hoàn chỉnh
6. **Debug logs** chi tiết để troubleshooting

### 🚀 **Chức Năng Hoạt Động:**
- ✅ **Create**: Nhập giá mới cho các khung giờ
- ✅ **Read**: Hiển thị giá từ Firebase chính xác
- ✅ **Update**: Sửa giá đã có
- ✅ **Delete**: Xóa dịch vụ không cần thiết
- ✅ **Real-time sync**: Tự động cập nhật UI sau khi lưu

## 📱 **Hướng Dẫn Sử Dụng**

1. **Xem bảng giá**: Mở sân → Chọn "Bảng giá & Dịch vụ"
2. **Chỉnh sửa**: Click nút "Chỉnh sửa" (biểu tượng bút chì)
3. **Nhập giá**: Click vào ô giá và nhập số tiền
4. **Lưu**: Click nút "Lưu" (biểu tượng đĩa)
5. **Hủy**: Click nút "Hủy" (biểu tượng X) để quay lại

## 🔧 **Troubleshooting**

### **Nếu vẫn không hiển thị giá:**
1. Kiểm tra console logs để xem có lỗi gì không
2. Đảm bảo Firebase connection hoạt động
3. Kiểm tra fieldId có đúng không
4. Thử refresh bằng nút "Làm mới"

### **Nếu có lỗi validation:**
1. Đảm bảo giá là số dương > 0
2. Không để trống tên dịch vụ
3. Kiểm tra format giá (chỉ số, không có ký tự đặc biệt)

---

**🎯 Tóm lại**: Vấn đề hiển thị giá đã được sửa hoàn toàn. CourtService giờ đây hoạt động chính xác với CRUD operations hoàn chỉnh và hiển thị dữ liệu real-time từ Firebase.
