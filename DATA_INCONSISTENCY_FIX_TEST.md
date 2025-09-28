# 🔧 Data Inconsistency Fix - Test Guide

## ✅ Đã sửa thành công!

### 🔍 Vấn đề đã xác định:

**Root Cause**: Query inconsistency giữa các methods trong `BookingRepository`:

1. **`getWaitingOpponentBookings`** (ViewModel sử dụng):
   ```kotlin
   .whereEqualTo("bookingType", "SOLO")
   .whereEqualTo("hasOpponent", false)
   ```

2. **`findWaitingBookingBySlot`** (UI sử dụng):
   ```kotlin
   .whereEqualTo("opponentMode", "WAITING_OPPONENT")  // ❌ Field không tồn tại!
   ```

**Từ data thực tế**:
```json
{
  "bookingType": "SOLO",
  "hasOpponent": false,
  "opponentMode": null  // ❌ Field này không có!
}
```

### 🔧 Giải pháp đã implement:

#### 1. **Fixed Query Consistency**:
```kotlin
// ✅ FIXED: Cả 2 methods giờ dùng cùng query
suspend fun findWaitingBookingBySlot(fieldId: String, date: String, slot: String): Result<Booking?> {
    val snapshot = firestore.collection(BOOKINGS_COLLECTION)
        .whereEqualTo("fieldId", fieldId)
        .whereEqualTo("date", date)
        .whereEqualTo("bookingType", "SOLO")        // ✅ Consistent
        .whereEqualTo("hasOpponent", false)         // ✅ Consistent
        .whereArrayContains("consecutiveSlots", slot)
        .get()
        .await()
}
```

#### 2. **Enhanced Debug Logging**:
```kotlin
println("🔍 DEBUG: findWaitingBookingBySlot query:")
println("  - fieldId: $fieldId")
println("  - date: $date")
println("  - slot: $slot")
println("🔍 DEBUG: Found ${bookings.size} bookings matching criteria")
```

### 🧪 Test Steps:

#### **Step 1: Test với booking thực tế**
1. **Mở app** và đi đến booking screen
2. **Chọn ngày 2025-09-29** (có booking thực tế)
3. **Click vào slot 08:00** (màu vàng)
4. **Quan sát logs**:

**Expected Logs**:
```
🔍 DEBUG: getWaitingOpponentBookings query:
  - fieldId: hRExp40X2ToxlzIr18SU
  - date: 2025-09-29
✅ DEBUG: Found 1 waiting opponent bookings
  [0] bookingId: 4f574e0e-fc88-40f1-895c-d16c4f723ec3, slots: [08:00, 08:30, 09:00]

🔍 DEBUG: findWaitingBookingBySlot query:
  - fieldId: hRExp40X2ToxlzIr18SU
  - date: 2025-09-29
  - slot: 08:00
🔍 DEBUG: Found 1 bookings matching criteria
  [0] bookingId: 4f574e0e-fc88-40f1-895c-d16c4f723ec3, slots: [08:00, 08:30, 09:00]

🎯 DEBUG: Found booking from database: 4f574e0e-fc88-40f1-895c-d16c4f723ec3
🎯 DEBUG: Starting 3-second timer for OpponentConfirmationDialog
🎯 DEBUG: After 3 seconds, stillSelected: true
🎯 DEBUG: Showing OpponentConfirmationDialog
```

#### **Step 2: Verify Dialog Display**
1. **Wait 3 seconds** sau khi click
2. **Dialog should appear** với message: "Bạn sẽ là đối thủ của [tên renter]"
3. **Auto-select** tất cả slots: 08:00, 08:30, 09:00

#### **Step 3: Test với slot không có booking**
1. **Click vào slot trống** (màu trắng)
2. **Should toggle normally** không có dialog

### 🎯 Expected Results:

#### ✅ **Success Case**:
- **ViewModel**: Tìm thấy 1 booking
- **Database**: Tìm thấy cùng 1 booking
- **UI**: Hiển thị dialog sau 3s
- **Auto-select**: Chọn tất cả slots của match

#### ❌ **Failure Case** (nếu vẫn có vấn đề):
- **ViewModel**: Tìm thấy booking
- **Database**: Không tìm thấy booking
- **UI**: Không hiển thị dialog

### 🔍 Debug Commands:

#### **Check Firebase Data**:
```bash
# Trong Firebase Console
Collection: bookings
Filter: fieldId = "hRExp40X2ToxlzIr18SU" AND date = "2025-09-29"
```

#### **Check Logs**:
```bash
# Trong Android Studio Logcat
Filter: "DEBUG: getWaitingOpponentBookings" OR "DEBUG: findWaitingBookingBySlot"
```

### 🚀 Next Steps:

1. **Test app** với steps trên
2. **Check logs** để verify queries hoạt động
3. **Report results** - dialog có hiển thị không?

### 📊 Data Flow:

```
ViewModel.loadOpponentTimes()
    ↓
getWaitingOpponentBookings() → Found 1 booking
    ↓
waitingOpponentTimes = [08:00, 08:30, 09:00]
    ↓
UI displays yellow slots
    ↓
User clicks 08:00
    ↓
findWaitingBookingBySlot() → Found same booking ✅
    ↓
OpponentConfirmationDialog appears ✅
```

## 🎉 Ready for Testing!

Hãy test và báo cáo kết quả logs!
