# 🧪 **CourtService CRUD Test Guide**

## 🎯 **Mục Đích Test**

Kiểm tra chức năng CRUD hoàn chỉnh của CourtService trong OwnerFieldDetailScreen:
- ✅ **Create**: Tạo bảng giá và dịch vụ mới
- ✅ **Read**: Đọc dữ liệu từ Firebase và hiển thị
- ✅ **Update**: Chỉnh sửa bảng giá và dịch vụ
- ✅ **Delete**: Xóa dịch vụ không cần thiết

## 🚀 **Bước 1: Chuẩn Bị Test**

### **1.1 Đăng nhập Owner Account**
```
1. Mở app FBTP_CN
2. Đăng nhập với tài khoản owner
3. Vào màn hình quản lý sân
4. Chọn một sân để xem chi tiết
```

### **1.2 Kiểm tra Tab CourtService**
```
1. Trong OwnerFieldDetailScreen
2. Chọn tab "BẢNG GIÁ & DỊCH VỤ" (tab thứ 2)
3. Kiểm tra dữ liệu hiện tại từ Firebase
```

## 📊 **Bước 2: Test CRUD Operations**

### **2.1 Test Create - Tạo Bảng Giá Mới**

#### **Scenario 1: Sân chưa có bảng giá**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Nhập giá cho các khung giờ:
   - T2-T6, 5h-12h: 55000
   - T2-T6, 12h-18h: 60000
   - T2-T6, 18h-24h: 70000
   - T7-CN, 5h-12h: 80000
   - T7-CN, 12h-18h: 85000
   - T7-CN, 18h-24h: 90000
3. Click nút 💾 (Save)
4. Kiểm tra: Loading dialog hiển thị
5. Kiểm tra: Thông báo thành công
6. Kiểm tra: Tự động thoát edit mode
7. Kiểm tra: Bảng giá hiển thị giá mới
```

#### **Expected Result:**
- ✅ Dữ liệu được lưu vào Firebase
- ✅ UI hiển thị giá mới
- ✅ Không có validation errors

### **2.2 Test Create - Tạo Dịch Vụ Mới**

#### **Scenario 2: Thêm dịch vụ mới**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Trong phần "DỊCH VỤ BỔ SUNG"
3. Thêm dịch vụ mới:
   - Danh mục "Banh": "Banh tennis" - "180000"
   - Danh mục "Nước đóng chai": "Coca Cola" - "15000"
4. Click nút 💾 (Save)
5. Kiểm tra: Dịch vụ mới xuất hiện trong UI
```

#### **Expected Result:**
- ✅ Dịch vụ mới được lưu vào Firebase
- ✅ UI hiển thị dịch vụ mới
- ✅ Dịch vụ được phân loại đúng danh mục

### **2.3 Test Read - Đọc Dữ Liệu Từ Firebase**

#### **Scenario 3: Refresh dữ liệu**
```
1. Click nút 🔄 (Refresh)
2. Kiểm tra: Dữ liệu được reload từ Firebase
3. Kiểm tra: UI hiển thị dữ liệu mới nhất
```

#### **Expected Result:**
- ✅ Dữ liệu được reload từ Firebase
- ✅ UI hiển thị dữ liệu mới nhất
- ✅ Không có lỗi loading

### **2.4 Test Update - Chỉnh Sửa Dữ Liệu**

#### **Scenario 4: Chỉnh sửa bảng giá**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Chỉnh sửa giá:
   - T2-T6, 5h-12h: 55000 → 60000
   - T7-CN, 18h-24h: 90000 → 95000
3. Click nút 💾 (Save)
4. Kiểm tra: Giá mới được lưu vào Firebase
5. Kiểm tra: UI hiển thị giá mới
```

#### **Expected Result:**
- ✅ Giá mới được lưu vào Firebase
- ✅ UI hiển thị giá mới
- ✅ Không có validation errors

#### **Scenario 5: Chỉnh sửa dịch vụ**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Chỉnh sửa dịch vụ:
   - "Sting" → "Sting Energy"
   - "12000" → "13000"
3. Click nút 💾 (Save)
4. Kiểm tra: Dịch vụ được cập nhật
```

