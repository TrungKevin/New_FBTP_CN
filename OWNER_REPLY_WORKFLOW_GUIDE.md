# 🎯 Hướng dẫn Workflow Reply của Owner

## 📋 Tổng quan
Hệ thống reply của owner hoạt động giống Facebook - khi owner reply vào đánh giá nào thì reply đó sẽ hiển thị ngay dưới đánh giá đó.

## 🔧 Cải thiện đã thực hiện

### 1. **UI Components**
- ✅ Cải thiện `ReviewItem.kt` với layout giống Facebook
- ✅ Thêm `ReplyInputBox` component đẹp mắt
- ✅ Cải thiện `ReplyItem` hiển thị avatar, tên, badge "Chủ sân"
- ✅ Sắp xếp lại thứ tự: Actions → Reply Box → Replies List

### 2. **Firebase Integration**
- ✅ Sửa field mapping warnings (`verified`, `anonymous`)
- ✅ Cải thiện logic `addReply()` trong Repository
- ✅ Cập nhật UI ngay lập tức trong ViewModel

### 3. **Workflow Logic**
```kotlin
// 1. Owner click nút "Phản hồi"
onReply = { text ->
    currentUser?.let { user ->
        viewModel.handleEvent(
            EvaluateCourtEvent.AddReply(
                reviewId = review.reviewId,
                reply = Reply(
                    userId = user.userId,
                    userName = user.name,
                    userAvatar = user.avatarUrl,
                    userRole = "OWNER",
                    comment = text,
                    isOwner = true
                )
            )
        )
    )
}
```

## 🎨 UI Flow

### **Trước khi reply:**
```
┌─────────────────────────────────┐
│ 👤 CrisMessi                    │
│ ⭐⭐⭐⭐⭐ 2/5                    │
│ "Bad"                           │
│ [❤️ 0] [💬 Phản hồi] [⚠️ Báo cáo] │
└─────────────────────────────────┘
```

### **Khi click "Phản hồi":**
```
┌─────────────────────────────────┐
│ 👤 CrisMessi                    │
│ ⭐⭐⭐⭐⭐ 2/5                    │
│ "Bad"                           │
│ [❤️ 0] [💬 Phản hồi] [⚠️ Báo cáo] │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ [📝 Viết phản hồi...]       │ │
│ │                    [Hủy][Gửi] │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

### **Sau khi gửi reply:**
```
┌─────────────────────────────────┐
│ 👤 CrisMessi                    │
│ ⭐⭐⭐⭐⭐ 2/5                    │
│ "Bad"                           │
│ [❤️ 0] [💬 Phản hồi] [⚠️ Báo cáo] │
│                                 │
│ 👤 Owner Name [Chủ sân] ⚙️      │
│ 16/09/2025 08:30                │
│ "Cảm ơn bạn đã phản hồi..."     │
└─────────────────────────────────┘
```

## 🚀 Cách test

### **Bước 1: Mở app với tài khoản Owner**
```kotlin
// Đảm bảo isOwner = true
EvaluateCourt(
    fieldId = "Q9FLuAC7s7jRSFgba68B",
    currentUser = ownerUser,
    isOwner = true, // ← Quan trọng!
    viewModel = evaluateViewModel
)
```

### **Bước 2: Tìm review có sẵn**
- Vào màn hình đánh giá sân
- Tìm review của khách hàng (ví dụ: CrisMessi - 2/5 "Bad")

### **Bước 3: Test reply workflow**
1. **Click nút "Phản hồi"** → Reply box xuất hiện
2. **Nhập nội dung** → Ví dụ: "Cảm ơn bạn đã phản hồi. Chúng tôi sẽ cải thiện dịch vụ."
3. **Click "Gửi"** → Reply xuất hiện ngay lập tức dưới review
4. **Kiểm tra Firebase** → Reply được lưu vào subcollection

### **Bước 4: Test các tính năng khác**
- ✅ **Edit reply**: Click ⚙️ → "Chỉnh sửa"
- ✅ **Delete reply**: Click ⚙️ → "Xóa"
- ✅ **Badge "Chủ sân"**: Hiển thị cho owner replies
- ✅ **Avatar**: Hiển thị avatar của owner

## 🔍 Debug Logs

Khi test, theo dõi logs:
```
✅ DEBUG: Đã thêm reply thành công với ID: [replyId]
✅ DEBUG: Đã load [X] reviews cho sân [fieldId]
✅ DEBUG: Show reply dialog: true/false
```

## 📱 Firebase Structure

```
reviews/
├── [reviewId]/
│   ├── fieldId: "Q9FLuAC7s7jRSFgba68B"
│   ├── renterId: "user123"
│   ├── rating: 2
│   ├── comment: "Bad"
│   ├── replies: [
│   │   {
│   │     replyId: "reply123",
│   │     userId: "owner456",
│   │     userName: "Owner Name",
│   │     userRole: "OWNER",
│   │     comment: "Cảm ơn bạn...",
│   │     isOwner: true,
│   │     createdAt: Timestamp
│   │   }
│   │ ]
│   └── replies/ (subcollection)
│       └── [replyId]/
│           ├── userId: "owner456"
│           ├── userName: "Owner Name"
│           ├── comment: "Cảm ơn bạn..."
│           └── isOwner: true
```

## ⚡ Performance Tips

1. **Optimistic Updates**: UI cập nhật ngay lập tức
2. **Background Sync**: Firebase sync chạy ngầm
3. **Avatar Caching**: Avatar được cache để load nhanh
4. **Lazy Loading**: Replies chỉ load khi cần

## 🐛 Troubleshooting

### **Reply không hiển thị:**
- Kiểm tra `isOwner = true`
- Kiểm tra Firebase permissions
- Xem logs để debug

### **UI không cập nhật:**
- Kiểm tra ViewModel state
- Đảm bảo `loadReviews()` được gọi
- Kiểm tra network connection

### **Firebase errors:**
- Kiểm tra field mapping (`verified`, `anonymous`)
- Kiểm tra collection permissions
- Xem Firebase console logs
