# Hướng Dẫn Test Card Thống Kê Cải Tiến

## Tổng Quan Các Cải Tiến

### 1. **Debug Logs Chi Tiết**
- Thêm debug logs để theo dõi quá trình tính toán thống kê
- Log từng booking/match được tính vào thống kê
- Log tổng kết cuối cùng của từng loại thống kê

### 2. **Logic Tính Toán Cải Tiến**
- **Chờ xác nhận**: PENDING bookings (Renter A) + FULL matches chưa kết thúc
- **Đã xác nhận**: PAID/CONFIRMED bookings (Renter A) + CONFIRMED matches chưa kết thúc  
- **Đã hủy**: CANCELLED bookings (Renter A) + CANCELLED matches
- **Doanh thu**: Chỉ tính các trận đã XÁC NHẬN và đã KẾT THÚC

### 3. **Real-time Updates**
- Lắng nghe matches cho tất cả các field của owner
- Cập nhật thống kê khi có thay đổi từ Firebase
- Hỗ trợ cả lọc theo ngày và theo phạm vi thời gian

## Cách Test

### Bước 1: Mở App và Kiểm Tra Debug Logs
1. Mở app và vào màn hình Owner Booking List
2. Mở Logcat và filter theo tag `System.out`
3. Tìm các log có prefix `📊 DEBUG: StatsHeader`

### Bước 2: Kiểm Tra Logic Filtering
1. **Test Tab "Đặt sân"**:
   - Chuyển sang tab "Đặt sân"
   - Tìm log `🔍 DEBUG: ALL BOOKINGS BEFORE FILTERING`
   - Kiểm tra xem có booking nào có `matchSide = "B"` không
   - Tìm log `🚨 CRITICAL: Found Renter B booking!` nếu có

2. **Test Tab "Trận đấu"**:
   - Chuyển sang tab "Trận đấu" 
   - Kiểm tra xem tất cả bookings đều hiển thị

### Bước 3: Kiểm Tra Card Thống Kê
1. **Kiểm tra số liệu**:
   - Chờ xác nhận: Số booking PENDING (Renter A) + matches FULL
   - Đã xác nhận: Số booking PAID/CONFIRMED (Renter A) + matches CONFIRMED
   - Đã hủy: Số booking CANCELLED (Renter A) + matches CANCELLED
   - Doanh thu: Tổng tiền từ bookings/matches đã kết thúc

2. **Test Real-time Updates**:
   - Thay đổi trạng thái booking (xác nhận/hủy)
   - Kiểm tra card thống kê có cập nhật ngay lập tức không
   - Tìm log `📊 DEBUG: StatsHeader Summary` để xem tổng kết

### Bước 4: Test Bộ Lọc
1. **Lọc theo ngày**:
   - Chọn một ngày cụ thể
   - Kiểm tra thống kê chỉ hiển thị dữ liệu ngày đó

2. **Lọc theo phạm vi**:
   - Chọn "1 tuần gần đây", "1 tháng gần đây", etc.
   - Kiểm tra thống kê hiển thị đúng phạm vi

## Debug Logs Quan Trọng

### Logs Filtering Booking
```
🔍 DEBUG: Starting filter process - selectedTab: Bookings, allBookings size: X
🔍 DEBUG: ALL BOOKINGS BEFORE FILTERING:
  [0] Booking bookingId:
    - renterId: userId
    - bookingType: SOLO/DUO
    - hasOpponent: true/false
    - matchSide: 'A'/'B'/null
    - status: PENDING/PAID/CONFIRMED/CANCELLED
🚨 CRITICAL: Found Renter B booking!
  - This should NOT appear in Đặt sân tab
```

### Logs Thống Kê
```
📊 DEBUG: StatsHeader - Initial bookings: X, filtered by owner: Y
📊 DEBUG: StatsHeader - After range filter (7 days): X -> Y
📊 DEBUG: StatsHeader - After date filter (2025-10-11): X -> Y
📊 DEBUG: Pending booking: bookingId (renterId)
📊 DEBUG: StatsHeader Summary:
  - Pending: X bookings + Y matches = Z
  - Confirmed: X bookings + Y matches = Z
  - Cancelled: X bookings + Y matches = Z
  - Revenue: Xđ bookings + Yđ matches = Zđ
```

## Kết Quả Mong Đợi

### ✅ Thành Công
- Tab "Đặt sân" chỉ hiển thị booking của Renter A (matchSide = null hoặc "A")
- Tab "Trận đấu" hiển thị tất cả bookings
- Card thống kê hiển thị số liệu chính xác
- Thống kê cập nhật real-time khi có thay đổi
- Debug logs hiển thị chi tiết quá trình tính toán

### ❌ Cần Sửa
- Nếu vẫn thấy Renter B trong tab "Đặt sân"
- Nếu thống kê không chính xác
- Nếu không có debug logs
- Nếu thống kê không cập nhật real-time

## Báo Cáo Kết Quả

Khi test xong, hãy báo cáo:
1. **Debug logs**: Có thấy các log debug không?
2. **Filtering**: Tab "Đặt sân" có còn hiển thị Renter B không?
3. **Thống kê**: Số liệu trong card có chính xác không?
4. **Real-time**: Thống kê có cập nhật khi thay đổi trạng thái không?
5. **Bộ lọc**: Lọc theo ngày/phạm vi có hoạt động đúng không?

Gửi kết quả test để tôi có thể điều chỉnh thêm nếu cần!
