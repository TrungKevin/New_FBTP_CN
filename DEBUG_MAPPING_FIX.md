# 🔍 **Debug Mapping Fix - Kiểm Tra Vấn Đề Hiển Thị Giá**

## 🚨 **Vấn Đề Đã Xác Định:**

Dựa trên dữ liệu Firebase bạn cung cấp, tôi đã xác định được **6 pricing rules** đã được lưu thành công:

### **📊 Dữ Liệu Firebase Có Sẵn:**

1. **T2 - T6 5h - 12h**: 20000 ₫ (WEEKDAY)
2. **T2 - T6 12h - 18h**: 30000 ₫ (WEEKDAY) 
3. **T2 - T6 18h - 24h**: 40000 ₫ (WEEKDAY)
4. **T7 - CN 5h - 12h**: 50000 ₫ (WEEKEND)
5. **T7 - CN 12h - 18h**: 60000 ₫ (WEEKEND)
6. **T7 - CN 18h - 24h**: 80000 ₫ (WEEKEND)

**Nhưng UI vẫn hiển thị "Chưa có giá"!** 

## 🔧 **Nguyên Nhân Đã Xác Định:**

**Mapping logic trong `updateUIDataFromFirebase` không khớp chính xác với dữ liệu Firebase!**

### **Vấn Đề Cụ Thể:**

1. **Description mapping sai**: Firebase dùng "Giá T2 - T6 - 12h - 18h" nhưng code tìm "12h - 18h"
2. **DayType mapping sai**: Firebase dùng "WEEKDAY"/"WEEKEND" nhưng code map thành "T2 - T6"/"T7 - CN"
3. **Template search fail**: Không tìm thấy template rule tương ứng → không cập nhật giá

## 🧪 **Hướng Dẫn Test Với Debug Logs Mới:**

### **Bước 1: Build và Chạy App**
```bash
./gradlew assembleDebug
# Chạy app trên device/emulator
```

### **Bước 2: Mở Console và Filter Logs**
1. **Mở Android Studio Logcat** hoặc terminal với `adb logcat`
2. **Filter logs** với tag: `System.out` hoặc tìm "DEBUG"
3. **Đảm bảo** bạn thấy được debug logs

### **Bước 3: Test Load Data Từ Firebase**
1. **Đăng nhập owner**
2. **Vào sân** có fieldId: `HNwo0FideMqG7PusJzOd`
3. **Chọn "Bảng giá & Dịch vụ"**
4. **Quan sát console logs** - sẽ thấy:

```
🚀 DEBUG: Bắt đầu load data cho field: HNwo0FideMqG7PusJzOd
🔄 DEBUG: Loading field data for fieldId: HNwo0FideMqG7PusJzOd
✅ DEBUG: Đã gửi lệnh load dữ liệu từ Firebase
🔄 DEBUG: LaunchedEffect triggered - pricingRules: 6, fieldServices: X, refreshTrigger: 1
🔄 DEBUG: Cập nhật dữ liệu từ Firebase
📊 Pricing Rules từ Firebase: 6 items
🛍️ Field Services từ Firebase: X items
✅ Có dữ liệu pricing rules, mapping...
```

### **Bước 4: Kiểm Tra Mapping Logic**
Bạn sẽ thấy logs chi tiết cho từng rule:

```
🔍 DEBUG: Xử lý rule: 6VOfYvCah3t4NEWsaEJB - Giá T2 - T6 - 12h - 18h - Giá: 30000
🔄 Mapping: 30 phút -> 12h - 18h, WEEKDAY -> T2 - T6
💰 Giá từ Firebase: 30000
🔍 DEBUG: Tìm template rule cho: T2 - T6 - 12h - 18h
🔍 DEBUG: Template search result:
  - Tìm: T2 - T6 - 12h - 18h
  - Template rules có sẵn:
    [0] T2 - T6 - 5h - 12h
    [1] T2 - T6 - 12h - 18h
    [2] T2 - T6 - 18h - 24h
    [3] T7 - CN - 5h - 12h
    [4] T7 - CN - 12h - 18h
    [5] T7 - CN - 18h - 24h
  - Template index tìm được: 1
✅ Cập nhật template rule [1] với giá: '30000' (rule.price: 30000)
```

