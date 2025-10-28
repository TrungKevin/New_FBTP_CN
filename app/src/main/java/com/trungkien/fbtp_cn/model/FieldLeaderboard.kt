package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class LeaderboardEntry(
    val renterId: String = "",
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val totalMatches: Int = 0,
    val winPercent: Float = 0f,
    val rank: Int = 0
)

@Keep
data class FieldLeaderboard(
    val fieldId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val entries: List<LeaderboardEntry> = emptyList()
)



