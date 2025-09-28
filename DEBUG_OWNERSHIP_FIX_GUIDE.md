# 🔧 Debug Ownership Fix Guide

## 🚨 **Vấn đề hiện tại**:

```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: null
  - currentUserId: PQI6i9abPOO1jDQQYD6BStJkNdP2
  - waitingSlotOwner map: {}
```

**Root Cause**: `waitingSlotOwner` map đang rỗng `{}`, không thể kiểm tra ownership.

## 🔍 **Debug Logs đã thêm**:

### **1. Match Loading Debug**:
```
🎯 DEBUG: Match loading for date 2025-09-28:
  - Total matches found: X
  [0] matchId: xxx, status: WAITING_OPPONENT, participants: 1
```

### **2. Slot Owner Population Debug**:
```
🎯 DEBUG: WAITING_OPPONENT match found:
  - matchId: fieldId2025092820002200
  - ownerId: koko_user_id
  - slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
  - slotToOwner[20:00] = koko_user_id
```

### **3. Final Map Debug**:
```
🎯 DEBUG: Final slotToOwner map:
  - slotToOwner: {20:00=koko_user_id, 20:30=koko_user_id, ...}
  - waiting slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
  - locked slots: []
```

## 🧪 **Test Steps**:

### **Step 1: Check Match Loading**
1. **Login** với account "koko"
2. **Navigate** đến booking screen
3. **Check logs** có:
   ```
   🎯 DEBUG: Match loading for date 2025-09-28:
     - Total matches found: 1
     [0] matchId: xxx, status: WAITING_OPPONENT, participants: 1
   ```

### **Step 2: Check Slot Owner Population**
1. **Look for logs**:
   ```
   🎯 DEBUG: WAITING_OPPONENT match found:
     - matchId: xxx
     - ownerId: koko_user_id
     - slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
     - slotToOwner[20:00] = koko_user_id
   ```

### **Step 3: Check Final Map**
1. **Look for logs**:
   ```
   🎯 DEBUG: Final slotToOwner map:
     - slotToOwner: {20:00=koko_user_id, ...}
   ```

### **Step 4: Test Ownership Check**
1. **Click** vào slot 20:00 (màu vàng)
2. **Expected logs**:
   ```
   🎯 DEBUG: Slot ownership check:
     - ownerId from map: koko_user_id
     - currentUserId: koko_user_id
     - waitingSlotOwner map: {20:00=koko_user_id, ...}
   🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot
   ```

## 🚨 **Potential Issues**:

### **Issue 1: No Matches Loaded**
```
🎯 DEBUG: Match loading for date 2025-09-28:
  - Total matches found: 0
```
**Fix**: Check `bookingRepo.listenMatchesByFieldDate` implementation

### **Issue 2: No WAITING_OPPONENT Matches**
```
🎯 DEBUG: Match loading for date 2025-09-28:
  - Total matches found: 1
  [0] matchId: xxx, status: FULL, participants: 2
```
**Fix**: Check match status in database

### **Issue 3: Empty slotToOwner Map**
```
🎯 DEBUG: Final slotToOwner map:
  - slotToOwner: {}
```
**Fix**: Check WAITING_OPPONENT match processing logic

## 🎯 **Expected Behavior**:

### **Scenario 1: Own Slot Click**
- **Action**: User "koko" clicks slot 20:00
- **Expected**: Toast "Khung giờ này bạn đã đặt"
- **Logs**: `ownerId == currentUserId`

### **Scenario 2: Other's Slot Click**
- **Action**: Different user clicks slot 20:00
- **Expected**: Dialog "Bạn sẽ là đối thủ của koko" after 3s
- **Logs**: `ownerId != currentUserId`

## 🔧 **Next Steps**:

1. **Run the app** and check debug logs
2. **Identify** which step is failing:
   - Match loading?
   - Slot owner population?
   - Ownership check?
3. **Report** the specific logs to fix the issue

## 📊 **Success Criteria**:

✅ **Match Loading**: `Total matches found: 1`  
✅ **Slot Population**: `slotToOwner[20:00] = koko_user_id`  
✅ **Ownership Check**: `ownerId from map: koko_user_id`  
✅ **Toast Display**: "Khung giờ này bạn đã đặt"
