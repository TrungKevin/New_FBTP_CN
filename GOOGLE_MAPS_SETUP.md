# Google Maps Setup Guide

## 🗺️ Cách lấy Google Maps API Key miễn phí

### 1. Tạo Google Cloud Project
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Đặt tên project (ví dụ: "FBTP_CN_Maps")

### 2. Bật Google Maps APIs
1. Vào "APIs & Services" > "Library"
2. Tìm và bật các API sau:
   - **Maps SDK for Android**
   - **Places API** (để tìm địa chỉ)
   - **Geocoding API** (để chuyển địa chỉ thành tọa độ)

### 3. Tạo API Key
1. Vào "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "API Key"
3. Copy API key được tạo

### 4. Cấu hình API Key
1. Mở file `app/src/main/AndroidManifest.xml`
2. Thay thế `YOUR_MAPS_API_KEY_HERE` bằng API key thật:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" />
```

### 5. Giới hạn API Key (Bảo mật)
1. Vào "APIs & Services" > "Credentials"
2. Click vào API key vừa tạo
3. Trong "Application restrictions":
   - Chọn "Android apps"
   - Thêm package name: `com.trungkien.fbtp_cn`
   - Thêm SHA-1 fingerprint của app

### 6. Quota và Billing
- **Miễn phí**: $200 credit/tháng
- **Maps SDK**: ~$5/1000 map loads
- **Places API**: ~$17/1000 requests
- **Geocoding API**: ~$5/1000 requests

### 7. Test ứng dụng
1. Build và chạy app
2. Vào tab "Map" trong Renter UI
3. Kiểm tra Google Maps hiển thị đúng

## 🔧 Troubleshooting

### Lỗi "Maps API key not found"
- Kiểm tra API key trong AndroidManifest.xml
- Đảm bảo đã bật Maps SDK for Android

### Lỗi "This app won't run without Google Play services"
- Cài đặt Google Play Services trên thiết bị test
- Hoặc dùng emulator với Google Play Services

### Map không hiển thị
- Kiểm tra internet connection
- Kiểm tra API key có đúng không
- Kiểm tra SHA-1 fingerprint

## 📱 Tính năng MapScreen

✅ **Đã implement:**
- Google Maps với markers cho sân thể thao
- Header với nút back, filter, toggle view
- Input địa chỉ và GPS button
- Chuyển đổi giữa Map view và List view
- Markers hiển thị tên sân, loại, giá

🔄 **Cần implement thêm:**
- Geocoding (chuyển địa chỉ thành tọa độ)
- GPS location permission handling
- Filter dialog cho loại sân, giá, khoảng cách
- List view với sân gần nhất
- Navigation từ marker đến chi tiết sân

## 💡 Gợi ý cải tiến

1. **Offline Maps**: Cache bản đồ để dùng offline
2. **Real-time Location**: Cập nhật vị trí real-time
3. **Route Planning**: Tính đường đi đến sân
4. **Clustering**: Nhóm markers khi zoom out
5. **Custom Markers**: Icons riêng cho từng loại sân
