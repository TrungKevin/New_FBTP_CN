package com.trungkien.fbtp_cn.ui.components.owner.profile

import android.net.Uri
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Service để upload ảnh lên Firebase Storage
 * Tập trung xử lý upload avatar và các loại ảnh khác
 */
class ImageUploadService {
    
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    
    /**
     * Convert ảnh từ URI thành base64 string
     * @param context Context để đọc file
     * @param imageUri URI của ảnh
     * @return Base64 string của ảnh
     */
    private fun convertImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            println("🔄 DEBUG: Converting image to base64...")
            
            // Đọc ảnh từ URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            if (bitmap != null) {
                // Resize ảnh để giảm dung lượng
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
                
                // Convert thành byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                
                // Convert thành base64
                val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                
                println("✅ DEBUG: Base64 conversion successful, size: ${base64String.length} chars")
                println("🔄 DEBUG: First 100 chars: ${base64String.take(100)}")
                
                // Kiểm tra kích thước base64 (Firestore limit ~1MB)
                if (base64String.length > 1000000) {
                    println("⚠️ WARNING: Base64 string too large (${base64String.length} chars), compressing more...")
                    
                    // Compress thêm với quality thấp hơn
                    val compressedOutputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, compressedOutputStream)
                    val compressedByteArray = compressedOutputStream.toByteArray()
                    val compressedBase64 = android.util.Base64.encodeToString(compressedByteArray, android.util.Base64.DEFAULT)
                    
                    println("✅ DEBUG: Compressed base64 size: ${compressedBase64.length} chars")
                    return compressedBase64
                }
                
                base64String
            } else {
                println("❌ ERROR: Failed to decode bitmap")
                null
            }
        } catch (e: Exception) {
            println("❌ ERROR: Base64 conversion failed: ${e.message}")
            null
        }
    }
    
    /**
     * Upload avatar của user lên Firebase Storage dưới dạng base64
     * @param context Context để convert ảnh
     * @param imageUri URI của ảnh từ ImagePicker
     * @param userId ID của user
     * @return Base64 string của ảnh đã upload thành công
     */
    suspend fun uploadAvatar(context: Context, imageUri: Uri, userId: String): Result<String> {
        return try {
            println("🔄 DEBUG: Starting avatar upload for user: $userId")
            
            // Convert ảnh thành base64
            val base64String = convertImageToBase64(context, imageUri)
            
            if (base64String != null) {
                println("✅ DEBUG: Avatar converted to base64 successfully")
                println("🔄 DEBUG: Base64 length: ${base64String.length} characters")
                
                // Trả về base64 string thay vì URL
                Result.success(base64String)
            } else {
                println("❌ ERROR: Failed to convert image to base64")
                Result.failure(Exception("Failed to convert image to base64"))
            }
            
        } catch (e: Exception) {
            println("❌ ERROR: Avatar upload failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Upload ảnh field lên Firebase Storage
     * @param imageUri URI của ảnh từ ImagePicker
     * @param fieldId ID của field
     * @return URL của ảnh đã upload thành công
     */
    suspend fun uploadFieldImage(imageUri: Uri, fieldId: String): Result<String> {
        return try {
            println("🔄 DEBUG: Starting field image upload for field: $fieldId")
            
            // Tạo unique filename
            val fileName = "field_${fieldId}_${UUID.randomUUID()}.jpg"
            val fieldImageRef: StorageReference = storageRef.child("field_images/$fileName")
            
            println("🔄 DEBUG: Uploading to path: field_images/$fileName")
            
            // Upload file
            val uploadTask = fieldImageRef.putFile(imageUri)
            val downloadUrl = uploadTask.await().storage.downloadUrl.await()
            
            println("✅ DEBUG: Field image upload successful: $downloadUrl")
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            println("❌ ERROR: Field image upload failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Xóa ảnh từ Firebase Storage
     * @param imageUrl URL của ảnh cần xóa
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            println("🔄 DEBUG: Deleting image: $imageUrl")
            
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            
            println("✅ DEBUG: Image deleted successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            println("❌ ERROR: Image deletion failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Upload multiple ảnh field cùng lúc
     * @param imageUris List URI của các ảnh
     * @param fieldId ID của field
     * @return List URL của các ảnh đã upload thành công
     */
    suspend fun uploadMultipleFieldImages(imageUris: List<Uri>, fieldId: String): Result<List<String>> {
        return try {
            println("🔄 DEBUG: Starting multiple field images upload for field: $fieldId")
            
            val uploadTasks = imageUris.mapIndexed { index, uri ->
                val fileName = "field_${fieldId}_${index}_${UUID.randomUUID()}.jpg"
                val fieldImageRef: StorageReference = storageRef.child("field_images/$fileName")
                
                println("🔄 DEBUG: Uploading image $index to path: field_images/$fileName")
                
                fieldImageRef.putFile(uri).await().storage.downloadUrl.await().toString()
            }
            
            println("✅ DEBUG: Multiple field images upload successful: ${uploadTasks.size} images")
            Result.success(uploadTasks)
            
        } catch (e: Exception) {
            println("❌ ERROR: Multiple field images upload failed: ${e.message}")
            Result.failure(e)
        }
    }
}
