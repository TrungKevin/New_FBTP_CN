# Hướng Dẫn Cấu Hình Firebase cho Google và Facebook Login

## 1. Cấu Hình Google Sign-In

### Bước 1: Lấy Web Client ID từ Firebase Console

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Project Settings** (⚙️) > **General**
4. Scroll xuống phần **Your apps** > chọn Android app
5. Tìm **Web client ID** (có dạng: `xxxxx.apps.googleusercontent.com`)
6. Copy Web Client ID này

### Bước 2: Thêm Web Client ID vào strings.xml

Mở file `app/src/main/res/values/strings.xml` và thêm:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

Thay `YOUR_WEB_CLIENT_ID_HERE` bằng Web Client ID bạn đã copy ở bước 1.

### Bước 3: Bật Google Sign-In trong Firebase Console

1. Vào Firebase Console > **Authentication** > **Sign-in method**
2. Tìm **Google** và click vào
3. Bật **Enable** toggle
4. Nhập **Project support email**
5. Click **Save**

### Bước 4: Thêm SHA-1 Certificate Fingerprint (nếu chưa có)

1. Vào Firebase Console > **Project Settings** > **General**
2. Scroll xuống phần **Your apps** > chọn Android app
3. Click **Add fingerprint**
4. Lấy SHA-1 của bạn bằng lệnh:
   ```bash
   # Windows (PowerShell)
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   
   # Mac/Linux
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Copy SHA-1 fingerprint và paste vào Firebase Console
6. Click **Save**

---

## 2. Cấu Hình Facebook Login

### Bước 1: Tạo Facebook App

1. Vào [Facebook Developers](https://developers.facebook.com/)
2. Click **My Apps** > **Create App**
3. Chọn **Consumer** hoặc **Business** > **Next**
4. Điền thông tin:
   - **App Name**: Tên app của bạn
   - **App Contact Email**: Email của bạn
5. Click **Create App**

### Bước 2: Thêm Facebook Login Product

1. Trong Facebook App Dashboard, tìm **Add Product**
2. Tìm **Facebook Login** và click **Set Up**
3. Chọn **Android** platform
4. Điền thông tin:
   - **Package Name**: `com.trungkien.fbtp_cn` (hoặc package name của bạn)
   - **Class Name**: `com.trungkien.fbtp_cn.MainActivity`
   - **Key Hashes**: Lấy SHA-1 như ở bước 4 của Google Sign-In, sau đó convert sang Base64:
     ```bash
     # Sử dụng SHA-1 từ bước trước, convert sang Base64
     # Hoặc dùng công cụ online: https://tomeko.net/online_tools/hex_to_base64.php
     ```
5. Click **Save**

### Bước 3: Lấy App ID và App Secret

1. Vào **Settings** > **Basic** trong Facebook App Dashboard
2. Copy **App ID** và **App Secret**

### Bước 4: Thêm Facebook App ID vào strings.xml

Mở file `app/src/main/res/values/strings.xml` và thêm:

```xml
<string name="facebook_app_id">YOUR_FACEBOOK_APP_ID_HERE</string>
```

Thay `YOUR_FACEBOOK_APP_ID_HERE` bằng App ID bạn đã copy.

### Bước 5: Cấu Hình AndroidManifest.xml

Mở file `app/src/main/AndroidManifest.xml` và thêm vào thẻ `<application>`:

```xml
<meta-data
    android:name="com.facebook.sdk.ApplicationId"
    android:value="@string/facebook_app_id" />
    
<meta-data
    android:name="com.facebook.sdk.ClientToken"
    android:value="@string/facebook_client_token" />
```

**Lưu ý**: Nếu không có `facebook_client_token`, bạn có thể bỏ qua meta-data thứ 2.

### Bước 6: Bật Facebook Login trong Firebase Console

1. Vào Firebase Console > **Authentication** > **Sign-in method**
2. Tìm **Facebook** và click vào
3. Bật **Enable** toggle
4. Nhập **App ID** và **App Secret** từ Facebook App
5. Click **Save**

### Bước 7: Thêm Facebook App ID vào strings.xml (nếu chưa có)

Đảm bảo bạn đã thêm Facebook App ID vào `strings.xml` như ở Bước 4.

---

## 3. Cấu Hình Firestore Rules (nếu cần)

Đảm bảo Firestore Rules cho phép đọc/ghi user document:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Cho phép user đọc/ghi document của chính họ
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Cho phép tạo document mới khi đăng ký
      allow create: if request.auth != null;
    }
  }
}
```

---

## 4. Kiểm Tra Cấu Hình

### Kiểm Tra Google Sign-In:
1. Chạy app
2. Click vào icon Google
3. Chọn tài khoản Google
4. Kiểm tra xem có đăng nhập thành công không

### Kiểm Tra Facebook Login:
1. Chạy app
2. Click vào icon Facebook
3. Đăng nhập Facebook
4. Kiểm tra xem có đăng nhập thành công không

---

## 5. Troubleshooting

### Lỗi Google Sign-In:
- **"10:"**: Kiểm tra Web Client ID trong `strings.xml`
- **"12500:"**: Kiểm tra SHA-1 fingerprint đã được thêm vào Firebase Console chưa
- **"7:"**: Kiểm tra Google Sign-In đã được bật trong Firebase Console chưa

### Lỗi Facebook Login:
- **"Invalid key hash"**: Kiểm tra Key Hash đã được thêm vào Facebook App Settings chưa
- **"App not setup"**: Kiểm tra Facebook Login đã được thêm vào Facebook App chưa
- **"Invalid App ID"**: Kiểm tra Facebook App ID trong `strings.xml` và `AndroidManifest.xml`

---

## 6. Lưu Ý Quan Trọng

1. **SHA-1 Fingerprint**: Cần thêm cả debug và release keystore SHA-1 vào Firebase Console
2. **Facebook App Review**: Facebook Login cần được review nếu muốn publish app lên Play Store
3. **Privacy Policy**: Cần có Privacy Policy URL khi sử dụng Facebook Login
4. **Test Users**: Có thể thêm test users trong Facebook App Settings > Roles > Test Users

---

## 7. Tài Liệu Tham Khảo

- [Google Sign-In Android](https://firebase.google.com/docs/auth/android/google-signin)
- [Facebook Login Android](https://developers.facebook.com/docs/facebook-login/android)
- [Firebase Authentication](https://firebase.google.com/docs/auth)

