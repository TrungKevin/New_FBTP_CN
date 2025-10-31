package com.trungkien.fbtp_cn.service

import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.MatchInvite
import kotlinx.coroutines.tasks.await

object MatchInviteNotificationService {
    // Gửi notification lời mời mới cho đối thủ
    suspend fun sendMatchInviteNotification(invite: MatchInvite) {
        val notif = hashMapOf(
            "toUserId" to invite.toRenterId,
            "type" to "MATCH_INVITE",
            "title" to "Lời mời thách đấu từ ${invite.fromName}",
            "body" to "Sân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}\nSĐT: ${invite.fromPhone}\nGhi chú: ${invite.note.ifBlank { "Không có" }}",
            "data" to mapOf(
                "fieldId" to invite.fieldId,
                "customData" to mapOf(
                    "inviteId" to invite.inviteId,
                    "fromRenterId" to invite.fromRenterId,
                    "toRenterId" to invite.toRenterId,
                    "fromName" to invite.fromName,
                    "fromPhone" to invite.fromPhone,
                    "date" to invite.date,
                    "timeRange" to invite.timeRange,
                    "note" to invite.note,
                    "fieldName" to invite.fieldName
                )
            ),
            "read" to false,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }

    // Gửi notification đã được xác nhận về cho người gửi
    suspend fun sendInviteAcceptNotification(invite: MatchInvite, accepterName: String) {
        val notif = hashMapOf(
            "toUserId" to invite.fromRenterId,
            "type" to "MATCH_ACCEPTED",
            "title" to "Đối thủ đã xác nhận thách đấu",
            "body" to "$accepterName đã xác nhận giao hữu với bạn.\nSân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}",
            "data" to mapOf(
                "fieldId" to invite.fieldId,
                "customData" to mapOf(
                    "inviteId" to invite.inviteId,
                    "fromRenterId" to invite.fromRenterId,
                    "toRenterId" to invite.toRenterId
                )
            ),
            "read" to false,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }

    // Gửi notification bị từ chối cho người gửi
    suspend fun sendInviteRejectNotification(invite: MatchInvite, rejecterName: String) {
        val notif = hashMapOf(
            "toUserId" to invite.fromRenterId,
            "type" to "MATCH_REJECTED",
            "title" to "Đối thủ từ chối lời mời giao hữu",
            "body" to "$rejecterName đã từ chối lời mời.\nSân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}",
            "data" to mapOf(
                "fieldId" to invite.fieldId,
                "customData" to mapOf(
                    "inviteId" to invite.inviteId,
                    "fromRenterId" to invite.fromRenterId,
                    "toRenterId" to invite.toRenterId
                )
            ),
            "read" to false,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }
}
