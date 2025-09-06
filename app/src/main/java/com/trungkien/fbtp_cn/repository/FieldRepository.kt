package com.trungkien.fbtp_cn.repository

import android.net.Uri
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.FieldImages
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.model.FieldService
import com.trungkien.fbtp_cn.model.Slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class FieldRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val FIELDS_COLLECTION = "fields"
        private const val PRICING_RULES_COLLECTION = "pricing_rules"
        private const val FIELD_SERVICES_COLLECTION = "field_services"
        private const val SLOTS_COLLECTION = "slots"
        private const val BOOKINGS_COLLECTION = "bookings"
        private const val REVIEWS_COLLECTION = "reviews"
    }
    
    /**
     * Thêm sân mới với đầy đủ thông tin
     */
    suspend fun addField(
        field: Field,
        images: List<Uri>,
        pricingRules: List<PricingRule>,
        fieldServices: List<FieldService>
    ): Result<String> {
        return try {
            // 1. Convert ảnh thành base64 string
            val base64Images = convertImagesToBase64(images)
            
            // 2. Cập nhật field với base64 strings
            val updatedField = field.copy(
                images = FieldImages(
                    mainImage = base64Images.getOrNull(0) ?: "",
                    image1 = base64Images.getOrNull(1) ?: "",
                    image2 = base64Images.getOrNull(2) ?: "",
                    image3 = base64Images.getOrNull(3) ?: ""
                )
            )
            
            // 3. Lưu field vào Firestore
            val fieldDoc = firestore.collection(FIELDS_COLLECTION).document()
            val fieldId = fieldDoc.id
            
            val fieldWithId = updatedField.copy(fieldId = fieldId)
            fieldDoc.set(fieldWithId).await()
            
            // 4. Tạo pricing rules
            if (pricingRules.isNotEmpty()) {
                createPricingRules(fieldId, pricingRules)
            }
            
            // 5. Tạo field services
            if (fieldServices.isNotEmpty()) {
                createFieldServices(fieldId, fieldServices)
            }
            
            Result.success(fieldId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert ảnh thành base64 string
     */
    private suspend fun convertImagesToBase64(images: List<Uri>): List<String> {
        return withContext(Dispatchers.IO) {
            val base64Images = mutableListOf<String>()
            
            images.forEachIndexed { index, uri ->
                try {
                    val context = firestore.app.applicationContext
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    
                    if (bytes != null) {
                        // Compress ảnh để giảm kích thước
                        val compressedBytes = compressImage(bytes)
                        val base64String = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                        base64Images.add(base64String)
                    } else {
                        base64Images.add("")
                    }
                } catch (e: Exception) {
                    // Nếu convert ảnh thất bại, thêm string rỗng
                    base64Images.add("")
                }
            }
            
            base64Images
        }
    }
    
    /**
     * Compress ảnh để giảm kích thước trước khi lưu vào Firestore
     */
    private fun compressImage(bytes: ByteArray): ByteArray {
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            // Resize ảnh nếu quá lớn (giới hạn 800x800)
            val maxSize = 800
            val width = bitmap.width
            val height = bitmap.height
            
            val scaledBitmap = if (width > maxSize || height > maxSize) {
                val scale = maxSize.toFloat() / maxOf(width, height)
                val newWidth = (width * scale).toInt()
                val newHeight = (height * scale).toInt()
                android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            // Convert về JPEG với chất lượng 80%
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val compressedBytes = outputStream.toByteArray()
            
            // Cleanup
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            outputStream.close()
            
            compressedBytes
        } catch (e: Exception) {
            // Nếu compress thất bại, trả về bytes gốc
            bytes
        }
    }
    
    /**
     * Tạo pricing rules cho sân
     */
    private suspend fun createPricingRules(fieldId: String, rules: List<PricingRule>) {
        val batch = firestore.batch()
        
        rules.forEach { rule ->
            val ruleDoc = firestore.collection(PRICING_RULES_COLLECTION).document()
            val ruleWithId = rule.copy(
                ruleId = ruleDoc.id,
                fieldId = fieldId
            )
            batch.set(ruleDoc, ruleWithId)
        }
        
        batch.commit().await()
    }
    
    /**
     * Tạo field services cho sân
     */
    private suspend fun createFieldServices(fieldId: String, services: List<FieldService>) {
        val batch = firestore.batch()
        
        services.forEach { service ->
            val serviceDoc = firestore.collection(FIELD_SERVICES_COLLECTION).document()
            val serviceWithId = service.copy(
                fieldServiceId = serviceDoc.id,
                fieldId = fieldId
            )
            batch.set(serviceDoc, serviceWithId)
        }
        
        batch.commit().await()
    }
    
    /**
     * Lấy danh sách sân của owner
     */
    suspend fun getFieldsByOwnerId(ownerId: String): Result<List<Field>> {
        return try {
            println("DEBUG: Querying fields for ownerId: $ownerId")
            val snapshot = firestore.collection(FIELDS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            println("DEBUG: Found ${snapshot.documents.size} documents")
            
            val fields = snapshot.documents.mapNotNull { doc ->
                println("DEBUG: Document ID: ${doc.id}, data: ${doc.data}")
                doc.toObject(Field::class.java)
            }
            
            println("DEBUG: Successfully converted ${fields.size} fields")
            Result.success(fields)
        } catch (e: Exception) {
            println("DEBUG: Error in getFieldsByOwnerId: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy thông tin sân theo ID
     */
    suspend fun getFieldById(fieldId: String): Result<Field?> {
        return try {
            val doc = firestore.collection(FIELDS_COLLECTION)
                .document(fieldId)
                .get()
                .await()
            
            val field = doc.toObject(Field::class.java)
            Result.success(field)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy pricing rules của sân
     */
    suspend fun getPricingRulesByFieldId(fieldId: String): Result<List<PricingRule>> {
        return try {
            println("🔄 DEBUG: FieldRepository.getPricingRulesByFieldId($fieldId)")
            println("🔍 DEBUG: Querying collection: $PRICING_RULES_COLLECTION")
            println("🔍 DEBUG: Filter: fieldId == $fieldId")
            
            // ✅ DEBUG: Kiểm tra toàn bộ collection trước
            val allRulesSnapshot = firestore.collection(PRICING_RULES_COLLECTION).get().await()
            println("🔍 DEBUG: Tổng số documents trong collection: ${allRulesSnapshot.size()}")
            if (allRulesSnapshot.size() > 0) {
                println("🔍 DEBUG: Sample documents:")
                allRulesSnapshot.documents.take(3).forEach { doc ->
                    val sampleRule = doc.toObject(PricingRule::class.java)
                    if (sampleRule != null) {
                        println("  📄 ${doc.id}: fieldId='${sampleRule.fieldId}', price=${sampleRule.price}, description='${sampleRule.description}'")
                    }
                }
            }
            
            val snapshot = firestore.collection(PRICING_RULES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            println("✅ DEBUG: Firebase query thành công")
            println("🔍 DEBUG: Snapshot size: ${snapshot.size()}")
            println("🔍 DEBUG: Documents count: ${snapshot.documents.size}")
            
            val rules = snapshot.documents.mapNotNull { doc ->
                val rule = doc.toObject(PricingRule::class.java)
                if (rule != null) {
                    println("  ✅ Document ${doc.id}: ruleId='${rule.ruleId}', fieldId='${rule.fieldId}', price=${rule.price}")
                } else {
                    println("  ⚠️ Document ${doc.id}: Không thể parse thành PricingRule")
                }
                rule
            }
            
            println("✅ DEBUG: Parsed ${rules.size} pricing rules thành công")
            Result.success(rules)
        } catch (e: Exception) {
            println("❌ ERROR: FieldRepository.getPricingRulesByFieldId thất bại: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy field services của sân
     */
    suspend fun getFieldServicesByFieldId(fieldId: String): Result<List<FieldService>> {
        return try {
            val snapshot = firestore.collection(FIELD_SERVICES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            val services = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FieldService::class.java)
            }
            
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Thêm pricing rule mới
     */
    suspend fun addPricingRule(pricingRule: PricingRule): Result<String> {
        return try {
            val ruleDoc = firestore.collection(PRICING_RULES_COLLECTION).document()
            val ruleId = ruleDoc.id
            
            val ruleWithId = pricingRule.copy(ruleId = ruleId)
            ruleDoc.set(ruleWithId).await()
            
            Result.success(ruleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Thêm field service mới
     */
    suspend fun addFieldService(fieldService: FieldService): Result<String> {
        return try {
            val serviceDoc = firestore.collection(FIELD_SERVICES_COLLECTION).document()
            val serviceId = serviceDoc.id
            
            val serviceWithId = fieldService.copy(fieldServiceId = serviceId)
            serviceDoc.set(serviceWithId).await()
            
            Result.success(serviceId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy slots theo fieldId và ngày
     */
    suspend fun getSlotsByFieldIdAndDate(fieldId: String, date: String): Result<List<Slot>> {
        return try {
            println("🔄 DEBUG: FieldRepository.getSlotsByFieldIdAndDate($fieldId, $date)")
            println("🔍 DEBUG: Querying collection: SLOTS_COLLECTION")
            println("🔍 DEBUG: Filter: fieldId == $fieldId AND date == $date")
            
            val snapshot = firestore.collection(SLOTS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()
            
            println("✅ DEBUG: Firebase query thành công")
            println("🔍 DEBUG: Snapshot size: ${snapshot.size()}")
            println("🔍 DEBUG: Documents count: ${snapshot.documents.size}")
            
            val slots = snapshot.toObjects(Slot::class.java)
            println("✅ DEBUG: Parsed ${slots.size} slots from Firebase")
            
            slots.forEachIndexed { index, slot ->
                println("  [$index] slotId: '${slot.slotId}', fieldId: '${slot.fieldId}', date: '${slot.date}', startAt: '${slot.startAt}', endAt: '${slot.endAt}', isBooked: ${slot.isBooked}")
            }
            
            Result.success(slots)
        } catch (e: Exception) {
            println("❌ ERROR: FieldRepository.getSlotsByFieldIdAndDate failed: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    

    

    

    

    
    /**
     * Cập nhật thông tin sân
     */
    suspend fun updateField(field: Field): Result<Unit> {
        return try {
            firestore.collection(FIELDS_COLLECTION)
                .document(field.fieldId)
                .set(field)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Kiểm tra xem sân có booking chưa hoàn thành và chưa qua thời gian sử dụng không
     */
    suspend fun checkFieldHasActiveBookings(fieldId: String): Result<Boolean> {
        return try {
            println("🔄 DEBUG: FieldRepository.checkFieldHasActiveBookings($fieldId)")
            
            // Kiểm tra bookings có status PENDING hoặc PAID (chưa hoàn thành)
            val bookingsSnapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereIn("status", listOf("PENDING", "PAID"))
                .get()
                .await()
            
            println("🔍 DEBUG: Found ${bookingsSnapshot.size()} active bookings for field $fieldId")
            
            if (bookingsSnapshot.size() == 0) {
                println("✅ DEBUG: No active bookings found - field can be deleted")
                return Result.success(false)
            }
            
            // Kiểm tra từng booking xem có còn trong thời gian sử dụng không
            val currentTime = System.currentTimeMillis()
            var hasValidBookings = false
            
            bookingsSnapshot.documents.forEach { doc ->
                val bookingData = doc.data
                val date = bookingData?.get("date") as? String
                val startAt = bookingData?.get("startAt") as? String
                val status = bookingData?.get("status") as? String
                
                println("  📄 Booking ${doc.id}: status=$status, date=$date, startAt=$startAt")
                
                if (date != null && startAt != null) {
                    // Tạo timestamp cho thời điểm kết thúc booking (giả sử mỗi booking 1 giờ)
                    val bookingDateTime = try {
                        val dateParts = date.split("-")
                        val timeParts = startAt.split(":")
                        val year = dateParts[0].toInt()
                        val month = dateParts[1].toInt() - 1 // Java Calendar months are 0-based
                        val day = dateParts[2].toInt()
                        val hour = timeParts[0].toInt()
                        val minute = timeParts[1].toInt()
                        
                        val calendar = java.util.Calendar.getInstance()
                        calendar.set(year, month, day, hour, minute, 0)
                        calendar.timeInMillis
                    } catch (e: Exception) {
                        println("  ❌ Error parsing date/time: ${e.message}")
                        currentTime + 86400000 // Default to tomorrow if parsing fails
                    }
                    
                    // Thêm 1 giờ để có thời gian kết thúc booking
                    val bookingEndTime = bookingDateTime + (60 * 60 * 1000)
                    
                    println("  🕐 Booking end time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(bookingEndTime))}")
                    println("  🕐 Current time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(currentTime))}")
                    
                    if (bookingEndTime > currentTime) {
                        println("  ⚠️ Booking ${doc.id} is still active (not expired)")
                        hasValidBookings = true
                    } else {
                        println("  ✅ Booking ${doc.id} has expired")
                    }
                }
            }
            
            println("🔍 DEBUG: Has valid (non-expired) bookings: $hasValidBookings")
            Result.success(hasValidBookings)
        } catch (e: Exception) {
            println("❌ ERROR: FieldRepository.checkFieldHasActiveBookings failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Xóa sân với kiểm tra booking status
     * Xóa: field info, pricing rules, field services, reviews, slots
     * Giữ lại: bookings (lịch sử đặt sân)
     */
    suspend fun deleteField(fieldId: String): Result<Unit> {
        return try {
            println("🔄 DEBUG: FieldRepository.deleteField($fieldId)")
            
            // 0. Kiểm tra authentication state
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            println("🔍 DEBUG: Current Firebase Auth User: ${currentUser?.uid}")
            if (currentUser == null) {
                println("❌ DEBUG: No authenticated user - cannot delete field")
                return Result.failure(Exception("Bạn cần đăng nhập để xóa sân"))
            }
            
            // 1. Kiểm tra xem có booking chưa hoàn thành không
            val checkResult = checkFieldHasActiveBookings(fieldId)
            checkResult.fold(
                onSuccess = { hasActiveBookings ->
                    if (hasActiveBookings) {
                        println("❌ DEBUG: Cannot delete field - has active bookings")
                        return Result.failure(Exception("Không thể xóa sân vì có khách hàng đã đặt và chưa qua thời gian sử dụng. Vui lòng đợi đến khi tất cả các khe giờ đã đặt đều qua thời gian sử dụng."))
                    }
                },
                onFailure = { exception ->
                    println("❌ DEBUG: Failed to check booking status: ${exception.message}")
                    return Result.failure(exception)
                }
            )
            
            println("✅ DEBUG: No active bookings found, proceeding with deletion")
            
            // 2. Xóa field info
            firestore.collection(FIELDS_COLLECTION)
                .document(fieldId)
                .delete()
                .await()
            println("✅ DEBUG: Field info deleted")
            
            // 3. Xóa pricing rules
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
            
            // 4. Xóa field services
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
            
            // 5. Xóa reviews (đánh giá sân)
            val reviewsSnapshot = firestore.collection(REVIEWS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            if (reviewsSnapshot.size() > 0) {
                val reviewsBatch = firestore.batch()
                reviewsSnapshot.documents.forEach { doc ->
                    reviewsBatch.delete(doc.reference)
                }
                reviewsBatch.commit().await()
                println("✅ DEBUG: ${reviewsSnapshot.size()} reviews deleted")
            }
            
            // 6. Xóa slots (khe giờ) của sân
            val slotsSnapshot = firestore.collection(SLOTS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            if (slotsSnapshot.size() > 0) {
                val slotsBatch = firestore.batch()
                slotsSnapshot.documents.forEach { doc ->
                    slotsBatch.delete(doc.reference)
                }
                slotsBatch.commit().await()
                println("✅ DEBUG: ${slotsSnapshot.size()} slots deleted")
            }
            
            // 7. Giữ lại bookings (lịch sử đặt sân) để tham khảo
            println("ℹ️ DEBUG: Bookings are preserved for historical records")
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: FieldRepository.deleteField failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật toàn bộ bảng giá và dịch vụ của một sân
     * Xóa dữ liệu cũ và thêm dữ liệu mới
     */
    suspend fun updateFieldPricingAndServices(
        fieldId: String,
        pricingRules: List<PricingRule>,
        fieldServices: List<FieldService>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // 1. Xóa tất cả pricing rules cũ của sân này
            val oldRulesSnapshot = firestore.collection(PRICING_RULES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            oldRulesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // 2. Xóa tất cả field services cũ của sân này
            val oldServicesSnapshot = firestore.collection(FIELD_SERVICES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            oldServicesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // 3. Thêm pricing rules mới
            pricingRules.forEach { rule ->
                val ruleDoc = firestore.collection(PRICING_RULES_COLLECTION).document()
                val ruleWithId = rule.copy(ruleId = ruleDoc.id)
                batch.set(ruleDoc, ruleWithId)
            }
            
            // 4. Thêm field services mới
            fieldServices.forEach { service ->
                val serviceDoc = firestore.collection(FIELD_SERVICES_COLLECTION).document()
                val serviceWithId = service.copy(fieldServiceId = serviceDoc.id)
                batch.set(serviceDoc, serviceWithId)
            }
            
            // 5. Commit tất cả thay đổi
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật chỉ dịch vụ của một sân
     * Xóa dịch vụ cũ và thêm dịch vụ mới
     */
    suspend fun updateFieldServices(
        fieldId: String,
        fieldServices: List<FieldService>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // 1. Xóa tất cả field services cũ của sân này
            val oldServicesSnapshot = firestore.collection(FIELD_SERVICES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            oldServicesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // 2. Thêm field services mới
            fieldServices.forEach { service ->
                val serviceDoc = firestore.collection(FIELD_SERVICES_COLLECTION).document()
                val serviceWithId = service.copy(fieldServiceId = serviceDoc.id)
                batch.set(serviceDoc, serviceWithId)
            }
            
            // 3. Commit tất cả thay đổi
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
