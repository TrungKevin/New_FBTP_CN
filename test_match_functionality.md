# Test Match Functionality

## Test Steps

1. **Renter A creates a "find opponent" booking:**
   - Select slots (e.g., 13:00-14:00)
   - Choose "Chưa, tìm đối thủ" 
   - Click "Xác nhận đặt"
   - Expected: Slots turn yellow, Match document created with status "WAITING_OPPONENT"

2. **Renter B joins as opponent:**
   - Click on yellow slots of Renter A
   - Wait for OpponentConfirmationDialog
   - Click "Xác nhận"
   - Expected: Slots turn red, Match document updated with status "FULL" and both participants

3. **Owner checks "Trận đấu" tab:**
   - Go to OwnerBookingListScreen
   - Switch to "Trận đấu" tab
   - Expected: Match card shows both participants

## Debug Logs to Check

Look for these debug messages in the logs:

```
🔍 DEBUG: RenterBookingCheckoutScreen - Button clicked:
🔍 DEBUG: BookingViewModel.create called:
🔍 DEBUG: Using createWaitingOpponentBooking
🔍 DEBUG: createWaitingOpponentBooking called:
🔍 DEBUG: About to commit batch...
✅ DEBUG: createWaitingOpponentBooking completed successfully
```

And for joining:

```
🔍 DEBUG: joinOpponent called with matchId:
🔍 DEBUG: Match document exists: true
🔍 DEBUG: Match status: WAITING_OPPONENT
✅ DEBUG: joinOpponent completed successfully
```

## Expected Firebase Data

### Match Document Structure:
```json
{
  "rangeKey": "fieldId2025093013001400",
  "fieldId": "fieldId",
  "date": "2025-09-30",
  "startAt": "13:00",
  "endAt": "14:00",
  "capacity": 2,
  "occupiedCount": 2,
  "participants": [
    {
      "bookingId": "bookingA",
      "renterId": "renterA",
      "side": "A"
    },
    {
      "bookingId": "bookingB", 
      "renterId": "renterB",
      "side": "B"
    }
  ],
  "price": 150000,
  "totalPrice": 150000,
  "status": "FULL",
  "matchType": "SINGLE",
  "notes": null,
  "createdAt": 1696000000000
}
```

### Booking Documents:
- Booking A: `bookingType: "SOLO"`, `hasOpponent: true`, `matchId: "fieldId2025093013001400"`
- Booking B: `bookingType: "DUO"`, `hasOpponent: true`, `matchId: "fieldId2025093013001400"`
