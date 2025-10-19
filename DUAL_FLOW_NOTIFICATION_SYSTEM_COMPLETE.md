# 🔔 DUAL FLOW NOTIFICATION SYSTEM COMPLETE

## 🎯 **Vấn đề đã được giải quyết:**

### ❌ **Vấn đề chính:**
- **Flow 1 (HAS_OPPONENT)**: Owner xác nhận chỉ gửi notification cho 1 renter ✅
- **Flow 2 (WAITING_OPPONENT)**: Owner xác nhận chỉ gửi notification cho 1 renter ❌
- **Logic cũ**: Chỉ gửi notification cho `booking.renterId` (renter A)
- **Thiếu**: Không gửi notification cho renter B trong match

### ✅ **Giải pháp:**

#### **1. Phân tích 2 Flow:**

**Flow 1: HAS_OPPONENT (Đã có đối thủ)**
- **Renter** chọn "Đã có đối thủ" 
- **BookingType**: `DUO`
- **HasOpponent**: `true`
- **Status**: `PENDING`
- **Hiển thị**: Tab "Đặt sân" của Owner
- **Owner xác nhận**: `PENDING` → `CONFIRMED`
- **Notification**: Gửi cho **1 renter** ✅

**Flow 2: WAITING_OPPONENT (Chờ đối thủ)**
- **Renter A** chọn "Chưa có đối thủ"
- **BookingType**: `SOLO`
- **HasOpponent**: `false`
- **Status**: `PENDING`
- **Hiển thị**: Tab "Trận đấu" của Owner (WaitingBookingCard)
- **Renter B** match: Tạo Match với 2 participants
- **Owner xác nhận**: `PENDING` → `CONFIRMED` (cho cả 2 renter)
- **Notification**: Gửi cho **cả 2 renter** ✅

#### **2. Logic Notification Mới:**

```kotlin
// ✅ FIX: Xử lý notification cho cả 2 flow
if (booking.bookingType == "SOLO" && !booking.hasOpponent && !booking.matchId.isNullOrBlank()) {
    // Flow 2: WAITING_OPPONENT - Gửi notification cho cả 2 renter trong match
    println("🔔 DEBUG: WAITING_OPPONENT flow - sending notifications to both renters")
    
    val matchDoc = firestore.collection(MATCHES_COLLECTION)
        .document(booking.matchId)
        .get()
        .await()
    
    if (matchDoc.exists()) {
        val match = matchDoc.toObject(Match::class.java)
        if (match != null && match.participants.size >= 2) {
            // Gửi notification cho cả 2 participants
            match.participants.forEach { participant ->
                renterNotificationHelper.notifyBookingConfirmed(
                    renterId = participant.renterId,
                    fieldName = fieldName,
                    date = booking.date,
                    time = booking.consecutiveSlots.firstOrNull() ?: "",
                    bookingId = booking.bookingId,
                    fieldId = booking.fieldId
                )
                println("🔔 DEBUG: Sent booking confirmed notification to renter: ${participant.renterId}")
            }
        }
    }
} else {
    // Flow 1: HAS_OPPONENT - Gửi notification cho 1 renter
    println("🔔 DEBUG: HAS_OPPONENT flow - sending notification to single renter")
    renterNotificationHelper.notifyBookingConfirmed(
        renterId = booking.renterId,
        fieldName = fieldName,
        date = booking.date,
        time = booking.consecutiveSlots.firstOrNull() ?: "",
        bookingId = booking.bookingId,
        fieldId = booking.fieldId
    )
}
```

#### **3. Áp dụng cho cả CONFIRMED và CANCELLED:**

- ✅ **CONFIRMED**: Gửi notification cho cả 2 renter trong WAITING_OPPONENT flow
- ✅ **CANCELLED**: Gửi notification cho cả 2 renter trong WAITING_OPPONENT flow
- ✅ **Fallback**: Nếu không lấy được match info, gửi cho renter hiện tại
- ✅ **Error handling**: Try-catch để đảm bảo không crash

## 🔍 **Debug Logs sẽ hiển thị:**

