# 🧪 **CourtService & FieldServiceManager - Test Cải Thiện**

## 🎯 **Mục Tiêu Test**

Kiểm tra các cải thiện đã thực hiện cho:
1. **Bảng giá sân**: Hiển thị giá chính xác sau khi nhập
2. **Dịch vụ bổ sung**: Hiển thị đúng theo từng sân

## 🔧 **Các Cải Thiện Đã Thực Hiện**

### **1. Bảng Giá Sân (CourtService)**

#### **✅ Logic Mapping Cải Thiện**
- Tạo hàm `mapFirebaseRuleToUI()` riêng biệt
- Mapping chính xác dựa trên `dayType` và `description`
- Fallback mapping dựa trên `minutes` nếu description không khớp

#### **✅ Logic Hiển Thị Cải Thiện**
- Normalize strings khi tìm kiếm rule
- Force new instance khi cập nhật state
- Unique ID cho rule mới

#### **✅ Logic Lưu Dữ Liệu Cải Thiện**
- Debug log chi tiết cho fieldId
- Kiểm tra dữ liệu trước khi gửi
- Validation đầy đủ

### **2. Dịch Vụ Bổ Sung (FieldServiceManager)**

#### **✅ Logic Lọc Theo Sân**
- Lọc dịch vụ theo `fieldId` trước khi mapping
- Chỉ hiển thị dịch vụ của sân hiện tại
- Debug log cho việc lọc

#### **✅ Logic Lưu Dữ Liệu**
- Đảm bảo `fieldId` được gán đúng
- Debug log chi tiết cho fieldId
- Validation đầy đủ

## 🧪 **Test Cases**

### **Test Case 1: Bảng Giá Sân**

#### **Bước 1: Vào CourtService**
1. Mở app
2. Vào Owner Field Detail Screen
3. Vào CourtService component

#### **Bước 2: Kiểm Tra Hiển Thị Giá**
1. Nhập giá cho các khung giờ khác nhau
2. Kiểm tra giá có hiển thị đúng không
3. Kiểm tra debug log trong console

#### **Bước 3: Lưu Dữ Liệu**
1. Click Save button
2. Kiểm tra debug log trong console
3. Kiểm tra dữ liệu có được lưu vào Firebase không

#### **Bước 4: Reload Dữ Liệu**
1. Refresh page hoặc reload data
2. Kiểm tra giá có hiển thị đúng không
3. Kiểm tra debug log trong console

### **Test Case 2: Dịch Vụ Bổ Sung**

#### **Bước 1: Vào FieldServiceManager**
1. Trong CourtService, scroll xuống phần dịch vụ
2. Kiểm tra FieldServiceManager component

#### **Bước 2: Kiểm Tra Hiển Thị Dịch Vụ**
1. Kiểm tra chỉ hiển thị dịch vụ của sân hiện tại
2. Kiểm tra debug log trong console
3. Kiểm tra fieldId trong log

#### **Bước 3: Thêm/Sửa Dịch Vụ**
1. Thêm dịch vụ mới
2. Sửa giá dịch vụ
3. Kiểm tra debug log trong console

#### **Bước 4: Lưu Dịch Vụ**
1. Click "Lưu Dịch Vụ" button
2. Kiểm tra debug log trong console
3. Kiểm tra fieldId có được gán đúng không

## 🔍 **Debug Logs Cần Kiểm Tra**

### **CourtService**
```
🚀 DEBUG: Bắt đầu load data cho field: [fieldId]
🔄 DEBUG: LaunchedEffect triggered - pricingRules: X, fieldServices: Y
🔍 DEBUG: Kết quả mapping:
  [0] T2 - T6 - 5h - 12h: '50000' (isEmpty: false)
💰 DEBUG: Pricing rules có giá > 0: X
```

### **FieldServiceManager**
```
🏟️ DEBUG: FieldServiceManager - Dịch vụ của sân [fieldId]: X items
💾 DEBUG: FieldServiceManager - Bắt đầu lưu dịch vụ vào Firebase
🏟️ Field ID: [fieldId]
✅ DEBUG: FieldServiceManager - Đã gửi lệnh lưu dịch vụ vào Firebase cho field: [fieldId]
```

## ✅ **Kết Quả Mong Đợi**

### **Bảng Giá Sân**
- ✅ Giá được hiển thị chính xác sau khi nhập
- ✅ Giá được lưu đúng vào Firebase
- ✅ Giá được load lại chính xác sau khi reload
- ✅ Debug log hiển thị đầy đủ thông tin

### **Dịch Vụ Bổ Sung**
- ✅ Chỉ hiển thị dịch vụ của sân hiện tại
- ✅ Dịch vụ được lưu với fieldId đúng
- ✅ Dữ liệu được phân tách rõ ràng theo từng sân
- ✅ Debug log hiển thị fieldId chính xác

## 🚨 **Vấn Đề Cần Chú Ý**

1. **String Normalization**: Đảm bảo strings được so sánh chính xác
2. **State Management**: Đảm bảo state được cập nhật đúng cách
3. **FieldId Mapping**: Đảm bảo fieldId được gán đúng cho tất cả dữ liệu
4. **Debug Logs**: Kiểm tra logs để xác định vấn đề

## 📝 **Ghi Chú Test**

- Test trên nhiều sân khác nhau
- Test với các giá trị khác nhau (0, số dương, số lớn)
- Test với các loại dịch vụ khác nhau
- Kiểm tra console logs để debug

## 🎯 **Kết Luận**

Sau khi hoàn thành test cases này, bạn sẽ có thể xác định:
1. Bảng giá sân có hiển thị giá chính xác không
2. Dịch vụ bổ sung có hiển thị đúng theo từng sân không
3. Dữ liệu có được lưu và load chính xác không

Hãy chạy test theo hướng dẫn và report kết quả! 🚀
