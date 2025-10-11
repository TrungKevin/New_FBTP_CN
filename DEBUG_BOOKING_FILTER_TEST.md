# 🔍 Debug Booking Filter Test Guide

## 🎯 **Mục đích**
Kiểm tra và fix logic filter booking trong tab "Đặt sân" để đảm bảo chỉ hiển thị booking của Renter A (người đặt đầu tiên) và chỉ khi họ chọn "đã có đối thủ" từ đầu.

## 📱 **Cách test**

### **Bước 1: Mở Owner Booking List Screen**
1. Đăng nhập với tài khoản Owner
2. Vào màn hình "Quản lý đặt sân"
3. Chọn tab "Đặt sân"

### **Bước 2: Quan sát Debug Logs**
Trong Android Studio Logcat, tìm các logs có format:
```
🔍 DEBUG: Booking [bookingId]:
  - renterId: [userId]
  - bookingType: [SOLO/DUO]
  - hasOpponent: [true/false]
  - matchSide: '[A/B/null]'
  - isOriginalBooker: [true/false]
  - hasOpponentFromStart: [true/false]
  - shouldShow: [true/false]
```

### **Bước 3: Kiểm tra booking của NaNaCa**
Tìm booking có `renterId` chứa NaNaCa và kiểm tra:
- `matchSide` có phải là `'B'` không?
- `shouldShow` có phải là `false` không?
- Có thấy log `❌ BOOKING WILL NOT BE SHOWN IN ĐẶT SÂN TAB` không?

## 🔍 **Các trường hợp cần kiểm tra**

### **Case 1: Booking của Renter A (MiMi)**
```
🔍 DEBUG: Booking [id]: renterId=MiMi, bookingType=DUO, hasOpponent=true, matchSide='A'
✅ BOOKING WILL BE SHOWN IN ĐẶT SÂN TAB
```

### **Case 2: Booking của Renter B (NaNaCa) - KHÔNG được hiển thị**
```
🔍 DEBUG: Booking [id]: renterId=NaNaCa, bookingType=DUO, hasOpponent=true, matchSide='B'
❌ BOOKING WILL NOT BE SHOWN IN ĐẶT SÂN TAB
```

### **Case 3: Booking chưa có đối thủ - KHÔNG được hiển thị**
```
🔍 DEBUG: Booking [id]: bookingType=SOLO, hasOpponent=false
ℹ️ INFO: Booking with no opponent from start - not showing in Đặt sân tab
❌ BOOKING WILL NOT BE SHOWN IN ĐẶT SÂN TAB
```

## 🚨 **Vấn đề có thể gặp**

### **Vấn đề 1: NaNaCa vẫn hiển thị trong tab "Đặt sân"**
**Nguyên nhân có thể:**
1. `matchSide` của NaNaCa không phải là `'B'`
2. Logic filter bị bypass ở đâu đó
3. Dữ liệu không đúng trong Firebase

**Cách fix:**
- Kiểm tra debug logs để xem giá trị thực tế
- Nếu `matchSide` không phải `'B'`, cần fix logic tạo booking

### **Vấn đề 2: Booking không có đối thủ vẫn hiển thị**
**Nguyên nhân có thể:**
1. `bookingType` không phải là `SOLO`
2. `hasOpponent` không phải là `false`

## 📊 **Kết quả mong đợi**

### **Tab "Đặt sân" chỉ hiển thị:**
- ✅ Booking của Renter A (matchSide = 'A' hoặc null)
- ✅ Booking có bookingType = 'DUO' và hasOpponent = true
- ✅ Booking đã chọn "đã có đối thủ" từ đầu

### **Tab "Đặt sân" KHÔNG hiển thị:**
- ❌ Booking của Renter B (matchSide = 'B')
- ❌ Booking có bookingType = 'SOLO'
- ❌ Booking có bookingType = 'DUO' nhưng hasOpponent = false

## 🔧 **Nếu vẫn có vấn đề**

Hãy copy debug logs và gửi cho tôi với format:
```
🔍 DEBUG: Booking [bookingId]:
  - renterId: [userId]
  - bookingType: [type]
  - hasOpponent: [true/false]
  - matchSide: '[A/B/null]'
  - isOriginalBooker: [true/false]
  - hasOpponentFromStart: [true/false]
  - shouldShow: [true/false]
```

Tôi sẽ phân tích và fix logic cho phù hợp.

## 📝 **Ghi chú**
- Debug logs sẽ hiển thị chi tiết từng booking
- Nếu thấy booking không mong muốn, hãy kiểm tra các giá trị trong logs
- Logic filter đã được cải thiện với debug logs chi tiết

---

**Phiên bản**: 1.0.0  
**Cập nhật**: 2025-01-11  
**Tác giả**: FBTP Development Team