### **Flow 1: HAS_OPPONENT**
```
🔔 DEBUG: About to send booking confirmed notification:
  - renterId: [renter_id]
  - fieldName: [field_name]
  - bookingId: [booking_id]
  - fieldId: [field_id]
  - bookingType: DUO
  - hasOpponent: true
  - matchId: null
🔔 DEBUG: HAS_OPPONENT flow - sending notification to single renter
🔔 DEBUG: Sent booking confirmed notification to renter: [renter_id]
```

### **Flow 2: WAITING_OPPONENT**
```
🔔 DEBUG: About to send booking confirmed notification:
  - renterId: [renter_a_id]
  - fieldName: [field_name]
  - bookingId: [booking_id]
  - fieldId: [field_id]
  - bookingType: SOLO
  - hasOpponent: false
  - matchId: [match_id]
🔔 DEBUG: WAITING_OPPONENT flow - sending notifications to both renters
🔔 DEBUG: Sent booking confirmed notification to renter: [renter_a_id]
🔔 DEBUG: Sent booking confirmed notification to renter: [renter_b_id]
```

## 🧪 **Cách test:**

### **Test Flow 1: HAS_OPPONENT**
1. **Renter** đặt sân với option "Đã có đối thủ"
2. **Owner** vào tab "Đặt sân" và click "Xác nhận"
3. **Kiểm tra logs**: `HAS_OPPONENT flow - sending notification to single renter`
4. **Renter** kiểm tra màn hình "Thông báo"

### **Test Flow 2: WAITING_OPPONENT**
1. **Renter A** đặt sân với option "Chưa có đối thủ"
2. **Renter B** match làm đối thủ
3. **Owner** vào tab "Trận đấu" và click "Xác nhận"
4. **Kiểm tra logs**: `WAITING_OPPONENT flow - sending notifications to both renters`
5. **Cả Renter A và B** kiểm tra màn hình "Thông báo"

### **Expected results:**
- ✅ **Flow 1**: 1 renter nhận notification
- ✅ **Flow 2**: 2 renter nhận notification
- ✅ **Debug logs**: Hiển thị đúng flow và số lượng renter

## 🔧 **Các thay đổi đã thực hiện:**

### **1. BookingRepository.kt - updateBookingStatus():**
- ✅ **CONFIRMED logic**: Phân biệt HAS_OPPONENT vs WAITING_OPPONENT
- ✅ **CANCELLED logic**: Phân biệt HAS_OPPONENT vs WAITING_OPPONENT
- ✅ **Match lookup**: Lấy thông tin match để tìm cả 2 participants
- ✅ **Dual notification**: Gửi notification cho cả 2 renter trong WAITING_OPPONENT
- ✅ **Fallback handling**: Nếu không lấy được match, gửi cho renter hiện tại
- ✅ **Error handling**: Try-catch để đảm bảo không crash

### **2. Debug logs:**
- ✅ **Flow detection**: Log để phân biệt flow
- ✅ **Match info**: Log thông tin match và participants
- ✅ **Notification count**: Log số lượng notification được gửi
- ✅ **Error handling**: Log lỗi nếu có

## 🎉 **Kết luận:**

- ✅ **Flow 1 (HAS_OPPONENT)**: Gửi notification cho 1 renter
- ✅ **Flow 2 (WAITING_OPPONENT)**: Gửi notification cho cả 2 renter
- ✅ **Logic**: Phân biệt đúng flow dựa trên bookingType, hasOpponent, matchId
- ✅ **Fallback**: Xử lý trường hợp không lấy được match info
- ✅ **Error handling**: Đảm bảo không crash khi có lỗi

**Bước tiếp theo**: Test cả 2 scenario để đảm bảo notification hoạt động đúng!

## 🚨 **Nếu vẫn không hoạt động:**

### **Kiểm tra:**
1. **Logs có hiển thị đúng flow không?**
2. **Match có tồn tại và có đủ 2 participants không?**
3. **Notification có được tạo cho cả 2 renter không?**

### **Debug steps:**
1. **Mở Firebase Console** → Firestore → matches collection
2. **Kiểm tra** match có đủ 2 participants không
3. **Kiểm tra** notifications collection có notification cho cả 2 renter không
4. **Kiểm tra** toUserId có đúng renter ID không
