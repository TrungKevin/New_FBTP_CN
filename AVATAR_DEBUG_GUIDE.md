# 🔍 Hướng dẫn Debug Avatar Upload

## 🎯 **Vấn đề hiện tại**
- Avatar URL trong Firebase Firestore vẫn là chuỗi rỗng `""`
- Cần debug để tìm nguyên nhân không lưu được base64 string

## 🔧 **Debug Steps**

### **1. Test trên Device/Emulator**

#### **Bước 1: Mở app và đăng nhập**
- Đăng nhập với account có userId: `RI00eb40uyVHSMhe3fyf17R1L5I2`
- Vào Profile → Edit Profile

#### **Bước 2: Chọn ảnh mới**
- Click vào avatar hoặc button "Chụp ảnh mới"
- Chọn ảnh từ gallery
- Kiểm tra preview ảnh hiển thị

#### **Bước 3: Save và kiểm tra logs**
- Click "Lưu thay đổi"
- Mở Logcat và filter theo tag: `DEBUG`
- Tìm các logs sau:

### **2. Debug Logs cần kiểm tra**

#### **🔄 ImageUploadService Logs:**
```
🔄 DEBUG: Converting image to base64...
✅ DEBUG: Base64 conversion successful, size: XXXX chars
🔄 DEBUG: First 100 chars: /9j/4AAQSkZJRgABAQAAAQABAAD...
```

#### **📱 EditProfileScreen Logs:**
```
🔄 DEBUG: Starting avatar upload process...
🔄 DEBUG: selectedImageUri: content://media/external/images/media/XXXX
🔄 DEBUG: userId: RI00eb40uyVHSMhe3fyf17R1L5I2
✅ DEBUG: Avatar upload successful
🔄 DEBUG: avatarUrl length: XXXXX
🔄 DEBUG: avatarUrl first 100 chars: /9j/4AAQSkZJRgABAQAAAQABAAD...
```

#### **💾 UserRepository Logs:**
```
🔄 DEBUG: Updating avatarUrl in Firestore...
🔄 DEBUG: avatarUrl length: XXXXX
🔄 DEBUG: avatarUrl first 100 chars: /9j/4AAQSkZJRgABAQAAAQABAAD...
🔄 DEBUG: Firestore updates: {avatarUrl=/9j/4AAQSkZJRgABAQAAAQABAAD..., updatedAt=XXXX}
🔄 DEBUG: User ID: RI00eb40uyVHSMhe3fyf17R1L5I2
✅ DEBUG: Firestore update successful
```

#### **🔄 AuthViewModel Logs:**
```
🔄 DEBUG: Profile update result: ok=true, msg=null
```

### **3. Các lỗi có thể gặp**

#### **❌ Lỗi 1: Base64 conversion failed**
```
❌ ERROR: Base64 conversion failed: java.io.FileNotFoundException
```
**Nguyên nhân:** Không đọc được file từ URI
**Giải pháp:** Kiểm tra permissions và URI validity

#### **❌ Lỗi 2: Base64 string too large**
```
⚠️ WARNING: Base64 string too large (1500000 chars), compressing more...
```
**Nguyên nhân:** Ảnh quá lớn, vượt quá Firestore limit
**Giải pháp:** Đã có auto-compression với quality 50%

#### **❌ Lỗi 3: Firestore update failed**
```
❌ ERROR: Firestore update failed: Permission denied
```
**Nguyên nhân:** Firebase Security Rules không cho phép
**Giải pháp:** Kiểm tra Firestore Rules

#### **❌ Lỗi 4: Profile update result: ok=false**
```
🔄 DEBUG: Profile update result: ok=false, msg=Some error message
```
**Nguyên nhân:** Lỗi trong AuthViewModel hoặc UserRepository
**Giải pháp:** Kiểm tra error message cụ thể

### **4. Kiểm tra Firebase Console**

#### **Firestore Database:**
1. Mở Firebase Console
2. Vào Firestore Database → Data
3. Chọn collection "users"
4. Chọn document "RI00eb40uyVHSMhe3fyf17R1L5I2"
5. Kiểm tra field "avatarUrl" có giá trị mới không

#### **Expected Result:**
- `avatarUrl` field có giá trị base64 string dài (50,000-100,000 characters)
- `updatedAt` field có timestamp mới
- Không còn là chuỗi rỗng `""`

### **5. Troubleshooting**

#### **Nếu không thấy debug logs:**
- Kiểm tra Logcat filter
- Đảm bảo app đang chạy debug build
- Kiểm tra console output

#### **Nếu base64 conversion failed:**
- Kiểm tra ảnh có hợp lệ không
- Thử với ảnh khác
- Kiểm tra permissions

#### **Nếu Firestore update failed:**
- Kiểm tra Firebase connection
- Kiểm tra Security Rules
- Kiểm tra user authentication

#### **Nếu update thành công nhưng không hiển thị:**
- Kiểm tra ProfileHeader có reload data không
- Kiểm tra AsyncImage có hỗ trợ base64 không
- Thử restart app

### **6. Test Cases**

#### **Test Case 1: Small Image**
- Chọn ảnh nhỏ (< 1MB)
- Expected: Base64 string ~50,000 chars
- Expected: Upload thành công

#### **Test Case 2: Large Image**
- Chọn ảnh lớn (> 5MB)
- Expected: Auto-compression với quality 50%
- Expected: Base64 string ~100,000 chars

#### **Test Case 3: Invalid Image**
- Chọn file không phải ảnh
- Expected: Error message
- Expected: Không crash app

### **7. Next Steps**

1. **Run test** với debug logs
2. **Identify issue** từ logs
3. **Fix specific problem** được tìm thấy
4. **Verify fix** bằng cách test lại
5. **Check Firebase Console** để confirm data được lưu

## 🎯 **Expected Final Result**

Sau khi debug thành công:
- ✅ Base64 string được tạo thành công
- ✅ Firestore update thành công
- ✅ Firebase Console hiển thị avatarUrl mới
- ✅ Profile hiển thị avatar mới
- ✅ Không còn chuỗi rỗng `""`
