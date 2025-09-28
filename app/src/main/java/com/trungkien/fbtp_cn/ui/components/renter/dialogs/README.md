# Renter Dialog Components

Thư mục này chứa các dialog components riêng biệt cho chức năng đặt sân của Renter, giúp code dễ bảo trì và tái sử dụng.

## 📁 Cấu trúc Files

```
dialogs/
├── OpponentConfirmationDialog.kt    # Dialog xác nhận đối thủ khi đặt vào khung giờ WAITING_OPPONENT
├── OpponentDialogUtils.kt           # Utility functions cho toast messages
├── OpponentDialogTest.kt           # Test component để kiểm tra dialogs
└── README.md                        # Tài liệu hướng dẫn
```

## 🔧 Components

### 1. OpponentConfirmationDialog

**Mục đích**: Hiển thị dialog xác nhận khi renter sau muốn đặt vào khung giờ có trạng thái WAITING_OPPONENT (màu vàng).

**Tính năng**:
- ✅ Hiển thị tên đối thủ đã đặt trước đó
- ✅ Hiển thị thông tin khung giờ và ngày
- ✅ Giao diện thân thiện với emoji và màu sắc
- ✅ Nút xác nhận và hủy rõ ràng
- ✅ Tích hợp với booking flow hiện tại

**Cách sử dụng**:
```kotlin
OpponentConfirmationDialog(
    isVisible = showOpponentDialog,
    opponentName = "Nguyễn Văn A",
    timeSlot = "20:00 - 22:30", 
    date = "28/09/2025",
    onConfirm = {
        // Xử lý xác nhận đặt lịch
        showOpponentDialog = false
        // Gọi API tạo booking và match
    },
    onCancel = {
        showOpponentDialog = false
    }
)
```

**Props**:
- `isVisible: Boolean` - Hiển thị dialog hay không
- `opponentName: String` - Tên đối thủ đã đặt trước đó
- `timeSlot: String` - Khung giờ (VD: "20:00 - 22:30")
- `date: String` - Ngày đặt (VD: "28/09/2025")
- `onConfirm: () -> Unit` - Callback khi xác nhận
- `onCancel: () -> Unit` - Callback khi hủy

### 2. OpponentConfirmationAlertDialog

**Mục đích**: Phiên bản AlertDialog đơn giản hơn, tương thích với code hiện tại.

**Cách sử dụng**:
```kotlin
OpponentConfirmationAlertDialog(
    isVisible = showJoinDialog,
    opponentName = opponentName,
    onConfirm = {
        // Xử lý xác nhận
        showJoinDialog = false
    },
    onCancel = {
        showJoinDialog = false
    }
)
```

## 🔄 Workflow Integration

### Khi renter chọn khung giờ WAITING_OPPONENT:

1. **Kiểm tra trạng thái**: Slot có màu vàng (WAITING_OPPONENT)
2. **Lấy thông tin đối thủ**: Từ database lấy tên renter đã đặt trước đó
3. **Hiển thị dialog**: Gọi `OpponentConfirmationDialog`
4. **Xác nhận**: Tạo booking mới và cập nhật match status thành FULL
5. **Lưu dữ liệu**: Cập nhật cả BOOKINGS và MATCHES tables

### Database Schema:

**BOOKINGS Table**:
```kotlin
bookingId: string
renterId: string  
ownerId: string
fieldId: string
date: string
startAt: string
endAt: string
matchId?: string
matchSide?: "A" | "B"
opponentMode?: "WAITING_OPPONENT" | "LOCKED_FULL"
status: "PENDING" | "PAID" | "CANCELLED" | "DONE"
```

**MATCHES Table**:
```kotlin
rangeKey: string
fieldId: string
date: string
startAt: string
endAt: string
capacity: number = 2
occupiedCount: 0 | 1 | 2
participants: [MatchParticipant]
status: "FREE" | "WAITING_OPPONENT" | "FULL"
```

## 🎨 UI/UX Features

- **Visual Feedback**: Emoji 🤝 để tạo cảm giác thân thiện
- **Color Coding**: Màu primary cho tiêu đề, màu variant cho mô tả
- **Responsive Design**: Tự động điều chỉnh theo kích thước màn hình
- **Accessibility**: Text rõ ràng, contrast tốt
- **Animation**: Smooth transitions khi hiện/ẩn dialog

### 3. OpponentDialogUtils

**Mục đích**: Utility functions để hiển thị toast messages một cách nhất quán.

**Tính năng**:
- ✅ Toast khi renter chọn lại khung giờ đã đặt của chính mình
- ✅ Toast khi khung giờ đã được đặt hoàn toàn
- ✅ Toast khi đặt lịch thành công
- ✅ Toast khi có lỗi xảy ra
- ✅ Composable wrapper để sử dụng trong Compose

**Cách sử dụng**:
```kotlin
val dialogUtils = rememberOpponentDialogUtils(context)

// Trong logic xử lý
if (ownerId == currentUser?.userId) {
    dialogUtils.showOwnSlotToast(context)
} else {
    dialogUtils.showSlotBookedToast(context)
}
```

### 4. OpponentDialogTest

**Mục đích**: Test component để kiểm tra các dialog components.

**Tính năng**:
- ✅ Test cả custom dialog và alert dialog
- ✅ Preview để kiểm tra UI
- ✅ Simulate các callback functions

## 🔧 Technical Notes

- Sử dụng Material Design 3
- Tương thích với Compose
- Hỗ trợ cả Dialog và AlertDialog
- Preview components để test UI
- Type-safe với Kotlin
- Utility functions để quản lý toast messages
