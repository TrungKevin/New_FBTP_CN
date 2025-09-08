# 🧪 Avatar Save Test Guide - Hướng dẫn test lưu avatar

## 🎯 **Mục tiêu:**
Đảm bảo avatar được lưu thành công vào field `avatarUrl` trong model User trong Firebase Firestore.

## 🔍 **Debug Logs đã thêm:**

### **1. Image Picker:**
```
🔄 DEBUG: Avatar clicked, launching image picker...
🔄 DEBUG: Image picker result: content://media/external/images/media/XXXX
🔄 DEBUG: Setting selectedImageUri to: content://media/external/images/media/XXXX
🔄 DEBUG: selectedImageUri set successfully
```

### **2. Save Button:**
```
🔄 DEBUG: Save button clicked
🔄 DEBUG: selectedImageUri: content://media/external/images/media/XXXX
🔄 DEBUG: currentUser?.userId: RI00eb40uyVHSMhe3fyf17R1L5I2
🔄 DEBUG: ownerName: Kien
🔄 DEBUG: ownerEmail: ronaldo@gmail.com
🔄 DEBUG: ownerPhone: 0921483538
🔄 DEBUG: Starting avatar upload process...
```

### **3. Image Upload Service:**
```
🔄 DEBUG: Starting avatar upload for user: RI00eb40uyVHSMhe3fyf17R1L5I2
🔄 DEBUG: Converting image to base64...
✅ DEBUG: Base64 conversion successful, size: XXXXX chars
🔄 DEBUG: Base64 length: XXXXX characters
```

### **4. User Repository:**
```
🔄 DEBUG: Updating avatarUrl in Firestore...
🔄 DEBUG: avatarUrl length: XXXXX
🔄 DEBUG: avatarUrl first 100 chars: /9j/4AAQSkZJRgABAQAAAQABAAD...
🔄 DEBUG: Firestore updates: {avatarUrl=/9j/4AAQSkZJRgABAQAAAQABAAD..., updatedAt=XXXX}
✅ DEBUG: Firestore update successful
```

### **5. Profile Update Result:**
```
🔄 DEBUG: Profile update result: ok=true, msg=null
```

## 🧪 **Test Steps:**

### **Step 1: Test Image Selection**
1. **Mở app** và đăng nhập
2. **Vào Profile → Edit Profile** (ModernEditProfileScreen)
3. **Click vào avatar** (circle với camera icon)
4. **Kiểm tra logs:** Tìm `🔄 DEBUG: Avatar clicked, launching image picker...`
5. **Chọn ảnh** từ gallery
6. **Kiểm tra logs:** Tìm `🔄 DEBUG: Image picker result: content://...`
7. **Kiểm tra logs:** Tìm `🔄 DEBUG: selectedImageUri set successfully`

### **Step 2: Test Save Process**
1. **Click "Lưu thay đổi"**
2. **Kiểm tra logs:** Tìm `🔄 DEBUG: Save button clicked`
3. **Kiểm tra logs:** Tìm `🔄 DEBUG: selectedImageUri: content://...`
4. **Kiểm tra logs:** Tìm `🔄 DEBUG: Starting avatar upload process...`

### **Step 3: Test Avatar Upload**
1. **Kiểm tra logs:** Tìm `🔄 DEBUG: Converting image to base64...`
2. **Kiểm tra logs:** Tìm `✅ DEBUG: Base64 conversion successful`
3. **Kiểm tra logs:** Tìm `🔄 DEBUG: Updating avatarUrl in Firestore...`
4. **Kiểm tra logs:** Tìm `✅ DEBUG: Firestore update successful`

### **Step 4: Test Firebase Console**
1. **Mở Firebase Console**
2. **Vào Firestore Database**
3. **Chọn collection `users`**
4. **Chọn document `RI00eb40uyVHSMhe3fyf17R1L5I2`**
5. **Kiểm tra field `avatarUrl`** có giá trị base64 mới

## 🎯 **Expected Results:**

