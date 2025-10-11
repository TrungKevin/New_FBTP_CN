package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class MatchResult(
    val resultId: String = "",
    val matchId: String = "", // rangeKey của Match
    val fieldId: String = "",
    val date: String = "",
    val startAt: String = "",
    val endAt: String = "",
    
    // Thông tin đội thắng
    val winnerSide: String? = null, // "A" hoặc "B"
    val winnerRenterId: String? = null,
    val winnerName: String? = null,
    val winnerPhone: String? = null,
    val winnerEmail: String? = null,
    
    // Thông tin đội thua
    val loserSide: String? = null, // "A" hoặc "B" 
    val loserRenterId: String? = null,
    val loserName: String? = null,
    val loserPhone: String? = null,
    val loserEmail: String? = null,
    
    // Thông tin trận đấu
    val matchType: String? = null, // "SINGLE" | "DOUBLE"
    val totalPrice: Long = 0,
    val notes: String? = null,
    
    // Tỉ số trận đấu
    val renterAScore: Int = 0, // Số bàn thắng của renter A
    val renterBScore: Int = 0, // Số bàn thắng của renter B
    val isDraw: Boolean = false, // Trạng thái hòa
    
    // Metadata
    val recordedAt: Long = System.currentTimeMillis(),
    val recordedBy: String = "", // userId của owner ghi kết quả
    val isVerified: Boolean = false // Có thể dùng để xác thực kết quả sau này
)
