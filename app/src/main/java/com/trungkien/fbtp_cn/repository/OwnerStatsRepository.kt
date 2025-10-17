package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.Match
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

            // 2) Lấy matches để biết cái nào còn hiệu lực (xác nhận) — phục vụ doanh thu kết thúc
            val matchesSnap = firestore.collection(MATCHES)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get().await()
            val matches = matchesSnap.toObjects(Match::class.java)
                .associateBy { it.rangeKey }

            // 3) Tính tổng đặt, tổng hủy
            val totalBookings = bookings.size
            val totalCancelled = bookings.count { it.status.equals("CANCELLED", true) }

            // 4) Doanh thu: chỉ tính các booking đã kết thúc trong khoảng ngày và đã được owner xác nhận
            // Điều kiện: status CONFIRMED/PAID và thời gian (date < today) hoặc (date == today và endAt < now)
            // Ở đây đơn giản hóa theo ngày: coi các booking có status CONFIRMED/PAID trong range là hoàn tất
            val finishedRevenue = bookings.asSequence()
                .filter { it.status.equals("CONFIRMED", true) || it.status.equals("PAID", true) }
                .sumOf { it.totalPrice }

            // 5) Bucket hóa dữ liệu hiển thị biểu đồ
            val (revBuckets, bookBuckets) = bucketizeByRange(bookings, startDate, endDate, bucket)

            // 6) Top fields theo rating >= 3.5
            val fieldsSnap = firestore.collection(FIELDS)
                .whereEqualTo("ownerId", ownerId)
                .get().await()
            val fields = fieldsSnap.toObjects(Field::class.java)
            val ratingByField = computeAvgRatingByField()
            val top = fields.map { f ->
                val fieldBookings = bookings.filter { it.fieldId == f.fieldId }
                val revenue = fieldBookings.filter { it.status.equals("CONFIRMED", true) || it.status.equals("PAID", true) }
                    .sumOf { it.totalPrice }
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

    private fun bucketizeByRange(
        bookings: List<Booking>,
        startDate: String,
        endDate: String,
        bucket: Bucket
    ): Pair<List<Long>, List<Int>> {
        return when (bucket) {
            Bucket.HOURLY -> {
                // 12 cột (ví dụ từ 08h đến 20h)
                val rev = MutableList(12) { 0L }
                val cnt = MutableList(12) { 0 }
                bookings.forEach { b ->
                    val hour = b.startAt.substring(0, 2).toIntOrNull() ?: 0
                    val idx = (hour - 8).coerceIn(0, 11)
                    cnt[idx] = cnt[idx] + 1
                    if (b.status.equals("CONFIRMED", true) || b.status.equals("PAID", true)) rev[idx] = rev[idx] + b.totalPrice
                }
                Pair(rev, cnt)
            }
            Bucket.DAILY -> {
                val start = java.time.LocalDate.parse(startDate)
                val end = java.time.LocalDate.parse(endDate)
                val days = java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1
                val rev = MutableList(days) { 0L }
                val cnt = MutableList(days) { 0 }
                bookings.forEach { b ->
                    val d = java.time.LocalDate.parse(b.date)
                    val idx = java.time.temporal.ChronoUnit.DAYS.between(start, d).toInt().coerceIn(0, days - 1)
                    cnt[idx] = cnt[idx] + 1
                    if (b.status.equals("CONFIRMED", true) || b.status.equals("PAID", true)) rev[idx] = rev[idx] + b.totalPrice
                }
                Pair(rev, cnt)
            }
            Bucket.MONTHLY -> {
                val rev = MutableList(12) { 0L }
                val cnt = MutableList(12) { 0 }
                bookings.forEach { b ->
                    val monthIdx = b.date.substring(5, 7).toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                    cnt[monthIdx] = cnt[monthIdx] + 1
                    if (b.status.equals("CONFIRMED", true) || b.status.equals("PAID", true)) rev[monthIdx] = rev[monthIdx] + b.totalPrice
                }
                Pair(rev, cnt)
            }
        }
    }
}


