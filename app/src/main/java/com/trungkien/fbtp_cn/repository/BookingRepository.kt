package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentSnapshot
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

    // ========================= SAFE PARSERS =========================
    private fun mapToServiceLineList(raw: Any?): List<ServiceLine> {
        val list = mutableListOf<ServiceLine>()
        if (raw is List<*>) {
            raw.forEach { item ->
                if (item is Map<*, *>) {
                    val service = ServiceLine(
                        serviceId = item["serviceId"] as? String ?: "",
                        name = item["name"] as? String ?: "",
                        billingType = item["billingType"] as? String ?: "UNIT",
                        price = (item["price"] as? Number)?.toLong() ?: 0L,
                        quantity = (item["quantity"] as? Number)?.toInt() ?: 0,
                        lineTotal = (item["lineTotal"] as? Number)?.toLong() ?: 0L
                    )
                    list.add(service)
                }
            }
        }
        return list
    }

    private fun parseMatchSafe(doc: DocumentSnapshot): Match? {
        return try {
            doc.toObject(Match::class.java)
        } catch (_: Exception) {
            try {
                val data = doc.data ?: return null
                val rangeKey = data["rangeKey"] as? String ?: doc.id
                val fieldId = data["fieldId"] as? String ?: ""
                val date = data["date"] as? String ?: ""
                val startAt = data["startAt"] as? String ?: ""
                val endAt = data["endAt"] as? String ?: ""
                val capacity = (data["capacity"] as? Number)?.toInt() ?: 2
                val occupiedCount = (data["occupiedCount"] as? Number)?.toInt() ?: 0
                val price = (data["price"] as? Number)?.toLong() ?: 0L
                val totalPrice = (data["totalPrice"] as? Number)?.toLong() ?: 0L
                val status = data["status"] as? String ?: "WAITING_OPPONENT"
                val matchType = data["matchType"] as? String

                val notesAny = data["notes"]
                val notes: List<String?> = when (notesAny) {
                    is String -> listOf(notesAny, null)
                    is List<*> -> {
                        val tmp = notesAny.map { it as? String }
                        (tmp + listOf<String?>(null, null)).take(2)
                    }
                    else -> listOf(data["noteA"] as? String, data["noteB"] as? String)
                }

                val participantsRaw = data["participants"]
                val participants: List<MatchParticipant> = if (participantsRaw is List<*>) {
                    participantsRaw.mapNotNull { m ->
                        if (m is Map<*, *>) {
                            MatchParticipant(
                                bookingId = m["bookingId"] as? String ?: "",
                                renterId = m["renterId"] as? String ?: "",
                                side = m["side"] as? String ?: "A"
                            )
                        } else null
                    }
                } else emptyList()

                val slBySideAny = data["serviceLinesBySide"]
                val slA = if (slBySideAny is Map<*, *>) mapToServiceLineList(slBySideAny["A"]) else emptyList() 
                val slB = if (slBySideAny is Map<*, *>) mapToServiceLineList(slBySideAny["B"]) else emptyList() 
                val serviceLinesBySide = mapOf("A" to slA, "B" to slB)

                val createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

                Match(
                    rangeKey = rangeKey,
                    fieldId = fieldId,
                    date = date,
                    startAt = startAt,
                    endAt = endAt,
                    capacity = capacity,
                    occupiedCount = occupiedCount,
                    participants = participants,
                    price = price,
                    totalPrice = totalPrice,
                    status = status,
                    matchType = matchType,
                    notes = notes,
                    serviceLinesBySide = serviceLinesBySide,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } catch (e: Exception) {
                println("❌ ERROR: parseMatchSafe fallback failed: ${e.message}")
                null
            }
        }
    }

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
            println("🔄 DEBUG: resetSlotsForBooking called for booking ${booking.bookingId}")
            println("  - fieldId: ${booking.fieldId}")
            println("  - date: ${booking.date}")
            println("  - slots: ${booking.consecutiveSlots}")
            
            // ✅ STEP 1: Reset slots collection (giữ nguyên logic cũ)
            val batch = firestore.batch()
            var slotsUpdated = 0
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
                    slotsUpdated++
                    println("🔄 DEBUG: Will reset slot ${startAt} (doc: ${doc.id})")
                } else {
                    println("⚠️ DEBUG: Slot ${startAt} not found in firebaseSlots collection")
                }
            }
            batch.commit().await()
            println("✅ DEBUG: Slots reset completed for booking ${booking.bookingId} -> ${booking.consecutiveSlots} (updated: $slotsUpdated slots)")
            
            // ✅ STEP 2: Xử lý chuyển đổi trạng thái màu sắc trong matches collection
            val matchId = booking.matchId
            if (!matchId.isNullOrBlank()) {
                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                    .document(matchId)
                    .get()
                    .await()
                
                if (matchDoc.exists()) {
                    val match = parseMatchSafe(matchDoc)
                    if (match != null) {
                        when {
                            // Trường hợp 1: Renter A hủy solo booking (WAITING_OPPONENT) → chuyển về trắng
                            booking.bookingType == "SOLO" && !booking.hasOpponent -> {
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "CANCELLED",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 CANCELLATION: SOLO booking cancelled - Reset to WHITE (CANCELLED)")
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
                                            "status" to "CANCELLED",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 CANCELLATION: Owner cancelled entire match - Reset to WHITE (CANCELLED)")
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
            
            // ✅ Logic 1 - HAS_OPPONENT: Renter đặt khe giờ với đối thủ sẵn có
            // - notes → Booking.notes (chỉ lưu vào Booking, KHÔNG tạo Match)
            // - serviceLines → Booking.serviceLines (chỉ lưu vào Booking)
            // - Booking này KHÔNG hiển thị trong OwnerMatchDetailScreen (vì không có Match)
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
                serviceLines = serviceLines, // ✅ Dịch vụ thêm → lưu vào Booking.serviceLines
                servicePrice = servicePrice,
                totalPrice = totalPrice,
                status = "PENDING",
                notes = notes, // ✅ Logic 1: notes lưu vào Booking.notes (vì không có Match)
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
                
                val allList = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                println("🔍 DEBUG: listenBookingsByRenter RAW result:")
                println("  - snapshot size: ${snapshot?.size() ?: 0}")
                println("  - all bookings found: ${allList.size}")
                
                // ✅ CRITICAL FIX: Double-check renterId filtering in memory
                val filteredList = allList.filter { booking ->
                    booking.renterId == renterId
                }
                
                println("🔍 DEBUG: After memory filtering:")
                println("  - renterId to filter: $renterId")
                println("  - filtered bookings: ${filteredList.size}")
                filteredList.forEachIndexed { index, booking ->
                    println("  [$index] bookingId: ${booking.bookingId}, renterId: ${booking.renterId}, type: ${booking.bookingType}, status: ${booking.status}, date: ${booking.date}")
                }
                
                val sortedList = filteredList.sortedByDescending { it.createdAt }
                onChange(sortedList)
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
                val list = snapshot?.documents?.mapNotNull { doc -> parseMatchSafe(doc) } ?: emptyList()
                println("✅ DEBUG: listenMatchesByFieldDate result:")
                println("  - snapshot size: ${snapshot?.size() ?: 0}")
                println("  - matches found: ${list.size}")
                list.forEachIndexed { index, match ->
                    println("  [$index] matchId: ${match.rangeKey}, status: ${match.status}, participants: ${match.participants.size}")
                }
                println("🔄 DEBUG: Calling onChange callback with ${list.size} matches")
                onChange(list)
            }
    }

    /**
     * ✅ NEW: Cho phép renter B cập nhật ghi chú và dịch vụ SAU KHI đã join
     * - Không đổi participants/status
     * - Cập nhật notes[1] và serviceLinesBySide["B"]
     */
    suspend fun updateOpponentDetails(
        matchId: String,
        renterId: String,
        notes: String?,
        serviceLines: List<ServiceLine>
    ): Result<Unit> {
        return try {
            val matchRef = firestore.collection(MATCHES_COLLECTION).document(matchId)
            val snap = matchRef.get().await()
            if (!snap.exists()) return Result.failure(IllegalStateException("Match not found"))
            val match = parseMatchSafe(snap) ?: return Result.failure(IllegalStateException("Match parse failed"))

            val isParticipantB = match.participants.any { it.side == "B" && it.renterId == renterId }
            if (!isParticipantB) return Result.failure(IllegalStateException("Current user is not renter B of this match"))

            val updateData = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )

            val currentNotes = (match.notes + listOf(null, null)).take(2)
            val newNotes = if (notes != null) listOf(currentNotes[0], notes) else currentNotes
            updateData["notes"] = newNotes

            val currentServicesMap = match.serviceLinesBySide.ifEmpty { mapOf("A" to emptyList(), "B" to emptyList()) }
            val newServicesMap = mapOf(
                "A" to (currentServicesMap["A"] ?: emptyList()),
                "B" to serviceLines
            )
            updateData["serviceLinesBySide"] = newServicesMap

            matchRef.update(updateData).await()
            println("✅ DEBUG: updateOpponentDetails saved for B -> notes='${newNotes[1]}' services=${serviceLines.size}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: updateOpponentDetails failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Cập nhật notes của Match
     */
    suspend fun updateMatchNotes(matchId: String, noteA: String?, noteB: String?): Result<Unit> {
        return try {
            val matchDoc = firestore.collection(MATCHES_COLLECTION).document(matchId).get().await()
            val current = parseMatchSafe(matchDoc)
            val updateData = mutableMapOf<String, Any>()
            if (current != null) {
                val curNotes = (current.notes + listOf(null, null)).take(2)
                val newNotes = listOf(noteA ?: curNotes[0], noteB ?: curNotes[1])
                updateData["notes"] = newNotes
            }
            // Legacy fields removed – only notes array is updated
            
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
            
            val match = parseMatchSafe(matchDoc)
            if (match == null) {
                return Result.failure(Exception("Failed to parse match"))
            }
            
            // Cập nhật theo trạng thái mới
            if (newStatus == "CANCELLED") {
                // ✅ FIX: Giữ match với status CANCELLED để hiển thị ở tab "Trận đấu" > "Đã hủy"
                // Hủy tất cả bookings liên quan nhưng giữ match để tracking
                try {
                    // ✅ FIX: Lưu participants TRƯỚC KHI reset match để có thể gửi notification sau
                    val participantsToNotify = match.participants.toList()
                    
                    // ✅ FIX: Lấy thông tin field để gửi notification
                    val fieldDoc = firestore.collection("fields")
                        .document(match.fieldId)
                        .get()
                        .await()
                    
                    val fieldName = fieldDoc.getString("name") ?: "Sân"
                    
                    // ✅ FIX: Gửi notification cho TẤT CẢ participants (kể cả WAITING_OPPONENT với 1 participant)
                    val notificationRepository = NotificationRepository()
                    val renterNotificationHelper = RenterNotificationHelper(notificationRepository)
                    
                    println("🔔 DEBUG: updateMatchStatus - sending cancellation notifications to ${participantsToNotify.size} renters")
                    
                    participantsToNotify.forEach { participant ->
                        try {
                            // Lấy thông tin booking để có thông tin chi tiết (nếu có bookingId)
                            if (!participant.bookingId.isNullOrEmpty()) {
                                val bookingDoc = firestore.collection(BOOKINGS_COLLECTION)
                                    .document(participant.bookingId)
                                    .get()
                                    .await()
                                
                                if (bookingDoc.exists()) {
                                    val booking = bookingDoc.toObject(Booking::class.java)
                                    if (booking != null) {
                                        renterNotificationHelper.notifyBookingCancelledByOwner(
                                            renterId = participant.renterId,
                                            fieldName = fieldName,
                                            date = booking.date,
                                            time = booking.consecutiveSlots.firstOrNull() ?: match.startAt,
                                            reason = null,
                                            bookingId = booking.bookingId,
                                            fieldId = booking.fieldId
                                        )
                                        println("🔔 DEBUG: Sent booking cancelled notification to renter: ${participant.renterId}")
                                    }
                                }
                            } else {
                                // Renter B không có bookingId, gửi notification trực tiếp
                                val res = notificationRepository.createNotification(
                                    toUserId = participant.renterId,
                                    type = "BOOKING_CANCELLED_BY_OWNER",
                                    title = "Trận đấu đã bị chủ sân hủy",
                                    body = "Sân $fieldName - ${match.startAt} ngày ${match.date} đã bị hủy.",
                                    data = NotificationData(
                                        bookingId = "",
                                        fieldId = match.fieldId,
                                        userId = null,
                                        customData = emptyMap()
                                    ),
                                    priority = "HIGH"
                                )
                                if (res.isSuccess) {
                                    println("🔔 DEBUG: Sent match cancelled notification to renter (no booking): ${participant.renterId}")
                                } else {
                                    println("❌ ERROR: Failed to send notification to renter ${participant.renterId}: ${res.exceptionOrNull()?.message}")
                                }
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Failed to send notification to renter ${participant.renterId}: ${e.message}")
                        }
                    }
                    
                    // ✅ NEW: Lấy toàn bộ bookings liên quan đến matchId này (kể cả renter A, B)
                    val matchBookingDocs = firestore.collection(BOOKINGS_COLLECTION)
                        .whereEqualTo("matchId", matchId)
                        .get()
                        .await()
                    val matchBookings = matchBookingDocs.documents.mapNotNull { it.toObject(Booking::class.java) }
                    val participantBookingIds = match.participants.mapNotNull { it.bookingId }
                    val bookingIdsFromQuery = matchBookings.mapNotNull { it.bookingId }
                    val allBookingIds = (participantBookingIds + bookingIdsFromQuery).toSet()
                    println("🔍 DEBUG: updateMatchStatus - cancelling bookings: $allBookingIds")
                    allBookingIds.forEach { bId ->
                        try {
                            // ✅ DEBUG: Get booking info before cancelling
                            val bookingBeforeDoc = firestore.collection(BOOKINGS_COLLECTION)
                                .document(bId)
                                .get()
                                .await()
                            val bookingBefore = bookingBeforeDoc.toObject(Booking::class.java)
                            println("🔍 DEBUG: Booking $bId before cancel:")
                            println("  - status: ${bookingBefore?.status}")
                            println("  - slots: ${bookingBefore?.consecutiveSlots}")
                            println("  - matchId: ${bookingBefore?.matchId}")
                            println("  - renterId: ${bookingBefore?.renterId}")
                            
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
                            
                            // ✅ DEBUG: Verify booking status after update
                            val updatedBookingDoc = firestore.collection(BOOKINGS_COLLECTION)
                                .document(bId)
                                .get()
                                .await()
                            val updatedBookingStatus = updatedBookingDoc.getString("status")
                            println("✅ DEBUG: Verified booking $bId status after update: $updatedBookingStatus")

                            // ✅ NEW: Đặt lại các khe giờ về trạng thái trống cho booking này
                            try {
                                val booking = matchBookings.firstOrNull { it.bookingId == bId }
                                    ?: updatedBookingDoc.toObject(Booking::class.java)
                                if (booking != null) {
                                    resetSlotsForBooking(booking.copy(status = "CANCELLED"))
                                }
                            } catch (e: Exception) {
                                println("❌ ERROR: Failed to reset slots for booking $bId: ${e.message}")
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Failed to cancel booking $bId on match cancel: ${e.message}")
                        }
                    }

                    // ✅ FIX: Reset match về CANCELLED để khe giờ có thể được đặt lại (màu trắng)
                    firestore.collection(MATCHES_COLLECTION)
                        .document(matchId)
                        .update(
                            mapOf(
                                "status" to "CANCELLED",
                                "occupiedCount" to 0,
                                "participants" to emptyList<Any>(),
                                "updatedAt" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    println("🔄 DEBUG: Match $matchId reset to CANCELLED (slots available again) - WHITE color")
                    println("🔄 DEBUG: Owner cancelled FULL match - Match status changed to CANCELLED")
                    println("🔄 DEBUG: This should trigger real-time update in BookingTimeSlotGrid")
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
            
            // ✅ REMOVED: Logic gửi notification khi hủy match đã được xử lý ở trên (dòng 580-640)
            // Không cần gửi lại ở đây vì match.participants đã bị clear

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

            // ✅ Logic 2 - FIND_OPPONENT: Renter A đặt khe giờ chưa có đối thủ
            // - TẤT CẢ dữ liệu (serviceLines, notes) → chỉ lưu vào Match, KHÔNG lưu vào Booking
            // - serviceLines → Match.serviceLinesA (KHÔNG lưu vào Booking.serviceLines)
            // - notes → Match.noteA (KHÔNG lưu vào Booking.notes)
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
                serviceLines = emptyList(), // ✅ FIX: FIND_OPPONENT - KHÔNG lưu serviceLines vào Booking, chỉ lưu vào Match.serviceLinesA
                servicePrice = 0, // ✅ FIX: Service price = 0 vì serviceLines = emptyList()
                totalPrice = basePrice, // ✅ FIX: Total price = basePrice (không có servicePrice)
                status = "PENDING",
                notes = null, // ✅ FIX: FIND_OPPONENT - KHÔNG lưu notes vào Booking, chỉ lưu vào Match.noteA
                hasOpponent = false,
                bookingType = "SOLO",
                opponentMode = "WAITING_OPPONENT",
                consecutiveSlots = consecutiveSlots,
                matchId = rangeKey,
                matchSide = "A"
            )

            // ✅ Logic 2: Tạo Match cho Renter A (chưa có đối thủ)
            // - TẤT CẢ dữ liệu của Renter A → lưu vào Match (array index 0)
            // - notes → Match.notes[0]
            // - serviceLines → Match.serviceLines[0]
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
                totalPrice = basePrice + servicePrice, // ✅ FIX: Total price = basePrice + servicePrice (từ serviceLines)
                status = "WAITING_OPPONENT",
                matchType = "SINGLE",
                // NEW arrays
                notes = listOf(notes, null),
                serviceLinesBySide = mapOf(
                    "A" to serviceLines,
                    "B" to emptyList()
                )
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
     * ✅ FIX: Renter thứ 2 tham gia làm đối thủ -> CHỈ cập nhật Match, KHÔNG tạo Booking B
     * - notes → Match.noteB
     * - serviceLines → Match.serviceLinesB
     * - participant B → thêm vào Match.participants (bookingId = "" vì không có Booking B)
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
            val match = parseMatchSafe(matchSnap) ?: return Result.failure(IllegalStateException("Match not found"))
            println("🔍 DEBUG: Match status: ${match.status}")
            if (match.status == "FULL") return Result.failure(IllegalStateException("Match already full"))

            // ✅ FIX: Logic 2 - FIND_OPPONENT: Renter B join vào match của Renter A
            // - CHỈ lưu vào Match, KHÔNG tạo Booking B
            // - notes → Match.noteB
            // - serviceLines → Match.serviceLinesB
            // - participant B → thêm vào Match.participants (bookingId = "" vì không có Booking B)
            println("🔍 DEBUG: Renter B joining - serviceLines count: ${serviceLines.size}")
            serviceLines.forEachIndexed { index, service ->
                println("  [$index] ${service.name} (id: ${service.serviceId}): qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
            }
            println("🔍 DEBUG: Renter B notes: '$notes'")

            // ✅ FIX: Chỉ cập nhật Match, không tạo Booking B
            val updatedParticipants = match.participants + MatchParticipant(
                bookingId = "", // ✅ FIX: Không có Booking B nên để empty
                renterId = renterId,
                side = "B"
            )
            
            // ✅ FIX: Tính tổng servicePrice của renter B
            val servicePriceB = serviceLines.sumOf { it.lineTotal }
            
            // ✅ FIX: Tính tổng servicePrice của renter A (từ match hiện tại)
            val servicePriceA = (match.serviceLinesBySide["A"] ?: emptyList()).sumOf { it.lineTotal }
            
            // ✅ FIX: Cập nhật totalPrice = basePrice + servicePrice A + servicePrice B
            val newTotalPrice = match.price + servicePriceA + servicePriceB
            
            val updateData = mutableMapOf<String, Any>(
                "occupiedCount" to 2,
                "status" to "FULL",
                "participants" to updatedParticipants,
                "totalPrice" to newTotalPrice, // ✅ FIX: Cập nhật totalPrice khi renter B join
                "updatedAt" to System.currentTimeMillis()
            )

            // ✅ NEW: Update array-based fields by reading current arrays and replacing index 1 (side B)
            val currentNotes = (match.notes + listOf(null, null)).take(2)
            val newNotes = if (notes != null) listOf(currentNotes[0], notes) else currentNotes
            updateData["notes"] = newNotes
            println("✅ DEBUG: Updating notes array: [A='${newNotes[0]}', B='${newNotes[1]}']")

            val currentServicesMap = match.serviceLinesBySide.ifEmpty { mapOf("A" to emptyList(), "B" to emptyList()) }
            val newServicesMap = mapOf(
                "A" to (currentServicesMap["A"] ?: emptyList()),
                "B" to serviceLines
            )
            updateData["serviceLinesBySide"] = newServicesMap
            println("✅ DEBUG: Updating serviceLinesBySide: A=${newServicesMap["A"]?.size ?: 0} items, B=${newServicesMap["B"]?.size ?: 0} items")
            println("✅ DEBUG: Updating totalPrice: basePrice=${match.price}, servicePriceA=$servicePriceA, servicePriceB=$servicePriceB, newTotalPrice=$newTotalPrice")

            // Stop mirroring to legacy fields
            serviceLines.forEachIndexed { index, service ->
                println("  [$index] serviceId=${service.serviceId}, name='${service.name}', qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
            }
            
            println("🔍 DEBUG: About to update Match document with updateData:")
            println("  - updateData keys: ${updateData.keys}")
            println("  - notes array in updateData: ${updateData["notes"]}")
            println("  - serviceLinesBySide sizes: ${(newServicesMap["A"]?.size ?: 0)} / ${(newServicesMap["B"]?.size ?: 0)}")
            
            matchRef.update(updateData).await()
            
            // ✅ Verify update by reading back the document
            val verifySnap = matchRef.get().await()
            val verifiedMatch = parseMatchSafe(verifySnap)
            println("✅ DEBUG: joinOpponent completed successfully - only updated Match, no Booking B created")
            println("  - noteB requested: '$notes'")
            println("  - notes[1] in Firestore: '${verifiedMatch?.notes?.getOrNull(1)}'")
            println("  - serviceLinesB requested count: ${serviceLines.size}")
            println("  - serviceLinesBySide['B'] count: ${verifiedMatch?.serviceLinesBySide?.get("B")?.size ?: 0}")
            println("  - participants count: ${updatedParticipants.size}")
            
            // ✅ Debug: Log verified serviceLinesBySide["B"]
            val verifiedB = verifiedMatch?.serviceLinesBySide?.get("B").orEmpty()
            if (verifiedB.isNotEmpty()) {
                println("✅ DEBUG: Verified serviceLinesBySide['B'] from Firestore:")
                verifiedB.forEachIndexed { index, service ->
                    println("  [$index] serviceId='${service.serviceId}', name='${service.name}', qty=${service.quantity}, price=${service.price}, total=${service.lineTotal}")
                }
            } else {
                println("⚠️ WARNING: serviceLinesBySide['B'] is empty in Firestore after update!")
            }
            
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
            
            // ✅ FIX: Return matchId thay vì bookingId (vì không tạo Booking B)
            return Result.success(matchId)
        } catch (e: Exception) {
            println("❌ ERROR: joinOpponent failed: ${e.message}")
            return Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Cho phép renter B cập nhật lại notes/serviceLines sau khi đã join, kể cả khi Match đang FULL
     * - Không thay đổi status/occupiedCount
     * - Chỉ ghi đè notes[1] và serviceLinesBySide["B"]
     */
    suspend fun updateRenterBInMatch(
        matchId: String,
        renterId: String,
        serviceLines: List<ServiceLine> = emptyList(),
        notes: String? = null
    ): Result<Unit> {
        return try {
            val matchRef = firestore.collection(MATCHES_COLLECTION).document(matchId)
            val snap = matchRef.get().await()
            if (!snap.exists()) return Result.failure(IllegalStateException("Match not found"))
            val match = parseMatchSafe(snap) ?: return Result.failure(IllegalStateException("Match parse error"))

            // Xác nhận có participant B
            val hasB = match.participants.any { it.side.equals("B", true) }
            if (!hasB) return Result.failure(IllegalStateException("Participant B not found in match"))

            // Ghi đè notes[1]
            val currentNotes = (match.notes + listOf(null, null)).take(2)
            val newNotes = if (notes != null) listOf(currentNotes[0], notes) else currentNotes

            // Ghi đè servicesBySide["B"]
            val currentServicesMap = match.serviceLinesBySide.ifEmpty { mapOf("A" to emptyList(), "B" to emptyList()) }
            val newServicesMap = mapOf(
                "A" to (currentServicesMap["A"] ?: emptyList()),
                "B" to serviceLines
            )

            val update = mapOf(
                "notes" to newNotes,
                "serviceLinesBySide" to newServicesMap,
                "updatedAt" to System.currentTimeMillis()
            )

            println("🔁 DEBUG: updateRenterBInMatch → matchId=$matchId, notesB='${newNotes.getOrNull(1)}', servicesB=${serviceLines.size}")
            matchRef.update(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: updateRenterBInMatch failed → ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Đảm bảo có Booking cho renter B khi đã join match (nếu chưa có thì tạo mới)
     * - Nếu đã tồn tại booking của renter B cho matchId này → trả về bookingId hiện có
     * - Nếu chưa có → tạo Booking B copy dữ liệu thời gian từ Booking A (cùng match), matchSide = "B"
     */
    suspend fun ensureBookingForRenterB(
        matchId: String,
        renterBId: String,
        ownerId: String,
        basePrice: Long
    ): Result<String> {
        return try {
            val bookingsCol = firestore.collection(BOOKINGS_COLLECTION)

            // 1) Tìm booking hiện có của renter B cho matchId
            val existingSnap = bookingsCol
                .whereEqualTo("matchId", matchId)
                .whereEqualTo("renterId", renterBId)
                .get()
                .await()
            val existing = existingSnap.documents.firstOrNull()?.getString("bookingId")
            if (existing != null) {
                println("✅ DEBUG: ensureBookingForRenterB → existing bookingId=$existing")
                return Result.success(existing)
            }

            // 2) Lấy booking của A làm mẫu (cùng match)
            val aSnap = bookingsCol
                .whereEqualTo("matchId", matchId)
                .get()
                .await()
            val bookingA = aSnap.toObjects(Booking::class.java).firstOrNull()
                ?: return Result.failure(IllegalStateException("Booking A not found for match $matchId"))

            val newBookingId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val bookingB = Booking(
                bookingId = newBookingId,
                renterId = renterBId,
                ownerId = ownerId,
                fieldId = bookingA.fieldId,
                date = bookingA.date,
                startAt = bookingA.startAt,
                endAt = bookingA.endAt,
                consecutiveSlots = bookingA.consecutiveSlots,
                basePrice = basePrice,
                bookingType = "SOLO",
                hasOpponent = false,
                matchId = matchId,
                status = "PENDING",
                createdAt = now,
                updatedAt = now,
                matchSide = "B",
                createdWithOpponent = false
            )

            bookingsCol.document(newBookingId).set(bookingB).await()
            println("✅ DEBUG: ensureBookingForRenterB → created bookingBId=$newBookingId for matchId=$matchId")
            Result.success(newBookingId)
        } catch (e: Exception) {
            println("❌ ERROR: ensureBookingForRenterB failed → ${e.message}")
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
            val activeMatchIds = activeMatchesSnap.documents
                .mapNotNull { parseMatchSafe(it) }
                .filter { it.status == "FULL" || it.status == "CONFIRMED" }
                .map { it.rangeKey }
                .toSet()

            val bookings = bookingsSnap.toObjects(Booking::class.java)
            
            // ✅ DEBUG: Log trạng thái của từng booking
            bookings.forEach { booking ->
                val isConfirmedOrPaid = booking.status.equals("CONFIRMED", true) || booking.status.equals("PAID", true)
                val belongsToActiveMatch = !booking.matchId.isNullOrBlank() && activeMatchIds.contains(booking.matchId)
                val willBeLocked = isConfirmedOrPaid || belongsToActiveMatch
                
                println("🔍 DEBUG: Booking ${booking.bookingId}:")
                println("  - status: ${booking.status}")
                println("  - matchId: ${booking.matchId}")
                println("  - isConfirmedOrPaid: $isConfirmedOrPaid")
                println("  - belongsToActiveMatch: $belongsToActiveMatch")
                println("  - willBeLocked: $willBeLocked")
                println("  - slots: ${booking.consecutiveSlots}")
                println("  - bookingType: ${booking.bookingType}")
                println("  - hasOpponent: ${booking.hasOpponent}")
                println("  - createdAt: ${booking.createdAt}")
                println("  - updatedAt: ${booking.updatedAt}")
                println("  - renterId: ${booking.renterId}")
                println("  - ownerId: ${booking.ownerId}")
            }
            
            // ✅ DEBUG: Log active matches
            println("🔍 DEBUG: Active matches for field $fieldId on $date:")
            activeMatchIds.forEach { matchId ->
                println("  - matchId: $matchId")
            }
            
            // ✅ DEBUG: Log all matches (including FREE/CANCELLED)
            val allMatches = activeMatchesSnap.documents.mapNotNull { parseMatchSafe(it) }
            println("🔍 DEBUG: All matches for field $fieldId on $date:")
            allMatches.forEach { match ->
                println("  - matchId: ${match.rangeKey}, status: ${match.status}, participants: ${match.participants.size}")
            }
            
            // ✅ FIX: Auto-fix bookings that are PAID but belong to cancelled matches
            val cancelledMatchIds = allMatches
                .filter { it.status == "CANCELLED" }
                .map { it.rangeKey }
                .toSet()
            
            val stuckBookings = bookings.filter { booking ->
                booking.status == "PAID" && 
                !booking.matchId.isNullOrBlank() && 
                cancelledMatchIds.contains(booking.matchId)
            }
            
            if (stuckBookings.isNotEmpty()) {
                println("🔧 DEBUG: Found ${stuckBookings.size} stuck PAID bookings belonging to cancelled matches:")
                stuckBookings.forEach { booking ->
                    println("  - bookingId: ${booking.bookingId}, matchId: ${booking.matchId}")
                }
                
                // Auto-fix these bookings
                stuckBookings.forEach { booking ->
                    try {
                        firestore.collection(BOOKINGS_COLLECTION)
                            .document(booking.bookingId)
                            .update(
                                mapOf(
                                    "status" to "CANCELLED",
                                    "updatedAt" to System.currentTimeMillis()
                                )
                            )
                            .await()
                        println("🔧 DEBUG: Auto-fixed stuck booking ${booking.bookingId} -> CANCELLED")
                    } catch (e: Exception) {
                        println("❌ ERROR: Failed to auto-fix booking ${booking.bookingId}: ${e.message}")
                    }
                }
            }
            
            val times = bookings
                .asSequence()
                .filter { booking ->
                    // ✅ FIX: Logic mới - chỉ khóa khe giờ khi:
                    // 1. Booking đã CONFIRMED/PAID (đã được owner xác nhận)
                    // 2. HOẶC booking thuộc match còn hiệu lực (FULL/CONFIRMED)
                    
                    val isConfirmedOrPaid = booking.status.equals("CONFIRMED", true) || booking.status.equals("PAID", true)
                    val belongsToActiveMatch = !booking.matchId.isNullOrBlank() && activeMatchIds.contains(booking.matchId)
                    
                    // Chỉ khóa khi booking đã được xác nhận hoặc thuộc match còn hiệu lực
                    isConfirmedOrPaid || belongsToActiveMatch
                }
                .flatMap { it.consecutiveSlots }
                .toSet()
                
            println("🔍 DEBUG: getBookedStartTimes - Field: $fieldId, Date: $date")
            println("🔍 DEBUG: - Total bookings: ${bookingsSnap.size()}")
            println("🔍 DEBUG: - Active matches: ${activeMatchIds.size}")
            println("🔍 DEBUG: - Locked time slots: ${times.size}")
            println("🔍 DEBUG: - Locked slots: $times")

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
                                    val match = parseMatchSafe(matchDoc)
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
                        // ✅ FIX: Xử lý cancellation notification cho cả 2 flow TRƯỚC KHI reset match
                        if (booking.bookingType == "SOLO" && !booking.hasOpponent && !booking.matchId.isNullOrBlank()) {
                            // Flow 2: WAITING_OPPONENT - Gửi notification cho cả 2 renter trong match
                            println("🔔 DEBUG: WAITING_OPPONENT flow - sending cancellation notifications to both renters")
                            
                            try {
                                // ✅ FIX: Lấy match info TRƯỚC KHI reset
                                val matchDoc = firestore.collection(MATCHES_COLLECTION)
                                    .document(booking.matchId)
                                    .get()
                                    .await()
                                
                                if (matchDoc.exists()) {
                                    val match = parseMatchSafe(matchDoc)
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
                                        
                                        // ✅ FIX: Cancel tất cả bookings trong match TRƯỚC KHI reset match
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
                        
                        // ✅ FIX: Reset match về CANCELLED SAU KHI đã gửi notification và cancel bookings
                        try {
                            val matchId = booking.matchId
                            if (!matchId.isNullOrBlank()) {
                                // ✅ FIX: Reset match về CANCELLED để khe giờ có thể được đặt lại (màu trắng)
                                firestore.collection(MATCHES_COLLECTION)
                                    .document(matchId)
                                    .update(
                                        mapOf(
                                            "status" to "CANCELLED",
                                            "occupiedCount" to 0,
                                            "participants" to emptyList<Any>(),
                                            "updatedAt" to System.currentTimeMillis()
                                        )
                                    )
                                    .await()
                                println("🔄 DEBUG: Match reset to CANCELLED due to booking cancel: $matchId - WHITE color")
                            }
                        } catch (e: Exception) {
                            println("❌ ERROR: Failed to reset match after cancel: ${e.message}")
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

            // 1) Lấy danh sách match theo ngày để xác định FULL/CONFIRMED
            val matchesSnap = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()
            val allMatches = matchesSnap.documents.mapNotNull { parseMatchSafe(it) }
            val activeMatchIds = allMatches
                .filter { it.status == "FULL" || it.status == "CONFIRMED" }
                .map { it.rangeKey }
                .toSet()
            println("🔍 DEBUG: getWaitingOpponentBookings → activeMatchIds size = ${activeMatchIds.size}")

            // 2) Lấy bookings SOLO, chưa có đối thủ, còn hiệu lực
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereEqualTo("bookingType", "SOLO")
                .whereEqualTo("hasOpponent", false)
                .whereIn("status", listOf("PENDING", "CONFIRMED"))
                .get()
                .await()

            val raw = snapshot.toObjects(Booking::class.java)

            // 3) Loại các booking đã thuộc match FULL/CONFIRMED để không bị hiển thị vàng nữa
            val bookings = raw.filter { booking ->
                val inActive = !booking.matchId.isNullOrBlank() && activeMatchIds.contains(booking.matchId)
                if (inActive) {
                    println("↪️ FILTER OUT waiting booking (belongs to FULL/CONFIRMED match): ${booking.bookingId} matchId=${booking.matchId}")
                }
                !inActive
            }

            println("✅ DEBUG: Found ${bookings.size} waiting opponent bookings after filtering active matches")
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
            println("🔍 DEBUG: getLockedBookings called for fieldId: $fieldId, date: $date")
            
            // ✅ NEW: Chỉ coi là "đỏ" khi match còn hiệu lực (FULL/CONFIRMED)
            val activeMatchesSnap = firestore.collection(MATCHES_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .get()
                .await()

            val allMatches = activeMatchesSnap.documents.mapNotNull { parseMatchSafe(it) }
            println("🔍 DEBUG: All matches found: ${allMatches.size}")
            allMatches.forEach { match ->
                println("  - Match ${match.rangeKey}: status=${match.status}, participants=${match.participants.size}")
            }

            val activeMatchIds = allMatches
                .filter { it.status == "FULL" || it.status == "CONFIRMED" }
                .map { it.rangeKey }
                .toSet()

            println("🔍 DEBUG: Active matches (FULL/CONFIRMED): ${activeMatchIds.size}")
            activeMatchIds.forEach { matchId ->
                println("  - Active matchId: $matchId")
            }

            if (activeMatchIds.isEmpty()) {
                println("✅ DEBUG: No active matches (FULL/CONFIRMED) => locked bookings = 0")
                return Result.success(emptyList())
            }

            // ✅ IMPORTANT: KHÔNG giới hạn theo bookingType/hasOpponent
            // Vì flow FIND_OPPONENT giữ booking A là SOLO/hasOpponent=false ngay cả khi match FULL
            // Ta chỉ cần lấy bookings theo ngày và filter theo matchId thuộc activeMatchIds
            val snapshot = firestore.collection(BOOKINGS_COLLECTION)
                .whereEqualTo("fieldId", fieldId)
                .whereEqualTo("date", date)
                .whereIn("status", listOf("PENDING", "CONFIRMED"))
                .get()
                .await()

            val allBookings = snapshot.toObjects(Booking::class.java)
            println("🔍 DEBUG: All candidate bookings found: ${allBookings.size}")
            allBookings.forEach { booking ->
                println("  - Booking ${booking.bookingId}: status=${booking.status}, matchId=${booking.matchId}")
            }

            val bookings = allBookings
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
                        val match = parseMatchSafe(matchSnap)
                        if (match != null) {
                            // Xóa renter đã hủy khỏi participants
                            val remainingParticipants = match.participants.filter { it.bookingId != bookingId }
                            
                            if (remainingParticipants.isEmpty()) {
                                // Nếu không còn participant nào, reset match về CANCELLED (màu trắng)
                                matchRef.update(
                                    mapOf(
                                        "status" to "CANCELLED",
                                        "occupiedCount" to 0,
                                        "participants" to emptyList<Any>(),
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                ).await()
                                println("🔄 DEBUG: Match ${current.matchId} reset to CANCELLED (no participants left) - WHITE color")
                            } else {
                                // Nếu còn 1 participant, chuyển về WAITING_OPPONENT (màu vàng)
                                matchRef.update(
                                    mapOf(
                                        "status" to "WAITING_OPPONENT",
                                        "occupiedCount" to 1,
                                        "participants" to remainingParticipants,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                ).await()
                                println("🔄 DEBUG: Match ${current.matchId} changed to WAITING_OPPONENT (1 participant left) - YELLOW color")
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
                        val match = parseMatchSafe(matchSnap)
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
                    val match = parseMatchSafe(document)
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
        println("🔍 DEBUG: listenMatchById called for matchId: $matchId")
        return firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ ERROR: listenMatchById error: ${e.message}")
                    onError(e)
                    return@addSnapshotListener
                }
                println("🔍 DEBUG: listenMatchById snapshot received:")
                println("  - exists: ${snapshot?.exists()}")
                println("  - hasPendingWrites: ${snapshot?.metadata?.hasPendingWrites()}")
                val match = if (snapshot != null && snapshot.exists()) parseMatchSafe(snapshot) else null
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
            
            // Cập nhật AI Profile cho cả 2 renter sau khi có match result mới
            try {
                val aiProfileRepo = AiProfileRepository()
                
                // Collect tất cả renter IDs cần cập nhật
                val renterIdsToUpdate = mutableSetOf<String>()
                
                if (matchResult.isDraw) {
                    // Nếu là draw, cả 2 renter đều tham gia
                    if (matchResult.winnerRenterId != null && matchResult.winnerRenterId.isNotBlank()) {
                        renterIdsToUpdate.add(matchResult.winnerRenterId)
                    }
                    if (matchResult.loserRenterId != null && matchResult.loserRenterId.isNotBlank()) {
                        renterIdsToUpdate.add(matchResult.loserRenterId)
                    }
                } else {
                    // Nếu không phải draw, cập nhật cho winner và loser
                    if (matchResult.winnerRenterId != null && matchResult.winnerRenterId.isNotBlank()) {
                        renterIdsToUpdate.add(matchResult.winnerRenterId)
                    }
                    if (matchResult.loserRenterId != null && matchResult.loserRenterId.isNotBlank()) {
                        renterIdsToUpdate.add(matchResult.loserRenterId)
                    }
                }
                
                // Cập nhật AI Profile cho mỗi renter (skill tổng thể + skill theo sân)
                renterIdsToUpdate.forEach { renterId ->
                    try {
                        // Skill tổng thể (fieldId = null)
                        aiProfileRepo.updateAiProfileFromMatchResult(renterId, null)
                        // Skill theo sân
                        if (matchResult.fieldId.isNotBlank()) {
                            aiProfileRepo.updateAiProfileFromMatchResult(renterId, matchResult.fieldId)
                        }
                    } catch (e: Exception) {
                        println("⚠️ WARN: Failed to update AI profile for renter $renterId: ${e.message}")
                    }
                }
                
                println("✅ DEBUG: Updated AI profiles for ${renterIdsToUpdate.size} renters after match result")
            } catch (e: Exception) {
                println("❌ ERROR: Failed to update AI profiles: ${e.message}")
                // Không fail toàn bộ saveMatchResult nếu update AI profile lỗi
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
