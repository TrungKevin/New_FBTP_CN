package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.ServiceLine
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.model.MatchParticipant
import com.trungkien.fbtp_cn.model.MatchResult
import com.trungkien.fbtp_cn.service.NotificationHelper
import com.trungkien.fbtp_cn.service.RenterNotificationHelper
import com.trungkien.fbtp_cn.service.GlobalNotificationHelper
import kotlinx.coroutines.tasks.await
import java.util.*
import com.trungkien.fbtp_cn.model.NotificationData

class BookingRepository(
    private val notificationHelper: NotificationHelper? = null
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()
    private val globalNotificationHelper = GlobalNotificationHelper(notificationRepository)
    private val SLOTS_COLLECTION = "slots"

    /**
     * ✅ Helper: Reset trạng thái các documents `slots` liên quan tới một booking.
     * Đặt isBooked=false và xoá bookingId để UI trở lại trạng thái bình thường.
     * 
     * ✅ FIXED LOGIC: Xử lý chuyển đổi trạng thái màu sắc khi hủy sân:
     * - SOLO (vàng) → FREE (trắng)
     * - FULL (đỏ) → WAITING_OPPONENT (vàng) khi 1 renter hủy
     * - FULL (đỏ) → FREE (trắng) khi owner hủy cả match
     */
    private suspend fun resetSlotsForBooking(booking: Booking) {
        try {
            // ✅ STEP 1: Reset slots collection (giữ nguyên logic cũ)
            val batch = firestore.batch()
            booking.consecutiveSlots.forEach { startAt ->
                val q = firestore.collection(SLOTS_COLLECTION)
                    .whereEqualTo("fieldId", booking.fieldId)
                    .whereEqualTo("date", booking.date)
                    .whereEqualTo("startAt", startAt)
                    .limit(1)
                    .get()
                    .await()
                val doc = q.documents.firstOrNull()
                if (doc != null) {
                    batch.update(doc.reference, mapOf(
                        "isBooked" to false,
                        "bookingId" to null
                    ))
                }
            }
            batch.commit().await()
            println("🔄 DEBUG: Slots reset for booking ${booking.bookingId} -> ${booking.consecutiveSlots}")
            
            // ✅ STEP 2: Xử lý chuyển đổi trạng thái màu sắc trong matches collection
            val matchId = booking.matchId
            if (!matchId.isNullOrBlank()) {
                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                    .document(matchId)
                    .get()
                    .await()
                
                if (matchDoc.exists()) {
                    val match = matchDoc.toObject(Match::class.java)
                    if (match != null) {
                        when {
                            // Trường hợp 1: Renter A hủy solo booking (WAITING_OPPONENT) → chuyển về trắng
                            booking.bookingType == "SOLO" && !booking.hasOpponent -> {
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "FREE",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 CANCELLATION: SOLO booking cancelled - Reset to WHITE (FREE)")
                            }
                            
                            // Trường hợp 2: Renter A hoặc B hủy trong match FULL → chuyển về vàng
                            match.status == "FULL" && match.participants.size == 2 -> {
                                val remainingParticipants = match.participants.filter { it.bookingId != booking.bookingId }
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "WAITING_OPPONENT",
                                            "occupiedCount" to 1,
                                            "participants" to remainingParticipants,
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 CANCELLATION: FULL match cancelled by one renter - Reset to YELLOW (WAITING_OPPONENT)")
                            }
                            
                            // Trường hợp 3: Owner hủy cả match (cả A và B) → chuyển về trắng
                            else -> {
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "FREE",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 CANCELLATION: Owner cancelled entire match - Reset to WHITE (FREE)")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ ERROR: resetSlotsForBooking failed: ${e.message}")
        }
    }
    
    companion object {
        private const val BOOKINGS_COLLECTION = "bookings"
        private const val MATCHES_COLLECTION = "matches"
        private const val MATCH_RESULTS_COLLECTION = "match_results"
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
        notes: String? = null,
        matchSide: String? = null, // ✅ FIX: Add matchSide parameter
        createdWithOpponent: Boolean = false // ✅ CRITICAL FIX: immutable origin flag
    ): Result<String> {
        return try {
            println("🔍 DEBUG: createBooking called:")
            println("  - renterId: $renterId")
            println("  - ownerId: $ownerId")
            println("  - fieldId: $fieldId")
            println("  - date: $date")
            println("  - consecutiveSlots: $consecutiveSlots")
            println("  - bookingType: $bookingType")
            println("  - hasOpponent: $hasOpponent")
            println("  - createdWithOpponent: $createdWithOpponent")
            
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
                consecutiveSlots = consecutiveSlots,
                matchSide = matchSide, // ✅ FIX: Add matchSide
                createdWithOpponent = createdWithOpponent // ✅ CRITICAL FIX: immutable origin flag
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
            
            // ✅ Gửi thông báo cho owner khi có đặt sân mới (Client-side Approach A)
            try {
                val result = notificationRepository.createNotification(
                    toUserId = ownerId,
                    type = "BOOKING_CREATED",
                    title = "Đặt sân mới!",
                    body = "Có đặt sân lúc $startAt ngày $date",
                    data = NotificationData(
                        bookingId = bookingId,
                        fieldId = fieldId,
                        userId = renterId,
                        customData = emptyMap()
                    ),
                    priority = "HIGH"
                )
                if (result.isSuccess) {
                    println("🔔 DEBUG: Notification CREATED -> ownerId=$ownerId, bookingId=$bookingId")
                } else {
                    println("❌ ERROR: Notification CREATE FAILED -> ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("❌ ERROR: Notification CREATE EXCEPTION -> ${e.message}")
            }
            
            // ✅ Gửi thông báo thành công cho renter (giữ nguyên helper nếu có)
            notificationHelper?.notifyBookingSuccess(
                renterId = renterId,
                fieldName = "Sân",
                date = date,
                time = startAt,
                bookingId = bookingId,
                fieldId = fieldId
            )
            
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
        println("🔍 DEBUG: listenBookingsByRenter called for renterId: $renterId")
        return firestore.collection(BOOKINGS_COLLECTION)
            .whereEqualTo("renterId", renterId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ ERROR: listenBookingsByRenter error: ${e.message}")
                    onError(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Booking::class.java)
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                println("🔍 DEBUG: listenBookingsByRenter result:")
                println("  - snapshot size: ${snapshot?.size() ?: 0}")
                println("  - bookings found: ${list.size}")
                list.forEachIndexed { index, booking ->
                    println("  [$index] bookingId: ${booking.bookingId}, type: ${booking.bookingType}, status: ${booking.status}, date: ${booking.date}")
                }
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
     * ✅ NEW: Cập nhật notes của Match
     */
    suspend fun updateMatchNotes(matchId: String, noteA: String?, noteB: String?): Result<Unit> {
        return try {
            val updateData = mutableMapOf<String, Any>()
            noteA?.let { updateData["noteA"] = it }
            noteB?.let { updateData["noteB"] = it }
            
            if (updateData.isNotEmpty()) {
                firestore.collection(MATCHES_COLLECTION)
                    .document(matchId)
                    .update(updateData)
                    .await()
                println("✅ DEBUG: Match notes updated: $matchId")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to update match notes: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * ✅ NEW: Cập nhật trạng thái của Match (OWNER xác nhận hoặc hủy)
     */
    suspend fun updateMatchStatus(matchId: String, newStatus: String): Result<Unit> {
        return try {
            // Lấy thông tin match trước khi cập nhật
            val matchDoc = firestore.collection(MATCHES_COLLECTION)
                .document(matchId)
                .get()
                .await()
            
            if (!matchDoc.exists()) {
                return Result.failure(Exception("Match not found"))
            }
            
            val match = matchDoc.toObject(Match::class.java)
            if (match == null) {
                return Result.failure(Exception("Failed to parse match"))
            }
            
            // Cập nhật theo trạng thái mới
            if (newStatus == "CANCELLED") {
                // ✅ FIX: Giữ match với status CANCELLED để hiển thị ở tab "Trận đấu" > "Đã hủy"
                // Hủy tất cả bookings liên quan nhưng giữ match để tracking
                try {
                    val participantBookingIds = match.participants.mapNotNull { it.bookingId }
                    participantBookingIds.forEach { bId ->
                        try {
                            firestore.collection(BOOKINGS_COLLECTION)
                                .document(bId)
                                .update(
                                    mapOf(
                                        "status" to "CANCELLED",
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                )
                                .await()
                            println("🔄 DEBUG: Booking $bId set to CANCELLED due to match cancel")

                            // ✅ NEW: Đặt lại các khe giờ về trạng thái trống cho booking này
                            try {
                                val bSnap = firestore.collection(BOOKINGS_COLLECTION)
                                    .document(bId)
                                    .get()
                                    .await()
                                val booking = bSnap.toObject(com.trungkien.fbtp_cn.model.Booking::class.java)
                                if (booking != null) {
                                    resetSlotsForBooking(booking)
                                }
                            } catch (e: Exception) {
                                println("❌ ERROR: Failed to reset slots for booking $bId: ${e.message}")
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Failed to cancel booking $bId on match cancel: ${e.message}")
                        }
                    }

                    // ✅ FIX: Giữ match với status CANCELLED thay vì reset về FREE
                    firestore.collection(MATCHES_COLLECTION)
                        .document(matchId)
                        .update(
                            mapOf(
                                "status" to "CANCELLED",
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    println("🔄 DEBUG: Match $matchId set to CANCELLED (kept for tracking)")
                } catch (e: Exception) {
                    println("❌ ERROR: Failed to reset match/bookings on cancel: ${e.message}")
                }
            } else {
                firestore.collection(MATCHES_COLLECTION)
                    .document(matchId)
                    .update(
                        mapOf(
                            "status" to newStatus,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }
            
            // Gửi notification cho renter khi match được xác nhận
            if (newStatus == "CONFIRMED" && match.status != "CONFIRMED") {
                try {
                    // Lấy thông tin field để có tên sân
                    val fieldDoc = firestore.collection("fields")
                        .document(match.fieldId)
                        .get()
                        .await()
                    
                    val fieldName = fieldDoc.getString("name") ?: "Sân"
                    
                    // Gửi notification cho tất cả participants
                    val notificationRepository = NotificationRepository()
                    val renterNotificationHelper = RenterNotificationHelper(notificationRepository)
                    
                    match.participants.forEach { participant ->
                        renterNotificationHelper.notifyBookingConfirmed(
                            renterId = participant.renterId,
                            fieldName = fieldName,
                            date = match.date,
                            time = match.startAt,
                            bookingId = matchId,
                            fieldId = match.fieldId
                        )
                    }
                    
                    println("🔔 DEBUG: Sent match confirmed notification to participants: ${match.participants.map { it.renterId }}")
                } catch (e: Exception) {
                    println("❌ ERROR: Failed to send match confirmed notification: ${e.message}")
                }
            }
            
            // Gửi thông báo cho renter khi match bị hủy bởi owner
            if (newStatus == "CANCELLED") {
                try {
                    val fieldDoc = firestore.collection("fields")
                        .document(match.fieldId)
                        .get()
                        .await()
                    val fieldName = fieldDoc.getString("name") ?: "Sân"

                    val notificationRepository = NotificationRepository()
                    match.participants.forEach { participant ->
                        try {
                            val res = notificationRepository.createNotification(
                                toUserId = participant.renterId,
                                type = "BOOKING_CANCELLED_BY_OWNER",
                                title = "Trận đấu đã bị chủ sân hủy",
                                body = "Sân $fieldName - ${match.startAt} ngày ${match.date} đã bị hủy.",
                                data = NotificationData(
                                    bookingId = participant.bookingId ?: "",
                                    fieldId = match.fieldId,
                                    userId = null,
                                    customData = emptyMap()
                                ),
                                priority = "HIGH"
                            )
                            if (res.isSuccess) {
                                println("🔔 DEBUG: Notified renter about match cancel -> ${participant.renterId}")
                            } else {
                                println("❌ ERROR: Notify renter match cancel failed -> ${res.exceptionOrNull()?.message}")
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Create notification match cancel failed -> ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("❌ ERROR: Failed to send cancel notifications: ${e.message}")
                }
            }

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
                notes = notes, // Notes chung của trận đấu
                noteA = notes   // Notes riêng của renter A (người đặt đầu tiên)
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
            
            // ✅ NEW: Gửi notifications sau khi tạo booking thành công
            try {
                // Lấy thông tin renter và field để gửi notification
                val renterDoc = firestore.collection("users").document(renterId).get().await()
                val fieldDoc = firestore.collection("fields").document(fieldId).get().await()
                
                val renterName = renterDoc.getString("name") ?: "Người chơi"
                val fieldName = fieldDoc.getString("name") ?: "Sân"
                val ownerId = fieldDoc.getString("ownerId") ?: ""
                
                println("🔔 DEBUG: Sending notifications for waiting opponent booking:")
                println("  - renterName: $renterName")
                println("  - fieldName: $fieldName")
                println("  - ownerId: $ownerId")
                
                // 1. Gửi thông báo cho Owner
                if (ownerId.isNotBlank()) {
                    globalNotificationHelper.notifyOwnerWaitingOpponent(
                        ownerId = ownerId,
                        renterName = renterName,
                        fieldName = fieldName,
                        date = date,
                        time = startAt,
                        bookingId = bookingId,
                        fieldId = fieldId
                    )
                }
                
                // 2. Gửi thông báo cho tất cả Renter (trừ renter đã đặt)
                globalNotificationHelper.notifyAllRentersOpponentAvailable(
                    waitingRenterName = renterName,
                    fieldName = fieldName,
                    date = date,
                    time = startAt,
                    bookingId = bookingId,
                    fieldId = fieldId,
                    excludeRenterId = renterId
                )
                
                println("🔔 DEBUG: All notifications sent successfully")
            } catch (e: Exception) {
                println("❌ ERROR: Failed to send notifications: ${e.message}")
                e.printStackTrace()
            }
            
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

            // update match FULL và lưu noteB
            val updatedParticipants = match.participants + MatchParticipant(bookingId = bookingId, renterId = renterId, side = "B")
            val updateData = mutableMapOf<String, Any>(
                "occupiedCount" to 2,
                "status" to "FULL",
                "participants" to updatedParticipants
            )
            // Lưu notes của renter B vào noteB
            notes?.let { updateData["noteB"] = it }
            
            batch.update(matchRef, updateData)

            batch.commit().await()
            println("✅ DEBUG: joinOpponent completed successfully, bookingId: $bookingId")
            
            // ✅ Thông báo cho owner là trận đã đủ người (Client-side Approach A)
            try {
                val result = notificationRepository.createNotification(
                    toUserId = ownerId,
                    type = "OPPONENT_JOINED",
                    title = "Có đối thủ tham gia!",
                    body = "Trận đấu lúc ${match.startAt} ngày ${match.date} đã đủ người",
                    data = NotificationData(
                        matchId = matchId,
                        fieldId = match.fieldId,
                        userId = renterId,
                        customData = emptyMap()
                    ),
                    priority = "HIGH"
                )
                if (result.isSuccess) {
                    println("🔔 DEBUG: Notification OPPONENT_JOINED CREATED -> ownerId=$ownerId, matchId=$matchId")
                } else {
                    println("❌ ERROR: Notification OPPONENT_JOINED CREATE FAILED -> ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("❌ ERROR: Notification OPPONENT_JOINED EXCEPTION -> ${e.message}")
            }
            
            // ✅ Gửi thông báo cho Renter A: đã có đối thủ (card riêng OPONENT_MATCHED)
            try {
                // Xác định renter A từ participants của match (tránh nhầm sang renter B)
                val renterAId = match.participants.firstOrNull()?.renterId
                if (!renterAId.isNullOrBlank()) {
                    val renterDoc = firestore.collection("users").document(renterId).get().await()
                    val opponentName = renterDoc.getString("name") ?: "Đối thủ"
                    val fieldSnap = firestore.collection("fields").document(match.fieldId).get().await()
                    val fieldName = fieldSnap.getString("name") ?: "Sân"
                    NotificationHelper(notificationRepository).notifyOpponentJoined(
                        renterAId = renterAId,
                        opponentName = opponentName,
                        fieldName = fieldName,
                        date = match.date,
                        time = match.startAt,
                        matchId = matchId,
                        fieldId = match.fieldId
                    )
                    println("🔔 DEBUG: Notified renter A about opponent joined: $renterAId")
                } else {
                    println("⚠️ WARN: Cannot detect renter A from match participants")
                }
            } catch (e: Exception) {
                println("❌ ERROR: Failed to notify renter A opponent joined: ${e.message}")
            }
            
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
            // Chỉ lấy các booking còn hiệu lực (không CANCELLED)
            val bookingsSnap = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereIn("status", listOf("PENDING", "PAID", "CONFIRMED"))
                .get()
                .await()

            // Lấy danh sách match còn hiệu lực (FULL/CONFIRMED) để khóa màu đúng
            val activeMatchesSnap = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()
            val activeMatchIds = activeMatchesSnap.toObjects(Match::class.java)
                .filter { it.status == "FULL" || it.status == "CONFIRMED" }
                .map { it.rangeKey }
                .toSet()

            val times = bookingsSnap.toObjects(Booking::class.java)
                .asSequence()
                .filter { booking ->
                    // Bỏ SOLO đang chờ đối thủ
                    val isWaitingSolo = booking.bookingType == "SOLO" && booking.hasOpponent == false
                    if (isWaitingSolo) return@filter false

                    // Với DUO/hasOpponent: chỉ khóa khi match còn hiệu lực hoặc booking đã CONFIRMED/PAID
                    val isConfirmedOrPaid = booking.status.equals("CONFIRMED", true) || booking.status.equals("PAID", true)
                    val belongsToActiveMatch = !booking.matchId.isNullOrBlank() && activeMatchIds.contains(booking.matchId)
                    isConfirmedOrPaid || belongsToActiveMatch
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
            // Lấy thông tin booking trước khi cập nhật
            val bookingDoc = firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .get()
                .await()
            
            if (!bookingDoc.exists()) {
                return Result.failure(Exception("Booking not found"))
            }
            
            val booking = bookingDoc.toObject(Booking::class.java)
            if (booking == null) {
                return Result.failure(Exception("Failed to parse booking"))
            }
            
            // Cập nhật status
            firestore.collection(BOOKINGS_COLLECTION)
                .document(bookingId)
                .update(mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            // Gửi notification + đồng bộ match/slot khi booking được xác nhận hoặc bị hủy
            if ((newStatus == "CONFIRMED" && booking.status != "CONFIRMED") || 
                (newStatus == "CANCELLED" && booking.status != "CANCELLED")) {
                try {
                    // Lấy thông tin field để có tên sân
                    val fieldDoc = firestore.collection("fields")
                        .document(booking.fieldId)
                        .get()
                        .await()
                    
                    val fieldName = fieldDoc.getString("name") ?: "Sân"
                    
                    // Gửi notification cho renter
                    val notificationRepository = NotificationRepository()
                    val renterNotificationHelper = RenterNotificationHelper(notificationRepository)
                    
                    if (newStatus == "CONFIRMED") {
                        println("🔔 DEBUG: About to send booking confirmed notification:")
                        println("  - renterId: ${booking.renterId}")
                        println("  - fieldName: $fieldName")
                        println("  - bookingId: ${booking.bookingId}")
                        println("  - fieldId: ${booking.fieldId}")
                        println("  - bookingType: ${booking.bookingType}")
                        println("  - hasOpponent: ${booking.hasOpponent}")
                        println("  - matchId: ${booking.matchId}")
                        
                        // ✅ FIX: Xử lý notification cho cả 2 flow
                        if (booking.bookingType == "SOLO" && !booking.hasOpponent && !booking.matchId.isNullOrBlank()) {
                            // Flow 2: WAITING_OPPONENT - Gửi notification cho cả 2 renter trong match
                            println("🔔 DEBUG: WAITING_OPPONENT flow - sending notifications to both renters")
                            
                            try {
                                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                                    .document(booking.matchId)
                                    .get()
                                    .await()
                                
                                if (matchDoc.exists()) {
                                    val match = matchDoc.toObject(Match::class.java)
                                    if (match != null && match.participants.size >= 2) {
                                        // Gửi notification cho cả 2 participants
                                        match.participants.forEach { participant ->
                                            renterNotificationHelper.notifyBookingConfirmed(
                                                renterId = participant.renterId,
                                                fieldName = fieldName,
                                                date = booking.date,
                                                time = booking.consecutiveSlots.firstOrNull() ?: "",
                                                bookingId = booking.bookingId,
                                                fieldId = booking.fieldId
                                            )
                                            println("🔔 DEBUG: Sent booking confirmed notification to renter: ${participant.renterId}")
                                        }
                                    } else {
                                        // Fallback: chỉ gửi cho renter hiện tại
                                        renterNotificationHelper.notifyBookingConfirmed(
                                            renterId = booking.renterId,
                                            fieldName = fieldName,
                                            date = booking.date,
                                            time = booking.consecutiveSlots.firstOrNull() ?: "",
                                            bookingId = booking.bookingId,
                                            fieldId = booking.fieldId
                                        )
                                        println("🔔 DEBUG: Fallback - sent booking confirmed notification to renter: ${booking.renterId}")
                                    }
                                } else {
                                    // Fallback: chỉ gửi cho renter hiện tại
                                    renterNotificationHelper.notifyBookingConfirmed(
                                        renterId = booking.renterId,
                                        fieldName = fieldName,
                                        date = booking.date,
                                        time = booking.consecutiveSlots.firstOrNull() ?: "",
                                        bookingId = booking.bookingId,
                                        fieldId = booking.fieldId
                                    )
                                    println("🔔 DEBUG: Fallback - sent booking confirmed notification to renter: ${booking.renterId}")
                                }
                            } catch (e: Exception) {
                                println("❌ ERROR: Failed to get match info, sending to single renter: ${e.message}")
                                // Fallback: chỉ gửi cho renter hiện tại
                                renterNotificationHelper.notifyBookingConfirmed(
                                    renterId = booking.renterId,
                                    fieldName = fieldName,
                                    date = booking.date,
                                    time = booking.consecutiveSlots.firstOrNull() ?: "",
                                    bookingId = booking.bookingId,
                                    fieldId = booking.fieldId
                                )
                                println("🔔 DEBUG: Fallback - sent booking confirmed notification to renter: ${booking.renterId}")
                            }
                        } else {
                            // Flow 1: HAS_OPPONENT - Gửi notification cho 1 renter
                            println("🔔 DEBUG: HAS_OPPONENT flow - sending notification to single renter")
                            renterNotificationHelper.notifyBookingConfirmed(
                                renterId = booking.renterId,
                                fieldName = fieldName,
                                date = booking.date,
                                time = booking.consecutiveSlots.firstOrNull() ?: "",
                                bookingId = booking.bookingId,
                                fieldId = booking.fieldId
                            )
                            println("🔔 DEBUG: Sent booking confirmed notification to renter: ${booking.renterId}")
                        }
                    } else if (newStatus == "CANCELLED") {
                        // ✅ FIX: Khôi phục trạng thái match/slot về bình thường cho matched bookings
                        try {
                            val matchId = booking.matchId
                            if (!matchId.isNullOrBlank()) {
                                // ✅ FIX: Reset match về FREE để khe giờ có thể được đặt lại
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "FREE",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 DEBUG: Match reset to FREE due to booking cancel: $matchId")
                                
                                // ✅ FIX: Reset tất cả bookings trong match này về CANCELLED
                                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .get()
                                    .await()
                                
                                if (matchDoc.exists()) {
                                    val match = matchDoc.toObject(Match::class.java)
                                    if (match != null && match.participants.size >= 2) {
                                        // Cancel tất cả bookings trong match
                                        match.participants.forEach { participant ->
                                            participant.bookingId?.let { bId ->
                                                try {
                                                    firestore.collection(BOOKINGS_COLLECTION)
                                                        .document(bId)
                                                        .update(
                                                            mapOf(
                                                                "status" to "CANCELLED",
                                                                "updatedAt" to System.currentTimeMillis()
                                                            )
                                                        )
                                                        .await()
                                                    println("🔄 DEBUG: Booking $bId cancelled due to match cancellation")
                                                } catch (e: Exception) {
                                                    println("❌ ERROR: Failed to cancel booking $bId: ${e.message}")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Failed to reset match after cancel: ${e.message}")
                        }
                        // ✅ FIX: Xử lý cancellation notification cho cả 2 flow
                        if (booking.bookingType == "SOLO" && !booking.hasOpponent && !booking.matchId.isNullOrBlank()) {
                            // Flow 2: WAITING_OPPONENT - Gửi notification cho cả 2 renter trong match
                            println("🔔 DEBUG: WAITING_OPPONENT flow - sending cancellation notifications to both renters")
                            
                            try {
                                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                                    .document(booking.matchId)
                                    .get()
                                    .await()
                                
                                if (matchDoc.exists()) {
                                    val match = matchDoc.toObject(Match::class.java)
                                    if (match != null && match.participants.size >= 2) {
                                        // Gửi notification cho cả 2 participants
                                        match.participants.forEach { participant ->
                                            renterNotificationHelper.notifyBookingCancelledByOwner(
                                                renterId = participant.renterId,
                                                fieldName = fieldName,
                                                date = booking.date,
                                                time = booking.consecutiveSlots.firstOrNull() ?: "",
                                                reason = null,
                                                bookingId = booking.bookingId,
                                                fieldId = booking.fieldId
                                            )
                                            println("🔔 DEBUG: Sent booking cancelled notification to renter: ${participant.renterId}")
                                        }
                                    } else {
                                        // Fallback: chỉ gửi cho renter hiện tại
                                        renterNotificationHelper.notifyBookingCancelledByOwner(
                                            renterId = booking.renterId,
                                            fieldName = fieldName,
                                            date = booking.date,
                                            time = booking.consecutiveSlots.firstOrNull() ?: "",
                                            reason = null,
                                            bookingId = booking.bookingId,
                                            fieldId = booking.fieldId
                                        )
                                        println("🔔 DEBUG: Fallback - sent booking cancelled notification to renter: ${booking.renterId}")
                                    }
                                } else {
                                    // Fallback: chỉ gửi cho renter hiện tại
                                    renterNotificationHelper.notifyBookingCancelledByOwner(
                                        renterId = booking.renterId,
                                        fieldName = fieldName,
                                        date = booking.date,
                                        time = booking.consecutiveSlots.firstOrNull() ?: "",
                                        reason = null,
                                        bookingId = booking.bookingId,
                                        fieldId = booking.fieldId
                                    )
                                    println("🔔 DEBUG: Fallback - sent booking cancelled notification to renter: ${booking.renterId}")
                                }
                            } catch (e: Exception) {
                                println("❌ ERROR: Failed to get match info for cancellation, sending to single renter: ${e.message}")
                                // Fallback: chỉ gửi cho renter hiện tại
                                renterNotificationHelper.notifyBookingCancelledByOwner(
                                    renterId = booking.renterId,
                                    fieldName = fieldName,
                                    date = booking.date,
                                    time = booking.consecutiveSlots.firstOrNull() ?: "",
                                    reason = null,
                                    bookingId = booking.bookingId,
                                    fieldId = booking.fieldId
                                )
                                println("🔔 DEBUG: Fallback - sent booking cancelled notification to renter: ${booking.renterId}")
                            }
                        } else {
                            // Flow 1: HAS_OPPONENT - Gửi notification cho 1 renter
                            println("🔔 DEBUG: HAS_OPPONENT flow - sending cancellation notification to single renter")
                            renterNotificationHelper.notifyBookingCancelledByOwner(
                                renterId = booking.renterId,
                                fieldName = fieldName,
                                date = booking.date,
                                time = booking.consecutiveSlots.firstOrNull() ?: "",
                                reason = null,
                                bookingId = booking.bookingId,
                                fieldId = booking.fieldId
                            )
                            println("🔔 DEBUG: Sent booking cancelled notification to renter: ${booking.renterId}")
                        }
                    }
                } catch (e: Exception) {
                    println("❌ ERROR: Failed to send booking notification: ${e.message}")
                }
            }
            
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
                .whereIn("status", listOf("PENDING", "CONFIRMED")) // ✅ FIX: Chỉ lấy bookings chưa bị hủy
                .get()
                .await()
            
            val bookings = snapshot.toObjects(Booking::class.java)
            println("✅ DEBUG: Found ${bookings.size} waiting opponent bookings (PENDING/CONFIRMED only)")
            bookings.forEachIndexed { index, booking ->
                println("  [$index] bookingId: ${booking.bookingId}, status: ${booking.status}, slots: ${booking.consecutiveSlots}")
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
            // ✅ NEW: Chỉ coi là "đỏ" khi match còn hiệu lực (FULL/CONFIRMED)
            val activeMatchesSnap = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()

            val activeMatchIds = activeMatchesSnap.toObjects(Match::class.java)
                .filter { it.status == "FULL" || it.status == "CONFIRMED" }
                .map { it.rangeKey }
                .toSet()

            if (activeMatchIds.isEmpty()) {
                println("✅ DEBUG: No active matches (FULL/CONFIRMED) => locked bookings = 0")
                return Result.success(emptyList())
            }

            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "DUO")
                .whereEqualTo("hasOpponent", true)
                .whereIn("status", listOf("PENDING", "CONFIRMED"))
                .get()
                .await()

            val bookings = snapshot.toObjects(Booking::class.java)
                .filter { !it.matchId.isNullOrBlank() && activeMatchIds.contains(it.matchId) }

            println("✅ DEBUG: Found ${bookings.size} locked bookings filtered by active matches")
            bookings.forEachIndexed { index, booking ->
                println("  [$index] bookingId: ${booking.bookingId}, matchId: ${booking.matchId}, status: ${booking.status}, slots: ${booking.consecutiveSlots}")
            }

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
            
            // ✅ Gửi thông báo cho Renter A khi có đối thủ tham gia
            // TODO: Cần lấy thông tin booking để gửi thông báo chính xác
            notificationHelper?.notifyOpponentJoined(
                renterAId = "", // TODO: Lấy từ booking
                opponentName = opponentName,
                fieldName = "Sân", // TODO: Lấy từ booking
                date = "", // TODO: Lấy từ booking
                time = "", // TODO: Lấy từ booking
                matchId = null,
                fieldId = null
            )
            
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
            val ref = firestore.collection(BOOKINGS_COLLECTION).document(bookingId)
            val snap = ref.get().await()
            val current = snap.toObject(Booking::class.java)
            
            ref.update(
                mapOf(
                    "status" to "CANCELLED",
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
            
            println("✅ DEBUG: Booking cancelled: $bookingId")

            // ✅ NEW: Đặt lại các khe giờ về trạng thái trống cho booking vừa hủy
            try {
                current?.let { resetSlotsForBooking(it) }
            } catch (e: Exception) {
                println("❌ ERROR: Failed to reset slots for cancelled booking $bookingId: ${e.message}")
            }
            
            // ✅ NEW: Xử lý match khi renter hủy booking
            try {
                if (current != null && !current.matchId.isNullOrBlank()) {
                    val matchRef = firestore.collection(MATCHES_COLLECTION).document(current.matchId!!)
                    val matchSnap = matchRef.get().await()
                    if (matchSnap.exists()) {
                        val match = matchSnap.toObject(Match::class.java)
                        if (match != null) {
                            // Xóa renter đã hủy khỏi participants
                            val remainingParticipants = match.participants.filter { it.bookingId != bookingId }
                            
                            if (remainingParticipants.isEmpty()) {
                                // Nếu không còn participant nào, reset match về FREE
                                matchRef.update(
                                    mapOf(
                                        "status" to "FREE",
                                        "occupiedCount" to 0,
                                        "participants" to emptyList<Any>(),
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                ).await()
                                println("🔄 DEBUG: Match ${current.matchId} reset to FREE (no participants left)")
                            } else {
                                // Nếu còn 1 participant, chuyển về WAITING_OPPONENT
                                matchRef.update(
                                    mapOf(
                                        "status" to "WAITING_OPPONENT",
                                        "occupiedCount" to 1,
                                        "participants" to remainingParticipants,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                ).await()
                                println("🔄 DEBUG: Match ${current.matchId} changed to WAITING_OPPONENT (1 participant left)")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ ERROR: Failed to update match on booking cancel: ${e.message}")
            }

            // ✅ Thông báo cho owner (Client-side Approach A)
            try {
                if (current != null) {
                    val result = notificationRepository.createNotification(
                        toUserId = current.ownerId,
                        type = "BOOKING_CANCELLED",
                        title = "Đặt sân bị hủy!",
                        body = "Khung giờ ${current.startAt} ngày ${current.date} đã hủy",
                        data = NotificationData(
                            bookingId = bookingId,
                            fieldId = current.fieldId,
                            userId = current.renterId,
                            customData = emptyMap()
                        ),
                        priority = "HIGH"
                    )
                    if (result.isSuccess) {
                        println("🔔 DEBUG: Notification CANCEL CREATED -> ownerId=${current.ownerId}, bookingId=$bookingId")
                    } else {
                        println("❌ ERROR: Notification CANCEL CREATE FAILED -> ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                println("❌ ERROR: Notification CANCEL EXCEPTION -> ${e.message}")
            }

            // ✅ NEW: Nếu là trận đã có đối thủ, thông báo cho cả renter A và B về việc owner hủy
            try {
                if (current != null && !current.matchId.isNullOrBlank()) {
                    val matchRef = firestore.collection(MATCHES_COLLECTION).document(current.matchId!!)
                    val matchSnap = matchRef.get().await()
                    if (matchSnap.exists()) {
                        val match = matchSnap.toObject(Match::class.java)
                        val participantIds = match?.participants?.mapNotNull { it.renterId } ?: emptyList()
                        val fieldSnap = firestore.collection("fields").document(current.fieldId).get().await()
                        val fieldName = fieldSnap.getString("name") ?: "Sân"

                        participantIds.forEach { renterId ->
                            try {
                                val notifyRes = notificationRepository.createNotification(
                                    toUserId = renterId,
                                    type = "BOOKING_CANCELLED_BY_OWNER",
                                    title = "Trận đấu đã bị chủ sân hủy",
                                    body = "Sân $fieldName - ${current.startAt} ngày ${current.date} đã bị hủy.",
                                    data = NotificationData(
                                        bookingId = bookingId,
                                        fieldId = current.fieldId,
                                        userId = current.ownerId,
                                        customData = emptyMap()
                                    ),
                                    priority = "HIGH"
                                )
                                if (notifyRes.isSuccess) {
                                    println("🔔 DEBUG: Notify renters cancel by owner -> renterId=$renterId")
                                } else {
                                    println("❌ ERROR: Notify renter cancel failed -> ${notifyRes.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                println("❌ ERROR: Create notification to renter failed -> ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ ERROR: Cancel notify to participants failed: ${e.message}")
            }
            
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
    
    /**
     * Lấy match theo ID
     */
    fun getMatchById(matchId: String, onSuccess: (Match?) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val match = document.toObject(Match::class.java)
                    onSuccess(match)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
    
    /**
     * ✅ NEW: Lắng nghe thay đổi match theo matchId (realtime)
     */
    fun listenMatchById(
        matchId: String,
        onChange: (Match?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val match = if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject(Match::class.java)
                } else null
                onChange(match)
            }
    }
    
    /**
     * Lưu kết quả trận đấu
     */
    suspend fun saveMatchResult(matchResult: MatchResult): Result<Unit> {
        return try {
            firestore.collection(MATCH_RESULTS_COLLECTION)
                .document(matchResult.resultId)
                .set(matchResult)
                .await()
            
            println("✅ DEBUG: Match result saved: ${matchResult.resultId}")
            
            // Gửi thông báo kết quả trận đấu cho renter
            try {
                // Lấy thông tin field để có tên sân
                val fieldDoc = firestore.collection("fields")
                    .document(matchResult.fieldId)
                    .get()
                    .await()
                
                val fieldName = fieldDoc.getString("name") ?: "Sân"
                
                // Gửi notification cho renter
                val notificationRepository = NotificationRepository()
                val renterNotificationHelper = RenterNotificationHelper(notificationRepository)
                
                // Gửi notification cho cả hai renter
                val renterAId = matchResult.winnerRenterId ?: ""
                val renterBId = matchResult.loserRenterId ?: ""
                
                if (renterAId.isNotBlank()) {
                    val isWinner = matchResult.winnerSide == "A"
                    renterNotificationHelper.notifyMatchResult(
                        renterId = renterAId,
                        fieldName = fieldName,
                        result = "${matchResult.renterAScore} - ${matchResult.renterBScore}",
                        isWinner = isWinner,
                        matchId = matchResult.matchId,
                        fieldId = matchResult.fieldId
                    )
                }
                
                if (renterBId.isNotBlank()) {
                    val isWinner = matchResult.winnerSide == "B"
                    renterNotificationHelper.notifyMatchResult(
                        renterId = renterBId,
                        fieldName = fieldName,
                        result = "${matchResult.renterAScore} - ${matchResult.renterBScore}",
                        isWinner = isWinner,
                        matchId = matchResult.matchId,
                        fieldId = matchResult.fieldId
                    )
                }
                
                println("🔔 DEBUG: Sent match result notifications to renters: $renterAId, $renterBId")
            } catch (e: Exception) {
                println("❌ ERROR: Failed to send match result notifications: ${e.message}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to save match result: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lấy kết quả trận đấu theo matchId
     */
    suspend fun getMatchResult(matchId: String): Result<MatchResult?> {
        return try {
            val query = firestore.collection(MATCH_RESULTS_COLLECTION)
                .whereEqualTo("matchId", matchId)
                .limit(1)
                .get()
                .await()
            
            if (!query.isEmpty) {
                val matchResult = query.documents.first().toObject(MatchResult::class.java)
                Result.success(matchResult)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get match result: ${e.message}")
            Result.failure(e)
        }
    }
}
