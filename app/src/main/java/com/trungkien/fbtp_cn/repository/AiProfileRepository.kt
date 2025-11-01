package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.AiProfile
import com.trungkien.fbtp_cn.model.MatchResult
import kotlinx.coroutines.tasks.await

/**
 * Repository để quản lý AI Profiles
 * Tự động cập nhật skill và formRecent khi có match result mới
 */
class AiProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val AI_PROFILES_COLLECTION = "ai_profiles"
    private val MATCH_RESULTS_COLLECTION = "match_results"
    private val C = 10f // Hằng số điều chỉnh weightedWinRate

    /**
     * Cập nhật AI Profile cho renter sau khi có match result mới
     * Gọi từ saveMatchResult() sau khi lưu match result thành công
     */
    suspend fun updateAiProfileFromMatchResult(
        renterId: String,
        fieldId: String? = null // null = skill tổng thể, có giá trị = skill theo sân
    ): Result<Unit> {
        return try {
            println("🔄 DEBUG: AiProfileRepository - Updating AI profile for renter: $renterId, fieldId: $fieldId")

            // 1. Lấy tất cả match_results của renter này (có thể filter theo fieldId)
            val matches = getMatchResultsForRenter(renterId, fieldId)

            // 2. Tính skill (weightedWinRate)
            val skill = calculateSkill(matches, renterId)

            // 3. Lấy 5 trận gần nhất và tạo formRecent
            val recent5 = matches.sortedByDescending { it.recordedAt }.take(5)
            val formRecent = recent5.map { match ->
                when {
                    match.isDraw -> "D"
                    match.winnerRenterId == renterId -> "W"
                    match.loserRenterId == renterId -> "L"
                    else -> "?" // Unknown (shouldn't happen)
                }
            }.reversed() // Trận gần nhất ở cuối

            val recentWins = formRecent.count { it == "W" }
            val recentTotal = formRecent.size
            val lastMatchAt = recent5.firstOrNull()?.recordedAt

            // 4. Tạo hoặc cập nhật AiProfile
            val profile = AiProfile(
                renterId = renterId,
                fieldId = fieldId,
                skill = skill,
                formRecent = formRecent,
                recentWins = recentWins,
                recentTotal = recentTotal,
                lastMatchAt = lastMatchAt,
                updatedAt = System.currentTimeMillis(),
                version = 1
            )

            // 5. Lưu vào Firestore (document ID = renterId hoặc renterId_fieldId)
            val docId = if (fieldId != null) "${renterId}_$fieldId" else renterId
            firestore.collection(AI_PROFILES_COLLECTION)
                .document(docId)
                .set(profile)
                .await()

            println("✅ DEBUG: AiProfileRepository - Updated AI profile for renter: $renterId")
            println("   - Skill: $skill")
            println("   - FormRecent: ${profile.formRecentString()}")
            println("   - RecentWins: $recentWins/$recentTotal")

            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: AiProfileRepository - Failed to update AI profile: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Lấy tất cả match_results của renter (có thể filter theo fieldId)
     */
    private suspend fun getMatchResultsForRenter(
        renterId: String,
        fieldId: String?
    ): List<MatchResult> {
        return try {
            // Lấy match_results mà renter là winner
            val winnerQuery = firestore.collection(MATCH_RESULTS_COLLECTION)
                .whereEqualTo("winnerRenterId", renterId)
            val winnerSnap = if (fieldId != null) {
                winnerQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                winnerQuery.get().await()
            }

            // Lấy match_results mà renter là loser
            val loserQuery = firestore.collection(MATCH_RESULTS_COLLECTION)
                .whereEqualTo("loserRenterId", renterId)
            val loserSnap = if (fieldId != null) {
                loserQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                loserQuery.get().await()
            }

            // Lấy match_results mà renter có draw (cần check cả 2 side)
            val drawQuery = firestore.collection(MATCH_RESULTS_COLLECTION)
                .whereEqualTo("isDraw", true)
            val drawSnap = if (fieldId != null) {
                drawQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                drawQuery.get().await()
            }

            // Combine và filter (có thể có duplicate nếu renter là cả winner và loser trong cùng trận)
            val allResults = mutableListOf<MatchResult>()
            
            winnerSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(MatchResult::class.java)
                    if (result != null) allResults.add(result)
                } catch (e: Exception) {
                    println("⚠️ WARN: Failed to parse match result: ${e.message}")
                }
            }
            
            loserSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(MatchResult::class.java)
                    if (result != null) allResults.add(result)
                } catch (e: Exception) {
                    println("⚠️ WARN: Failed to parse match result: ${e.message}")
                }
            }

            // Lọc draw matches (renter có thể là winnerRenterId hoặc loserRenterId trong draw)
            drawSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(MatchResult::class.java)
                    if (result != null && result.isDraw) {
                        // Chỉ thêm nếu renter tham gia (winner hoặc loser)
                        if (result.winnerRenterId == renterId || result.loserRenterId == renterId) {
                            if (allResults.none { it.resultId == result.resultId }) {
                                allResults.add(result)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("⚠️ WARN: Failed to parse match result: ${e.message}")
                }
            }

            // Remove duplicates (same resultId)
            val uniqueResults = allResults.distinctBy { it.resultId }

            println("🔍 DEBUG: Found ${uniqueResults.size} match results for renter: $renterId")
            return uniqueResults
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get match results: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Tính skill (weightedWinRate) từ match_results
     * Công thức: winRate * (N / (N + C))
     */
    private fun calculateSkill(matches: List<MatchResult>, renterId: String): Float {
        if (matches.isEmpty()) return 0f

        var wins = 0
        var losses = 0
        var draws = 0

        matches.forEach { match ->
            when {
                match.isDraw && (match.winnerRenterId == renterId || match.loserRenterId == renterId) -> {
                    draws++
                }
                match.winnerRenterId == renterId -> {
                    wins++
                }
                match.loserRenterId == renterId -> {
                    losses++
                }
            }
        }

        val totalMatches = wins + losses + draws
        if (totalMatches == 0) return 0f

        val winRate = wins.toFloat() / totalMatches.toFloat()
        val weighted = winRate * (totalMatches.toFloat() / (totalMatches.toFloat() + C))

        println("📊 DEBUG: Calculate skill for renter: $renterId")
        println("   - Wins: $wins, Losses: $losses, Draws: $draws")
        println("   - Total: $totalMatches, WinRate: $winRate, Skill: $weighted")

        return weighted.coerceIn(0f, 1f)
    }

    /**
     * Lấy AI Profile của renter (có thể theo fieldId)
     */
    suspend fun getAiProfile(renterId: String, fieldId: String? = null): Result<AiProfile?> {
        return try {
            val docId = if (fieldId != null) "${renterId}_$fieldId" else renterId
            val doc = firestore.collection(AI_PROFILES_COLLECTION)
                .document(docId)
                .get()
                .await()

            if (doc.exists()) {
                val profile = doc.toObject(AiProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get AI profile: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Lấy AI Profiles của nhiều renters (batch)
     * Dùng khi hiển thị danh sách đối thủ
     */
    suspend fun getAiProfiles(renterIds: List<String>, fieldId: String? = null): Result<Map<String, AiProfile>> {
        return try {
            val profiles = mutableMapOf<String, AiProfile>()
            
            renterIds.forEach { renterId ->
                val profileResult = getAiProfile(renterId, fieldId)
                if (profileResult.isSuccess) {
                    profileResult.getOrNull()?.let { profile ->
                        profiles[renterId] = profile
                    }
                }
            }
            
            Result.success(profiles)
        } catch (e: Exception) {
            println("❌ ERROR: Failed to get AI profiles: ${e.message}")
            Result.failure(e)
        }
    }
}
