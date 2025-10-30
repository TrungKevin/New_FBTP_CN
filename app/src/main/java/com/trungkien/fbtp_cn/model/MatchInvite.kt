package com.trungkien.fbtp_cn.model

data class MatchInvite(
    val inviteId: String = "",           // id tự sinh
    val fromRenterId: String = "",       // người gửi
    val fromName: String = "",           // tên người gửi
    val fromPhone: String = "",          // SĐT người gửi
    val toRenterId: String = "",         // người nhận
    val fieldId: String = "",            // id sân
    val fieldName: String = "",          // tên sân
    val date: String = "",
    val timeRange: String = "",
    val note: String = "",
    val status: String = "pending",      // pending, accepted, rejected
    val timestamp: Long = System.currentTimeMillis()
)
