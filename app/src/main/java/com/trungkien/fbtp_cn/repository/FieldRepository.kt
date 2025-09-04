package com.trungkien.fbtp_cn.repository

import android.net.Uri
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.*
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
     * Xóa sân
     */
    suspend fun deleteField(fieldId: String): Result<Unit> {
        return try {
            // Xóa field
            firestore.collection(FIELDS_COLLECTION)
                .document(fieldId)
                .delete()
                .await()
            
            // Xóa pricing rules
            val rulesSnapshot = firestore.collection(PRICING_RULES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            val batch = firestore.batch()
            rulesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            // Xóa field services
            val servicesSnapshot = firestore.collection(FIELD_SERVICES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .get()
                .await()
            
            val servicesBatch = firestore.batch()
            servicesSnapshot.documents.forEach { doc ->
                servicesBatch.delete(doc.reference)
            }
            servicesBatch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
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
