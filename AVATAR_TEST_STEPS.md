# 🎯 Avatar Test Steps - Hướng dẫn test avatar upload

## 🔍 **Phân tích vấn đề hiện tại:**

### **✅ Profile đã được lưu thành công:**
- `updatedAt`: `1757231165012` (đã được cập nhật)
- `name`: `"kien"` (đã được cập nhật)
- `email`: `"ronaldo@gmail.com"`
- `phone`: `"0921483538"`

### **❌ Avatar chưa được lưu:**
- `avatarUrl`: `""` (vẫn là chuỗi rỗng)

### **🔍 Nguyên nhân:**
**User đã click "Lưu thay đổi" nhưng chưa chọn ảnh**, nên chỉ có profile được update mà không có avatar.

## 🧪 **Test Steps để lưu avatar:**

### **Step 1: Mở Edit Profile Screen**
1. **Mở app** và đăng nhập
2. **Vào Profile → Edit Profile** (ModernEditProfileScreen)
3. **Kiểm tra:** Màn hình hiển thị avatar circle với camera icon

### **Step 2: Chọn ảnh avatar**
1. **Click vào avatar** (circle với camera icon)
2. **Kiểm tra logs:** `🔄 DEBUG: Avatar clicked, launching image picker...`
3. **Chọn ảnh** từ gallery
4. **Kiểm tra logs:** `🔄 DEBUG: Image picker result: content://...`
5. **Kiểm tra logs:** `🔄 DEBUG: selectedImageUri set successfully`
6. **Kiểm tra:** Avatar hiển thị ảnh mới trong UI

### **Step 3: Lưu avatar**
1. **Click "Lưu thay đổi"**
2. **Kiểm tra logs:** `🔄 DEBUG: Save button clicked`
3. **Kiểm tra logs:** `🔄 DEBUG: selectedImageUri: content://...`
4. **Kiểm tra logs:** `🔄 DEBUG: Starting avatar upload process...`

### **Step 4: Kiểm tra upload process**
1. **Kiểm tra logs:** `🔄 DEBUG: Converting image to base64...`
2. **Kiểm tra logs:** `✅ DEBUG: Base64 conversion successful`
3. **Kiểm tra logs:** `🔄 DEBUG: Updating avatarUrl in Firestore...`
4. **Kiểm tra logs:** `✅ DEBUG: Firestore update successful`

### **Step 5: Verify Firebase Console**
1. **Mở Firebase Console**
2. **Vào Firestore Database → users → RI00eb40uyVHSMhe3fyf17R1L5I2**
3. **Kiểm tra field `avatarUrl`** có giá trị base64 mới (không còn `""`)

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
🔄 DEBUG: avatarUrl length: XXXXX
🔄 DEBUG: avatarUrl first 100 chars: /9j/4AAQSkZJRgABAQAAAQABAAD...
🔄 DEBUG: Firestore updates: {avatarUrl=/9j/4AAQSkZJRgABAQAAAQABAAD..., updatedAt=XXXX}
✅ DEBUG: Firestore update successful
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
- **Expected:** Auto-compression với quality 80%
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

## 🎉 **Summary:**

**Vấn đề hiện tại:** User chưa chọn ảnh nên chỉ có profile được update mà không có avatar.

**Giải pháp:** User cần click vào avatar để chọn ảnh trước khi lưu.

**App đã sẵn sàng để test avatar upload với debug logs chi tiết!** 🎉

**Hãy test theo các bước trên và cung cấp logs để confirm avatar được lưu vào `avatarUrl`!** 🔍
