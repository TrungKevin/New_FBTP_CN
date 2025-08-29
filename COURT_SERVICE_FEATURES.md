# 🏟️ CourtService - Chức Năng Quản Lý Bảng Giá & Dịch Vụ

## 📋 Tổng Quan

`CourtService` là component hoàn chỉnh cho phép **Owner** quản lý bảng giá sân và danh sách dịch vụ của từng sân thể thao. Component này hỗ trợ đầy đủ các thao tác CRUD (Create, Read, Update, Delete).

## ✨ Tính Năng Chính

### 1. 🎯 **Quản Lý Bảng Giá Sân**
- **Hiển thị**: Bảng giá theo ngày (T2-T6, T7-CN) và khung giờ (5h-12h, 12h-18h, 18h-24h)
- **Chỉnh sửa**: Owner có thể thay đổi khung giờ và giá tiền
- **Lưu trữ**: Dữ liệu được lưu vào Firebase với cấu trúc `PricingRule`

### 2. 🛍️ **Quản Lý Danh Sách Dịch Vụ**
- **Phân loại**: Dịch vụ được nhóm theo danh mục (Banh, Nước đóng chai, Phí Thuê Vợt, Dịch vụ khác)
- **Thêm mới**: Owner có thể thêm dịch vụ mới với dialog
- **Chỉnh sửa**: Sửa tên và giá dịch vụ
- **Xóa**: Xóa dịch vụ không cần thiết

### 3. 🔄 **Chế Độ Chỉnh Sửa**
- **Edit Mode**: Click nút ✏️ để vào chế độ chỉnh sửa
- **Save**: Click nút 💾 để lưu thay đổi
- **Cancel**: Click nút ❌ để hủy thay đổi và reload dữ liệu gốc

## 🎮 Cách Sử Dụng

### **Bước 1: Xem Dữ Liệu**
- Component tự động load dữ liệu từ Firebase khi khởi tạo
- Hiển thị bảng giá và danh sách dịch vụ hiện tại

### **Bước 2: Vào Chế Độ Chỉnh Sửa**
- Click nút ✏️ (Edit) ở góc phải header
- Giao diện chuyển sang edit mode với các input fields

### **Bước 3: Chỉnh Sửa Bảng Giá**
- **Khung giờ**: Click vào ô khung giờ để chỉnh sửa
- **Giá**: Click vào ô giá để nhập giá mới (giá/30 phút)

### **Bước 4: Quản Lý Dịch Vụ**
- **Thêm dịch vụ**: Click nút ➕ để mở dialog thêm dịch vụ mới
- **Chỉnh sửa**: Click vào tên hoặc giá dịch vụ để sửa
- **Xóa**: Click nút 🗑️ để xóa dịch vụ

### **Bước 5: Lưu Thay Đổi**
- Click nút 💾 (Save) để lưu tất cả thay đổi
- Dữ liệu được gửi lên Firebase
- Tự động thoát edit mode khi lưu thành công

## 🗄️ Cấu Trúc Dữ Liệu

### **PricingRule (Bảng Giá)**
```kotlin
data class PricingRule(
    val ruleId: String,           // ID tự động tạo
    val fieldId: String,          // ID sân
    val dayType: String,          // "WEEKDAY" | "WEEKEND"
    val slots: Int,               // Số khe giờ
    val minutes: Int,             // 30 phút mỗi khe
    val price: Long,              // Tổng giá cho khung giờ
    val calcMode: String,         // "CEIL_TO_RULE"
    val description: String       // Mô tả khung giờ
)
```

### **FieldService (Dịch Vụ)**
```kotlin
data class FieldService(
    val fieldServiceId: String,   // ID tự động tạo
    val fieldId: String,          // ID sân
    val name: String,             // Tên dịch vụ
    val price: Long,              // Giá dịch vụ
    val billingType: String,      // "PER_UNIT" | "FLAT_PER_BOOKING"
    val allowQuantity: Boolean,   // Cho phép chọn số lượng
    val description: String       // Mô tả dịch vụ
)
```

## 🔧 Tính Năng Kỹ Thuật

### **1. Auto-Save & Sync**
- Dữ liệu được lưu vào Firebase real-time
- Tự động reload khi có thay đổi
- Xử lý lỗi và hiển thị thông báo

### **2. Validation & Error Handling**
- Kiểm tra dữ liệu trước khi lưu
- Xử lý lỗi mạng và Firebase
- Hiển thị loading state khi đang lưu

### **3. Responsive UI**
- Giao diện thích ứng với mọi kích thước màn hình
- Input fields với placeholder text
- Visual feedback cho các thao tác

### **4. Performance Optimization**
- Lazy loading dữ liệu
- Efficient state management
- Minimal re-renders

## 📱 Giao Diện Người Dùng

### **Header Section**
```
┌─────────────────────────────────────┐
│ BẢNG GIÁ & DỊCH VỤ          [✏️]   │
└─────────────────────────────────────┘
```

### **Bảng Giá Sân**
```
┌─────────┬────────────┬──────────┐
│  Thứ    │ Khung giờ  │  Giá (₫) │
├─────────┼────────────┼──────────┤
│ T2 - T6 │  5h - 12h  │   50000   │
│ T2 - T6 │ 12h - 18h  │   60000   │
│ T2 - T6 │ 18h - 24h  │   70000   │
│ T7 - CN │  5h - 12h  │   80000   │
│ T7 - CN │ 12h - 18h  │   90000   │
│ T7 - CN │ 18h - 24h  │  100000   │
└─────────┴────────────┴──────────┘
```

### **Danh Sách Dịch Vụ**
```
┌─────────────────────────────────────┐
│ DANH SÁCH DỊCH VỤ            [➕]   │
├─────────────────────────────────────┤
│ Banh                                │
│ ├─ [Tên] [Giá] [🗑️]               │
│ └─ [Tên] [Giá] [🗑️]               │
│                                     │
│ Nước đóng chai                      │
│ ├─ Sting        12000 ₫            │
│ ├─ Revie        15000 ₫            │
│ └─ [Tên] [Giá] [🗑️]               │
│                                     │
│ Phí Thuê Vợt                       │
│ └─ [Tên] [Giá] [🗑️]               │
└─────────────────────────────────────┘
```

## 🚀 Tương Lai

### **Tính Năng Dự Kiến**
- [ ] Drag & Drop để sắp xếp dịch vụ
- [ ] Import/Export dữ liệu từ Excel
- [ ] Lịch sử thay đổi giá
- [ ] Thông báo khi giá thay đổi
- [ ] So sánh giá với sân khác

### **Cải Tiến UI/UX**
- [ ] Dark mode support
- [ ] Animation cho các thao tác
- [ ] Undo/Redo functionality
- [ ] Bulk edit operations

## 📞 Hỗ Trợ

Nếu gặp vấn đề hoặc cần hỗ trợ:
1. Kiểm tra log trong Android Studio
2. Xác nhận kết nối Firebase
3. Kiểm tra quyền truy cập dữ liệu

---

**Phiên bản**: 1.0.0  
**Cập nhật**: 2024-08-28  
**Tác giả**: FBTP Development Team
