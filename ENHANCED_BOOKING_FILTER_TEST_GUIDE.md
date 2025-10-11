# Enhanced Booking Filter Test Guide

## Mục đích
Kiểm tra logic lọc booking đã được cải thiện để đảm bảo chỉ hiển thị booking của Renter A (người đặt đầu tiên) và chỉ khi họ chọn "đã có đối thủ" ngay từ đầu trong tab "Đặt sân".

## Logic lọc mới (Enhanced)
```kotlin
// 1. Must be Renter A (original booker)
val isOriginalBooker = booking.matchSide == null || booking.matchSide == "A"

// 2. Must have chosen "đã có đối thủ" from the start
val hasOpponentFromStart = booking.bookingType == "DUO" && booking.hasOpponent == true

// 3. Additional safety check: explicitly exclude Renter B
val isNotRenterB = booking.matchSide != "B"

val shouldShow = isOriginalBooker && hasOpponentFromStart && isNotRenterB
```

## Các trường hợp cần kiểm tra

### ✅ Trường hợp ĐÚNG (sẽ hiển thị trong tab "Đặt sân")
1. **Renter A với DUO + hasOpponent=true**
   - `matchSide`: null hoặc "A"
   - `bookingType`: "DUO"
   - `hasOpponent`: true
   - **Kết quả**: ✅ Hiển thị

### ❌ Trường hợp SAI (KHÔNG hiển thị trong tab "Đặt sân")
1. **Renter B (đối thủ)**
   - `matchSide`: "B"
   - **Kết quả**: ❌ Không hiển thị

2. **Renter A với SOLO (chưa có đối thủ)**
   - `matchSide`: null hoặc "A"
   - `bookingType`: "SOLO"
   - **Kết quả**: ❌ Không hiển thị

3. **Renter A với DUO nhưng hasOpponent=false**
   - `matchSide`: null hoặc "A"
   - `bookingType`: "DUO"
   - `hasOpponent`: false
   - **Kết quả**: ❌ Không hiển thị

## Hướng dẫn test

### Bước 1: Mở ứng dụng và đi đến Owner Booking List
1. Mở ứng dụng với tài khoản Owner
2. Đi đến màn hình "Quản lý đặt sân"
3. Chọn tab "Đặt sân"

### Bước 2: Kiểm tra debug logs
Mở Logcat và tìm các log có prefix:
- `🔍 DEBUG: Starting filter process`
- `🔍 DEBUG: ALL BOOKINGS BEFORE FILTERING`
- `🔍 DEBUG: Enhanced Filtering Booking`
- `🔍 DEBUG: FINAL FILTERED LIST VERIFICATION`

### Bước 3: Phân tích debug logs

#### Log mẫu cho booking hợp lệ:
```
🔍 DEBUG: Enhanced Filtering Booking booking123:
  - renterId: userA
  - bookingType: 'DUO'
  - hasOpponent: true
  - matchSide: 'A'
  - isOriginalBooker: true
  - hasOpponentFromStart: true
  - isNotRenterB: true
  - shouldShow: true
  ✅ BOOKING WILL BE SHOWN IN ĐẶT SÂN TAB
    - This is a valid Renter A booking with opponent from start
```

#### Log mẫu cho Renter B (KHÔNG hợp lệ):
```
🔍 DEBUG: Enhanced Filtering Booking booking456:
  - renterId: userB
  - bookingType: 'DUO'
  - hasOpponent: true
  - matchSide: 'B'
  - isOriginalBooker: false
  - hasOpponentFromStart: true
  - isNotRenterB: false
  - shouldShow: false
  🚨 CRITICAL: Found Renter B booking!
    - renterId: userB
    - This should NOT appear in Đặt sân tab
    - matchSide: 'B'
    - bookingType: 'DUO'
    - hasOpponent: true
    - isOriginalBooker: false
    - hasOpponentFromStart: true
    - isNotRenterB: false
    - shouldShow: false
    - ❌ This booking will be FILTERED OUT
  ❌ BOOKING WILL NOT BE SHOWN IN ĐẶT SÂN TAB
    - Reason: Is Renter B
```

#### Log mẫu cho SOLO booking (KHÔNG hợp lệ):
```
🔍 DEBUG: Enhanced Filtering Booking booking789:
  - renterId: userA
  - bookingType: 'SOLO'
  - hasOpponent: false
  - matchSide: 'A'
  - isOriginalBooker: true
  - hasOpponentFromStart: false
  - isNotRenterB: true
  - shouldShow: false
  ℹ️ INFO: SOLO booking (no opponent from start) - not showing in Đặt sân tab
  ❌ BOOKING WILL NOT BE SHOWN IN ĐẶT SÂN TAB
    - Reason: No opponent from start
```

### Bước 4: Kiểm tra Final Verification
Tìm log `🔍 DEBUG: FINAL FILTERED LIST VERIFICATION` và đảm bảo:
- KHÔNG có booking nào có `matchSide: 'B'`
- Tất cả booking trong danh sách cuối đều có:
  - `matchSide`: null hoặc "A"
  - `bookingType`: "DUO"
  - `hasOpponent`: true

### Bước 5: Kiểm tra UI
1. Trong tab "Đặt sân", chỉ hiển thị các card booking hợp lệ
2. Không có card nào của Renter B (như NaNaCa, MiMi khi họ là đối thủ)
3. Không có card nào của booking SOLO (chưa có đối thủ)

## Các lỗi cần báo cáo

### Lỗi 1: Renter B vẫn hiển thị
Nếu thấy booking của Renter B trong tab "Đặt sân":
```
🚨 CRITICAL ERROR: Renter B (userB) is in the final list!
🚨 This indicates a bug in the filtering logic!
🚨 This booking should have been filtered out!
```

### Lỗi 2: SOLO booking hiển thị
Nếu thấy booking SOLO trong tab "Đặt sân":
```
⚠️ WARNING: Invalid booking for Đặt sân tab:
  - matchSide: 'A' (should be null or 'A') ✅
  - bookingType: 'SOLO' (should be 'DUO') ❌
  - hasOpponent: false (should be true) ❌
```

## Kết quả mong đợi
- Tab "Đặt sân": Chỉ hiển thị booking của Renter A với `bookingType="DUO"` và `hasOpponent=true`
- Tab "Trận đấu": Hiển thị tất cả booking (bao gồm cả Renter B và SOLO)
- Debug logs: Rõ ràng và chi tiết, không có lỗi CRITICAL ERROR

## Báo cáo kết quả
Sau khi test, hãy cung cấp:
1. Screenshot của tab "Đặt sân"
2. Screenshot của tab "Trận đấu" 
3. Copy debug logs từ Logcat
4. Xác nhận có/không có lỗi CRITICAL ERROR
