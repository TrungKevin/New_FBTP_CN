package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.ServiceLine
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.model.MatchParticipant
import kotlinx.coroutines.tasks.await
import java.util.*

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val BOOKINGS_COLLECTION = "bookings"
        private const val MATCHES_COLLECTION = "matches"
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
     * ✅ NEW: Lắng nghe Match theo fieldId + date để render slot vàng/đỏ realtime
     */
    fun listenMatchesByFieldDate(
        fieldId: String,
        date: String,
        onChange: (List<Match>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        println("🔍 DEBUG: listenMatchesByFieldDate called:")
        println("  - fieldId: $fieldId")
        println("  - date: $date")
        println("  - MATCHES_COLLECTION: $MATCHES_COLLECTION")
        
        return firestore.collection(MATCHES_COLLECTION)
            .whereEqualTo("fieldId", fieldId)
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { 
                    println("❌ ERROR: listenMatchesByFieldDate error: ${e.message}")
                    onError(e); 
                    return@addSnapshotListener 
                }
                val list = snapshot?.toObjects(Match::class.java) ?: emptyList()
                println("✅ DEBUG: listenMatchesByFieldDate result:")
                println("  - snapshot size: ${snapshot?.size() ?: 0}")
                println("  - matches found: ${list.size}")
                list.forEachIndexed { index, match ->
                    println("  [$index] matchId: ${match.rangeKey}, status: ${match.status}, participants: ${match.participants.size}")
                }
                onChange(list)
            }
    }

    /**
     * ✅ NEW: Cập nhật trạng thái của Match (OWNER xác nhận hoặc hủy)
     */
    suspend fun updateMatchStatus(matchId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection(MATCHES_COLLECTION)
                .document(matchId)
                .update(mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            println("✅ DEBUG: Match status updated: $matchId -> $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to update match status: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Đảm bảo có document Match cho booking SOLO đang chờ đối thủ
     * Nếu chưa tồn tại, tạo mới với participant A và trạng thái WAITING_OPPONENT
     */
    suspend fun createMatchIfMissing(match: Match): Result<Unit> {
        return try {
            println("🔍 DEBUG: createMatchIfMissing called with rangeKey: ${match.rangeKey}")
            val ref = firestore.collection(MATCHES_COLLECTION).document(match.rangeKey)
            val snap = ref.get().await()
            println("🔍 DEBUG: Match document exists: ${snap.exists()}")
            if (!snap.exists()) {
                val upsert = match.copy(
                    status = match.status.ifBlank { "WAITING_OPPONENT" },
                    occupiedCount = if (match.participants.size >= 1) 1 else 0
                )
                ref.set(upsert).await()
                println("✅ DEBUG: Match document created successfully")
            } else {
                println("✅ DEBUG: Match document already exists")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: createMatchIfMissing failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Tạo booking SOLO chờ đối thủ + tạo Match WAITING_OPPONENT
     */
    suspend fun createWaitingOpponentBooking(
        renterId: String,
        ownerId: String,
        fieldId: String,
        date: String,
        consecutiveSlots: List<String>,
        basePrice: Long,
        serviceLines: List<ServiceLine> = emptyList(),
        notes: String? = null
    ): Result<String> {
        return try {
            println("🔍 DEBUG: createWaitingOpponentBooking called:")
            println("  - renterId: $renterId")
            println("  - ownerId: $ownerId")
            println("  - fieldId: $fieldId")
            println("  - date: $date")
            println("  - consecutiveSlots: $consecutiveSlots")
            println("  - basePrice: $basePrice")
            
            val bookingId = UUID.randomUUID().toString()
            val startAt = consecutiveSlots.first()
            val endAt = consecutiveSlots.last()
            val slotsCount = consecutiveSlots.size
            val minutes = slotsCount * 30
            val servicePrice = serviceLines.sumOf { it.lineTotal }
            val totalPrice = basePrice + servicePrice
            val rangeKey = "$fieldId${date.replace("-", "")}${startAt.replace(":", "")}${endAt.replace(":", "")}"

            println("🔍 DEBUG: Generated data:")
            println("  - bookingId: $bookingId")
            println("  - startAt: $startAt")
            println("  - endAt: $endAt")
            println("  - rangeKey: $rangeKey")
            println("  - totalPrice: $totalPrice")

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
                hasOpponent = false,
                bookingType = "SOLO",
                opponentMode = "WAITING_OPPONENT",
                consecutiveSlots = consecutiveSlots,
                matchId = rangeKey,
                matchSide = "A"
            )

            val match = Match(
                rangeKey = rangeKey,
                fieldId = fieldId,
                date = date,
                startAt = startAt,
                endAt = endAt,
                capacity = 2,
                occupiedCount = 1,
                participants = listOf(MatchParticipant(bookingId = bookingId, renterId = renterId, side = "A")),
                price = basePrice,
                totalPrice = totalPrice,
                status = "WAITING_OPPONENT",
                matchType = "SINGLE",
                notes = notes
            )

            println("🔍 DEBUG: Created objects:")
            println("  - booking: $booking")
            println("  - match: $match")

            val batch = firestore.batch()
            val bookingDoc = firestore.collection(BOOKINGS_COLLECTION).document(bookingId)
            val matchDoc = firestore.collection(MATCHES_COLLECTION).document(rangeKey)
            batch.set(bookingDoc, booking)
            batch.set(matchDoc, match)
            
            println("🔍 DEBUG: About to commit batch...")
            batch.commit().await()
            println("✅ DEBUG: createWaitingOpponentBooking completed successfully")
            println("  - bookingId: $bookingId")
            println("  - matchId: $rangeKey")
            
            Result.success(bookingId)
        } catch (e: Exception) {
            println("❌ ERROR: createWaitingOpponentBooking failed: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Renter thứ 2 tham gia làm đối thủ -> tạo booking B, cập nhật match FULL và booking A
     */
    suspend fun joinOpponent(
        matchId: String,
        renterId: String,
        ownerId: String,
        basePrice: Long,
        serviceLines: List<ServiceLine> = emptyList(),
        notes: String? = null
    ): Result<String> {
        return try {
            println("🔍 DEBUG: joinOpponent called with matchId: $matchId")
            val matchRef = firestore.collection(MATCHES_COLLECTION).document(matchId)
            val matchSnap = matchRef.get().await()
            println("🔍 DEBUG: Match document exists: ${matchSnap.exists()}")
            val match = matchSnap.toObject(Match::class.java) ?: return Result.failure(IllegalStateException("Match not found"))
            println("🔍 DEBUG: Match status: ${match.status}")
            if (match.status == "FULL") return Result.failure(IllegalStateException("Match already full"))

            val bookingId = UUID.randomUUID().toString()
            val servicePrice = serviceLines.sumOf { it.lineTotal }
            val totalPrice = basePrice + servicePrice
            val bookingB = Booking(
                bookingId = bookingId,
                renterId = renterId,
                ownerId = ownerId,
                fieldId = match.fieldId,
                date = match.date,
                startAt = match.startAt,
                endAt = match.endAt,
                slotsCount = ((match.endAt.substring(0,2)+match.endAt.substring(3,5)).toInt() - (match.startAt.substring(0,2)+match.startAt.substring(3,5)).toInt())/50, // placeholder, không dùng
                minutes = 60,
                basePrice = basePrice,
                serviceLines = serviceLines,
                servicePrice = servicePrice,
                totalPrice = totalPrice,
                status = "PENDING",
                notes = notes,
                hasOpponent = true,
                bookingType = "DUO",
                consecutiveSlots = emptyList(),
                matchId = matchId,
                matchSide = "B"
            )

            val batch = firestore.batch()
            val bookingBDoc = firestore.collection(BOOKINGS_COLLECTION).document(bookingId)
            batch.set(bookingBDoc, bookingB)

            // update match FULL
            val updatedParticipants = match.participants + MatchParticipant(bookingId = bookingId, renterId = renterId, side = "B")
            batch.update(matchRef, mapOf(
                "occupiedCount" to 2,
                "status" to "FULL",
                "participants" to updatedParticipants
            ))

            batch.commit().await()
            println("✅ DEBUG: joinOpponent completed successfully, bookingId: $bookingId")
            Result.success(bookingId)
        } catch (e: Exception) {
            println("❌ ERROR: joinOpponent failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Tìm booking SOLO đang chờ đối thủ theo slot
     */
    suspend fun findWaitingBookingBySlot(
        fieldId: String,
        date: String,
        slot: String
    ): Result<Booking?> {
        return try {
            println("🔍 DEBUG: findWaitingBookingBySlot query:")
            println("  - fieldId: $fieldId")
            println("  - date: $date")
            println("  - slot: $slot")
            
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "SOLO")
                .whereEqualTo("hasOpponent", false)
                .whereArrayContains("consecutiveSlots", slot)
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("🔍 DEBUG: Found ${bookings.size} bookings matching criteria")
            bookings.forEachIndexed { index, booking ->
                println("  [$index] bookingId: ${booking.bookingId}, slots: ${booking.consecutiveSlots}")
            }
            
            val booking = bookings.firstOrNull()
            Result.success(booking)
        } catch (e: Exception) {
            println("❌ ERROR: findWaitingBookingBySlot failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Lắng nghe thay đổi bookings theo ownerId (realtime)
     */
    fun listenBookingsByOwner(
        ownerId: String,
        onChange: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
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

            // Chỉ coi là "đỏ" (đã đặt hoàn toàn) khi không phải SOLO đang chờ đối thủ
            // - DUO hoặc hasOpponent = true
            // - Hoặc các trạng thái đã xác nhận/thanh toán
            val times = snapshot.toObjects(Booking::class.java)
                .asSequence()
                .filter { booking ->
                    val isWaitingSolo = (booking.bookingType == "SOLO" && booking.hasOpponent == false)
                    val isConfirmed = booking.status.equals("CONFIRMED", ignoreCase = true) ||
                            booking.status.equals("PAID", ignoreCase = true)
                    val isDuoOrHasOpponent = (booking.bookingType == "DUO" || booking.hasOpponent == true)
                    // Đỏ khi không phải SOLO chờ, hoặc đã confirmed/paid
                    (!isWaitingSolo) || isConfirmed || isDuoOrHasOpponent
                }
                .flatMap { it.consecutiveSlots }
                .toSet()

            Result.success(times)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Cập nhật trạng thái booking
     */
    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy bookings đang tìm đối thủ (SOLO)
     */
    suspend fun getWaitingOpponentBookings(fieldId: String, date: String): Result<List<Booking>> {
        return try {
            println("🔍 DEBUG: getWaitingOpponentBookings query:")
            println("  - fieldId: $fieldId")
            println("  - date: $date")
            
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "SOLO")
                .whereEqualTo("hasOpponent", false)
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("✅ DEBUG: Found ${bookings.size} waiting opponent bookings")
            bookings.forEachIndexed { index, booking ->
                println("  [$index] bookingId: ${booking.bookingId}, slots: ${booking.consecutiveSlots}")
            }
            
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
     * Xóa booking khỏi Firestore
     */
    suspend fun deleteBooking(bookingId: String): Result<Unit> {
        return try {
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .delete()
                .await()

            println("✅ DEBUG: Booking deleted: $bookingId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to delete booking: ${e.message}")
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
