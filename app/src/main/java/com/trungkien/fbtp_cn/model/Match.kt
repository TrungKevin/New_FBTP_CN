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
    // ✅ NEW (preferred): notes[0] = note A, notes[1] = note B
    val notes: List<String?> = listOf(null, null),
    // ⛔ Firestore không hỗ trợ nested arrays -> dùng map theo side thay cho List<List<...>>
    // ✅ NEW (preferred): serviceLinesBySide["A"] = A, serviceLinesBySide["B"] = B
    val serviceLinesBySide: Map<String, List<ServiceLine>> = mapOf(
        "A" to emptyList(),
        "B" to emptyList()
    ),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis() // ✅ FIX: Thêm updatedAt field
)

@Keep
data class MatchParticipant(
    val bookingId: String = "",
    val renterId: String = "",
    val side: String = "A" // "A" | "B"
)
