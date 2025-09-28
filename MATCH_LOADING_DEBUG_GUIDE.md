# 🔧 Match Loading Debug Guide

## 🚨 **Vấn đề hiện tại**:

```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: null
  - currentUserId: PQI6i9abPOO1jDQQYD6BStJkNdP2
  - waitingSlotOwner map: {}
```

**Root Cause**: `waitingSlotOwner` map rỗng vì **match loading không hoạt động**.

## 🔍 **Debug Logs đã thêm**:

### **1. Match Loading Call Debug**:
```
🔍 DEBUG: listenMatchesByFieldDate called:
  - fieldId: hRExp40X2ToxlzIr18SU
  - date: 2025-09-28
  - MATCHES_COLLECTION: matches
```

### **2. Match Loading Result Debug**:
```
✅ DEBUG: listenMatchesByFieldDate result:
  - snapshot size: X
  - matches found: Y
  [0] matchId: xxx, status: WAITING_OPPONENT, participants: 1
```

### **3. Match Processing Debug**:
```
🎯 DEBUG: Match loading for date 2025-09-28:
  - Total matches found: X
  [0] matchId: xxx, status: WAITING_OPPONENT, participants: 1
```

## 🧪 **Test Steps**:

### **Step 1: Check Match Loading Call**
1. **Login** với account "koko"
2. **Navigate** đến booking screen ngày 2025-09-28
3. **Check logs** có:
   ```
   🔍 DEBUG: listenMatchesByFieldDate called:
     - fieldId: hRExp40X2ToxlzIr18SU
     - date: 2025-09-28
     - MATCHES_COLLECTION: matches
   ```

### **Step 2: Check Match Loading Result**
1. **Look for logs**:
   ```
   ✅ DEBUG: listenMatchesByFieldDate result:
     - snapshot size: X
     - matches found: Y
   ```

### **Step 3: Check Match Processing**
1. **Look for logs**:
   ```
   🎯 DEBUG: Match loading for date 2025-09-28:
     - Total matches found: X
   ```

## 🚨 **Potential Issues**:

### **Issue 1: No Match Loading Call**
**Symptoms**: Không có logs `🔍 DEBUG: listenMatchesByFieldDate called:`
**Cause**: `LaunchedEffect` không chạy hoặc `bookingRepo.listenMatchesByFieldDate` không được gọi
**Fix**: Check `LaunchedEffect(fieldId, selectedDate)` dependencies

### **Issue 2: Firestore Error**
**Symptoms**: 
```
❌ ERROR: listenMatchesByFieldDate error: [error message]
```
**Cause**: Firestore permission hoặc network issue
**Fix**: Check Firestore rules và network connection

### **Issue 3: No Matches in Database**
**Symptoms**:
```
✅ DEBUG: listenMatchesByFieldDate result:
  - snapshot size: 0
  - matches found: 0
```
**Cause**: Không có matches trong database cho fieldId và date này
**Fix**: Check database có matches không

### **Issue 4: Matches Found But Not Processed**
**Symptoms**:
```
✅ DEBUG: listenMatchesByFieldDate result:
  - snapshot size: 1
  - matches found: 1
  [0] matchId: xxx, status: WAITING_OPPONENT, participants: 1
```
Nhưng không có logs:
```
🎯 DEBUG: Match loading for date 2025-09-28:
```
**Cause**: `onChange` callback không được gọi hoặc có lỗi trong processing
**Fix**: Check `onChange` callback logic

## 🎯 **Expected Behavior**:

### **Scenario 1: Successful Match Loading**
```
🔍 DEBUG: listenMatchesByFieldDate called:
  - fieldId: hRExp40X2ToxlzIr18SU
  - date: 2025-09-28
  - MATCHES_COLLECTION: matches

✅ DEBUG: listenMatchesByFieldDate result:
  - snapshot size: 1
  - matches found: 1
  [0] matchId: fieldId2025092820002200, status: WAITING_OPPONENT, participants: 1

🎯 DEBUG: Match loading for date 2025-09-28:
  - Total matches found: 1
  [0] matchId: fieldId2025092820002200, status: WAITING_OPPONENT, participants: 1

🎯 DEBUG: WAITING_OPPONENT match found:
  - matchId: fieldId2025092820002200
  - ownerId: koko_user_id
  - slots: [20:00, 20:30, 21:00, 21:30, 22:00, 22:30]
  - slotToOwner[20:00] = koko_user_id

🎯 DEBUG: Final slotToOwner map:
  - slotToOwner: {20:00=koko_user_id, ...}
```

### **Scenario 2: Own Slot Click**
```
🎯 DEBUG: Slot ownership check:
  - ownerId from map: koko_user_id
  - currentUserId: koko_user_id
  - waitingSlotOwner map: {20:00=koko_user_id, ...}
🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot
```

## 🔧 **Next Steps**:

1. **Run the app** và check debug logs
2. **Identify** step nào failing:
   - Match loading call?
   - Firestore query?
   - Match processing?
3. **Report** specific logs để fix issue

## 📊 **Success Criteria**:

✅ **Match Loading Call**: `listenMatchesByFieldDate called`  
✅ **Firestore Query**: `snapshot size: 1, matches found: 1`  
✅ **Match Processing**: `Total matches found: 1`  
✅ **Slot Owner Population**: `slotToOwner[20:00] = koko_user_id`  
✅ **Ownership Check**: `ownerId from map: koko_user_id`  
✅ **Toast Display**: "Khung giờ này bạn đã đặt"
