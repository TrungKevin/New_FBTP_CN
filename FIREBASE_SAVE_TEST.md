# 🔥 Kiểm Tra Lưu Dữ Liệu Vào Firebase

## 🎯 **Mục Tiêu Kiểm Tra**

**"Kiểm tra khi nhập cột giá thì có lưu được giá trị vào Firebase hay không"**

## 🔍 **Debug Logs Đã Thêm**

### **1. Khi User Nhập Giá**
```
🔍 DEBUG: User thay đổi giá cho rule [0]: '' -> '55000'
💰 DEBUG: Giá mới: '55000' (length: 5, isEmpty: false)
✅ DEBUG: Giá hợp lệ: true
✅ DEBUG: Đã cập nhật pricingRules[0].price = '55000'
```

### **2. Khi Click Save Button**
```
💾 DEBUG: Save button được click!
📊 DEBUG: Trước khi lưu, pricingRules có 6 items:
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
  [1] CourtPricingRule(id=2, dayOfWeek=T2 - T6, timeSlot=12h - 18h, price=60000)
  ...
🛍️ DEBUG: Trước khi lưu, services có X items:
  [0] CourtServiceItem(...)
```

### **3. Khi Bắt Đầu Lưu Dữ Liệu**
```
💾 DEBUG: Bắt đầu lưu dữ liệu vào Firebase
📊 Input pricing rules: 6 items
  [0] CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=55000)
🔍 DEBUG: Tạo PricingRule với description: Giá T2 - T6 - 5h - 12h
```

### **4. Khi Kiểm Tra Dữ Liệu Trước Khi Gửi**
```
🔍 DEBUG: Kiểm tra dữ liệu trước khi gửi:
  - fieldId: field_123
  - newPricingRules.size: 6
  - newFieldServices.size: 3
💰 DEBUG: Pricing rules có giá > 0: 6
  [0] Giá: 55000 ₫ - Giá T2 - T6 - 5h - 12h
  [1] Giá: 60000 ₫ - Giá T2 - T6 - 12h - 18h
  ...
🛍️ DEBUG: Field services có dữ liệu: 3
  [0] Banh: 5000 ₫
  [1] Nước: 15000 ₫
  [2] Vợt: 20000 ₫
```

### **5. Khi Gửi Lệnh Lưu**
```
🚀 DEBUG: Gửi lệnh lưu dữ liệu vào Firebase...
💾 DEBUG: Dữ liệu sẽ lưu vào Firebase:
📊 Pricing Rules sẽ lưu: 6 items
  [0] PricingRule:
    - ruleId: 
    - fieldId: field_123
    - dayType: WEEKDAY
    - description: Giá T2 - T6 - 5h - 12h
    - price: 55000
    - minutes: 30
🛍️ Field Services sẽ lưu: 3 items
  [0] FieldService:
    - fieldServiceId: 
    - fieldId: field_123
    - name: Banh
    - price: 5000
    - billingType: PER_UNIT
✅ Đã gửi lệnh lưu dữ liệu vào Firebase
⏳ DEBUG: Đang chờ Firebase xử lý...
```

### **6. Khi Firebase Trả Về Kết Quả**

#### **Thành Công:**
```
✅ DEBUG: Firebase trả về thành công: Pricing and services updated successfully
🎯 DEBUG: Dữ liệu đã được lưu vào Firebase thành công!
🔄 DEBUG: Bắt đầu reload data từ Firebase...
🔄 DEBUG: Đã tăng refreshTrigger: 1
```

#### **Lỗi:**
```
❌ DEBUG: Firebase trả về lỗi: Permission denied
🚨 DEBUG: Dữ liệu KHÔNG được lưu vào Firebase!
🔍 DEBUG: Nguyên nhân có thể:
  - Firebase connection failed
  - Firebase rules không cho phép write
  - Dữ liệu không hợp lệ
  - Network error
```

## 🧪 **Test Chi Tiết**

### **Bước 1: Vào CourtService**
```
1. Mở app
2. Vào CourtService component
3. Quan sát cột giá hiện tại (có thể hiển thị "Chưa có giá")
4. Mở Logcat để theo dõi debug logs
```

### **Bước 2: Nhập Giá Mới**
```
1. Click nút ✏️ (Edit)
2. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
3. Quan sát Logcat để xem logs "User thay đổi giá"
```

### **Bước 3: Click Save Button**
```
1. Click nút 💾 (Save)
2. Quan sát Logcat để xem logs "Save button được click"
3. Quan sát logs "Trước khi lưu, pricingRules có X items"
4. Quan sát logs "Bắt đầu lưu dữ liệu vào Firebase"
```

