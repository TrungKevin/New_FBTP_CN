# 🔧 Own Slot Toast Logic - Test Guide

## ✅ Đã thêm debug logs để kiểm tra!

### 🔍 **Logic đã implement**:

```kotlin
if (waitingOpponentSlots.contains(slot) || waitingTimesFromVm.contains(slot)) {
    val ownerId = waitingSlotOwner[slot]
    val currentUserId = currentUser?.userId
    
    if (ownerId != null && ownerId == currentUserId) {
        // ✅ User click vào slot của chính mình
        OpponentDialogUtils.showOwnSlotToast(context)
    } else {
        // ✅ User click vào slot của người khác
        // Hiển thị OpponentConfirmationDialog sau 3s
    }
}
```

### 🧪 **Test Scenarios**:

#### **Scenario 1: User click vào slot của chính mình**
1. **Đăng nhập** với account "koko"
2. **Đặt slot** 20:00-22:30 với "tôi chưa có đối thủ"
3. **Click lại vào slot** 20:00 (màu vàng)
4. **Expected**: Toast "Khung giờ này bạn đã đặt"

#### **Scenario 2: User click vào slot của người khác**
1. **Đăng nhập** với account khác
2. **Click vào slot** 20:00 (màu vàng của "koko")
3. **Expected**: Dialog "Bạn sẽ là đối thủ của koko" sau 3s

### 📊 **Expected Debug Logs**:

#### **Scenario 1 (Own Slot)**:
```
🎯 DEBUG: Clicked on WAITING_OPPONENT slot: 20:00
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id
  - currentUserId: koko_user_id
  - waitingSlotOwner map: {20:00=koko_user_id, 20:30=koko_user_id, ...}
🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot
```

#### **Scenario 2 (Other's Slot)**:
```
🎯 DEBUG: Clicked on WAITING_OPPONENT slot: 20:00
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id
  - currentUserId: other_user_id
  - waitingSlotOwner map: {20:00=koko_user_id, 20:30=koko_user_id, ...}
🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot - starting timer
```

### 🔍 **Debug Logs để kiểm tra**:

#### **1. Match Loading**:
```
🎯 DEBUG: WAITING_OPPONENT match found:
  - matchId: fieldId2025092820002200
  - ownerId: koko_user_id
  - slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
  - slotToOwner[20:00] = koko_user_id
  - slotToOwner[20:30] = koko_user_id
  ...
```

#### **2. Slot Click**:
```
🎯 DEBUG: Clicked on WAITING_OPPONENT slot: 20:00
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id
  - currentUserId: current_user_id
  - waitingSlotOwner map: {20:00=koko_user_id, ...}
```

### 🎯 **Test Steps**:

#### **Step 1: Tạo booking của chính mình**
1. **Login** với account "koko"
2. **Chọn ngày** 2025-09-28
3. **Chọn slots** 20:00-22:30
4. **Chọn** "Tôi chưa có đối thủ"
5. **Confirm booking**

#### **Step 2: Test click vào slot của chính mình**
1. **Click vào slot** 20:00 (màu vàng)
2. **Check logs** có `ownerId == currentUserId`
3. **Expected**: Toast "Khung giờ này bạn đã đặt"

#### **Step 3: Test với account khác**
1. **Logout** và login với account khác
2. **Click vào slot** 20:00 (màu vàng)
3. **Check logs** có `ownerId != currentUserId`
4. **Expected**: Dialog sau 3s

### 🚨 **Potential Issues**:

#### **Issue 1: waitingSlotOwner map empty**
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: null
  - currentUserId: koko_user_id
  - waitingSlotOwner map: {}
```
**Fix**: Check match loading logic

#### **Issue 2: ownerId không match**
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: wrong_user_id
  - currentUserId: koko_user_id
```
**Fix**: Check match.participants.firstOrNull()?.renterId

### 🎉 **Ready for Testing!**

Hãy test với các scenarios trên và báo cáo logs để verify logic hoạt động đúng!
