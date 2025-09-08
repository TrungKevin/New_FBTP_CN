# 📍 Avatar Storage Location - Nơi lưu trữ avatar

## 🎯 **Avatar được lưu ở đâu:**

### **1. 📱 Local Processing (ImageUploadService)**
```kotlin
// File: app/src/main/java/com/trungkien/fbtp_cn/ui/components/owner/profile/ImageUploadService.kt

suspend fun uploadAvatar(context: Context, imageUri: Uri, userId: String): Result<String> {
    // 1. Convert ảnh thành base64 string
    val base64String = convertImageToBase64(context, imageUri)
    
    // 2. Resize ảnh: 300x300 pixels
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
    
    // 3. Compress ảnh: JPEG 80% quality
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    
    // 4. Encode thành base64 string
    val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    
    // 5. Trả về base64 string (KHÔNG upload lên Firebase Storage)
    return Result.success(base64String)
}
```

### **2. 💾 Firebase Firestore Database**
```kotlin
// File: app/src/main/java/com/trungkien/fbtp_cn/repository/UserRepository.kt

fun updateCurrentUserProfile(avatarUrl: String? = null, ...) {
    val updates = mutableMapOf<String, Any>()
    if (avatarUrl != null) {
        updates["avatarUrl"] = avatarUrl  // Lưu base64 string vào Firestore
    }
    
    // Lưu vào collection "users" với document ID = userId
    firestore.collection("users").document(uid)
        .set(updates, SetOptions.merge())
}
```

### **3. 🗄️ User Data Model**
```kotlin
// File: app/src/main/java/com/trungkien/fbtp_cn/model/User.kt

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",  // Base64 string được lưu ở đây
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

## 🔍 **Chi tiết quá trình lưu trữ:**

### **Step 1: Image Selection**
- User chọn ảnh từ gallery
- Ảnh được lưu tạm thời trong `selectedImageUri`

### **Step 2: Image Processing**
- Ảnh được convert thành `Bitmap`
- Resize về 300x300 pixels
- Compress với JPEG 80% quality
- Encode thành base64 string

### **Step 3: Database Storage**
- Base64 string được lưu vào Firebase Firestore
- Collection: `users`
- Document ID: `userId` (ví dụ: `RI00eb40uyVHSMhe3fyf17R1L5I2`)
- Field: `avatarUrl` (chứa base64 string)

### **Step 4: Display**
- `ProfileHeader` đọc `avatarUrl` từ Firestore
- Kiểm tra nếu là base64 string (starts with "data:image", "/9j/", "iVBOR")
- Hiển thị bằng `AsyncImage`

## 📊 **Cấu trúc dữ liệu trong Firebase:**

### **Collection: users**
```
Document ID: RI00eb40uyVHSMhe3fyf17R1L5I2
├── userId: "RI00eb40uyVHSMhe3fyf17R1L5I2"
├── name: "Kien"
├── email: "ronaldo@gmail.com"
├── phone: "0921483538"
├── avatarUrl: "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k="
├── role: "OWNER"
├── createdAt: 1755762779000
└── updatedAt: 1757227770868
```

## 🔧 **Ưu điểm của cách lưu trữ này:**

### **✅ Pros:**
1. **Đơn giản:** Không cần Firebase Storage
2. **Nhanh:** Không cần download URL
3. **Offline:** Có thể hiển thị ngay khi có data
4. **Bảo mật:** Không cần public URL
5. **Tiết kiệm:** Không tốn storage quota

### **❌ Cons:**
1. **Kích thước:** Base64 string lớn hơn file gốc ~33%
2. **Firestore limit:** Mỗi document có limit ~1MB
3. **Performance:** Có thể chậm với ảnh lớn

## 🎯 **Kích thước ước tính:**

### **Ảnh 300x300 JPEG 80%:**
- **File gốc:** ~15-30KB
- **Base64 string:** ~20-40KB
- **Firestore document:** ~50-100KB (bao gồm các field khác)

### **Giới hạn Firestore:**
- **Document size limit:** 1MB
- **Field size limit:** 1MB
- **Avatar base64:** ~20-40KB (an toàn)

## 🚀 **Cách kiểm tra avatar đã được lưu:**

### **1. Firebase Console:**
1. Mở Firebase Console
2. Vào Firestore Database
3. Chọn collection `users`
4. Chọn document `RI00eb40uyVHSMhe3fyf17R1L5I2`
5. Kiểm tra field `avatarUrl` có giá trị base64

### **2. Debug Logs:**
```
🔄 DEBUG: Updating avatarUrl in Firestore...
🔄 DEBUG: avatarUrl length: XXXXX
🔄 DEBUG: Firestore updates: {avatarUrl=/9j/4AAQSkZJRgABAQAAAQABAAD..., updatedAt=XXXX}
✅ DEBUG: Firestore update successful
```

### **3. App Display:**
- `ProfileHeader` hiển thị avatar từ base64 string
- `EditProfileScreen` hiển thị avatar hiện tại

## 📝 **Tóm tắt:**

**Avatar được lưu trong Firebase Firestore Database:**
- **Collection:** `users`
- **Document:** `userId`
- **Field:** `avatarUrl`
- **Format:** Base64 string
- **Size:** ~20-40KB
- **Location:** Firebase Cloud Firestore

**KHÔNG sử dụng Firebase Storage** - chỉ lưu base64 string trực tiếp trong Firestore document.
