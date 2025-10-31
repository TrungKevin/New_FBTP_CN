package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.trungkien.fbtp_cn.model.MatchInvite
import com.trungkien.fbtp_cn.service.MatchInviteNotificationService

data class SlotSuggestion(
    val facilityId: String,
    val courtId: String,
    val date: String,
    val timeRange: String,
    val distanceKm: Double = 0.0
)

sealed class MatchRequestResult {
    object Booked : MatchRequestResult()
    data class NeedAlternative(val suggestions: List<SlotSuggestion>) : MatchRequestResult()
    data class Error(val message: String) : MatchRequestResult()
}

class MatchRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TIME_SLOTS = "TIME_SLOT"
    private val BOOKINGS = "BOOKING"
    private val REQUESTS = "MATCH_REQUEST"

    suspend fun sendMatchRequest(
        renterAId: String,
        renterBId: String,
        facilityId: String,
        courtId: String,
        date: String,
        timeRange: String
    ): MatchRequestResult {
        return try {
            val slotQuery = firestore.collection(TIME_SLOTS)
                .whereEqualTo("facilityId", facilityId)
                .whereEqualTo("courtId", courtId)
                .whereEqualTo("date", date)
                .whereEqualTo("timeRange", timeRange)
                .get().await()

            if (slotQuery.isEmpty) {
                return MatchRequestResult.Error("Không tìm thấy slot")
            }
            val slotDoc = slotQuery.documents.first()
            val status = slotDoc.getString("status") ?: "available"

            // Lưu yêu cầu
            firestore.collection(REQUESTS).add(
                hashMapOf(
                    "renterA" to renterAId,
                    "renterB" to renterBId,
                    "facilityId" to facilityId,
                    "courtId" to courtId,
                    "date" to date,
                    "timeRange" to timeRange,
                    "status" to if (status == "available") "accepted" else "pending"
                )
            ).await()

            if (status == "available") {
                firestore.collection(BOOKINGS).add(
                    hashMapOf(
                        "facilityId" to facilityId,
                        "courtId" to courtId,
                        "date" to date,
                        "timeRange" to timeRange,
                        "renterA" to renterAId,
                        "renterB" to renterBId,
                        "status" to "confirmed"
                    )
                ).await()
                firestore.collection(TIME_SLOTS).document(slotDoc.id)
                    .update("status", "booked").await()
                MatchRequestResult.Booked
            } else {
                val suggestions = suggestAlternatives(facilityId, date, timeRange)
                MatchRequestResult.NeedAlternative(suggestions)
            }
        } catch (e: Exception) {
            MatchRequestResult.Error(e.message ?: "Gửi lời mời thất bại")
        }
    }

    suspend fun acceptInvite(inviteId: String, accepterName: String): Result<Unit> {
        return try {
            val docRef = FirebaseFirestore.getInstance().collection("match_invites").document(inviteId)
            val snap = docRef.get().await()
            val invite = snap.toObject(com.trungkien.fbtp_cn.model.MatchInvite::class.java)
                ?: return Result.failure(IllegalStateException("Invite not found"))
            docRef.update("status", "accepted").await()
            MatchInviteNotificationService.sendInviteAcceptNotification(invite, accepterName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectInvite(inviteId: String, rejecterName: String): Result<Unit> {
        return try {
            val docRef = FirebaseFirestore.getInstance().collection("match_invites").document(inviteId)
            val snap = docRef.get().await()
            val invite = snap.toObject(com.trungkien.fbtp_cn.model.MatchInvite::class.java)
                ?: return Result.failure(IllegalStateException("Invite not found"))
            docRef.update("status", "rejected").await()
            MatchInviteNotificationService.sendInviteRejectNotification(invite, rejecterName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMatchRequestFull(
        renterAId: String,
        renterBId: String,
        facilityId: String,
        courtId: String,
        fieldName: String,
        date: String,
        timeRange: String,
        phone: String,
        note: String
    ): MatchRequestResult {
        return try {
            fun parseMinutes(range: String): Pair<Int, Int>? {
                val parts = range.split("-").map { it.trim() }
                if (parts.size != 2) return null
                fun toMin(hhmm: String): Int {
                    val p = hhmm.split(":").map { it.trim() }
                    val h = p.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
                    val m = p.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                    return h * 60 + m
                }
                val start = toMin(parts[0])
                val end = toMin(parts[1])
                return if (end > start) start to end else null
            }
            fun isOverlap(a: String, b: String): Boolean {
                val ma = parseMinutes(a) ?: return false
                val mb = parseMinutes(b) ?: return false
                val (aStart, aEnd) = ma
                val (bStart, bEnd) = mb
                return aStart < bEnd && bStart < aEnd
            }
            // 1) Kiểm tra khung giờ đã có người đặt chưa (bookings)
            val occupiedStatuses = listOf("PENDING", "CONFIRMED", "PAID", "BOOKED", "ACCEPTED")
            val bookingSnap = try {
                // Tránh lỗi index: chỉ filter theo fieldId + date, còn status lọc trong bộ nhớ
                FirebaseFirestore.getInstance().collection("bookings")
                    .whereEqualTo("fieldId", facilityId)
                    .whereEqualTo("date", date)
                    .get().await()
            } catch (_: Exception) { null }
            if (bookingSnap != null) {
                val hasOverlap = bookingSnap.documents.any { d ->
                    val tr = d.getString("timeRange") ?: return@any false
                    val st = d.getString("status") ?: ""
                    occupiedStatuses.contains(st) && isOverlap(tr, timeRange)
                }
                if (hasOverlap) return MatchRequestResult.Error("Khung giờ này đã có người đặt")
            }

            // 2) Kiểm tra thêm trong slots (nếu hệ thống dùng slots)
            val slotSnap = try {
                FirebaseFirestore.getInstance().collection("slots")
                    .whereEqualTo("fieldId", facilityId)
                    .whereEqualTo("date", date)
                    .whereEqualTo("status", "booked")
                    .get().await()
            } catch (_: Exception) { null }
            if (slotSnap != null) {
                val hasOverlapSlot = slotSnap.documents.any { d ->
                    val tr = d.getString("timeRange") ?: return@any false
                    isOverlap(tr, timeRange)
                }
                if (hasOverlapSlot) return MatchRequestResult.Error("Khung giờ này đã có người đặt")
            }

            val inviteId = FirebaseFirestore.getInstance().collection("match_invites").document().id
            val matchInvite = MatchInvite(
                inviteId = inviteId,
                fromRenterId = renterAId,
                fromName = "", // TODO: lấy tên user hiện tại (bổ sung nếu profile available)
                fromPhone = phone,
                toRenterId = renterBId,
                fieldId = facilityId,
                fieldName = fieldName,
                date = date,
                timeRange = timeRange,
                note = note,
                status = "pending"
            )
            FirebaseFirestore.getInstance().collection("match_invites").document(inviteId).set(matchInvite).await()
            // Gửi notification
            MatchInviteNotificationService.sendMatchInviteNotification(matchInvite)
            MatchRequestResult.Booked
        } catch (e: Exception) {
            MatchRequestResult.Error(e.message ?: "Gửi lời mời thất bại")
        }
    }

    private suspend fun suggestAlternatives(
        facilityId: String,
        date: String,
        requestedTime: String
    ): List<SlotSuggestion> {
        return try {
            val nearby = firestore.collection(TIME_SLOTS)
                .whereEqualTo("facilityId", facilityId)
                .whereEqualTo("date", date)
                .whereEqualTo("status", "available")
                .get().await()
                .documents
                .mapNotNull { d ->
                    SlotSuggestion(
                        facilityId = d.getString("facilityId") ?: facilityId,
                        courtId = d.getString("courtId") ?: "",
                        date = d.getString("date") ?: date,
                        timeRange = d.getString("timeRange") ?: "",
                        distanceKm = 0.0
                    )
                }
            // Đơn giản: ưu tiên khung giờ gần requestedTime
            nearby.sortedBy { timeDiff(it.timeRange, requestedTime) }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun timeDiff(a: String, b: String): Int {
        fun parse(t: String): Int {
            val start = t.split("-").firstOrNull() ?: return 0
            val parts = start.split(":").mapNotNull { it.toIntOrNull() }
            return parts.getOrNull(0)?.times(60)?.plus(parts.getOrNull(1) ?: 0) ?: 0
        }
        return kotlin.math.abs(parse(a) - parse(b))
    }
}


