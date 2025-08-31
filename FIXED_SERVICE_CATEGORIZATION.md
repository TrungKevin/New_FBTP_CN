# Sửa Lỗi Phân Loại Dịch Vụ - FieldServiceManager

## Vấn Đề Đã Được Giải Quyết

**Vấn đề cũ**: Khi owner nhập "RedBull" vào phần "Nước đóng chai", nhưng khi hiển thị lại nó lại nằm dưới "Dịch vụ khác".

**Nguyên nhân**: Logic mapping tự động phân loại dịch vụ dựa trên tên (ví dụ: "RedBull" không có trong danh sách kiểm tra "Nước đóng chai").

## Giải Pháp Đã Áp Dụng

### 1. Lưu Danh Mục Gốc
- **Trước**: Chỉ dựa vào tên dịch vụ để tự động phân loại
- **Sau**: Lưu danh mục gốc vào trường `description` của Firebase

```kotlin
description = "Dịch vụ: ${service.name} - Danh mục: ${service.category}"
```

### 2. Logic Mapping Mới
- **Ưu tiên 1**: Đọc danh mục từ `description` (nếu có)
- **Ưu tiên 2**: Fallback về logic cũ nếu không có danh mục

```kotlin
val mappedCategory = if (service.description.contains("Danh mục:")) {
    // Đọc danh mục từ description
    val categoryStart = service.description.indexOf("Danh mục:") + "Danh mục:".length
    val category = service.description.substring(categoryStart).trim()
    category
} else {
    // Fallback: Logic cũ
    // ... logic phân loại dựa trên tên
}
```

### 3. Sửa Cảnh Báo Firestore
- Thêm `@PropertyName("available")` cho `isAvailable` trong `FieldService`
- Thêm `@PropertyName("active")` cho `isActive` trong `PricingRule`

## Cách Hoạt Động Mới

### Khi Owner Nhập Dịch Vụ:
1. Owner chọn danh mục "Nước đóng chai"
2. Nhập tên "RedBull" và giá
3. Lưu vào Firebase với description: "Dịch vụ: RedBull - Danh mục: Nước đóng chai"

### Khi Hiển Thị Dịch Vụ:
1. Đọc dữ liệu từ Firebase
2. Kiểm tra `description` có chứa "Danh mục:" không
3. Nếu có → sử dụng danh mục từ description
4. Nếu không → fallback về logic cũ

## Kết Quả

✅ **RedBull** sẽ luôn hiển thị đúng ở phần **"Nước đóng chai"**  
✅ **Dịch vụ khác** sẽ hiển thị đúng ở phần **"Dịch vụ khác"**  
✅ **Thuê dụng cụ** sẽ hiển thị đúng ở phần **"Thuê dụng cụ"**  

## Lợi Ích

1. **Chính xác**: Dịch vụ hiển thị đúng danh mục owner đã chọn
2. **Linh hoạt**: Owner có thể đặt tên dịch vụ tùy ý
3. **Tương thích**: Vẫn hỗ trợ dữ liệu cũ (fallback)
4. **Dễ bảo trì**: Logic rõ ràng, dễ debug

## Test Case

### Test 1: RedBull
- **Input**: Nhập "RedBull" vào "Nước đóng chai"
- **Expected**: Hiển thị ở "Nước đóng chai"
- **Result**: ✅ Đúng

### Test 2: Coca Cola
- **Input**: Nhập "Coca Cola" vào "Nước đóng chai"
- **Expected**: Hiển thị ở "Nước đóng chai"
- **Result**: ✅ Đúng

### Test 3: Vợt Tennis
- **Input**: Nhập "Vợt Tennis" vào "Thuê dụng cụ"
- **Expected**: Hiển thị ở "Thuê dụng cụ"
- **Result**: ✅ Đúng

## Lưu Ý Kỹ Thuật

- Dữ liệu cũ (không có "Danh mục:" trong description) vẫn hoạt động bình thường
- Logic fallback đảm bảo tương thích ngược
- Debug logs đầy đủ để theo dõi quá trình mapping
- Build thành công, không có lỗi compile
