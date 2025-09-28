# Hướng dẫn Test Opponent Matching Dialog

## 🎯 Mục đích Test

Kiểm tra dialog xác nhận đối thủ khi renter sau muốn đặt vào khung giờ có trạng thái WAITING_OPPONENT (màu vàng).

## 📋 Test Cases

### Test Case 1: Dialog hiển thị khi chọn khung giờ WAITING_OPPONENT

**Bước thực hiện:**
1. Mở app và đăng nhập với tài khoản renter
2. Chọn một sân bóng
3. Chọn ngày có khung giờ WAITING_OPPONENT (màu vàng)
4. Click vào khung giờ màu vàng

**Kết quả mong đợi:**
- ✅ Dialog `OpponentConfirmationDialog` hiển thị
- ✅ Hiển thị tên đối thủ đã đặt trước đó
- ✅ Hiển thị thông tin khung giờ và ngày
- ✅ Có nút "Xác nhận đặt lịch" và "Hủy"

### Test Case 2: Toast khi chọn lại khung giờ đã đặt của chính mình

**Bước thực hiện:**
1. Đăng nhập với tài khoản đã đặt khung giờ WAITING_OPPONENT
2. Chọn lại chính khung giờ đó

**Kết quả mong đợi:**
- ✅ Toast hiển thị: "Khung giờ này bạn đã đặt"
- ✅ Không hiển thị dialog

### Test Case 3: Toast khi khung giờ đã được đặt hoàn toàn

**Bước thực hiện:**
1. Chọn khung giờ có trạng thái FULL (màu đỏ)

**Kết quả mong đợi:**
- ✅ Toast hiển thị: "Khung giờ này đã được đặt"
- ✅ Không hiển thị dialog

### Test Case 4: Xác nhận đặt lịch thành công

**Bước thực hiện:**
1. Chọn khung giờ WAITING_OPPONENT
2. Click "Xác nhận đặt lịch" trong dialog

**Kết quả mong đợi:**
- ✅ Dialog đóng
- ✅ Toast hiển thị: "Đặt lịch thành công!"
- ✅ Khung giờ chuyển từ màu vàng sang màu đỏ (FULL)
- ✅ Dữ liệu được lưu vào BOOKINGS và MATCHES tables

### Test Case 5: Hủy đặt lịch

**Bước thực hiện:**
1. Chọn khung giờ WAITING_OPPONENT
2. Click "Hủy" trong dialog

**Kết quả mong đợi:**
- ✅ Dialog đóng
- ✅ Khung giờ vẫn giữ màu vàng
- ✅ Không có thay đổi dữ liệu

## 🔧 Test với Preview Components

### Test UI Components:

```kotlin
// Trong Android Studio, mở file:
app/src/main/java/com/trungkien/fbtp_cn/ui/components/renter/dialogs/OpponentConfirmationDialog.kt

// Click vào @Preview để xem UI
@Preview(showBackground = true)
@Composable
private fun OpponentConfirmationDialogPreview()

@Preview(showBackground = true) 
@Composable
private fun OpponentConfirmationAlertDialogPreview()
```

### Test Dialog Test Screen:

```kotlin
// Trong Android Studio, mở file:
app/src/main/java/com/trungkien/fbtp_cn/ui/components/renter/dialogs/OpponentDialogTest.kt

// Click vào @Preview để test cả hai loại dialog
@Preview(showBackground = true)
@Composable
private fun OpponentDialogTestScreenPreview()
```

## 📊 Database Verification

### Kiểm tra BOOKINGS Table:

```sql
-- Kiểm tra booking mới được tạo
SELECT * FROM bookings 
WHERE matchId IS NOT NULL 
ORDER BY createdAt DESC 
LIMIT 5;

-- Kiểm tra matchSide (A hoặc B)
SELECT bookingId, renterId, matchSide, opponentMode 
FROM bookings 
WHERE matchId IS NOT NULL;
```

### Kiểm tra MATCHES Table:

```sql
-- Kiểm tra match được cập nhật
SELECT * FROM matches 
WHERE status = 'FULL' 
ORDER BY createdAt DESC 
LIMIT 5;

-- Kiểm tra participants
SELECT rangeKey, occupiedCount, participants 
FROM matches 
WHERE status = 'FULL';
```

## 🐛 Debug Information

### Log Messages để theo dõi:

```
🔄 DEBUG: selectedSlots changed: [20:30, 21:00, 21:30, 22:00, 22:30, 20:00]
🔄 DEBUG: selectedSlotsByDate: {2025-09-28=[20:30, 21:00, 21:30, 22:00, 22:30, 20:00]}
✅ DEBUG: User has opponent - slots locked for 2025-09-28: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
```

### Kiểm tra trạng thái khung giờ:

```kotlin
// Trong BookingTimeSlotGrid.kt
val isWaitingOpponentForThisDate = waitingOpponentSlots.contains(slot) || waitingOpponentTimes.contains(slot)
val isLockedForThisDate = lockedSlots.contains(slot) || lockedOpponentTimes.contains(slot)
```

## ✅ Checklist Test

- [ ] Dialog hiển thị đúng khi chọn khung giờ WAITING_OPPONENT
- [ ] Toast hiển thị đúng khi chọn lại khung giờ đã đặt của chính mình
- [ ] Toast hiển thị đúng khi khung giờ đã được đặt hoàn toàn
- [ ] Xác nhận đặt lịch thành công và cập nhật database
- [ ] Hủy đặt lịch không ảnh hưởng đến dữ liệu
- [ ] UI components hiển thị đúng trong preview
- [ ] Không có lỗi linting
- [ ] Performance tốt, không lag khi hiển thị dialog

## 🚀 Next Steps

Sau khi test thành công:

1. **Deploy**: Đưa code lên production
2. **Monitor**: Theo dõi logs và user feedback
3. **Optimize**: Cải thiện performance nếu cần
4. **Extend**: Thêm tính năng mới như notification, email confirmation, etc.
