# Debug Guide - OpponentConfirmationDialog Not Showing

## 🔍 Vấn đề hiện tại:

Từ log có thể thấy renter đã chọn các khung giờ từ 20:00-22:30 (6 slots), nhưng `OpponentConfirmationDialog` không hiển thị. Có thể có một số nguyên nhân:

## 🎯 Debug Steps:

### 1. Kiểm tra trạng thái slots:

Khi bạn mở màn hình booking, hãy xem log có hiển thị:

```
🎯 DEBUG: Current slot states for 2025-09-28:
  - waitingOpponentSlots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
  - lockedSlots: []
  - waitingTimesFromVm: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
```

**Nếu `waitingOpponentSlots` và `waitingTimesFromVm` đều rỗng:**
- Có nghĩa là các khung giờ này không phải là WAITING_OPPONENT slots
- Chúng chỉ là slots trống bình thường
- Logic sẽ không trigger `OpponentConfirmationDialog`

### 2. Kiểm tra khi click vào slot:

Khi bạn click vào khung giờ 20:00, hãy xem log có hiển thị:

```
🎯 DEBUG: Clicked on WAITING_OPPONENT slot: 20:00
🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot - starting timer
🎯 DEBUG: Found cached match: match_123
🎯 DEBUG: Auto-selecting match slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
🎯 DEBUG: Starting 3-second timer for OpponentConfirmationDialog
```

**Nếu không thấy log này:**
- Có nghĩa là slot không được nhận diện là WAITING_OPPONENT
- Logic sẽ chạy vào phần toggle slot bình thường

### 3. Kiểm tra sau 3 giây:

Sau khi click và đợi 3 giây, hãy xem log có hiển thị:

```
🎯 DEBUG: After 3 seconds, stillSelected: true
🎯 DEBUG: Showing OpponentConfirmationDialog
```

**Nếu không thấy log này:**
- Timer có thể bị hủy
- Hoặc slot không còn được chọn

## 🔧 Các nguyên nhân có thể:

### 1. **Slots không phải WAITING_OPPONENT:**
- Các khung giờ 20:00-22:30 có thể chỉ là slots trống bình thường
- Chưa có renter nào đặt với trạng thái "tôi chưa có đối thủ"

### 2. **Data không được load đúng:**
- `waitingOpponentSlotsByDate` có thể rỗng
- `waitingTimesFromVm` có thể rỗng
- Cần kiểm tra data từ Firebase

### 3. **Logic condition không đúng:**
- Condition `waitingOpponentSlots.contains(slot) || waitingTimesFromVm.contains(slot)` có thể false
- Cần kiểm tra cả hai điều kiện

## 🧪 Test Cases:

### Test Case 1: Kiểm tra trạng thái slots
1. Mở màn hình booking
2. Xem log trạng thái slots
3. **Expected**: `waitingOpponentSlots` và `waitingTimesFromVm` không rỗng

### Test Case 2: Click vào WAITING_OPPONENT slot
1. Click vào khung giờ màu vàng (nếu có)
2. Xem log debug
3. **Expected**: Thấy log "Clicked on WAITING_OPPONENT slot"

### Test Case 3: Đợi 3 giây
1. Click vào WAITING_OPPONENT slot
2. Đợi 3 giây không click gì khác
3. **Expected**: Thấy log "Showing OpponentConfirmationDialog"

## 🚀 Solutions:

### Solution 1: Tạo WAITING_OPPONENT slots
Nếu không có WAITING_OPPONENT slots, cần:
1. Tạo booking với trạng thái "tôi chưa có đối thủ"
2. Hoặc manually set data trong Firebase

### Solution 2: Kiểm tra data loading
Cần kiểm tra:
1. `fieldViewModel.handleEvent(FieldEvent.LoadSlotsByFieldIdAndDate(fieldId, selectedDate.toString()))`
2. Data có được load đúng không
3. `waitingOpponentTimes` có được set đúng không

### Solution 3: Sửa logic condition
Có thể cần sửa condition để phù hợp với data structure thực tế.

## 📱 Next Steps:

1. **Chạy app và xem debug log**
2. **Xác định nguyên nhân** từ log
3. **Sửa logic** dựa trên nguyên nhân
4. **Test lại** để đảm bảo hoạt động đúng

## 🎯 Expected Behavior:

Khi có WAITING_OPPONENT slots:
1. Click vào slot → Tự động chọn tất cả slots của match
2. Sau 3 giây → Hiển thị `OpponentConfirmationDialog`
3. Xác nhận → Chuyển slots từ màu vàng → màu đỏ

## 🔍 Debug Commands:

Để xem log chi tiết:
```bash
adb logcat | grep "🎯 DEBUG"
```

Hoặc filter theo package:
```bash
adb logcat | grep "com.trungkien.fbtp_cn"
```

## 📋 Checklist:

- [ ] Kiểm tra trạng thái slots khi mở màn hình
- [ ] Kiểm tra log khi click vào slot
- [ ] Kiểm tra log sau 3 giây
- [ ] Xác định nguyên nhân từ log
- [ ] Sửa logic nếu cần
- [ ] Test lại để đảm bảo hoạt động đúng

## 🎉 Kết luận:

Vấn đề có thể là:
1. **Không có WAITING_OPPONENT slots** - cần tạo data test
2. **Data không được load đúng** - cần kiểm tra data loading
3. **Logic condition không đúng** - cần sửa condition

Hãy chạy app và xem debug log để xác định nguyên nhân chính xác! 🎯