### **Bước 4: Quan Sát Quá Trình Lưu**
```
1. Quan sát logs "Kiểm tra dữ liệu trước khi gửi"
2. Quan sát logs "Pricing rules có giá > 0: X"
3. Quan sát logs "Dữ liệu sẽ lưu vào Firebase"
4. Quan sát logs "Gửi lệnh lưu dữ liệu vào Firebase"
5. Quan sát logs "Đã gửi lệnh lưu dữ liệu vào Firebase"
6. Quan sát logs "Đang chờ Firebase xử lý..."
```

### **Bước 5: Quan Sát Kết Quả Firebase**
```
1. Đợi Firebase xử lý (có thể mất vài giây)
2. Quan sát logs từ Firebase:
   - Nếu thành công: "Firebase trả về thành công"
   - Nếu lỗi: "Firebase trả về lỗi"
3. Quan sát logs "Bắt đầu reload data từ Firebase"
4. Quan sát logs "Đã tăng refreshTrigger: X"
```

## 🔍 **Kiểm Tra Kết Quả**

### **Nếu Lưu Thành Công:**
- ✅ Logs "User thay đổi giá" xuất hiện khi nhập
- ✅ Logs "Save button được click" xuất hiện
- ✅ Logs "Bắt đầu lưu dữ liệu vào Firebase" xuất hiện
- ✅ Logs "Kiểm tra dữ liệu trước khi gửi" xuất hiện
- ✅ Logs "Pricing rules có giá > 0: 6" xuất hiện
- ✅ Logs "Gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ✅ Logs "Đã gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ✅ Logs "Firebase trả về thành công" xuất hiện
- ✅ Logs "Bắt đầu reload data từ Firebase" xuất hiện

### **Nếu Lưu Thất Bại:**
- ✅ Logs "User thay đổi giá" xuất hiện khi nhập
- ✅ Logs "Save button được click" xuất hiện
- ✅ Logs "Bắt đầu lưu dữ liệu vào Firebase" xuất hiện
- ✅ Logs "Kiểm tra dữ liệu trước khi gửi" xuất hiện
- ✅ Logs "Pricing rules có giá > 0: 6" xuất hiện
- ✅ Logs "Gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ✅ Logs "Đã gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ❌ Logs "Firebase trả về lỗi" xuất hiện
- ❌ Logs "Dữ liệu KHÔNG được lưu vào Firebase" xuất hiện

## 🚨 **Nguyên Nhân Có Thể Gây Lỗi**

### **1. Firebase Connection**
- Internet connection không ổn định
- Firebase project không đúng
- Firebase configuration sai

### **2. Firebase Rules**
- Rules không cho phép write
- Rules quá nghiêm ngặt
- Rules không đúng collection

### **3. Dữ Liệu Không Hợp Lệ**
- Giá không phải số
- FieldId không đúng
- Dữ liệu null hoặc empty

### **4. Network Issues**
- Timeout
- Connection refused
- DNS issues

## 🎯 **Kết Quả Mong Đợi**

Sau khi test:
- ✅ User nhập giá → Logs "User thay đổi giá" xuất hiện
- ✅ Click Save → Logs "Save button được click" xuất hiện
- ✅ Bắt đầu lưu → Logs "Bắt đầu lưu dữ liệu vào Firebase" xuất hiện
- ✅ Kiểm tra dữ liệu → Logs "Kiểm tra dữ liệu trước khi gửi" xuất hiện
- ✅ Gửi lệnh → Logs "Gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ✅ Firebase xử lý → Logs "Đã gửi lệnh lưu dữ liệu vào Firebase" xuất hiện
- ✅ Kết quả thành công → Logs "Firebase trả về thành công" xuất hiện
- ✅ Reload data → Logs "Bắt đầu reload data từ Firebase" xuất hiện

## 🚀 **Bước Tiếp Theo**

1. **Test ngay lập tức** theo hướng dẫn trên
2. **Monitor Logcat** để xem debug logs
3. **Cho biết kết quả** và logs nào xuất hiện
4. **Nếu có lỗi**, cung cấp logs để debug tiếp

**Hãy test ngay và cho biết kết quả!** 🎯

**Lưu ý**: Debug logs sẽ giúp chúng ta xác định chính xác điểm gây lỗi trong việc lưu dữ liệu vào Firebase. Bây giờ tôi đã thêm rất nhiều debug logs để theo dõi toàn bộ flow từ khi user nhập giá đến khi Firebase trả về kết quả.
