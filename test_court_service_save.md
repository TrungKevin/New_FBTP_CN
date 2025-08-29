# 🧪 Test CourtService - Kiểm Tra Việc Lưu Và Load Dữ Liệu

## 🎯 **Mục Đích Test**

Kiểm tra xem dữ liệu bảng giá có được lưu vào Firebase và hiển thị ngược lại lên UI không.

## 🔍 **Các Bước Test**

### **Bước 1: Kiểm Tra Trạng Thái Ban Đầu**
```
1. Vào CourtService component
2. Kiểm tra: Bảng giá hiển thị 6 khung giờ với giá trống
3. Kiểm tra: Logcat hiển thị "Không có dữ liệu pricing rules, tạo mẫu trống"
```

### **Bước 2: Nhập Dữ Liệu Bảng Giá**
```
1. Click nút ✏️ (Edit)
2. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
```

### **Bước 3: Lưu Dữ Liệu**
```
1. Click nút 💾 (Save)
2. Kiểm tra: Loading dialog hiển thị
3. Kiểm tra: Logcat hiển thị "Đã gửi lệnh lưu dữ liệu vào Firebase"
```

### **Bước 4: Kiểm Tra Kết Quả**
```
1. Đợi loading hoàn thành
2. Kiểm tra: UI hiển thị giá mới (55000, 60000, 70000, 80000, 85000, 90000)
3. Kiểm tra: Logcat hiển thị "Cập nhật bảng giá và dịch vụ thành công!"
```

## 🚨 **Vấn Đề Có Thể Gặp**

### **1. Dữ Liệu Không Được Lưu**
```
- Kiểm tra: Firebase console có dữ liệu mới không
- Kiểm tra: Logcat có lỗi gì không
- Kiểm tra: Network connection
```

### **2. Dữ Liệu Được Lưu Nhưng Không Hiển Thị**
```
- Kiểm tra: LaunchedEffect có được trigger không
- Kiểm tra: uiState.pricingRules có dữ liệu không
- Kiểm tra: updateUIDataFromFirebase có được gọi không
```

### **3. Mapping Dữ Liệu Sai**
```
- Kiểm tra: Description có đúng format không
- Kiểm tra: dayType có đúng không
- Kiểm tra: price có được convert đúng không
```

## 🔧 **Debug Commands**

### **Kiểm Tra Firebase Console**
```
1. Vào Firebase Console
2. Chọn project
3. Vào Firestore Database
4. Kiểm tra collection "pricing_rules"
5. Kiểm tra collection "field_services"
```

### **Kiểm Tra Logcat**
```
Filter: "DEBUG"
Keywords: "Bắt đầu lưu", "Đã gửi lệnh", "Cập nhật thành công", "LaunchedEffect triggered"
```

## 📊 **Expected Results**

### **Sau Khi Lưu Thành Công:**
```
✅ Loading dialog hiển thị
✅ Success message: "Cập nhật bảng giá và dịch vụ thành công!"
✅ UI tự động reload
✅ Bảng giá hiển thị giá mới:
   - T2-T6, 5h-12h: 55000 ₫/30'
   - T2-T6, 12h-18h: 60000 ₫/30'
   - T2-T6, 18h-24h: 70000 ₫/30'
   - T7-CN, 5h-12h: 80000 ₫/30'
   - T7-CN, 12h-18h: 85000 ₫/30'
   - T7-CN, 18h-24h: 90000 ₫/30'
```

### **Sau Khi Refresh:**
```
✅ Click nút 🔄
✅ Dữ liệu được reload từ Firebase
✅ UI hiển thị dữ liệu mới nhất
```

## 🐛 **Nếu Có Lỗi**

### **Lỗi 1: Dữ Liệu Không Được Lưu**
```
Nguyên nhân: Firebase connection, permission, validation
Giải pháp: Kiểm tra Firebase rules, network, validation logic
```

### **Lỗi 2: Dữ Liệu Được Lưu Nhưng Không Hiển Thị**
```
Nguyên nhân: State management, LaunchedEffect, mapping logic
Giải pháp: Kiểm tra uiState, LaunchedEffect triggers, mapping functions
```

### **Lỗi 3: Mapping Sai**
```
Nguyên nhân: Description format, dayType mapping
Giải pháp: Kiểm tra saveData function, description format
```

## 📝 **Ghi Chú Test**

- Test trên device thật hoặc emulator
- Đảm bảo có internet connection
- Kiểm tra Firebase project configuration
- Monitor Logcat để debug
- Test nhiều lần để đảm bảo consistency
