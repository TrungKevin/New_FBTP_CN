# Complete Fix Test Guide - Renter Booking với "Đã có đối thủ"

## Vấn đề đã được fix
**Root Cause**: Khi renter đặt khe giờ và chọn "đã có đối thủ", booking được tạo với:
- `bookingType = "DUO"`
- `hasOpponent = true`
- `matchSide = null` (không được set)

Với logic strict mới chỉ chấp nhận `matchSide = "A"`, booking này không hiển thị trong tab "Đặt sân".

**Fix**: Thêm `matchSide = "A"` khi renter chọn "đã có đối thủ".

## Thay đổi đã thực hiện

### 1. BookingEvent.Create
```kotlin
data class Create(
    // ... existing parameters
    val matchSide: String? = null // ✅ FIX: Add matchSide parameter
): BookingEvent()
```

### 2. BookingRepository.createBooking
```kotlin
suspend fun createBooking(
    // ... existing parameters
    matchSide: String? = null // ✅ FIX: Add matchSide parameter
): Result<String>
```

### 3. RenterBookingCheckoutScreen
```kotlin
matchSide = if (bookingMode == "HAS_OPPONENT") "A" else null // ✅ FIX: Set matchSide="A" for DUO bookings
```

## Test Cases

### ✅ Test Case 1: Renter chọn "Đã có đối thủ"
1. **Mở ứng dụng** với tài khoản Renter
2. **Chọn sân** và khung giờ
3. **Chọn "Đã có đối thủ"** (bookingMode = "HAS_OPPONENT")
4. **Hoàn thành booking**

**Kết quả mong đợi:**
- Booking được tạo với:
  - `bookingType = "DUO"`
  - `hasOpponent = true`
  - `matchSide = "A"`
- Booking hiển thị trong tab "Đặt sân" của Owner

### ✅ Test Case 2: Renter chọn "Tìm đối thủ"
1. **Mở ứng dụng** với tài khoản Renter
2. **Chọn sân** và khung giờ
3. **Chọn "Tìm đối thủ"** (bookingMode = "FIND_OPPONENT")
4. **Hoàn thành booking**

**Kết quả mong đợi:**
- Booking được tạo với:
  - `bookingType = "SOLO"`
  - `hasOpponent = false`
  - `matchSide = null`
- Booking KHÔNG hiển thị trong tab "Đặt sân" của Owner
- Booking hiển thị trong tab "Trận đấu" của Owner (waiting for opponent)

## Debug Logs để kiểm tra

### 1. RenterBookingCheckoutScreen logs
```
🔍 DEBUG: RenterBookingCheckoutScreen - Button clicked:
  - bookingMode: HAS_OPPONENT
  - bookingType: DUO
  - hasOpponent: true
  - matchSide: A (should be "A" for DUO)
```

### 2. BookingViewModel logs
```
🔍 DEBUG: BookingViewModel.create called:
  - bookingType: DUO
  - hasOpponent: true
  - matchSide: A
```

### 3. OwnerBookingListScreen logs
```
🔍 STRICT Bookings filter -> id=booking123, side='A', duoWithOpponent=true, isRenterA=true, show=true
```

## Hướng dẫn test chi tiết

### Bước 1: Test Renter Booking
1. **Mở ứng dụng** với tài khoản Renter
2. **Đi đến sân** và chọn khung giờ
3. **Chọn "Đã có đối thủ"**
4. **Hoàn thành booking**
5. **Kiểm tra Logcat** cho debug logs

### Bước 2: Test Owner View
1. **Mở ứng dụng** với tài khoản Owner (của sân vừa đặt)
2. **Đi đến "Quản lý đặt sân"**
3. **Chọn tab "Đặt sân"**
4. **Kiểm tra** booking vừa tạo có hiển thị không

### Bước 3: Kiểm tra Debug Logs
Tìm các log sau trong Logcat:

#### RenterBookingCheckoutScreen:
```
🔍 DEBUG: RenterBookingCheckoutScreen - Button clicked:
  - bookingMode: HAS_OPPONENT
  - bookingType: DUO
  - hasOpponent: true
```

#### BookingViewModel:
```
🔍 DEBUG: BookingViewModel.create called:
  - bookingType: DUO
  - hasOpponent: true
  - matchSide: A
```

#### OwnerBookingListScreen:
```
🔍 STRICT Bookings filter -> id=booking123, side='A', duoWithOpponent=true, isRenterA=true, show=true
```

## Kết quả mong đợi

### Trước fix:
- Renter chọn "Đã có đối thủ" → Booking không hiển thị trong tab "Đặt sân" ❌
- Debug log: `side='null'` → `isRenterA=false` → `show=false`

### Sau fix:
- Renter chọn "Đã có đối thủ" → Booking hiển thị trong tab "Đặt sân" ✅
- Debug log: `side='A'` → `isRenterA=true` → `show=true`

## Báo cáo kết quả
Sau khi test, hãy cung cấp:
1. **Screenshot** của Renter booking với "Đã có đối thủ"
2. **Screenshot** của Owner tab "Đặt sân" (có booking mới)
3. **Copy debug logs** từ Logcat
4. **Xác nhận** booking hiển thị đúng trong tab "Đặt sân"

## Lưu ý quan trọng
- **DUO bookings** (đã có đối thủ) → `matchSide = "A"` → Hiển thị trong "Đặt sân"
- **SOLO bookings** (tìm đối thủ) → `matchSide = null` → Không hiển thị trong "Đặt sân"
- **Renter B bookings** → `matchSide = "B"` → Không hiển thị trong "Đặt sân"
