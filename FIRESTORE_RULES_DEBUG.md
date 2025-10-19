# 🔧 FIRESTORE RULES & NOTIFICATION CLEANUP

## 🎯 **Vấn đề hiện tại:**

### ❌ **PERMISSION_DENIED:**
```
❌ ERROR: NotificationRepository.listenNotificationsByUser - PERMISSION_DENIED: Missing or insufficient permissions.
❌ ERROR: NotificationRepository.listenUnreadNotificationCount - PERMISSION_DENIED: Missing or insufficient permissions.
```

### ⚠️ **isRead Field Warnings:**
```
Firestore W (25.0.0) [CustomClassMapper]: No setter/field for isRead found on class com.trungkien.fbtp_cn.model.Notification
```

## 🔍 **Phân tích:**

### **1. PERMISSION_DENIED:**
- Rules đã được deploy thành công
- Logic rules đã đúng: `allow read: if signedIn() && resource.data.toUserId == request.auth.uid`
- Có thể do **cache** hoặc **timing issue**

### **2. isRead Field Warnings:**
- Model chỉ có field `read`, không có `isRead`
- Warnings xuất hiện khi đọc notifications cũ từ database
- Có thể có notifications cũ vẫn có field `isRead`

## ✅ **Giải pháp:**

### **1. Clear Firebase Cache:**
```bash
# Restart app để clear cache
adb shell am force-stop com.trungkien.fbtp_cn
```

### **2. Test với user khác:**
- User hiện tại: `RI00eb40uyVHSMhe3fyfl7RlL5I2` (Owner)
- User khác: `PQI6i9abPOO1jDQQYD6BStJkNdP2` (Renter)

### **3. Kiểm tra Firebase Console:**
- Vào Firebase Console → Firestore → notifications collection
- Kiểm tra có notifications nào có field `isRead` không
- Xóa notifications cũ nếu cần

### **4. Test notification creation:**
- Tạo notification mới để test rules
- Kiểm tra logs có `PERMISSION_DENIED` không

## 🧪 **Test Steps:**

### **Step 1: Clear Cache**
```bash
adb shell am force-stop com.trungkien.fbtp_cn
# Restart app
```

### **Step 2: Test Owner Notifications**
- Login với Owner account
- Vào màn hình notifications
- Kiểm tra logs có `PERMISSION_DENIED` không

### **Step 3: Test Renter Notifications**
- Login với Renter account  
- Vào màn hình notifications
- Kiểm tra logs có `PERMISSION_DENIED` không

### **Step 4: Test Notification Creation**
- Owner confirm booking
- Kiểm tra notification có được tạo không
- Kiểm tra logs có `PERMISSION_DENIED` không

## 🔧 **Nếu vẫn không hoạt động:**

### **Option 1: Temporary Open Rules**
```javascript
// NOTIFICATIONS - TEMPORARY OPEN FOR DEBUGGING
match /notifications/{notificationId} {
  allow read, write: if signedIn();
}
```

### **Option 2: Check User Authentication**
- Kiểm tra user có đăng nhập đúng không
- Kiểm tra `request.auth.uid` có đúng không

### **Option 3: Debug Rules**
- Thêm debug logs vào rules
- Kiểm tra từng condition

## 📊 **Expected Results:**

### **After Fix:**
```
✅ DEBUG: NotificationRepository.listenNotificationsByUser - Loaded X notifications for user [user_id]
✅ DEBUG: NotificationViewModel.loadNotifications - Loaded X notifications
🔔 DEBUG: NotificationRepository.listenUnreadNotificationCount - User [user_id] has X unread notifications
```

### **No More:**
```
❌ ERROR: NotificationRepository.listenNotificationsByUser - PERMISSION_DENIED
❌ ERROR: NotificationRepository.listenUnreadNotificationCount - PERMISSION_DENIED
```

## 🎯 **Next Steps:**

1. **Clear app cache** và restart
2. **Test với cả Owner và Renter**
3. **Kiểm tra Firebase Console** cho notifications cũ
4. **Test notification creation** khi owner confirm booking
5. **Verify dual flow notifications** hoạt động

**Hãy thử clear cache và test lại!** 🚀