### **✅ Nếu thành công:**
```
🔄 DEBUG: Avatar clicked, launching image picker...
🔄 DEBUG: Image picker result: content://media/external/images/media/XXXX
🔄 DEBUG: Setting selectedImageUri to: content://media/external/images/media/XXXX
🔄 DEBUG: selectedImageUri set successfully
🔄 DEBUG: Save button clicked
🔄 DEBUG: selectedImageUri: content://media/external/images/media/XXXX
🔄 DEBUG: Starting avatar upload process...
🔄 DEBUG: Converting image to base64...
✅ DEBUG: Base64 conversion successful, size: XXXXX chars
🔄 DEBUG: Updating avatarUrl in Firestore...
🔄 DEBUG: Firestore updates: {avatarUrl=/9j/4AAQSkZJRgABAQAAAQABAAD..., updatedAt=XXXX}
✅ DEBUG: Firestore update successful
🔄 DEBUG: Profile update result: ok=true, msg=null
```

**Firebase Console:** Field `avatarUrl` có giá trị base64 string dài (50,000-100,000 characters)

### **❌ Nếu có vấn đề:**

#### **Vấn đề 1: Không có logs từ image picker**
- **Nguyên nhân:** User chưa click vào avatar
- **Giải pháp:** Click vào avatar để mở image picker

#### **Vấn đề 2: Image picker returned null**
- **Nguyên nhân:** User cancel hoặc không chọn ảnh
- **Giải pháp:** Chọn ảnh từ gallery

#### **Vấn đề 3: selectedImageUri is null**
- **Nguyên nhân:** Image selection failed
- **Giải pháp:** Kiểm tra permissions hoặc thử ảnh khác

#### **Vấn đề 4: Base64 conversion failed**
- **Nguyên nhân:** Ảnh không hợp lệ hoặc quá lớn
- **Giải pháp:** Thử ảnh khác hoặc kiểm tra permissions

#### **Vấn đề 5: Firestore update failed**
- **Nguyên nhân:** Firebase rules hoặc network issue
- **Giải pháp:** Kiểm tra Firebase connection và rules

## 🔧 **Troubleshooting:**

### **1. Kiểm tra Permissions:**
- Đảm bảo app có quyền đọc storage
- Kiểm tra `AndroidManifest.xml` có `READ_EXTERNAL_STORAGE`

### **2. Kiểm tra Firebase Rules:**
```javascript
match /users/{uid} {
  allow create: if isSelf(uid);
  allow read, update, delete: if isSelf(uid);
}
```

### **3. Kiểm tra Network:**
- Đảm bảo có kết nối internet
- Kiểm tra Firebase project configuration

### **4. Kiểm tra Image Size:**
- Ảnh quá lớn có thể gây lỗi
- ImageUploadService tự động resize về 300x300

## 📱 **Test Cases:**

### **Test Case 1: Small Image (< 1MB)**
- **Expected:** Base64 string ~50,000 chars
- **Expected:** Upload thành công

### **Test Case 2: Large Image (> 5MB)**
- **Expected:** Auto-compression với quality 50%
- **Expected:** Base64 string ~100,000 chars

### **Test Case 3: Invalid Image**
- **Expected:** Error message
- **Expected:** Không crash app

### **Test Case 4: No Image Selected**
- **Expected:** Update profile without avatar
- **Expected:** `avatarUrl` giữ nguyên giá trị cũ

## 🚀 **Next Steps:**

1. **Test trên device** với debug logs
2. **Identify issue** từ logs cụ thể
3. **Fix specific problem** được tìm thấy
4. **Verify fix** bằng cách test lại
5. **Check Firebase Console** để confirm data được lưu

## 📊 **Success Criteria:**

- ✅ Image picker hoạt động
- ✅ Base64 conversion thành công
- ✅ Firestore update thành công
- ✅ Firebase Console hiển thị `avatarUrl` mới
- ✅ Profile hiển thị avatar mới
- ✅ Không còn chuỗi rỗng `""`

**App đã sẵn sàng để test với debug logs chi tiết!** 🎉

**Hãy test và cung cấp logs để tôi có thể debug chính xác vấn đề lưu avatar!** 🔍
