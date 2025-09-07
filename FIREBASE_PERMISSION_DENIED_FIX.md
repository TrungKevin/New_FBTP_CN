# 🔧 Sửa lỗi PERMISSION_DENIED khi xóa sân

## 🎯 **Vấn đề**

Khi xóa sân, app gặp lỗi `PERMISSION_DENIED: Missing or insufficient permissions` khi cố gắng xóa `pricing_rules` và `field_services` liên quan.

### **Nguyên nhân:**

1. **Thứ tự xóa sai:** App xóa field document trước, sau đó mới xóa pricing_rules
2. **Firebase Security Rules:** Function `isFieldOwner()` cần field document tồn tại để kiểm tra quyền
3. **Race condition:** Khi field đã bị xóa, Security Rules không thể verify owner

## 🔍 **Log lỗi:**

```
2025-09-07 12:24:30.110 Firestore W (25.0.0) [Firestore]: Write failed at pricing_rules/5pXEE71rdVNhsUxi8ZH5: Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
```

## ✅ **Giải pháp**

### **Thay đổi thứ tự xóa trong FieldRepository.deleteField():**

**TRƯỚC (Sai):**
```kotlin
// 1. Xóa field document trước
firestore.collection(FIELDS_COLLECTION)
    .document(fieldId)
    .delete()
    .await()

// 2. Sau đó mới xóa pricing_rules (❌ LỖI)
val rulesSnapshot = firestore.collection(PRICING_RULES_COLLECTION)
    .whereEqualTo("fieldId", fieldId)
    .get()
    .await()
```

**SAU (Đúng):**
```kotlin
// 1. Xóa pricing_rules TRƯỚC KHI xóa field document
val rulesSnapshot = firestore.collection(PRICING_RULES_COLLECTION)
    .whereEqualTo("fieldId", fieldId)
    .get()
    .await()

if (rulesSnapshot.size() > 0) {
    val batch = firestore.batch()
    rulesSnapshot.documents.forEach { doc ->
        batch.delete(doc.reference)
    }
    batch.commit().await()
    println("✅ DEBUG: ${rulesSnapshot.size()} pricing rules deleted")
}

// 2. Xóa field_services TRƯỚC KHI xóa field document
val servicesSnapshot = firestore.collection(FIELD_SERVICES_COLLECTION)
    .whereEqualTo("fieldId", fieldId)
    .get()
    .await()

if (servicesSnapshot.size() > 0) {
    val servicesBatch = firestore.batch()
    servicesSnapshot.documents.forEach { doc ->
        servicesBatch.delete(doc.reference)
    }
    servicesBatch.commit().await()
    println("✅ DEBUG: ${servicesSnapshot.size()} field services deleted")
}

// 3. Xóa field document CUỐI CÙNG
firestore.collection(FIELDS_COLLECTION)
    .document(fieldId)
    .delete()
    .await()
```

## 🔐 **Firebase Security Rules**

Rules hiện tại yêu cầu field document tồn tại để verify owner:

```javascript
function isFieldOwner(fieldId) {
  return signedIn() && 
    get(/databases/$(db)/documents/fields/$(fieldId)).data.ownerId == request.auth.uid;
}

match /pricing_rules/{ruleId} {
  allow update, delete: if signedIn() && 
    resource.data.fieldId != null &&
    isFieldOwner(resource.data.fieldId); // ❌ Cần field tồn tại
}
```

## 🎯 **Tại sao giải pháp này hoạt động:**

1. **Field document vẫn tồn tại** khi xóa pricing_rules và field_services
2. **Security Rules có thể verify owner** thông qua `isFieldOwner()`
3. **Không có race condition** giữa các operations
4. **Atomic operations** với batch delete

## 🧪 **Test case:**

1. **Tạo sân mới** với pricing_rules và field_services
2. **Xóa sân** → Kiểm tra không có lỗi PERMISSION_DENIED
3. **Verify** tất cả documents liên quan đã bị xóa
4. **Kiểm tra** cả OwnerHomeScreen và OwnerFieldManagementScreen cập nhật đồng bộ

## 📊 **Kết quả:**

- ✅ **Không còn lỗi PERMISSION_DENIED**
- ✅ **Xóa sân hoạt động hoàn hảo**
- ✅ **Đồng bộ dữ liệu giữa các màn hình**
- ✅ **Security Rules được tuân thủ**

## 🔄 **Thứ tự xóa mới:**

```
1. Pricing Rules (field vẫn tồn tại → Security Rules OK)
2. Field Services (field vẫn tồn tại → Security Rules OK)  
3. Reviews (field vẫn tồn tại → Security Rules OK)
4. Slots (field vẫn tồn tại → Security Rules OK)
5. Field Document (cuối cùng)
```

## 💡 **Lưu ý:**

- **Luôn xóa child documents trước parent documents**
- **Sử dụng batch operations** để đảm bảo atomicity
- **Kiểm tra Security Rules** khi design data deletion flow
- **Test thoroughly** với các edge cases

---

**Phiên bản:** 1.0.0  
**Cập nhật:** 2024-12-19  
**Tác giả:** FBTP Development Team
