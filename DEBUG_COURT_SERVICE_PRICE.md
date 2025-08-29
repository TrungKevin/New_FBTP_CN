# 🔍 **Debug CourtService - Vấn Đề Hiển Thị Giá**

## 🚨 **Vấn Đề Hiện Tại**

Mặc dù đã sửa logic mapping, giá vẫn không hiển thị sau khi lưu vào Firebase. Luôn hiển thị "Chưa có giá".

## 🔧 **Đã Thêm Debug Logs**

Tôi đã thêm các debug logs chi tiết để kiểm tra vấn đề:

### **1. Debug khi tìm rule**
```kotlin
// DEBUG: Kiểm tra rule tìm được
println("🔍 DEBUG: Tìm rule cho $dayOfWeek - $timeSlot")
println("  - pricingRules.size: ${pricingRules.size}")
println("  - existingRule: $existingRule")
println("  - existingRule?.price: '${existingRule?.price}' (isEmpty: ${existingRule?.price?.isEmpty()})")
```

### **2. Debug khi thay đổi giá**
```kotlin
println("🔍 DEBUG: onValueChange cho $dayOfWeek - $timeSlot với giá: '$newPrice'")
println("  - existingRule: $existingRule")
println("  - pricingRules.size trước: ${pricingRules.size}")
// ... chi tiết về việc cập nhật/thêm rule
```

### **3. Debug khi hiển thị**
```kotlin
println("🔍 DEBUG: Hiển thị cho $dayOfWeek - $timeSlot")
println("  - existingRule?.price: '${existingRule?.price}'")
println("  - existingRule?.price?.isNotEmpty(): ${existingRule?.price?.isNotEmpty()}")
println("  - displayText: '$displayText'")
```

## 🧪 **Hướng Dẫn Test và Debug**

### **Bước 1: Chạy App và Mở Console**
1. Build và chạy app
2. Mở Android Studio Logcat hoặc terminal với `adb logcat`
3. Filter logs với tag: `System.out` hoặc tìm "DEBUG"

### **Bước 2: Test Nhập Giá**
1. Đăng nhập owner
2. Vào một sân → Chọn "Bảng giá & Dịch vụ"
3. Click "Chỉnh sửa"
4. Nhập giá cho T2-T6 5h-12h: "50000"
5. **Quan sát console logs**

### **Bước 3: Kiểm Tra Logs**

Bạn sẽ thấy logs như sau:

```
🔍 DEBUG: onValueChange cho T2 - T6 - 5h - 12h với giá: '50000'
  - existingRule: null
  - pricingRules.size trước: 0
  - Tạo rule mới
  - Đã thêm rule mới: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=50000, ...)
  - pricingRules.size sau: 1
  - pricingRules hiện tại:
    [0] T2 - T6 - 5h - 12h: '50000'
```

### **Bước 4: Kiểm Tra Hiển Thị**
1. Thoát edit mode (click "Hủy" hoặc "Lưu")
2. **Quan sát console logs khi hiển thị**

Bạn sẽ thấy logs như sau:

```
🔍 DEBUG: Tìm rule cho T2 - T6 - 5h - 12h
  - pricingRules.size: 1
  - existingRule: CourtPricingRule(id=1, dayOfWeek=T2 - T6, timeSlot=5h - 12h, price=50000, ...)
  - existingRule?.price: '50000' (isEmpty: false)

🔍 DEBUG: Hiển thị cho T2 - T6 - 5h - 12h
  - existingRule?.price: '50000'
  - existingRule?.price?.isNotEmpty(): true
  - displayText: '50000 ₫'
```

## 🔍 **Các Vấn Đề Có Thể Xảy Ra**

### **Vấn Đề 1: State không được cập nhật**
**Triệu chứng**: `pricingRules.size` luôn = 0
**Nguyên nhân**: State management có vấn đề
**Giải pháp**: Kiểm tra `LaunchedEffect` và `updateUIDataFromFirebase`

### **Vấn Đề 2: Rule không được tìm thấy**
**Triệu chứng**: `existingRule` luôn = null
**Nguyên nhân**: Logic tìm kiếm sai hoặc dữ liệu không khớp
**Giải pháp**: Kiểm tra `dayOfWeek` và `timeSlot` có khớp không

### **Vấn Đề 3: Giá bị mất sau khi lưu**
**Triệu chứng**: Giá có trong `pricingRules` nhưng không hiển thị
**Nguyên nhân**: Logic hiển thị sai hoặc state bị reset
**Giải pháp**: Kiểm tra `isNotEmpty()` logic

## 📱 **Test Cases Cụ Thể**

### **Test Case 1: Nhập giá mới**
```
1. Edit mode → Nhập "50000" cho T2-T6 5h-12h
2. Kiểm tra logs: existingRule = null, tạo rule mới
3. Thoát edit mode
4. Kiểm tra logs: existingRule có giá, hiển thị "50000 ₫"
```

### **Test Case 2: Sửa giá cũ**
```
1. Edit mode → Sửa "50000" thành "60000"
2. Kiểm tra logs: existingRule có giá, cập nhật rule
3. Thoát edit mode
4. Kiểm tra logs: hiển thị "60000 ₫"
```

### **Test Case 3: Lưu vào Firebase**
```
1. Nhập giá → Click "Lưu"
2. Kiểm tra logs: Firebase success
3. Kiểm tra logs: Reload data từ Firebase
4. Kiểm tra logs: Mapping dữ liệu từ Firebase
5. Kiểm tra logs: Hiển thị giá từ Firebase
```

## 🚀 **Bước Tiếp Theo**

1. **Chạy test** theo hướng dẫn trên
2. **Copy toàn bộ logs** và gửi cho tôi
3. **Chụp ảnh màn hình** hiện tại
4. **Mô tả chính xác** những gì bạn thấy

Với debug logs này, chúng ta sẽ biết chính xác vấn đề nằm ở đâu và sửa nó một cách hiệu quả!

---

**💡 Lưu ý**: Debug logs sẽ hiển thị trong console của Android Studio hoặc `adb logcat`. Hãy đảm bảo bạn có thể thấy được logs này khi test.