#### **Expected Result:**
- ✅ Dịch vụ được cập nhật trong Firebase
- ✅ UI hiển thị thông tin mới
- ✅ Không có validation errors

### **2.5 Test Delete - Xóa Dữ Liệu**

#### **Scenario 6: Xóa dịch vụ**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Click nút 🗑️ (Delete) bên cạnh dịch vụ "Revie"
3. Click nút 💾 (Save)
4. Kiểm tra: Dịch vụ biến mất khỏi UI
```

#### **Expected Result:**
- ✅ Dịch vụ được xóa khỏi Firebase
- ✅ UI không còn hiển thị dịch vụ đã xóa
- ✅ Không có validation errors

## 🔍 **Bước 3: Test Validation**

### **3.1 Test Validation Errors**

#### **Scenario 7: Giá không hợp lệ**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Nhập giá không hợp lệ:
   - T2-T6, 5h-12h: "abc" (không phải số)
   - T2-T6, 12h-18h: "-1000" (số âm)
3. Click nút 💾 (Save)
4. Kiểm tra: Validation errors hiển thị
```

#### **Expected Result:**
- ❌ Validation errors hiển thị
- ❌ Dữ liệu không được lưu vào Firebase
- ❌ UI vẫn ở edit mode

#### **Scenario 8: Dịch vụ thiếu thông tin**
```
1. Click nút ✏️ (Edit) để vào edit mode
2. Tạo dịch vụ thiếu thông tin:
   - Tên: "Banh mới" (có tên)
   - Giá: "" (không có giá)
3. Click nút 💾 (Save)
4. Kiểm tra: Validation errors hiển thị
```

#### **Expected Result:**
- ❌ Validation errors hiển thị
- ❌ Dữ liệu không được lưu vào Firebase
- ❌ UI vẫn ở edit mode

## 🚨 **Bước 4: Test Error Handling**

### **4.1 Test Network Errors**

#### **Scenario 9: Mất kết nối mạng**
```
1. Tắt WiFi/mobile data
2. Thực hiện thao tác lưu dữ liệu
3. Kiểm tra: Error message hiển thị
```

#### **Expected Result:**
- ❌ Error message hiển thị
- ❌ Dữ liệu không được lưu
- ❌ UI vẫn ở edit mode

### **4.2 Test Firebase Errors**

#### **Scenario 10: Firebase permission denied**
```
1. Sử dụng tài khoản không có quyền write
2. Thực hiện thao tác lưu dữ liệu
3. Kiểm tra: Firebase error message hiển thị
```

#### **Expected Result:**
- ❌ Firebase error message hiển thị
- ❌ Dữ liệu không được lưu
- ❌ UI vẫn ở edit mode

## 📱 **Bước 5: Test UI/UX**

### **5.1 Test Responsive Design**

#### **Scenario 11: Thay đổi orientation**
```
1. Xoay màn hình từ portrait sang landscape
2. Kiểm tra: UI hiển thị đúng
3. Kiểm tra: Bảng giá và dịch vụ không bị vỡ layout
```

#### **Expected Result:**
- ✅ UI hiển thị đúng ở cả hai orientation
- ✅ Layout không bị vỡ
- ✅ Dữ liệu vẫn hiển thị chính xác

### **5.2 Test Accessibility**

#### **Scenario 12: Screen reader support**
```
1. Bật screen reader
2. Navigate qua các elements
3. Kiểm tra: Content descriptions đầy đủ
```

#### **Expected Result:**
- ✅ Screen reader đọc được tất cả content
- ✅ Content descriptions rõ ràng
- ✅ Navigation logic hợp lý

## 🔄 **Bước 6: Test Performance**

### **6.1 Test Loading Performance**

#### **Scenario 13: Load dữ liệu lớn**
```
1. Tạo sân với nhiều pricing rules và services
2. Kiểm tra: Thời gian load dữ liệu
3. Kiểm tra: UI responsiveness
```

#### **Expected Result:**
- ✅ Dữ liệu load trong thời gian hợp lý (< 3 giây)
- ✅ UI responsive trong quá trình loading
- ✅ Loading indicator hiển thị rõ ràng

