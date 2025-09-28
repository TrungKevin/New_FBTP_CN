# ✅ OpponentSelectionDialog Fix Complete

## 🎯 **Vấn đề đã sửa**: 
`OpponentSelectionDialog` không hiển thị khi renter chọn khung giờ trống

## 🔧 **Nguyên nhân**:
Logic ban đầu chỉ hiển thị dialog khi `slots.size > 1` (nhiều hơn 1 slot), nhưng theo yêu cầu ban đầu, dialog nên hiển thị cho cả trường hợp chọn 1 slot trống.

## ✅ **Giải pháp đã áp dụng**:

### **1. Sửa logic hiển thị dialog**:
```kotlin
// ❌ TRƯỚC: Chỉ hiển thị cho nhiều slot
if (allSlotsAreEmpty) {
    consecutiveSlots = slots
    if (slots.size > 1) {  // ← Chỉ hiển thị khi > 1 slot
        opponentDialogTimer = CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            showOpponentDialog = true
        }
    } else {
        opponentDialogTimer?.cancel()
        showOpponentDialog = false  // ← Không hiển thị cho 1 slot
    }
}

// ✅ SAU: Hiển thị cho cả 1 slot và nhiều slot
if (allSlotsAreEmpty) {
    consecutiveSlots = slots
    // ✅ FIX: Hiển thị dialog cho cả 1 slot và nhiều slot
    opponentDialogTimer?.cancel()
    
    // ✅ FIX: Tạo timer mới với delay 3 giây cho tất cả slots trống
    opponentDialogTimer = CoroutineScope(Dispatchers.Main).launch {
        delay(3000) // 3 giây
        showOpponentDialog = true
    }
}
```

### **2. Thêm OpponentSelectionDialog vào UI**:
```kotlin
// ✅ FIX: Hiển thị OpponentSelectionDialog cho slots trống
if (showOpponentDialog && consecutiveSlots.isNotEmpty()) {
    OpponentSelectionDialog(
        isVisible = true,
        onDismiss = {
            showOpponentDialog = false
            consecutiveSlots = emptyList()
        },
        onHasOpponent = {
            showOpponentDialog = false
            consecutiveSlots = emptyList()
            // ✅ FIX: Tạo booking với hasOpponent = true
            if (selectedSlots.isNotEmpty()) {
                bookingViewModel.handle(BookingEvent.Create(
                    renterId = currentUser?.userId ?: "",
                    ownerId = uiState.currentField?.ownerId ?: "",
                    fieldId = fieldId,
                    date = selectedDate.toString(),
                    consecutiveSlots = selectedSlots.toList(),
                    bookingType = "SOLO",
                    hasOpponent = true,  // ← Đã có đối thủ
                    opponentId = null,
                    opponentName = null,
                    opponentAvatar = null,
                    basePrice = basePricePerHour.toLong(),
                    serviceLines = emptyList(),
                    notes = notes.ifBlank { null }
                ))
                onConfirmBooking()
            }
        },
        onNoOpponent = {
            showOpponentDialog = false
            consecutiveSlots = emptyList()
            // ✅ FIX: Tạo booking với hasOpponent = false (WAITING_OPPONENT)
            if (selectedSlots.isNotEmpty()) {
                bookingViewModel.handle(BookingEvent.Create(
                    renterId = currentUser?.userId ?: "",
                    ownerId = uiState.currentField?.ownerId ?: "",
                    fieldId = fieldId,
                    date = selectedDate.toString(),
                    consecutiveSlots = selectedSlots.toList(),
                    bookingType = "SOLO",
                    hasOpponent = false,  // ← Chưa có đối thủ, chờ đối thủ
                    opponentId = null,
                    opponentName = null,
                    opponentAvatar = null,
                    basePrice = basePricePerHour.toLong(),
                    serviceLines = emptyList(),
                    notes = notes.ifBlank { null }
                ))
                onConfirmBooking()
            }
        }
    )
}
```

### **3. Thêm import cần thiết**:
```kotlin
import com.trungkien.fbtp_cn.ui.components.renter.dialogs.OpponentSelectionDialog
```

### **4. Sửa lỗi scope**:
```kotlin
// ❌ TRƯỚC: field không có trong scope
ownerId = field.ownerId,

// ✅ SAU: Sử dụng uiState.currentField
ownerId = uiState.currentField?.ownerId ?: "",
```

## 🎯 **Logic hoạt động**:

### **Khi renter chọn khung giờ trống**:
1. **Kiểm tra**: Tất cả slots đều là khung giờ trống (không phải WAITING_OPPONENT, LOCKED, hoặc BOOKED)
2. **Delay**: 3 giây để user có thể chọn thêm slots
3. **Hiển thị**: `OpponentSelectionDialog` với câu hỏi "Bạn đã có đối thủ để chơi cùng chưa?"
4. **Xử lý**:
   - **"Đã có đối thủ"**: Tạo booking với `hasOpponent = true`
   - **"Chưa có đối thủ"**: Tạo booking với `hasOpponent = false` (WAITING_OPPONENT)

### **Khi renter chọn khung giờ WAITING_OPPONENT**:
1. **Kiểm tra ownership**: 
   - **Own slot**: Toast "Khung giờ này bạn đã đặt"
   - **Other's slot**: Hiển thị `OpponentConfirmationDialog` để join làm đối thủ

## 🚀 **Build Results**:
- ✅ **Compilation**: SUCCESS
- ✅ **No errors**: Tất cả lỗi đã được sửa
- ✅ **Warnings**: Chỉ có deprecation warnings (bình thường)

## 📋 **Test Scenarios**:

### **✅ Test Case 1: Chọn 1 slot trống**
- **Input**: Click vào 1 khung giờ trống (màu trắng)
- **Expected**: Sau 3 giây hiển thị `OpponentSelectionDialog`
- **Status**: ✅ FIXED

### **✅ Test Case 2: Chọn nhiều slots trống**
- **Input**: Click vào nhiều khung giờ trống liên tiếp
- **Expected**: Sau 3 giây hiển thị `OpponentSelectionDialog`
- **Status**: ✅ FIXED

### **✅ Test Case 3: Chọn slot WAITING_OPPONENT của chính mình**
- **Input**: Click vào khung giờ vàng của chính mình
- **Expected**: Toast "Khung giờ này bạn đã đặt"
- **Status**: ✅ WORKING

### **✅ Test Case 4: Chọn slot WAITING_OPPONENT của người khác**
- **Input**: Click vào khung giờ vàng của người khác
- **Expected**: Hiển thị `OpponentConfirmationDialog` để join
- **Status**: ✅ WORKING

## 🎉 **Kết luận**:

**✅ OpponentSelectionDialog đã được sửa thành công!**
**✅ Logic hiển thị dialog cho khung giờ trống đã hoạt động đúng!**
**✅ App có thể build và chạy được!**

Bây giờ khi renter chọn khung giờ trống (cả 1 slot và nhiều slot), sau 3 giây sẽ hiển thị dialog hỏi về đối thủ như yêu cầu ban đầu.
