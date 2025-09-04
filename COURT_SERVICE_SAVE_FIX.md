# 🔧 Sửa lỗi lưu bảng giá sân - Giữ nguyên dịch vụ từ Firebase

## 🐛 **Vấn đề ban đầu:**
Khi lưu bảng giá sân, các dịch vụ bổ sung bị cập nhật lại thành mock data thay vì giữ nguyên dữ liệu đã lấy từ Firebase.

## 🔍 **Nguyên nhân:**
1. Hàm `saveData()` đang nhận `emptyList()` cho services thay vì `uiState.fieldServices`
2. Hàm `saveData()` đang xử lý `services` như `List<CourtServiceItem>` thay vì `List<FieldService>`
3. Logic tạo `newFieldServices` đang tạo mới thay vì giữ nguyên dữ liệu từ Firebase

## ✅ **Các thay đổi đã thực hiện:**

### 1. **Sửa lời gọi hàm saveData()**
```kotlin
// Trước:
saveData(field.fieldId, pricingRules, emptyList(), fieldViewModel)

// Sau:
saveData(field.fieldId, pricingRules, uiState.fieldServices, fieldViewModel)
```

### 2. **Cập nhật signature của hàm saveData()**
```kotlin
// Trước:
private fun saveData(
    fieldId: String,
    pricingRules: List<CourtPricingRule>,
    services: List<CourtServiceItem>,
    fieldViewModel: FieldViewModel
)

// Sau:
private fun saveData(
    fieldId: String,
    pricingRules: List<CourtPricingRule>,
    fieldServices: List<FieldService>,
    fieldViewModel: FieldViewModel
)
```

### 3. **Sửa logic xử lý fieldServices**
```kotlin
// Trước: Tạo mới field services
val newFieldServices = services
    .filter { service -> service.name.isNotEmpty() && service.price.isNotEmpty() }
    .map { service ->
        FieldService(
            fieldServiceId = "",
            fieldId = fieldId,
            name = service.name,
            price = service.price.toLongOrNull() ?: 0L,
            // ...
        )
    }

// Sau: Giữ nguyên field services từ Firebase
val newFieldServices = fieldServices.map { service ->
    service.copy(fieldId = fieldId)
}
```

### 4. **Cập nhật hàm validateData()**
```kotlin
// Trước:
private fun validateData(pricingRules: List<CourtPricingRule>, services: List<CourtServiceItem>): List<String>

// Sau:
private fun validateData(pricingRules: List<CourtPricingRule>): List<String>
```

### 5. **Cập nhật debug logs**
- Thêm debug logs để hiển thị field services đầu vào
- Cập nhật messages để phản ánh việc giữ nguyên dữ liệu từ Firebase

## 🎯 **Kết quả:**
- ✅ Khi lưu bảng giá sân, các dịch vụ bổ sung sẽ được giữ nguyên từ Firebase
- ✅ Không còn bị reset về mock data
- ✅ Dữ liệu dịch vụ được bảo toàn qua các lần lưu
- ✅ Debug logs rõ ràng để theo dõi quá trình xử lý

## 🔄 **Luồng hoạt động mới:**
1. Load dữ liệu từ Firebase → `uiState.fieldServices` chứa dịch vụ thực
2. User chỉnh sửa bảng giá sân
3. Click Save → Truyền `uiState.fieldServices` vào `saveData()`
4. `saveData()` giữ nguyên `fieldServices` từ Firebase
5. Gửi cả pricing rules mới và field services cũ vào Firebase
6. Dữ liệu dịch vụ được bảo toàn

## 📝 **Lưu ý:**
- Dịch vụ được quản lý bởi `FieldServiceManager` riêng biệt
- `CourtService` chỉ quản lý bảng giá sân
- Khi lưu bảng giá, chỉ cập nhật pricing rules, giữ nguyên field services
