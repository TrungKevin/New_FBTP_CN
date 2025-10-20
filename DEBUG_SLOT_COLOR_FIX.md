# DEBUG: Slot Màu Đỏ Không Chuyển Thành Trắng Sau Khi Owner Hủy Match

## 🔍 Vấn Đề
Trong hình, các slot 21:00, 21:30, 22:00 vẫn hiển thị màu đỏ sau khi owner hủy match. Điều này có nghĩa là logic chưa hoạt động đúng.

## 🎯 Expected Behavior
Khi **Owner hủy booking của 2 renter (FULL match)**:
1. **Backend**: Match status → `CANCELLED`
2. **Real-time Update**: `FieldViewModel` listener detect change
3. **UI Update**: `loadOpponentTimes()` filter out `CANCELLED` matches
4. **Visual Result**: `BookingTimeSlotGrid` hiển thị màu trắng (có thể đặt lại)

## 🔧 Debug Steps

### Step 1: Kiểm tra Real-time Listener
```kotlin
// Trong FieldViewModel.startRealtimeSlotsForDate()
dayMatchesListener = bookingRepo.listenMatchesByFieldDate(
    fieldId = fieldId,
    date = date,
    onChange = { matches ->
        println("🔄 DEBUG: Real-time listener triggered!")
        println("  - Matches count: ${matches.size}")
        matches.forEach { match ->
            println("  - Match ${match.rangeKey}: status=${match.status}, participants=${match.participants.size}")
        }
        // khi matches thay đổi, reload các nguồn màu
        loadBookedStartTimes(fieldId, date)
        loadOpponentTimes(fieldId, date)
    },
    onError = { e -> println("❌ ERROR: startRealtimeSlotsForDate: ${e.message}") }
)
```

### Step 2: Kiểm tra getLockedBookings Logic
```kotlin
// Trong BookingRepository.getLockedBookings()
val activeMatchIds = activeMatchesSnap.toObjects(Match::class.java)
    .filter { it.status == "FULL" || it.status == "CONFIRMED" } // ✅ Chỉ lấy FULL/CONFIRMED
    .map { it.rangeKey }
    .toSet()

println("🔍 DEBUG: Active matches for field $fieldId on $date:")
activeMatchIds.forEach { matchId ->
    println("  - matchId: $matchId")
}

// Nếu không có active matches, locked bookings = 0
if (activeMatchIds.isEmpty()) {
    println("✅ DEBUG: No active matches (FULL/CONFIRMED) => locked bookings = 0")
    return Result.success(emptyList())
}
```

### Step 3: Kiểm tra loadOpponentTimes
```kotlin
// Trong FieldViewModel.loadOpponentTimes()
val waiting = repo.getWaitingOpponentBookings(fieldId, date)
val locked = repo.getLockedBookings(fieldId, date)
val waitingTimes = waiting.getOrNull()?.flatMap { it.consecutiveSlots }?.toSet() ?: emptySet()
val lockedTimes = locked.getOrNull()?.flatMap { it.consecutiveSlots }?.toSet() ?: emptySet()

println("✅ DEBUG: LoadOpponentTimes results:")
println("  - waitingTimes: $waitingTimes")
println("  - lockedTimes: $lockedTimes")
println("🔄 DEBUG: After Owner cancels FULL match, lockedTimes should be empty")
println("🔄 DEBUG: This will make BookingTimeSlotGrid show WHITE color")
```

## 🧪 Test Case

### Test Scenario:
1. **Tạo FULL match** (2 renter đã match)
2. **Owner cancel match** từ `OwnerBookingListScreen`
3. **Kiểm tra logs** để confirm:
   - Match status → `CANCELLED`
   - Real-time listener được trigger
   - `getLockedBookings` return empty list
   - `lockedTimes` = empty set
   - UI hiển thị màu trắng

### Expected Logs:
```
🔄 DEBUG: Real-time listener triggered!
  - Matches count: 1
  - Match fieldId20241020121000213000: status=CANCELLED, participants=0
🔍 DEBUG: Active matches for field fieldId on 2024-10-20:
✅ DEBUG: No active matches (FULL/CONFIRMED) => locked bookings = 0
✅ DEBUG: LoadOpponentTimes results:
  - waitingTimes: []
  - lockedTimes: []
🔄 DEBUG: After Owner cancels FULL match, lockedTimes should be empty
🔄 DEBUG: This will make BookingTimeSlotGrid show WHITE color
```

## 🔧 Potential Fixes

### Fix 1: Thêm Debug Logs Chi Tiết
```kotlin
// Trong BookingRepository.listenMatchesByFieldDate()
return firestore.collection(MATCHES_COLLECTION)
    .whereEqualTo("fieldId", fieldId)
    .whereEqualTo("date", date)
    .addSnapshotListener { snapshot, e ->
        if (e != null) { 
            println("❌ ERROR: listenMatchesByFieldDate error: ${e.message}")
            onError(e); 
            return@addSnapshotListener 
        }
        val list = snapshot?.toObjects(Match::class.java) ?: emptyList()
        println("✅ DEBUG: listenMatchesByFieldDate result:")
        println("  - snapshot size: ${snapshot?.size() ?: 0}")
        println("  - matches found: ${list.size}")
        list.forEachIndexed { index, match ->
            println("  [$index] matchId: ${match.rangeKey}, status: ${match.status}, participants: ${match.participants.size}")
        }
        onChange(list)
    }
```

### Fix 2: Force Refresh UI
```kotlin
// Trong FieldViewModel.startRealtimeSlotsForDate()
onChange = { matches ->
    println("🔄 DEBUG: Real-time listener triggered!")
    // Force refresh UI
    loadBookedStartTimes(fieldId, date)
    loadOpponentTimes(fieldId, date)
    
    // Additional debug
    println("🔄 DEBUG: Forcing UI refresh after match change")
}
```

### Fix 3: Kiểm tra Timing
```kotlin
// Thêm delay để đảm bảo Firebase update hoàn tất
onChange = { matches ->
    println("🔄 DEBUG: Real-time listener triggered!")
    // Delay để đảm bảo Firebase update hoàn tất
    delay(100)
    loadBookedStartTimes(fieldId, date)
    loadOpponentTimes(fieldId, date)
}
```

## 📱 Test Instructions

1. **Build và chạy app**
2. **Tạo FULL match** (2 renter)
3. **Owner cancel match**
4. **Kiểm tra logs** trong Android Studio Logcat
5. **Kiểm tra UI** - slots phải chuyển từ đỏ → trắng
6. **Report kết quả** với logs chi tiết

## 🎯 Success Criteria

- ✅ Match status → `CANCELLED`
- ✅ Real-time listener được trigger
- ✅ `getLockedBookings` return empty list
- ✅ `lockedTimes` = empty set
- ✅ UI hiển thị màu trắng
- ✅ Slots có thể được đặt lại

## 📝 Notes

- Vấn đề có thể là **timing** - Firebase update chưa hoàn tất khi listener được trigger
- Có thể cần **force refresh** UI sau khi detect change
- **Debug logs** sẽ giúp xác định chính xác vấn đề ở đâu
