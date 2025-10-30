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
            "inviteId" to invite.inviteId,
            "title" to "Lời mời thách đấu từ ${invite.fromName}",
            "content" to "Sân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}\nSĐT: ${invite.fromPhone}\nGhi chú: ${invite.note}",
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }

    // Gửi notification đã được xác nhận về cho người gửi
    suspend fun sendInviteAcceptNotification(invite: MatchInvite, accepterName: String) {
        val notif = hashMapOf(
            "toUserId" to invite.fromRenterId,
            "type" to "MATCH_ACCEPTED",
            "inviteId" to invite.inviteId,
            "title" to "Đối thủ đã xác nhận thách đấu",
            "content" to "${accepterName} đã xác nhận giao hữu với bạn.\nSân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}",
            "status" to "accepted",
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }

    // Gửi notification bị từ chối cho người gửi
    suspend fun sendInviteRejectNotification(invite: MatchInvite, rejecterName: String) {
        val notif = hashMapOf(
            "toUserId" to invite.fromRenterId,
            "type" to "MATCH_REJECTED",
            "inviteId" to invite.inviteId,
            "title" to "Đối thủ từ chối lời mời giao hữu",
            "content" to "${rejecterName} đã từ chối lời mời.\nSân: ${invite.fieldName}\nNgày: ${invite.date}\nGiờ: ${invite.timeRange}",
            "status" to "rejected",
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("notifications").add(notif).await()
    }
}
