package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.ServiceLine
import kotlinx.coroutines.tasks.await
import java.util.*

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val BOOKINGS_COLLECTION = "bookings"
    }
    
    /**
     * Tạo booking mới với logic đối thủ
     */
    suspend fun createBooking(
        renterId: String,
        ownerId: String,
        fieldId: String,
        date: String,
        consecutiveSlots: List<String>,
        bookingType: String, // "SOLO" hoặc "DUO"
        hasOpponent: Boolean = false,
        opponentId: String? = null,
        opponentName: String? = null,
        opponentAvatar: String? = null,
        basePrice: Long,
        serviceLines: List<ServiceLine> = emptyList(),
        notes: String? = null
    ): Result<String> {
        return try {
            val bookingId = UUID.randomUUID().toString()
            
            // Tính toán thời gian bắt đầu và kết thúc
            val startAt = consecutiveSlots.first()
            val endAt = consecutiveSlots.last()
            val slotsCount = consecutiveSlots.size
            val minutes = slotsCount * 30 // Mỗi slot 30 phút
            
            // Tính tổng giá
            val servicePrice = serviceLines.sumOf { it.lineTotal }
            val totalPrice = basePrice + servicePrice
            
            val booking = Booking(
                bookingId = bookingId,
                renterId = renterId,
                ownerId = ownerId,
                fieldId = fieldId,
                date = date,
                startAt = startAt,
                endAt = endAt,
                slotsCount = slotsCount,
                minutes = minutes,
                basePrice = basePrice,
                serviceLines = serviceLines,
                servicePrice = servicePrice,
                totalPrice = totalPrice,
                status = "PENDING",
                notes = notes,
                // ✅ NEW: Thông tin đối thủ
                hasOpponent = hasOpponent,
                opponentId = opponentId,
                opponentName = opponentName,
                opponentAvatar = opponentAvatar,
                bookingType = bookingType,
                consecutiveSlots = consecutiveSlots
            )
            
            // Lưu vào Firebase
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .set(booking)
                .await()
            // Cập nhật trạng thái các slots tương ứng (đánh dấu isBooked)
            try {
                val batch = firestore.batch()
                consecutiveSlots.forEach { s ->
                    val q = firestore.collection("slots")
                        .whereEqualTo("fieldId", fieldId)
                        .whereEqualTo("date", date)
                        .whereEqualTo("startAt", s)
                        .get().await()
                    q.documents.forEach { doc ->
                        batch.update(doc.reference, mapOf(
                            "isBooked" to true,
                            "bookingId" to bookingId
                        ))
                    }
                }
                batch.commit().await()
            } catch (_: Exception) { }
            
            println("✅ DEBUG: Booking created successfully: $bookingId")
            println("  - Type: $bookingType")
            println("  - Has opponent: $hasOpponent")
            println("  - Consecutive slots: $consecutiveSlots")
            
            Result.success(bookingId)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to create booking: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bookings theo renterId (mới nhất trước)
     */
    suspend fun getBookingsByRenter(renterId: String): Result<List<Booking>> {
        return try {
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("renterId", renterId)
                .get()
                .await()
            val list = snapshot.toObjects(Booking::class.java)
                .sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Lắng nghe thay đổi bookings theo renterId (realtime)
     */
    fun listenBookingsByRenter(
        renterId: String,
        onChange: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("renterId", renterId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Booking::class.java)
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                onChange(list)
            }
    }
    
    /**
     * Lấy bookings theo fieldId và date
     */
    suspend fun getBookingsByFieldAndDate(fieldId: String, date: String): Result<List<Booking>> {
        return try {
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("✅ DEBUG: Found ${bookings.size} bookings for field $fieldId on $date")
            
            Result.success(bookings)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get bookings: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Lấy danh sách startAt đã được đặt (để khóa màu trong grid)
     */
    suspend fun getBookedStartTimes(fieldId: String, date: String): Result<Set<String>> {
        return try {
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()
            val times = snapshot.toObjects(Booking::class.java)
                .flatMap { it.consecutiveSlots }
                .toSet()
            Result.success(times)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bookings đang tìm đối thủ (SOLO)
     */
    suspend fun getWaitingOpponentBookings(fieldId: String, date: String): Result<List<Booking>> {
        return try {
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "SOLO")
                .whereEqualTo("hasOpponent", false)
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("✅ DEBUG: Found ${bookings.size} waiting opponent bookings")
            
            Result.success(bookings)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get waiting opponent bookings: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bookings đã có đối thủ (DUO)
     */
    suspend fun getLockedBookings(fieldId: String, date: String): Result<List<Booking>> {
        return try {
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "DUO")
                .whereEqualTo("hasOpponent", true)
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("✅ DEBUG: Found ${bookings.size} locked bookings")
            
            Result.success(bookings)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get locked bookings: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Cập nhật booking khi có đối thủ tham gia
     */
    suspend fun joinOpponent(
        bookingId: String,
        opponentId: String,
        opponentName: String,
        opponentAvatar: String?
    ): Result<Unit> {
        return try {
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(
                    mapOf(
                        "hasOpponent" to true,
                        "opponentId" to opponentId,
                        "opponentName" to opponentName,
                        "opponentAvatar" to opponentAvatar,
                        "bookingType" to "DUO",
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            println("✅ DEBUG: Opponent joined booking: $bookingId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to join opponent: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Hủy booking
     */
    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(
                    mapOf(
                        "status" to "CANCELLED",
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            println("✅ DEBUG: Booking cancelled: $bookingId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to cancel booking: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy booking theo ID
     */
    suspend fun getBookingById(bookingId: String): Result<Booking?> {
        return try {
            val document = firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .await()
            
            if (document.exists()) {
                val booking = document.toObject(Booking::class.java)
                Result.success(booking)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get booking: ${e.message}")
            Result.failure(e)
        }
    }
}
