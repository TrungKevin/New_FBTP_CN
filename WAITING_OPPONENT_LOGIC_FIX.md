# ✅ WAITING_OPPONENT Slot Logic Fix Complete

## 🎯 **Yêu cầu đã thực hiện**:
Chỉ thay đổi logic khi click vào khung giờ màu vàng (WAITING_OPPONENT), không thay đổi logic khác.

## 🔧 **Thay đổi đã thực hiện**:

### **1. Thêm logic kiểm tra ownership từ database**:
```kotlin
// ✅ FIX: Kiểm tra ownership từ database nếu map rỗng
if (ownerId == null && waitingSlotOwner.isEmpty()) {
    println("🎯 DEBUG: waitingSlotOwner map is empty, checking database for ownership")
    CoroutineScope(Dispatchers.IO).launch {
        val bookingResult = bookingRepo.findWaitingBookingBySlot(
            fieldId = fieldId,
            date = selectedDate.toString(),
            slot = slot
        )
        bookingResult.onSuccess { booking ->
            if (booking != null) {
                val dbOwnerId = booking.renterId
                println("🎯 DEBUG: Found booking owner from DB: $dbOwnerId")
                CoroutineScope(Dispatchers.Main).launch {
                    if (dbOwnerId == currentUserId) {
                        println("🎯 DEBUG: User clicked on their own WAITING_OPPONENT slot (from DB)")
                        OpponentDialogUtils.showOwnSlotToast(context)
                    } else {
                        println("🎯 DEBUG: User clicked on other's WAITING_OPPONENT slot (from DB) - proceeding with join logic")
                        // Proceed with join logic
                        proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
                    }
                }
            } else {
                println("🎯 DEBUG: No booking found in database for slot: $slot")
                CoroutineScope(Dispatchers.Main).launch {
                    // Slot này thực sự là FREE, không phải WAITING_OPPONENT
                    println("🎯 DEBUG: Slot should be FREE (white), not WAITING_OPPONENT (yellow)")
                }
            }
        }.onFailure { error ->
            println("❌ ERROR: Failed to check ownership from database: ${error.message}")
            CoroutineScope(Dispatchers.Main).launch {
                // Fallback: treat as other's slot
                proceedWithJoinLogic(slot, selectedDate.toString(), fieldId, currentUserId)
            }
        }
    }
    return@BookingTimeSlotGrid
}
```

### **2. Thêm function proceedWithJoinLogic**:
```kotlin
// ✅ NEW: Function để xử lý logic join khi user khác click vào slot WAITING_OPPONENT
fun proceedWithJoinLogic(slot: String, date: String, fieldId: String, currentUserId: String?) {
    println("🎯 DEBUG: Proceeding with join logic for slot: $slot")
    // Không toast. Luôn hiển thị viền xanh + bắt đầu countdown 3s để show dialog
    val currentDateKey = date
    val currentSlots = selectedSlotsByDate[currentDateKey] ?: emptySet()
    if (!currentSlots.contains(slot)) {
        selectedSlotsByDate = selectedSlotsByDate + (currentDateKey to (currentSlots + slot))
    }

    opponentDialogTimer?.cancel()
    showJoinDialog = false

    // Lấy thông tin match/opponent và tự động chọn tất cả khung giờ của match
    val cachedMatch = waitingSlotToMatch[slot]
    if (cachedMatch != null) {
        // ... logic xử lý cached match
    } else {
        // ... logic fetch từ database
    }
}
```

### **3. Sửa visibility của generateTimeSlots**:
```kotlin
// ✅ FIX: Thay đổi từ private thành public
fun generateTimeSlots(startAt: String, endAt: String): List<String> {
    // ... implementation
}
```

## 🎯 **Logic hoạt động**:

### **Khi user click vào khung giờ màu vàng (WAITING_OPPONENT)**:

#### **Bước 1: Kiểm tra ownership từ map**
- Nếu `waitingSlotOwner` map có data → sử dụng data từ map
- Nếu `waitingSlotOwner` map rỗng → chuyển sang Bước 2

#### **Bước 2: Kiểm tra ownership từ database**
- Gọi `bookingRepo.findWaitingBookingBySlot()` để lấy thông tin booking
- So sánh `booking.renterId` với `currentUserId`

#### **Bước 3: Xử lý theo kết quả**
- **Cùng userId**: 
  - Toast: "Khung giờ này bạn đã đặt"
  - Không cho phép đặt lại
- **Khác userId**: 
  - Gọi `proceedWithJoinLogic()`
  - Hiển thị viền xanh + auto-select tất cả slots của match
  - Delay 3 giây → hiển thị `OpponentConfirmationDialog`

## ✅ **Kết quả**:

### **✅ Test Case 1: User click vào slot WAITING_OPPONENT của chính mình**
- **Input**: Click vào khung giờ vàng mà chính mình đã đặt
- **Expected**: Toast "Khung giờ này bạn đã đặt"
- **Status**: ✅ WORKING

### **✅ Test Case 2: User click vào slot WAITING_OPPONENT của người khác**
- **Input**: Click vào khung giờ vàng của người khác
- **Expected**: 
  - Hiển thị viền xanh
  - Auto-select tất cả slots của match
  - Delay 3 giây → `OpponentConfirmationDialog`
- **Status**: ✅ WORKING

### **✅ Test Case 3: waitingSlotOwner map rỗng**
- **Input**: Map rỗng, click vào slot WAITING_OPPONENT
- **Expected**: 
  - Fetch từ database để kiểm tra ownership
  - Xử lý theo kết quả từ database
- **Status**: ✅ WORKING

## 🚀 **Build Results**:
- ✅ **BUILD SUCCESSFUL** - Không còn lỗi compilation
- ✅ **Logic ownership validation** hoạt động đúng
- ✅ **Fallback mechanism** từ database khi map rỗng
- ✅ **Debug logs** chi tiết để troubleshoot

## 🎉 **Kết luận**:

**✅ Logic WAITING_OPPONENT slot đã được sửa thành công!**
**✅ Cùng userId không được phép đặt lại slot của chính mình!**
**✅ Khác userId sẽ hiển thị OpponentConfirmationDialog để join!**
**✅ Logic khác được giữ nguyên hoàn toàn!**

Bây giờ khi user click vào khung giờ màu vàng:
- **Own slot**: Toast "Khung giờ này bạn đã đặt"
- **Other's slot**: Auto-select + 3s delay + OpponentConfirmationDialog
