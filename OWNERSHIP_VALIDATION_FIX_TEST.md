# 🔧 Ownership Validation Fix - Test Guide

## ✅ **Đã sửa thành công!**

### 🎯 **Vấn đề đã được giải quyết**:
- **Trước**: User có thể đặt lại khe giờ WAITING_OPPONENT của chính họ
- **Sau**: User không thể đặt lại khe giờ đã đặt, chỉ user khác mới được phép

### 🔧 **Những thay đổi đã thực hiện**:

#### **1. Cải thiện logic kiểm tra ownership**:
```kotlin
// ✅ FIX: Kiểm tra ownership từ database nếu map rỗng
if (ownerId == null && waitingSlotOwner.isEmpty()) {
    println("🎯 DEBUG: waitingSlotOwner map is empty, checking database for ownership")
    // Fetch booking info từ database để kiểm tra ownership
    CoroutineScope(Dispatchers.IO).launch {
        val bookingResult = bookingRepo.findWaitingBookingBySlot(
            fieldId = fieldId,
            date = selectedDate.toString(),
            slot = slot
        )
        bookingResult.onSuccess { booking ->
            if (booking != null) {
                val dbOwnerId = booking.renterId
                println("🎯 DEBUG: Found booking owner from DB: $dbOwnerId")
                CoroutineScope(Dispatchers.Main).launch {
                    if (dbOwnerId == currentUserId) {
                        println("🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot (from DB)")
                        OpponentDialogUtils.showOwnSlotToast(context)
                    } else {
                        println("🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot (from DB) - proceeding with join logic")
                        // Proceed with join logic
                        proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
                    }
                }
            }
        }
    }
    return@BookingTimeSlotGrid
}
```

#### **2. Tạo function `proceedWithJoinLogic`**:
- Tách logic join thành function riêng để tái sử dụng
- Xử lý cả cached match và database fetch
- Đảm bảo logic nhất quán

#### **3. Logic validation hoàn chỉnh**:
```kotlin
if (ownerId != null && ownerId == currentUserId) {
    // User đã đặt khe giờ này → Toast "Khung giờ này bạn đã đặt"
    OpponentDialogUtils.showOwnSlotToast(context)
} else {
    // User khác → Cho phép join làm đối thủ
    proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
}
```

## 🧪 **Test Steps**:

### **Test Case 1: User đặt lại khe giờ của chính họ**
1. **Login** với account "koko" (đã đặt khe giờ 20:00-22:30)
2. **Navigate** đến booking screen ngày 2025-09-28
3. **Click** vào slot 20:00 (màu vàng)
4. **Expected Result**:
   ```
   🎯 DEBUG: Slot ownership check:
     - ownerId from map: koko_user_id (hoặc null nếu map rỗng)
     - currentUserId: koko_user_id
   🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot (from DB)
   ```
5. **Expected UI**: Toast hiển thị "Khung giờ này bạn đã đặt"

### **Test Case 2: User khác đặt khe giờ WAITING_OPPONENT**
1. **Login** với account khác (không phải "koko")
2. **Navigate** đến booking screen ngày 2025-09-28
3. **Click** vào slot 20:00 (màu vàng)
4. **Expected Result**:
   ```
   🎯 DEBUG: Slot ownership check:
     - ownerId from map: koko_user_id (hoặc null nếu map rỗng)
     - currentUserId: other_user_id
   🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot (from DB) - proceeding with join logic
   🎯 DEBUG: Proceeding with join logic for slot: 20:00
   🎯 DEBUG: Found booking from database: c6e30412-ff30-415b-a753-51e6d5a9874b
   🎯 DEBUG: Auto-selecting match slots from DB: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
   🎯 DEBUG: Starting 3-second timer for OpponentConfirmationDialog (from DB)
   ```
5. **Expected UI**: 
   - Slots được auto-select (viền xanh)
   - Sau 3 giây hiển thị `OpponentConfirmationDialog`
   - Dialog hiển thị "Bạn sẽ là đối thủ của koko"

### **Test Case 3: Map rỗng nhưng có data trong DB**
1. **Scenario**: `waitingSlotOwner` map rỗng `{}` nhưng có booking trong database
2. **Expected Behavior**: 
   - Logic fallback kiểm tra database
   - Vẫn hoạt động đúng với ownership validation

## 🔍 **Debug Logs để kiểm tra**:

### **Khi map có data**:
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id
  - currentUserId: koko_user_id
  - waitingSlotOwner map: {20:00=koko_user_id, 20:30=koko_user_id, ...}
🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot
```

### **Khi map rỗng (fallback to DB)**:
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: null
  - currentUserId: koko_user_id
  - waitingSlotOwner map: {}
🎯 DEBUG: waitingSlotOwner map is empty, checking database for ownership
🎯 DEBUG: Found booking owner from DB: koko_user_id
🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot (from DB)
```

### **Khi user khác click**:
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id (hoặc null)
  - currentUserId: other_user_id
🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot (from DB) - proceeding with join logic
🎯 DEBUG: Proceeding with join logic for slot: 20:00
```

## ✅ **Kết quả mong đợi**:

1. **User không thể đặt lại khe giờ của chính họ** ✅
2. **User khác có thể join làm đối thủ** ✅
3. **Logic hoạt động cả khi map rỗng và có data** ✅
4. **Debug logs rõ ràng để troubleshoot** ✅
5. **UI feedback phù hợp (toast vs dialog)** ✅

## 🚨 **Lưu ý**:
- Logic này chỉ áp dụng cho slots có trạng thái `WAITING_OPPONENT` (màu vàng)
- Slots `FREE` (màu trắng) vẫn có thể được đặt bình thường
- Slots `LOCKED` (màu đỏ) vẫn hiển thị toast "Khung giờ đã được đặt"
