# 🏟️ **FOOTBALL & PICKLE COURT PRICING FIX - Hướng Dẫn Test**

## 🎯 **Vấn Đề Đã Xác Định**

**Football và Pickle courts không hiển thị pricing rules** mặc dù dữ liệu đã được lưu vào Firebase.

**Nguyên nhân**: Pricing rules không được load từ Firebase cho các sân này.

## ✅ **Các Fix Đã Áp Dụng**

### **1. Enhanced Debug Logging**
- ✅ Thêm debug logs chi tiết trong `CourtService.kt`
- ✅ Thêm debug logs trong `FieldViewModel.kt`
- ✅ Thêm debug logs trong `FieldRepository.kt`

### **2. Error Handling**
- ✅ Xử lý lỗi khi load data từ Firebase
- ✅ Log chi tiết các bước xử lý
- ✅ Hiển thị thông tin field khi không có pricing rules

### **3. Data Validation**
- ✅ Kiểm tra field ID, name, sports
- ✅ Log số lượng pricing rules được trả về
- ✅ Log chi tiết từng rule được parse

## 🧪 **Hướng Dẫn Test**

### **Bước 1: Build và Cài Đặt App**
```bash
./gradlew assembleDebug
# Cài đặt APK vào device/emulator
```

### **Bước 2: Đăng Nhập và Vào Sân**
1. **Đăng nhập** với tài khoản owner
2. **Vào một sân football hoặc pickle**
3. **Chọn tab "Bảng giá & Dịch vụ"**

### **Bước 3: Kiểm Tra Debug Logs**
Trong **Logcat**, filter theo tag `DEBUG` và tìm:

```
🔄 DEBUG: LaunchedEffect triggered - pricingRules: 0, fieldServices: 0
⚠️ WARNING: Không có pricing rules nào từ Firebase!
🔍 DEBUG: Field ID đang query: [field_id]
🔍 DEBUG: Field name: [field_name]
🔍 DEBUG: Field sports: [sports_list]
```

### **Bước 4: Kiểm Tra Firebase Console**
1. **Vào Firebase Console**
2. **Chọn project**
3. **Vào Firestore Database**
4. **Kiểm tra collection `pricing_rules`**
5. **Tìm documents có `fieldId` tương ứng**

### **Bước 5: Kiểm Tra Security Rules**
Đảm bảo Firestore Security Rules cho phép:
```javascript
match /pricing_rules/{ruleId} {
  allow read: if true;  // ✅ Cho phép đọc
  allow create, update, delete: if signedIn() && isFieldOwner(fieldId);
}
```

## 🔍 **Debug Logs Cần Kiểm Tra**

### **1. CourtService.kt**
```
🔄 DEBUG: LaunchedEffect triggered
🔍 DEBUG: Raw Firebase data
⚠️ WARNING: Không có pricing rules nào từ Firebase!
```

### **2. FieldViewModel.kt**
```
🔄 DEBUG: FieldViewModel.loadPricingRulesByFieldId([field_id])
✅ DEBUG: LoadPricingRulesByFieldId thành công: X rules
❌ ERROR: LoadPricingRulesByFieldId thất bại
```

### **3. FieldRepository.kt**
```
🔄 DEBUG: FieldRepository.getPricingRulesByFieldId([field_id])
🔍 DEBUG: Querying collection: pricing_rules
🔍 DEBUG: Filter: fieldId == [field_id]
✅ DEBUG: Firebase query thành công
🔍 DEBUG: Snapshot size: X
```

## 🚨 **Các Trường Hợp Có Thể Xảy Ra**

### **Trường Hợp 1: Không Có Data**
```
🔍 DEBUG: Snapshot size: 0
⚠️ WARNING: Không có pricing rules nào từ Firebase!
```
**Giải pháp**: Tạo pricing rules mới cho sân

### **Trường Hợp 2: Permission Denied**
```
❌ ERROR: LoadPricingRulesByFieldId thất bại
❌ ERROR: Exception: [permission_error]
```
**Giải pháp**: Kiểm tra Firestore Security Rules

### **Trường Hợp 3: Data Parse Error**
```
🔍 DEBUG: Document [doc_id]: Không thể parse thành PricingRule
```
**Giải pháp**: Kiểm tra cấu trúc data trong Firebase

## 🔧 **Cách Khắc Phục**

### **1. Tạo Pricing Rules Mới**
1. **Vào CourtService**
2. **Click nút ✏️ (Edit)**
3. **Nhập giá cho các khung giờ**
4. **Click 💾 (Save)**

### **2. Kiểm Tra Security Rules**
```javascript
// Đảm bảo rules cho phép đọc
match /pricing_rules/{ruleId} {
  allow read: if true;
}
```

### **3. Kiểm Tra Data Structure**
Đảm bảo mỗi pricing rule có:
- `fieldId`: ID của sân
- `price`: Giá tiền (number)
- `dayType`: "WEEKDAY" | "WEEKEND" | "HOLIDAY"
- `description`: Mô tả khung giờ

## 📱 **Test Cases**

### **Test Case 1: Football Court**
```
Field ID: field_football_001
Field Name: Sân Bóng Đá ABC
Field Sports: ["FOOTBALL"]
Expected: Hiển thị pricing rules
```

### **Test Case 2: Pickle Court**
```
Field ID: field_pickle_001
Field Name: Sân Pickleball XYZ
Field Sports: ["PICKLEBALL"]
Expected: Hiển thị pricing rules
```

### **Test Case 3: Tennis Court (Control)**
```
Field ID: field_tennis_001
Field Name: Sân Tennis DEF
Field Sports: ["TENNIS"]
Expected: Hiển thị pricing rules (đã hoạt động)
```

## 📊 **Kết Quả Mong Đợi**

Sau khi fix:
1. ✅ **Debug logs hiển thị đầy đủ**
2. ✅ **Pricing rules được load từ Firebase**
3. ✅ **UI hiển thị giá thay vì "Chưa có giá"**
4. ✅ **Có thể edit và save pricing rules**

## 🆘 **Nếu Vẫn Không Hoạt Động**

1. **Kiểm tra Logcat** để xem lỗi cụ thể
2. **Kiểm tra Firebase Console** để xem data
3. **Kiểm tra Security Rules** để đảm bảo permission
4. **Tạo issue mới** với logs chi tiết

---

**Lưu ý**: Đây là hướng dẫn test để xác định nguyên nhân gốc rễ của vấn đề. Sau khi xác định được nguyên nhân, sẽ có fix cụ thể.