### **Bước 5: Kiểm Tra Kết Quả Mapping**
Sau khi xử lý tất cả 6 rules, bạn sẽ thấy:

```
✅ Đã map 6 pricing rules thành công
  [0] CourtPricingRule: dayOfWeek=T2 - T6, timeSlot=5h - 12h, price='20000'
  [1] CourtPricingRule: dayOfWeek=T2 - T6, timeSlot=12h - 18h, price='30000'
  [2] CourtPricingRule: dayOfWeek=T2 - T6, timeSlot=18h - 24h, price='40000'
  [3] CourtPricingRule: dayOfWeek=T7 - CN, timeSlot=5h - 12h, price='50000'
  [4] CourtPricingRule: dayOfWeek=T7 - CN, timeSlot=12h - 18h, price='60000'
  [5] CourtPricingRule: dayOfWeek=T7 - CN, timeSlot=18h - 24h, price='80000'
```

### **Bước 6: Kiểm Tra UI Hiển Thị**
Bây giờ UI sẽ hiển thị:

```
🔍 DEBUG: Tìm rule cho T2 - T6 - 5h - 12h
  - pricingRules.size: 6
  - existingRule: CourtPricingRule(id=..., dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=20000, ...)
  - existingRule?.price: '20000' (isEmpty: false)

🔍 DEBUG: Hiển thị cho T2 - T6 - 5h - 12h
  - existingRule?.price: '20000'
  - existingRule?.price?.isNotEmpty(): true
  - displayText: '20000 ₫'
```

## 🔍 **Các Vấn Đề Có Thể Xảy Ra:**

### **Vấn Đề 1: Template Search Fail**
**Triệu chứng**: `Template index tìm được: -1`
**Nguyên nhân**: Mapping logic sai
**Giải pháp**: Kiểm tra `mappedDayOfWeek` và `mappedTimeSlot`

### **Vấn Đề 2: Price Không Được Set**
**Triệu chứng**: `price=''` trong CourtPricingRule
**Nguyên nhân**: Logic cập nhật template fail
**Giải pháp**: Kiểm tra `templateIndex` và `templateRules[index]`

### **Vấn Đề 3: State Không Được Cập Nhật**
**Triệu chứng**: `pricingRules.size: 0` trong UI
**Nguyên nhân**: State assignment fail
**Giải pháp**: Kiểm tra `pricingRules = newPricingRules`

## 📱 **Kết Quả Mong Đợi Sau Khi Fix:**

### ✅ **UI Sẽ Hiển Thị:**
- **T2 - T6 5h - 12h**: "20000 ₫" ✅
- **T2 - T6 12h - 18h**: "30000 ₫" ✅  
- **T2 - T6 18h - 24h**: "40000 ₫" ✅
- **T7 - CN 5h - 12h**: "50000 ₫" ✅
- **T7 - CN 12h - 18h**: "60000 ₫" ✅
- **T7 - CN 18h - 24h**: "80000 ₫" ✅

### ❌ **Không Còn:**
- "Chưa có giá" ở bất kỳ ô nào
- Pricing rules trống
- Mapping errors

## 🚀 **Bước Tiếp Theo:**

1. **Chạy app** và test theo hướng dẫn trên
2. **Copy toàn bộ debug logs** và gửi cho tôi
3. **Chụp ảnh màn hình** hiện tại
4. **Mô tả chính xác** những gì bạn thấy

Với debug logs mới này, chúng ta sẽ biết chính xác vấn đề nằm ở đâu trong mapping logic và sửa nó một cách hiệu quả! 🚀

---

**💡 Lưu ý**: Debug logs sẽ hiển thị chi tiết từng bước mapping, giúp chúng ta xác định chính xác vấn đề và sửa nó.
