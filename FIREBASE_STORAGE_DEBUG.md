# 🔧 Debug Firebase Storage Avatar Upload

## 🎯 **Vấn đề hiện tại**
- Avatar URL trong Firebase là chuỗi rỗng `""`
- Chức năng upload avatar không hoạt động
- Cần kiểm tra Firebase Storage permissions và debug logs

## 🔍 **Các bước debug**

### **1. Kiểm tra Firebase Storage Security Rules**

Truy cập Firebase Console → Storage → Rules và đảm bảo có rules sau:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow authenticated users to upload avatars
    match /avatars/{userId}/{fileName} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to upload field images
    match /field_images/{fileName} {
      allow read, write: if request.auth != null;
    }
    
    // Allow public read access to avatars and field images
    match /avatars/{allPaths=**} {
      allow read: if true;
    }
    
    match /field_images/{allPaths=**} {
      allow read: if true;
    }
  }
}
```

### **2. Kiểm tra Debug Logs**

Đã thêm debug logs vào EditProfileScreen:
```kotlin
println("🔄 DEBUG: Starting avatar upload process...")
println("🔄 DEBUG: selectedImageUri: $selectedImageUri")
println("🔄 DEBUG: userId: ${currentUser.userId}")
println("✅ DEBUG: Avatar upload successful: $avatarUrl")
println("🔄 DEBUG: Profile update result: ok=$ok, msg=$msg")
```

### **3. Kiểm tra ImageUploadService**

Service đã có debug logs:
```kotlin
println("🔄 DEBUG: Starting avatar upload for user: $userId")
println("🔄 DEBUG: Uploading to path: avatars/$fileName")
println("✅ DEBUG: Avatar upload successful: $downloadUrl")
println("❌ ERROR: Avatar upload failed: ${e.message}")
```

## 🚀 **Các bước test**

### **Test 1: Kiểm tra Firebase Storage Rules**
1. Mở Firebase Console
2. Vào Storage → Rules
3. Đảm bảo có rules cho phép authenticated users upload

### **Test 2: Test upload với debug logs**
1. Mở app và vào EditProfileScreen
2. Chọn ảnh mới
3. Click "Lưu thay đổi"
4. Kiểm tra Logcat để xem debug logs

### **Test 3: Kiểm tra Firebase Storage**
1. Mở Firebase Console
2. Vào Storage → Files
3. Kiểm tra có folder "avatars" và file ảnh không

## 🔧 **Các lỗi có thể gặp**

### **Lỗi 1: Permission Denied**
```
❌ ERROR: Avatar upload failed: Permission denied
```
**Giải pháp:** Cập nhật Firebase Storage Security Rules

### **Lỗi 2: Network Error**
```
❌ ERROR: Avatar upload failed: Network error
```
**Giải pháp:** Kiểm tra kết nối internet

### **Lỗi 3: Invalid URI**
```
❌ ERROR: Avatar upload failed: Invalid URI
```
**Giải pháp:** Kiểm tra selectedImageUri có hợp lệ không

## 📱 **Test trên device**

1. **Build và install app**
2. **Đăng nhập với account có userId: RI00eb40uyVHSMhe3fyfl7RlL5I2**
3. **Vào Profile → Edit Profile**
4. **Chọn ảnh mới từ gallery**
5. **Click "Lưu thay đổi"**
6. **Kiểm tra Logcat và Firebase Console**

## 🎯 **Expected Results**

### **Success Case:**
- Debug logs hiển thị upload thành công
- Firebase Storage có file ảnh mới
- Firebase Firestore có avatarUrl mới
- Profile hiển thị ảnh mới

### **Failure Case:**
- Debug logs hiển thị lỗi cụ thể
- Firebase Storage không có file mới
- Firebase Firestore avatarUrl vẫn là ""

## 🔄 **Next Steps**

1. **Test với debug logs** để xác định lỗi cụ thể
2. **Cập nhật Firebase Storage Rules** nếu cần
3. **Kiểm tra network và permissions**
4. **Verify upload success** trong Firebase Console
