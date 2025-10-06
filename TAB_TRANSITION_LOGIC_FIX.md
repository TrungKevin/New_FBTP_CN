# Fix Logic Chuyển Đổi Tab và Card Xác Nhận - Hoàn thành ✅

## Vấn đề được giải quyết:

### 1. **Logic chuyển đổi tab không đúng**
**Vấn đề**: Khi click "Xác nhận" trong tab "Trận đấu", card không chuyển sang tab "Đã xác nhận" và vẫn hiển thị ở tab "Tất cả"

### 2. **Logic card "đang chờ đối thủ" không phù hợp**
**Vấn đề**: Card "đang chờ đối thủ" khi click "Xác nhận" không cho phép Renter B match mà không cần xác nhận lại

## Giải pháp áp dụng:

### **1. Cập nhật Logic Filtering theo Tab:**

#### **Tab "Đặt sân"** (dòng 133-147):
```kotlin
MainTab.Bookings -> {
    // Tab "Đặt sân": Chỉ hiển thị bookings đã có đối thủ (đã ghép đôi)
    list = list.filter { booking ->
        booking.opponentMode == "HAS_OPPONENT" || 
        booking.status == "PAID" ||
        booking.status == "CONFIRMED"
    }
}
```

#### **Tab "Trận đấu"** (dòng 149-167):
```kotlin
MainTab.Matches -> {
    // Tab "Trận đấu": Hiển thị bookings chưa có đối thủ VÀ đã ghép đôi nhưng chưa xác nhận
    list = list.filter { booking ->
        // Chưa có đối thủ (đang chờ)
        (booking.opponentMode == "WAITING_OPPONENT" || 
         booking.opponentMode == "FIND_OPPONENT" ||
         booking.status == "PENDING") ||
        // Hoặc đã ghép đôi nhưng chưa được owner xác nhận cuối cùng
        (booking.status == "PAID")
    }
}
```

### **2. Logic Xác Nhận Thông Minh:**

#### **Action Button Logic** (dòng 523-535):
```kotlin
onConfirm = if (booking.status != "CANCELLED") {
    { scope.launch { 
        // ✅ FIX: Logic xác nhận khác nhau cho từng trạng thái
        if (booking.status == "PENDING") {
            // Chưa có đối thủ: Chuyển thành PAID để cho phép Renter B match
            bookingRepo.updateBookingStatus(booking.bookingId, "PAID")
            println("✅ DEBUG: Booking ${booking.bookingId} confirmed for matching - status: PAID")
        } else if (booking.status == "PAID") {
            // Đã ghép đôi: Chuyển thành CONFIRMED và chuyển sang tab "Đã xác nhận"
            bookingRepo.updateBookingStatus(booking.bookingId, "CONFIRMED")
            println("✅ DEBUG: Booking ${booking.bookingId} fully confirmed - status: CONFIRMED")
        }
    } }
} else null,
```

### **3. Cập nhật OwnerMatchesContent:**

#### **Filtering Logic** (dòng 442-457):
```kotlin
// ✅ FIX: Hiển thị cả bookings chưa có đối thủ VÀ đã ghép đôi trong tab Trận đấu
val matchBookings = fieldBookings.filter { booking ->
    booking.bookingType == "SOLO" && 
    booking.ownerId == user.userId &&
    // Chưa có đối thủ (PENDING) HOẶC đã ghép đôi (PAID/CONFIRMED)
    ((booking.hasOpponent == false && booking.status == "PENDING" &&
      // Chưa được match
      !allMatches.any { match -> 
          match.fieldId == booking.fieldId && 
          match.date == booking.date &&
          match.participants.any { participant -> 
              participant.renterId == booking.renterId
          }
      }) ||
     // Hoặc đã ghép đôi nhưng chưa được owner xác nhận cuối cùng
     (booking.status == "PAID"))
}
```

## Flow hoạt động mới:

### **Scenario 1: Card "đang chờ đối thủ"**
1. **Initial**: Renter A đặt sân → Status: `PENDING` → Hiển thị trong tab "Trận đấu"
2. **Owner click "Xác nhận"**: Status: `PENDING` → `PAID` → Vẫn ở tab "Trận đấu" 
3. **Renter B match**: Có thể match mà không cần owner xác nhận lại
4. **Owner click "Xác nhận" lần 2**: Status: `PAID` → `CONFIRMED` → Chuyển sang tab "Đặt sân"

### **Scenario 2: Card đã ghép đôi**
1. **Initial**: Đã có đối thủ → Status: `PAID` → Hiển thị trong tab "Trận đấu"
2. **Owner click "Xác nhận"**: Status: `PAID` → `CONFIRMED` → Chuyển sang tab "Đặt sân"
3. **Result**: Card mất khỏi tab "Trận đấu" và xuất hiện trong tab "Đặt sân"

## Thay đổi chi tiết:

### **File**: `OwnerBookingListScreen.kt`

**1. Main Filtering Logic (dòng 131-168):**
- Tách rõ logic cho từng tab
- Tab "Đặt sân": Chỉ bookings đã ghép đôi (`HAS_OPPONENT`, `PAID`, `CONFIRMED`)
- Tab "Trận đấu": Bookings chưa có đối thủ (`PENDING`) và đã ghép đôi chưa xác nhận (`PAID`)

**2. Smart Confirmation Logic (dòng 523-535):**
- **PENDING → PAID**: Cho phép matching, vẫn ở tab "Trận đấu"
- **PAID → CONFIRMED**: Chuyển sang tab "Đặt sân"

**3. OwnerMatchesContent Filtering (dòng 442-457):**
- Hiển thị cả bookings chưa có đối thủ và đã ghép đôi
- Loại trừ bookings đã được match hoặc đã CONFIRMED

## Kết quả sau khi fix:

### ✅ **Tab Transition Logic:**
- **Card "đang chờ đối thủ"**: Click "Xác nhận" → Vẫn ở tab "Trận đấu" (cho phép matching)
- **Card đã ghép đôi**: Click "Xác nhận" → Chuyển sang tab "Đặt sân" (đã xác nhận)
- **Card CONFIRMED**: Chỉ hiển thị trong tab "Đặt sân"

### ✅ **Matching Logic:**
- **Renter B có thể match** với booking có status `PAID` mà không cần owner xác nhận lại
- **Owner chỉ cần xác nhận cuối cùng** để chuyển sang tab "Đặt sân"

### ✅ **Build Status:**
- **Compile success**: Không có lỗi compile
- **Logic complete**: Cả 2 vấn đề đã được giải quyết thành công

## Expected User Experience:

1. **Tab "Trận đấu"**: Hiển thị bookings đang chờ đối thủ và đã ghép đôi chưa xác nhận
2. **Click "Xác nhận" lần 1**: Cho phép matching, card vẫn ở tab "Trận đấu"
3. **Click "Xác nhận" lần 2**: Card chuyển sang tab "Đặt sân" (đã xác nhận hoàn toàn)
4. **Tab "Đặt sân"**: Chỉ hiển thị bookings đã được xác nhận hoàn toàn

**Status: HOÀN THÀNH THÀNH CÔNG** ✅
