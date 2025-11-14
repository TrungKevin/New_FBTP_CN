package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

/**
 * AI Profile cho renter - lưu skill và phong độ gần đây
 * Sprint 1 MVP: Minimal version với skill và formRecent
 * 
 * Mục đích:
 * - skill: weightedWinRate đã tính sẵn để AI Agent sử dụng nhanh
 * - formRecent: phong độ 5 trận gần nhất để hiển thị trên UI
 */
@Keep
data class AiProfile(
    val renterId: String = "", // ID của renter
    val fieldId: String? = null, // ID sân (null = skill tổng thể, có giá trị = skill thúyeo sân)
    
    /**
     * Skill level: weightedWinRate đã tính sẵn
     * Công thức: winRate * (N / (N + C)) với C = 10
     * Giá trị: 0.0 - 1.0
     */
    val skill: Float = 0f,
    
    /**
     * Phong độ gần đây: 5 trận gần nhất
     * Format: "W-W-L-W-D" (W = Win, L = Loss, D = Draw)
     * Hoặc danh sách: ["W", "W", "L", "W", "D"]
     */
    val formRecent: List<String> = emptyList(), // ["W", "W", "L", "W", "D"]
    
    /**
     * Tóm tắt phong độ (tính từ formRecent)
     */
    val recentWins: Int = 0, // Số trận thắng trong 5 trận gần nhất
    val recentTotal: Int = 0, // Tổng số trận gần nhất (có thể < 5)
    
    /**
     * Metadata
     */
    val lastMatchAt: Long? = null, // Timestamp trận đấu gần nhất
    val updatedAt: Long = System.currentTimeMillis(), // Thời điểm cập nhật profile
    val version: Int = 1 // Version của profile (để migrate sau này)
) {
    /**
     * Tính win rate của phong độ gần đây
     */
    fun recentWinRate(): Float {
        return if (recentTotal > 0) recentWins.toFloat() / recentTotal.toFloat() else 0f
    }
    
    /**
     * Lấy chuỗi formRecent dạng "W-W-L-W-D"
     */
    fun formRecentString(): String {
        return formRecent.joinToString("-")
    }
    
    /**
     * Đánh giá phong độ (Good, Average, Poor)
     */
    fun formRating(): String {
        if (recentTotal == 0) return "N/A"
        val winRate = recentWinRate()
        return when {
            winRate >= 0.7f -> "Tốt"
            winRate >= 0.4f -> "Ổn định"
            else -> "Cần cải thiện"
        }
    }
}
