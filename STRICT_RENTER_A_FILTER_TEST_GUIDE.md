# Strict Renter A Filter Test Guide

## Root Cause Fix
**Vấn đề gốc**: Logic cũ coi `matchSide == null` là Renter A, nhưng nếu Firestore không populate `matchSide` cho Renter B, thì những record B đó sẽ pass qua filter và hiển thị trong tab "Đặt sân".

**Giải pháp**: Chỉ chấp nhận booking có `matchSide == "A"` (case-insensitive), loại bỏ hoàn toàn `null`.

## Logic mới (Strict)

### Helper Function
```kotlin
private fun Booking.isRenterAStrict(): Boolean {
    // Only true when explicitly marked as A
    val side = matchSide?.trim()?.uppercase()
    return side == "A"
}
```

### Filter Logic
```kotlin
list = list.filter { booking ->
    val duoWithOpponent = booking.bookingType.equals("DUO", true) && booking.hasOpponent
    val show = duoWithOpponent && booking.isRenterAStrict()
    show
}
```

## Các trường hợp test

### ✅ Trường hợp ĐÚNG (sẽ hiển thị trong tab "Đặt sân")
1. **Renter A với DUO + hasOpponent=true + matchSide="A"**
   - `matchSide`: "A" (exactly)
   - `bookingType`: "DUO"
   - `hasOpponent`: true
   - **Kết quả**: ✅ Hiển thị

### ❌ Trường hợp SAI (KHÔNG hiển thị trong tab "Đặt sân")
1. **Renter B với matchSide="B"**
   - `matchSide`: "B"
   - **Kết quả**: ❌ Không hiển thị

2. **Renter B với matchSide=null (Firestore không populate)**
   - `matchSide`: null
   - **Kết quả**: ❌ Không hiển thị (STRICT FIX)

3. **Renter A với SOLO (chưa có đối thủ)**
   - `matchSide`: "A"
   - `bookingType`: "SOLO"
   - **Kết quả**: ❌ Không hiển thị

4. **Renter A với DUO nhưng hasOpponent=false**
   - `matchSide`: "A"
   - `bookingType`: "DUO"
   - `hasOpponent`: false
   - **Kết quả**: ❌ Không hiển thị

5. **Renter A với matchSide=null (edge case)**
   - `matchSide`: null
   - **Kết quả**: ❌ Không hiển thị (STRICT FIX)

## Debug Logs mới

### Log mẫu cho booking hợp lệ:
```
🔍 STRICT Bookings filter -> id=booking123, side='A', duoWithOpponent=true, isRenterA=true, show=true
```

### Log mẫu cho Renter B với matchSide="B":
```
🔍 STRICT Bookings filter -> id=booking456, side='B', duoWithOpponent=true, isRenterA=false, show=false
```

### Log mẫu cho Renter B với matchSide=null (STRICT FIX):
```
🔍 STRICT Bookings filter -> id=booking789, side='null', duoWithOpponent=true, isRenterA=false, show=false
```

### Log mẫu cho SOLO booking:
```
🔍 STRICT Bookings filter -> id=booking999, side='A', duoWithOpponent=false, isRenterA=true, show=false
```

## Hướng dẫn test

### Bước 1: Mở ứng dụng và đi đến Owner Booking List
1. Mở ứng dụng với tài khoản Owner
2. Đi đến màn hình "Quản lý đặt sân"
3. Chọn tab "Đặt sân"

### Bước 2: Kiểm tra debug logs
Mở Logcat và tìm các log có prefix:
- `🔍 STRICT Bookings filter`
- `🔍 DEBUG: FINAL FILTERED LIST VERIFICATION`

### Bước 3: Phân tích debug logs

#### Tìm log `🔍 STRICT Bookings filter` cho mỗi booking:
- `side='A'` + `duoWithOpponent=true` + `isRenterA=true` + `show=true` → ✅ Hiển thị
- `side='B'` + `show=false` → ❌ Không hiển thị (Renter B)
- `side='null'` + `show=false` → ❌ Không hiển thị (STRICT FIX)
- `duoWithOpponent=false` + `show=false` → ❌ Không hiển thị (SOLO)

### Bước 4: Kiểm tra Final Verification
Tìm log `🔍 DEBUG: FINAL FILTERED LIST VERIFICATION` và đảm bảo:
- Tất cả booking trong danh sách cuối đều có `matchSide: 'A'`
- Không có booking nào có `matchSide: 'B'` hoặc `matchSide: null`
- Tất cả booking đều có `bookingType: 'DUO'` và `hasOpponent: true`

### Bước 5: Kiểm tra UI
1. Trong tab "Đặt sân", chỉ hiển thị các card booking hợp lệ
2. Không có card nào của Renter B (dù có matchSide="B" hay null)
3. Không có card nào của booking SOLO (chưa có đối thủ)

## StatsHeader cũng được cập nhật

Tất cả thống kê trong StatsHeader cũng sử dụng `booking.isRenterAStrict()`:
- Chờ xác nhận: Chỉ đếm booking có `matchSide="A"`
- Đã xác nhận: Chỉ đếm booking có `matchSide="A"`
- Đã hủy: Chỉ đếm booking có `matchSide="A"`
- Doanh thu: Chỉ tính booking có `matchSide="A"`

## Kết quả mong đợi

### Trước fix (có bug):
- Renter B với `matchSide=null` → Hiển thị trong "Đặt sân" ❌
- Renter B với `matchSide="B"` → Hiển thị trong "Đặt sân" ❌

### Sau fix (STRICT):
- Renter B với `matchSide=null` → Không hiển thị trong "Đặt sân" ✅
- Renter B với `matchSide="B"` → Không hiển thị trong "Đặt sân" ✅
- Chỉ Renter A với `matchSide="A"` + `DUO` + `hasOpponent=true` → Hiển thị ✅

## Báo cáo kết quả
Sau khi test, hãy cung cấp:
1. Screenshot của tab "Đặt sân" (không có Renter B)
2. Screenshot của tab "Trận đấu" (có đầy đủ cả Renter A và B)
3. Copy debug logs từ Logcat (tìm `🔍 STRICT Bookings filter`)
4. Xác nhận không còn booking nào có `matchSide=null` hoặc `matchSide="B"` trong tab "Đặt sân"

## Lưu ý quan trọng
- **STRICT FIX**: Bây giờ `matchSide=null` được coi là KHÔNG phải Renter A
- **Chỉ chấp nhận**: `matchSide="A"` (case-insensitive)
- **Loại bỏ hoàn toàn**: `matchSide=null`, `matchSide="B"`, hoặc bất kỳ giá trị nào khác
