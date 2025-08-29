package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Match(
    val rangeKey: String,
    val fieldId: String,
    val date: String,
    val startAt: String,
    val endAt: String,
    val capacity: Int = 2,
    val occupiedCount: Int = 0,
    val participants: List<MatchParticipant> = emptyList(),
    val price: Long,
    val totalPrice: Long,
    val status: String, // "FREE" | "WAITING_OPPONENT" | "FULL"
    val matchType: String? = null, // "SINGLE" | "DOUBLE"
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class MatchParticipant(
    val bookingId: String,
    val renterId: String,
    val side: String // "A" | "B"
)
