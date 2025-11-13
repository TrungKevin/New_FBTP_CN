package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.model.MatchParticipant
import com.trungkien.fbtp_cn.model.ServiceLine
import kotlinx.coroutines.tasks.await

data class OwnerStats(
    val totalRevenue: Long,
    val totalBookings: Int,
    val totalCancelled: Int,
    val chartRevenueByBucket: List<Long>,
    val chartBookingsByBucket: List<Int>,
    val topFields: List<TopFieldStat>
)

data class TopFieldStat(
    val fieldId: String,
    val name: String,
    val avgRating: Double,
    val bookings: Int,
    val revenue: Long
)

class OwnerStatsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val BOOKINGS = "bookings"
    private val MATCHES = "matches"
    private val FIELDS = "fields"
    private val REVIEWS = "reviews"

    suspend fun loadStats(
        ownerId: String,
        startDate: String, // yyyy-MM-dd
        endDate: String,   // yyyy-MM-dd (inclusive)
        bucket: Bucket
    ): Result<OwnerStats> {
        return try {
            // 1) Lấy tất cả bookings thuộc owner (lọc theo ngày ở client để tránh yêu cầu composite index)
            val bookingsSnap = firestore.collection(BOOKINGS)
                .whereEqualTo("ownerId", ownerId)
                .get().await()
            val bookingsAll = bookingsSnap.toObjects(Booking::class.java)
            val bookings = bookingsAll.filter { b ->
                val d = runCatching { java.time.LocalDate.parse(b.date) }.getOrNull()
                d != null && !d.isBefore(java.time.LocalDate.parse(startDate)) && !d.isAfter(java.time.LocalDate.parse(endDate))
            }
            println("STATS DEBUG: ownerId=$ownerId range=$startDate..$endDate totalAll=${bookingsAll.size} afterFilter=${bookings.size}")

            // 2) Lấy tất cả fields của owner để lấy fieldIds
            val fieldsSnap = firestore.collection(FIELDS)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val fields = fieldsSnap.toObjects(Field::class.java)
            val fieldIds = fields.map { it.fieldId }
            println("STATS DEBUG: Found ${fieldIds.size} fields for owner")

            // 3) Lấy matches của owner (qua fieldIds) trong khoảng thời gian
            val matchesAll = mutableListOf<Match>()
            if (fieldIds.isNotEmpty()) {
                // Lấy matches theo từng fieldId (tránh composite index)
                fieldIds.forEach { fieldId ->
                    try {
                        val matchesSnap = firestore.collection(MATCHES)
                            .whereEqualTo("fieldId", fieldId)
                            .get()
                            .await()
                        // Parse matches an toàn với parseMatchSafe
                        matchesSnap.documents.forEach { doc ->
                            val match = parseMatchSafe(doc)
                            if (match != null) {
                                val matchDate = runCatching { java.time.LocalDate.parse(match.date) }.getOrNull()
                                if (matchDate != null && 
                                    !matchDate.isBefore(java.time.LocalDate.parse(startDate)) && 
                                    !matchDate.isAfter(java.time.LocalDate.parse(endDate))) {
                                    matchesAll.add(match)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("⚠️ WARNING: Failed to load matches for field $fieldId: ${e.message}")
                    }
                }
            }
            println("STATS DEBUG: Found ${matchesAll.size} matches in range")

            // 4) Tính tổng đặt, tổng hủy
            val totalBookings = bookings.size
            val totalCancelled = bookings.count { it.status.equals("CANCELLED", true) }

            // 5) Doanh thu từ bookings: chỉ tính các booking đã được owner xác nhận (CONFIRMED/PAID) và đã kết thúc
            // ✅ Lưu ý: Không tính booking A nếu booking A thuộc về match (có matchId) để tránh trùng lặp
            val matchIds = matchesAll.map { it.rangeKey }.toSet()
            val revenueFromBookings = bookings.asSequence()
                .filter { it.isFinishedRevenue() }
                .filter { booking -> 
                    // Bỏ qua booking nếu booking thuộc về match (để tránh tính trùng)
                    // Match.totalPrice đã bao gồm cả basePrice và servicePrice của cả 2 renter
                    booking.matchId.isNullOrEmpty() || !matchIds.contains(booking.matchId)
                }
                .sumOf { it.totalPrice }

            // 6) Doanh thu từ matches: chỉ tính các match đã được owner xác nhận (CONFIRMED/FULL) và đã kết thúc
            // ✅ Match.totalPrice đã bao gồm cả basePrice và servicePrice của cả renter A và renter B
            val revenueFromMatches = matchesAll.asSequence()
                .filter { it.isFinishedMatchRevenue() }
                .sumOf { it.totalPrice }

            // 7) Tổng doanh thu = bookings (không thuộc match) + matches
            val finishedRevenue = revenueFromBookings + revenueFromMatches
            println("STATS DEBUG: Revenue breakdown - Bookings: $revenueFromBookings, Matches: $revenueFromMatches, Total: $finishedRevenue")

            // 8) Bucket hóa dữ liệu hiển thị biểu đồ (bao gồm cả bookings và matches)
            val (revBuckets, bookBuckets) = bucketizeByRange(bookings, matchesAll, startDate, endDate, bucket)

            // 9) Top fields theo rating >= 3.5 (tính doanh thu từ cả bookings và matches)
            // ✅ Note: fields đã được lấy ở bước 2, không cần lấy lại
            val ratingByField = computeAvgRatingByField()
            val top = fields.map { f ->
                val fieldBookings = bookings.filter { it.fieldId == f.fieldId }
                val fieldMatches = matchesAll.filter { it.fieldId == f.fieldId }
                val fieldMatchIds = fieldMatches.map { it.rangeKey }.toSet()
                
                // Doanh thu từ bookings của field này (không thuộc match để tránh trùng lặp)
                val revenueFromBookings = fieldBookings
                    .filter { it.isFinishedRevenue() }
                    .filter { booking -> 
                        booking.matchId.isNullOrEmpty() || !fieldMatchIds.contains(booking.matchId)
                    }
                    .sumOf { it.totalPrice }
                
                // Doanh thu từ matches của field này
                val revenueFromMatches = fieldMatches
                    .filter { it.isFinishedMatchRevenue() }
                    .sumOf { it.totalPrice }
                
                val revenue = revenueFromBookings + revenueFromMatches
                
                TopFieldStat(
                    fieldId = f.fieldId,
                    name = f.name,
                    avgRating = ratingByField[f.fieldId] ?: 0.0,
                    bookings = fieldBookings.size,
                    revenue = revenue
                )
            }
                .filter { it.avgRating >= 3.5 }
                .sortedByDescending { it.revenue }
                .take(5)

            Result.success(
                OwnerStats(
                    totalRevenue = finishedRevenue,
                    totalBookings = totalBookings,
                    totalCancelled = totalCancelled,
                    chartRevenueByBucket = revBuckets,
                    chartBookingsByBucket = bookBuckets,
                    topFields = top
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun computeAvgRatingByField(): Map<String, Double> {
        // reviews: renterId, fieldId, rating
        val reviewsSnap = firestore.collection(REVIEWS).get().await()
        val byField = mutableMapOf<String, MutableList<Double>>()
        reviewsSnap.documents.forEach { d ->
            val fieldId = d.getString("fieldId") ?: return@forEach
            val rating = (d.getLong("rating") ?: 0L).toDouble()
            byField.getOrPut(fieldId) { mutableListOf() }.add(rating)
        }
        return byField.mapValues { (_, list) -> if (list.isEmpty()) 0.0 else list.sum() / list.size }
    }

    enum class Bucket { HOURLY, DAILY, MONTHLY }

    /**
     * ✅ Helper function: Parse Match từ DocumentSnapshot an toàn (xử lý legacy data)
     */
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
     * ✅ Helper function: Convert raw data to ServiceLine list
     */
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

    /**
     * ✅ Helper function: Kiểm tra match có được tính vào doanh thu không
     * Điều kiện:
     * 1. Status phải là CONFIRMED hoặc FULL (đã được owner xác nhận)
     * 2. Match phải đã kết thúc (date < today hoặc date == today && endAt < now)
     */
    private fun Match.isFinishedMatchRevenue(): Boolean {
        // 1. Kiểm tra status - phải được owner xác nhận
        val statusOk = this.status.equals("CONFIRMED", true) || this.status.equals("FULL", true)
        if (!statusOk) return false
        
        // 2. Kiểm tra match đã kết thúc chưa
        val matchDate = runCatching { java.time.LocalDate.parse(this.date) }.getOrNull()
        if (matchDate == null) return false
        
        val today = java.time.LocalDate.now()
        val now = java.time.LocalTime.now()
        
        return when {
            matchDate.isBefore(today) -> true // Đã qua ngày -> đã kết thúc
            matchDate == today -> {
                // Cùng ngày: kiểm tra endAt
                val endTime = runCatching {
                    val parts = this.endAt.split(":")
                    java.time.LocalTime.of(parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0)
                }.getOrNull()
                endTime != null && endTime.isBefore(now) // Đã qua thời gian kết thúc
            }
            else -> false // Chưa đến ngày -> chưa kết thúc
        }
    }

    /**
     * ✅ Helper function: Kiểm tra booking có được tính vào doanh thu không
     * Điều kiện:
     * 1. Status phải là CONFIRMED hoặc PAID (đã được owner xác nhận)
     * 2. Booking phải đã kết thúc (date < today hoặc date == today && endAt < now)
     */
    private fun Booking.isFinishedRevenue(): Boolean {
        // 1. Kiểm tra status - phải được owner xác nhận
        val statusOk = this.status.equals("CONFIRMED", true) || this.status.equals("PAID", true)
        if (!statusOk) return false
        
        // 2. Kiểm tra booking đã kết thúc chưa
        val bookingDate = runCatching { java.time.LocalDate.parse(this.date) }.getOrNull()
        if (bookingDate == null) return false
        
        val today = java.time.LocalDate.now()
        val now = java.time.LocalTime.now()
        
        return when {
            bookingDate.isBefore(today) -> true // Đã qua ngày -> đã kết thúc
            bookingDate == today -> {
                // Cùng ngày: kiểm tra endAt
                val endTime = runCatching {
                    val parts = this.endAt.split(":")
                    java.time.LocalTime.of(parts[0].toInt(), parts.getOrNull(1)?.toInt() ?: 0)
                }.getOrNull()
                endTime != null && endTime.isBefore(now) // Đã qua thời gian kết thúc
            }
            else -> false // Chưa đến ngày -> chưa kết thúc
        }
    }

    private fun bucketizeByRange(
        bookings: List<Booking>,
        matches: List<Match>,
        startDate: String,
        endDate: String,
        bucket: Bucket
    ): Pair<List<Long>, List<Int>> {
        return when (bucket) {
            Bucket.HOURLY -> {
                // 12 cột (ví dụ từ 08h đến 20h)
                val rev = MutableList(12) { 0L }
                val cnt = MutableList(12) { 0 }
                
                val matchIds = matches.map { it.rangeKey }.toSet()
                
                // Tính từ bookings (không thuộc match để tránh trùng lặp)
                bookings.forEach { b ->
                    val hour = b.startAt.substring(0, 2).toIntOrNull() ?: 0
                    val idx = (hour - 8).coerceIn(0, 11)
                    cnt[idx] = cnt[idx] + 1
                    
                    if (b.isFinishedRevenue() && (b.matchId.isNullOrEmpty() || !matchIds.contains(b.matchId))) {
                        rev[idx] = rev[idx] + b.totalPrice
                    }
                }
                
                // Tính từ matches
                matches.forEach { m ->
                    val hour = m.startAt.substring(0, 2).toIntOrNull() ?: 0
                    val idx = (hour - 8).coerceIn(0, 11)
                    // Không tăng cnt vì match đã được tính trong booking
                    
                    if (m.isFinishedMatchRevenue()) {
                        rev[idx] = rev[idx] + m.totalPrice
                    }
                }
                
                Pair(rev, cnt)
            }
            Bucket.DAILY -> {
                val start = java.time.LocalDate.parse(startDate)
                val end = java.time.LocalDate.parse(endDate)
                val days = java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1
                val rev = MutableList(days) { 0L }
                val cnt = MutableList(days) { 0 }
                
                val matchIds = matches.map { it.rangeKey }.toSet()
                
                // Tính từ bookings (không thuộc match để tránh trùng lặp)
                bookings.forEach { b ->
                    val d = java.time.LocalDate.parse(b.date)
                    val idx = java.time.temporal.ChronoUnit.DAYS.between(start, d).toInt().coerceIn(0, days - 1)
                    cnt[idx] = cnt[idx] + 1
                    
                    if (b.isFinishedRevenue() && (b.matchId.isNullOrEmpty() || !matchIds.contains(b.matchId))) {
                        rev[idx] = rev[idx] + b.totalPrice
                    }
                }
                
                // Tính từ matches
                matches.forEach { m ->
                    val d = java.time.LocalDate.parse(m.date)
                    val idx = java.time.temporal.ChronoUnit.DAYS.between(start, d).toInt().coerceIn(0, days - 1)
                    // Không tăng cnt vì match đã được tính trong booking
                    
                    if (m.isFinishedMatchRevenue()) {
                        rev[idx] = rev[idx] + m.totalPrice
                    }
                }
                
                Pair(rev, cnt)
            }
            Bucket.MONTHLY -> {
                val rev = MutableList(12) { 0L }
                val cnt = MutableList(12) { 0 }
                
                val matchIds = matches.map { it.rangeKey }.toSet()
                
                // Tính từ bookings (không thuộc match để tránh trùng lặp)
                bookings.forEach { b ->
                    val monthIdx = b.date.substring(5, 7).toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                    cnt[monthIdx] = cnt[monthIdx] + 1
                    
                    if (b.isFinishedRevenue() && (b.matchId.isNullOrEmpty() || !matchIds.contains(b.matchId))) {
                        rev[monthIdx] = rev[monthIdx] + b.totalPrice
                    }
                }
                
                // Tính từ matches
                matches.forEach { m ->
                    val monthIdx = m.date.substring(5, 7).toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                    // Không tăng cnt vì match đã được tính trong booking
                    
                    if (m.isFinishedMatchRevenue()) {
                        rev[monthIdx] = rev[monthIdx] + m.totalPrice
                    }
                }
                
                Pair(rev, cnt)
            }
        }
    }
}


