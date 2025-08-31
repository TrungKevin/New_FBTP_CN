# FieldServiceManager - Quản lý Dịch vụ Sân

## Tổng quan
`FieldServiceManager` là một Composable riêng biệt được tạo ra để quản lý CRUD cho bảng dịch vụ bổ sung của sân. Nó được tích hợp vào `CourtService` và hoạt động độc lập với phần bảng giá sân.

## Tính năng chính

### ✅ **3 Danh mục Dịch vụ**
1. **Nước đóng chai** - Dịch vụ theo đơn vị (PER_UNIT)
2. **Thuê dụng cụ** - Dịch vụ theo lần đặt sân (FLAT_PER_BOOKING)  
3. **Dịch vụ khác** - Dịch vụ theo đơn vị (PER_UNIT)

### ✅ **CRUD Operations**
- **Create**: Thêm dịch vụ mới với tên và giá
- **Read**: Hiển thị danh sách dịch vụ từ Firebase
- **Update**: Chỉnh sửa tên và giá dịch vụ
- **Delete**: Xóa dịch vụ không cần thiết

### ✅ **Tích hợp Firebase**
- Lưu trữ vào collection `FIELD_SERVICES`
- Mapping chính xác giữa UI và Firebase data
- Real-time updates khi có thay đổi

## Cách sử dụng

### 1. Import vào CourtService
```kotlin
import com.trungkien.fbtp_cn.ui.components.owner.info.FieldServiceManager
```

### 2. Thay thế phần dịch vụ cũ
```kotlin
// Thay vì code dịch vụ cũ dài dòng
// Sử dụng FieldServiceManager đơn giản:

FieldServiceManager(
    fieldId = field.fieldId,
    fieldViewModel = fieldViewModel,
    isEditMode = isEditMode
)
```

### 3. Truyền isEditMode
- `isEditMode = true`: Hiển thị TextField để chỉnh sửa
- `isEditMode = false`: Chỉ hiển thị thông tin (view mode)

## Cấu trúc File

### FieldServiceManager.kt
- **Model**: `FieldServiceItem` - Dễ hiển thị và chỉnh sửa
- **Composable chính**: `FieldServiceManager`
- **Composable phụ**: `ServiceRow`, `AddServiceRow`
- **Helper functions**: Mapping, validation, save

### CourtService.kt
- Đã được cập nhật để sử dụng `FieldServiceManager`
- Loại bỏ code dịch vụ cũ không cần thiết
- Tập trung vào bảng giá sân

## Workflow

### 1. **Load Data**
```kotlin
// Tự động load từ Firebase khi component mount
LaunchedEffect(fieldId, refreshTrigger) {
    loadFieldServices(fieldId, fieldViewModel)
}
```

### 2. **Edit Mode**
- Owner click nút Edit
- Hiển thị TextField cho tên và giá
- Có thể thêm/xóa dịch vụ
- Validation trước khi lưu

### 3. **Save to Firebase**
```kotlin
// Lưu vào Firebase FieldService collection
fieldViewModel.handleEvent(FieldEvent.UpdateFieldServices(fieldId, newFieldServices))
```

### 4. **Real-time Update**
- Tự động reload data sau khi save thành công
- Hiển thị thông báo thành công/lỗi
- UI cập nhật ngay lập tức

## Mapping Logic

### Firebase → UI
```kotlin
val mappedCategory = when (service.billingType) {
    "PER_UNIT" -> when {
        service.name.contains("Nước", ignoreCase = true) -> "Nước đóng chai"
        service.name.contains("Vợt", ignoreCase = true) -> "Thuê dụng cụ"
        else -> "Dịch vụ khác"
    }
    "FLAT_PER_BOOKING" -> "Thuê dụng cụ"
    else -> "Dịch vụ khác"
}
```

### UI → Firebase
```kotlin
val billingType = when (service.category) {
    "Nước đóng chai" -> "PER_UNIT"
    "Thuê dụng cụ" -> "FLAT_PER_BOOKING"
    "Dịch vụ khác" -> "PER_UNIT"
}
```

## Validation Rules

### Dịch vụ hợp lệ phải có:
1. **Tên dịch vụ**: Không được để trống
2. **Giá**: Phải là số > 0
3. **Trạng thái**: `isActive = true`

### Error Messages:
- "Giá không được để trống cho dịch vụ: [tên]"
- "Giá không hợp lệ cho dịch vụ [tên]: [giá]"
- "Giá phải lớn hơn 0 cho dịch vụ: [tên]"
- "Vui lòng nhập ít nhất một dịch vụ"

## Template Mẫu

### Nước đóng chai
- Sting: 12,000 ₫
- Revie: 15,000 ₫
- [Trống để owner điền]

### Thuê dụng cụ
- [Trống để owner điền]
- [Trống để owner điền]

### Dịch vụ khác
- [Trống để owner điền]
- [Trống để owner điền]

## Lợi ích

### ✅ **Tách biệt concerns**
- Bảng giá sân và dịch vụ hoạt động độc lập
- Dễ maintain và debug

### ✅ **Tái sử dụng**
- `FieldServiceManager` có thể dùng ở nơi khác
- Logic CRUD được đóng gói hoàn chỉnh

### ✅ **Dễ mở rộng**
- Thêm danh mục mới dễ dàng
- Thay đổi validation rules đơn giản

### ✅ **Performance**
- Chỉ reload phần dịch vụ khi cần thiết
- Không ảnh hưởng đến bảng giá sân

## Troubleshooting

### Lỗi thường gặp:
1. **"services không được resolve"** → Đã xóa references cũ
2. **"BasicTextField không có placeholder"** → Đã sửa parameter
3. **"UpdateFieldServices không tồn tại"** → Đã thêm vào FieldEvent

### Debug:
- Sử dụng `println` statements trong code
- Kiểm tra Firebase console
- Xem logcat trong Android Studio

## Kết luận

`FieldServiceManager` cung cấp giải pháp hoàn chỉnh để quản lý dịch vụ sân với:
- **UI thân thiện** cho owner
- **CRUD operations** đầy đủ
- **Tích hợp Firebase** mượt mà
- **Code sạch** và dễ maintain

Việc tách biệt này giúp `CourtService` tập trung vào bảng giá sân, trong khi `FieldServiceManager` xử lý tất cả logic liên quan đến dịch vụ.