### **6.2 Test Save Performance**

#### **Scenario 14: Lưu dữ liệu lớn**
```
1. Chỉnh sửa nhiều pricing rules và services
2. Click Save
3. Kiểm tra: Thời gian lưu dữ liệu
```

#### **Expected Result:**
- ✅ Dữ liệu được lưu trong thời gian hợp lý (< 5 giây)
- ✅ Loading dialog hiển thị rõ ràng
- ✅ Progress indicator nếu cần thiết

## 📋 **Bước 7: Test Data Consistency**

### **7.1 Test Data Sync**

#### **Scenario 15: Đồng bộ dữ liệu**
```
1. Mở app trên 2 thiết bị khác nhau
2. Chỉnh sửa dữ liệu trên thiết bị 1
3. Refresh dữ liệu trên thiết bị 2
4. Kiểm tra: Dữ liệu được đồng bộ
```

#### **Expected Result:**
- ✅ Dữ liệu được đồng bộ giữa 2 thiết bị
- ✅ Không có conflict data
- ✅ Timestamp được cập nhật chính xác

## 🎯 **Kết Quả Mong Đợi**

### **✅ Success Cases:**
- Tất cả CRUD operations hoạt động chính xác
- Dữ liệu được lưu và đọc từ Firebase thành công
- UI hiển thị dữ liệu chính xác và responsive
- Validation hoạt động đúng
- Error handling rõ ràng

### **❌ Failure Cases:**
- Validation errors được hiển thị rõ ràng
- Network errors được handle gracefully
- Firebase errors được hiển thị với thông tin hữu ích
- UI không bị crash khi có lỗi

## 🚀 **Cách Chạy Test**

### **Manual Testing:**
```
1. Follow từng scenario theo thứ tự
2. Ghi lại kết quả cho mỗi test case
3. Report bugs nếu có
4. Verify fixes sau khi developer sửa
```

### **Automated Testing (Future):**
```
1. Implement unit tests cho ViewModel
2. Implement integration tests cho Repository
3. Implement UI tests cho Compose components
4. Setup CI/CD pipeline
```

## 📝 **Test Report Template**

```
Test Date: _______________
Tester: ________________
App Version: ___________

✅ Passed Tests:
- [ ] Test Create - Bảng giá mới
- [ ] Test Create - Dịch vụ mới
- [ ] Test Read - Refresh data
- [ ] Test Update - Chỉnh sửa giá
- [ ] Test Update - Chỉnh sửa dịch vụ
- [ ] Test Delete - Xóa dịch vụ
- [ ] Test Validation - Giá không hợp lệ
- [ ] Test Validation - Dịch vụ thiếu thông tin
- [ ] Test Error Handling - Network error
- [ ] Test Error Handling - Firebase error
- [ ] Test UI/UX - Responsive design
- [ ] Test UI/UX - Accessibility
- [ ] Test Performance - Loading
- [ ] Test Performance - Saving
- [ ] Test Data Consistency - Sync

❌ Failed Tests:
- [ ] Test Case: _______________
  - Expected: _______________
  - Actual: _______________
  - Steps to reproduce: _______________

🐛 Bugs Found:
- [ ] Bug Description: _______________
  - Severity: High/Medium/Low
  - Steps to reproduce: _______________
  - Expected behavior: _______________
  - Actual behavior: _______________

📊 Test Summary:
- Total Tests: ___
- Passed: ___
- Failed: ___
- Success Rate: ___%
- Critical Issues: ___
- Recommendations: _______________
```

## 🎉 **Kết Luận**

Test plan này sẽ giúp đảm bảo CourtService hoạt động chính xác và đáng tin cậy. Sau khi hoàn thành tất cả test cases, owner sẽ có thể:

✅ **Quản lý bảng giá và dịch vụ một cách dễ dàng**
✅ **Lưu trữ dữ liệu an toàn vào Firebase**
✅ **Hiển thị dữ liệu chính xác và real-time**
✅ **Xử lý lỗi một cách graceful**
✅ **Có trải nghiệm người dùng tốt**

Hãy chạy test theo plan này và report kết quả để đảm bảo chất lượng của CourtService! 🚀
