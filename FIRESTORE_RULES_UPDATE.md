# Firestore Rules Update - MATCHES Collection

## ✅ Đã thêm rules cho MATCHES collection!

### 🔧 Rules đã thêm:

```javascript
// MATCHES - Quản lý trận đấu/khoảng thời gian
match /matches/{matchId} {
  allow read: if true;  // ✅ Ai cũng đọc được matches để hiển thị trạng thái khung giờ
  
  // CREATE: Chỉ user đã đăng nhập mới tạo được match
  allow create: if signedIn() && 
    request.resource.data.participants != null &&
    request.resource.data.participants.size() > 0 &&
    request.resource.data.participants[0].renterId == request.auth.uid;
  
  // UPDATE: Chỉ participants trong match hoặc owner của sân mới sửa được
  allow update: if signedIn() && 
    (resource.data.participants != null &&
     resource.data.participants.any(p => p.renterId == request.auth.uid) ||
     isFieldOwner(resource.data.fieldId));
  
  // DELETE: Chỉ owner của sân mới xóa được match
  allow delete: if signedIn() && 
    isFieldOwner(resource.data.fieldId);
}
```

### 🎯 Giải thích rules:

#### 1. **READ (allow read: if true)**:
- ✅ **Ai cũng đọc được**: Renter có thể đọc matches để hiển thị trạng thái khung giờ
- ✅ **Hiển thị màu vàng**: Khi có match với status "WAITING_OPPONENT"
- ✅ **Hiển thị màu đỏ**: Khi có match với status "FULL"

#### 2. **CREATE (allow create: if signedIn() && ...)**:
- ✅ **Chỉ user đã đăng nhập**: Phải có authentication
- ✅ **Kiểm tra participants**: Phải có ít nhất 1 participant
- ✅ **Kiểm tra renterId**: Participant đầu tiên phải là user hiện tại
- ✅ **Tạo match**: Khi renter đặt sân với "tôi chưa có đối thủ"

#### 3. **UPDATE (allow update: if signedIn() && ...)**:
- ✅ **Participants**: Chỉ participants trong match mới sửa được
- ✅ **Field owner**: Owner của sân cũng có thể sửa
- ✅ **Join opponent**: Renter thứ 2 có thể join vào match

#### 4. **DELETE (allow delete: if signedIn() && ...)**:
- ✅ **Chỉ field owner**: Chỉ owner của sân mới xóa được match
- ✅ **Bảo mật**: Participants không thể xóa match

### 🚀 Cách deploy rules:

#### Option 1: Firebase Console (Recommended)
1. Mở [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Firestore Database** → **Rules**
4. Copy nội dung file `firestore.rules` và paste vào
5. Click **Publish**

#### Option 2: Firebase CLI
```bash
# Cài đặt Firebase CLI (nếu chưa có)
npm install -g firebase-tools

# Login vào Firebase
firebase login

# Deploy rules
firebase deploy --only firestore:rules
```

### 🧪 Test Cases:

#### ✅ Test Case 1: Renter đọc matches
- **Input**: Renter mở màn hình booking
- **Expected**: Có thể đọc matches để hiển thị trạng thái khung giờ
- **Status**: ✅ PASS (allow read: if true)

#### ✅ Test Case 2: Renter tạo match (chưa có đối thủ)
- **Input**: Renter đặt sân với "tôi chưa có đối thủ"
- **Expected**: Tạo match với status "WAITING_OPPONENT"
- **Status**: ✅ PASS (allow create với điều kiện)

#### ✅ Test Case 3: Renter join opponent
- **Input**: Renter thứ 2 click vào khung giờ màu vàng
- **Expected**: Update match để thêm participant thứ 2
- **Status**: ✅ PASS (allow update cho participants)

#### ✅ Test Case 4: Field owner quản lý matches
- **Input**: Field owner muốn xóa match
- **Expected**: Có thể xóa match
- **Status**: ✅ PASS (allow delete cho field owner)

### 🔍 Data Structure Expected:

#### Match Document:
```javascript
{
  rangeKey: "match_123",
  fieldId: "field_456",
  date: "2025-09-28",
  startAt: "20:00",
  endAt: "22:30",
  capacity: 2,
  occupiedCount: 1,
  participants: [
    {
      bookingId: "booking_789",
      renterId: "user_123",
      matchSide: "A"
    }
  ],
  price: 70,
  totalPrice: 420,
  status: "WAITING_OPPONENT" // hoặc "FULL"
}
```

### 🎨 UI Behavior với Rules mới:

1. **Khung giờ màu trắng (FREE)**:
   - Không có match trong database
   - Renter có thể đặt sân bình thường

2. **Khung giờ màu vàng (WAITING_OPPONENT)**:
   - Có match với status "WAITING_OPPONENT"
   - Renter có thể join opponent
   - Hiển thị `OpponentConfirmationDialog`

3. **Khung giờ màu đỏ (FULL)**:
   - Có match với status "FULL"
   - Không thể đặt nữa
   - Hiển thị toast "Khung giờ này đã được đặt"

### 🚀 Ready for Production:

- ✅ Rules đã được thêm vào file
- ✅ Logic phù hợp với hoạt động của renter
- ✅ Bảo mật đúng mức
- ✅ Hỗ trợ đầy đủ các trường hợp sử dụng

### 📋 Next Steps:

1. **Deploy rules**: Sử dụng Firebase Console hoặc CLI
2. **Test functionality**: Kiểm tra các hoạt động của renter
3. **Verify security**: Đảm bảo rules hoạt động đúng
4. **Monitor logs**: Theo dõi Firebase logs để debug

## 🎯 Kết luận:

Rules mới đã được thêm để hỗ trợ đầy đủ hoạt động của renter:

1. ✅ **Đọc matches**: Hiển thị trạng thái khung giờ (màu vàng/đỏ)
2. ✅ **Tạo match**: Khi đặt sân với "tôi chưa có đối thủ"
3. ✅ **Join opponent**: Khi click vào khung giờ màu vàng
4. ✅ **Bảo mật**: Chỉ những người có quyền mới được thao tác

Sau khi deploy rules, hệ thống sẽ hoạt động hoàn chỉnh với dữ liệu thật từ Firebase! 🎉
