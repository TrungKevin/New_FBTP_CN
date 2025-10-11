package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Match(
    val rangeKey: String = "",
    val fieldId: String = "",
    val date: String = "",
    val startAt: String = "",
    val endAt: String = "",
    val capacity: Int = 2,
    val occupiedCount: Int = 0,
    val participants: List<MatchParticipant> = emptyList(),
    val price: Long = 0,
    val totalPrice: Long = 0,
    val status: String = "WAITING_OPPONENT", // "FREE" | "WAITING_OPPONENT" | "FULL"
    val matchType: String? = null, // "SINGLE" | "DOUBLE"
    val notes: String? = null, // Notes chung của trận đấu
    val noteA: String? = null, // Notes riêng của renter A
    val noteB: String? = null, // Notes riêng của renter B
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class MatchParticipant(
    val bookingId: String = "",
    val renterId: String = "",
    val side: String = "A" // "A" | "B"
)
