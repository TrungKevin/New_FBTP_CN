package com.trungkien.fbtp_cn.repository

import androidx.annotation.Keep
import com.trungkien.fbtp_cn.model.LeaderboardEntry
import kotlin.math.abs
import kotlin.math.exp

@Keep
data class OpponentSuggestion(
    val renterId: String,
    val score: Float,
    val pWin: Float,
    val source: String = "heuristic"
)

@Keep
data class OutcomeProbabilities(
    val pWin: Float,
    val pDraw: Float,
    val pLose: Float
)

/**
 * MVP AI Agent: đề xuất đối thủ dựa trên độ gần kỹ năng và một số ưu tiên cơ bản.
 * - score càng cao càng tốt
 * - pWin ~ xác suất thắng ước lượng dựa trên chênh lệch kỹ năng
 */
class AIAgent {
    fun suggestOpponents(
        meWeighted: Float?,
        candidates: List<LeaderboardEntry>,
        limit: Int = 5
    ): List<OpponentSuggestion> {
        if (candidates.isEmpty()) return emptyList()

        // Nếu thiếu kỹ năng của bản thân, lấy median trong candidates để ước lượng
        val my = meWeighted ?: candidates.map { it.weightedWinRate }.sorted().let { arr ->
            if (arr.isEmpty()) 0.5f else arr[arr.size / 2]
        }

        // Heuristic điểm: càng gần kỹ năng càng tốt; ưu tiên nhiều trận hơn chút
        val scored = candidates.map { e ->
            val diff = abs(my - e.weightedWinRate)
            val closeness = 1f - diff.coerceIn(0f, 1f)
            val volumeBoost = (e.totalMatches.coerceAtMost(50)) / 50f * 0.15f
            val score = 0.85f * closeness + volumeBoost

            // Logistic ước lượng pWin theo chênh lệch kỹ năng (k > 4 tạo dốc vừa phải)
            val k = 4f
            val z = (my - e.weightedWinRate) * k
            val pWin = (1f / (1f + exp((-z).toDouble()))).toFloat()

            OpponentSuggestion(renterId = e.renterId, score = score, pWin = pWin)
        }

        return scored.sortedByDescending { it.score }.take(limit)
    }

    /**
     * Ước lượng xác suất thắng/hòa/thua dựa trên chênh lệch kỹ năng.
     * pDraw tăng khi hai kỹ năng gần nhau (tối đa ~25%).
     */
    fun estimateOutcomeProbabilities(myWeighted: Float, opponentWeighted: Float): OutcomeProbabilities {
        val diff = kotlin.math.abs(myWeighted - opponentWeighted).coerceIn(0f, 1f)
        val closeness = 1f - diff
        val baseDraw = 0.05f
        val pDraw = (baseDraw + 0.20f * closeness).coerceIn(0f, 0.3f)

        val k = 4f
        val z = (myWeighted - opponentWeighted) * k
        val sigmoid = (1f / (1f + kotlin.math.exp((-z).toDouble()))).toFloat()
        val remain = (1f - pDraw).coerceIn(0f, 1f)
        val pWin = (sigmoid * remain).coerceIn(0f, 1f)
        val pLose = (remain - pWin).coerceIn(0f, 1f)
        return OutcomeProbabilities(pWin = pWin, pDraw = pDraw, pLose = pLose)
    }
}


