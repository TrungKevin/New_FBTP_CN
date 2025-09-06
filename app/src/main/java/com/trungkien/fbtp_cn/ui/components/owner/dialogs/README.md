# Owner Field Dialogs

Thư mục này chứa các dialog components riêng biệt cho chức năng quản lý sân của Owner, giúp code dễ bảo trì và tái sử dụng.

## 📁 Cấu trúc Files

```
dialogs/
├── DeleteFieldDialog.kt    # Dialog xác nhận xóa sân
├── EditFieldDialog.kt      # Dialog chỉnh sửa thông tin sân
└── README.md              # Tài liệu hướng dẫn
```

## 🔧 Components

### 1. DeleteFieldDialog

**Mục đích**: Hiển thị dialog xác nhận xóa sân với cảnh báo chi tiết.

**Tính năng**:
- ✅ Cảnh báo rõ ràng về hậu quả của việc xóa
- ✅ Hiển thị danh sách dữ liệu sẽ bị xóa
- ✅ Loading state khi đang xử lý
- ✅ Tích hợp với FieldViewModel
- ✅ Tự động đóng dialog khi xóa thành công

**Cách sử dụng**:
```kotlin
DeleteFieldDialog(
    field = field,
    fieldViewModel = fieldViewModel,
    onDismiss = { showDeleteDialog = false },
    onConfirm = { 
        showDeleteDialog = false
        onBackClick() // Quay lại màn hình trước
    }
)
```

**Props**:
- `field: Field` - Thông tin sân cần xóa
- `fieldViewModel: FieldViewModel` - ViewModel để xử lý logic
- `onDismiss: () -> Unit` - Callback khi đóng dialog
- `onConfirm: () -> Unit` - Callback khi xác nhận xóa

### 2. EditFieldDialog

**Mục đích**: Dialog chỉnh sửa thông tin cơ bản của sân.

**Tính năng**:
- ✅ Form chỉnh sửa các trường thông tin cơ bản
- ✅ Validation dữ liệu đầu vào
- ✅ Loading state khi đang lưu
- ✅ Tích hợp với FieldViewModel
- ✅ Tự động reload dữ liệu sau khi lưu

**Các trường có thể chỉnh sửa**:
- Tên sân
- Địa chỉ
- Mô tả
- Số điện thoại
- Giờ hoạt động (mở/đóng)
- Trạng thái hoạt động

**Cách sử dụng**:
```kotlin
EditFieldDialog(
    field = field,
    fieldViewModel = fieldViewModel,
    onDismiss = { showEditDialog = false },
    onSave = { 
        showEditDialog = false
        // Reload field data để hiển thị thông tin mới
        fieldViewModel.handleEvent(FieldEvent.LoadFieldById(field.fieldId))
    }
)
```

**Props**:
- `field: Field` - Thông tin sân cần chỉnh sửa
- `fieldViewModel: FieldViewModel` - ViewModel để xử lý logic
- `onDismiss: () -> Unit` - Callback khi đóng dialog
- `onSave: () -> Unit` - Callback khi lưu thành công

## 🎯 Lợi ích của việc tách riêng

### ✅ **Tách biệt concerns**
- Mỗi dialog có trách nhiệm riêng biệt
- Dễ debug và maintain
- Code rõ ràng, dễ hiểu

### ✅ **Tái sử dụng**
- Có thể sử dụng ở nhiều màn hình khác
- Logic được đóng gói hoàn chỉnh
- Không cần duplicate code

### ✅ **Dễ mở rộng**
- Thêm tính năng mới dễ dàng
- Thay đổi UI không ảnh hưởng logic chính
- Có thể thêm validation rules riêng

### ✅ **Performance**
- Chỉ load dialog khi cần thiết
- Không ảnh hưởng đến màn hình chính
- Memory efficient

## 🔄 Workflow

### **Xóa sân**:
1. Owner click button xóa (🗑️) trong TopAppBar
2. Hiển thị `DeleteFieldDialog` với cảnh báo
3. Owner xác nhận xóa
4. `FieldViewModel` xử lý xóa từ Firebase
5. Tự động quay lại màn hình trước

### **Chỉnh sửa sân**:
1. Owner click button chỉnh sửa (✏️) trong DetailInfoCourt
2. Hiển thị `EditFieldDialog` với form chỉnh sửa
3. Owner chỉnh sửa thông tin và lưu
4. `FieldViewModel` cập nhật vào Firebase
5. Tự động reload dữ liệu mới

## 🛠️ Tích hợp vào màn hình khác

Để sử dụng các dialog này trong màn hình khác:

```kotlin
// Import
import com.trungkien.fbtp_cn.ui.components.owner.dialogs.DeleteFieldDialog
import com.trungkien.fbtp_cn.ui.components.owner.dialogs.EditFieldDialog

// State
var showDeleteDialog by remember { mutableStateOf(false) }
var showEditDialog by remember { mutableStateOf(false) }

// Trong UI
if (showDeleteDialog) {
    DeleteFieldDialog(
        field = field,
        fieldViewModel = fieldViewModel,
        onDismiss = { showDeleteDialog = false },
        onConfirm = { showDeleteDialog = false }
    )
}

if (showEditDialog) {
    EditFieldDialog(
        field = field,
        fieldViewModel = fieldViewModel,
        onDismiss = { showEditDialog = false },
        onSave = { showEditDialog = false }
    )
}
```

## 🔐 Bảo mật

- Chỉ Owner của sân mới có thể xóa/chỉnh sửa
- Firebase Security Rules đã được cấu hình
- Validation dữ liệu ở cả client và server

## 📱 UI/UX

- Material Design 3
- Responsive design
- Loading states
- Error handling
- Toast notifications
- Confirmation dialogs

## 🚀 Tương lai

### **Tính năng có thể thêm**:
- [ ] Undo/Redo functionality
- [ ] Bulk edit operations
- [ ] Image editing trong EditFieldDialog
- [ ] Advanced validation rules
- [ ] Auto-save draft

### **Cải tiến UI/UX**:
- [ ] Animation transitions
- [ ] Dark mode support
- [ ] Accessibility improvements
- [ ] Keyboard shortcuts

---

**Phiên bản**: 1.0.0  
**Cập nhật**: 2024-12-19  
**Tác giả**: FBTP Development Team
