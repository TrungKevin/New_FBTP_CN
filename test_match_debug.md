# Test Match Debug Guide

## Steps to Test Match Functionality

### Step 1: Renter A creates "find opponent" booking
1. Open the app
2. Go to a field booking screen
3. Select consecutive time slots (e.g., 13:00-14:00)
4. Wait for OpponentSelectionDialog to appear
5. Choose "Chưa, tìm đối thủ"
6. Wait for FindOpponentDialog to appear
7. Click "Xác nhận"
8. Click "Xác nhận đặt" button

**Expected Debug Logs:**
```
🔍 DEBUG: FindOpponentDialog.onConfirm called:
🔍 DEBUG: bookingMode set to: FIND_OPPONENT
🔍 DEBUG: RenterBookingCheckoutScreen - Button clicked:
🔍 DEBUG: BookingViewModel.create called:
🔍 DEBUG: Using createWaitingOpponentBooking
🔍 DEBUG: createWaitingOpponentBooking called:
🔍 DEBUG: About to commit batch...
✅ DEBUG: createWaitingOpponentBooking completed successfully
```

### Step 2: Check Firebase Console
1. Go to Firebase Console
2. Navigate to Firestore Database
3. Check `matches` collection
4. Look for a document with:
   - `status: "WAITING_OPPONENT"`
   - `occupiedCount: 1`
   - `participants` array with one participant

### Step 3: Renter B joins as opponent
1. Switch to another user account
2. Go to the same field and date
3. Click on the yellow slots (should show green border)
4. Wait for OpponentConfirmationDialog
5. Click "Xác nhận"

**Expected Debug Logs:**
```
🔍 DEBUG: joinOpponent called with matchId:
🔍 DEBUG: Match document exists: true
🔍 DEBUG: Match status: WAITING_OPPONENT
✅ DEBUG: joinOpponent completed successfully
```

### Step 4: Check Owner "Trận đấu" tab
1. Switch to owner account
2. Go to OwnerBookingListScreen
3. Switch to "Trận đấu" tab
4. Should see match card with both participants

## Common Issues to Check

1. **No debug logs appearing**: Check if app is running and logs are being captured
2. **createWaitingOpponentBooking not called**: Check if bookingMode is set correctly
3. **Match document not created**: Check Firestore rules and batch write
4. **Match not appearing in owner tab**: Check if status is "FULL" and filtering logic

## Debug Commands
```bash
# Clear logs
adb logcat -c

# Monitor logs
adb logcat | findstr "DEBUG\|ERROR\|✅\|❌"

# Check specific app logs
adb logcat -s "com.trungkien.fbtp_cn"
```
