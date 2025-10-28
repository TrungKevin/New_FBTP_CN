package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.trungkien.fbtp_cn.model.FieldLeaderboard
import com.trungkien.fbtp_cn.model.LeaderboardEntry
import com.trungkien.fbtp_cn.model.MatchResult
import kotlinx.coroutines.tasks.await

class LeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val COLLECTION = "leaderboards"
    private val MATCH_RESULTS = "match_results"

    suspend fun getLeaderboard(fieldId: String): Result<FieldLeaderboard?> {
        return try {
            val doc = firestore.collection(COLLECTION).document(fieldId).get().await()
            Result.success(doc.toObject(FieldLeaderboard::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recomputeAndSave(fieldId: String): Result<FieldLeaderboard> {
        return try {
            val resultsSnap: QuerySnapshot = firestore.collection(MATCH_RESULTS)
                .whereEqualTo("fieldId", fieldId)
                .get().await()

            val results = resultsSnap.documents.mapNotNull { it.toObject(MatchResult::class.java) }

            val stats = mutableMapOf<String, LeaderboardEntry>()

            results.forEach { r ->
                if (r.isDraw) {
                    r.winnerRenterId?.let { renterId ->
                        val cur = stats[renterId] ?: LeaderboardEntry(renterId = renterId)
                        stats[renterId] = cur.copy(
                            draws = cur.draws + 1,
                            totalMatches = cur.totalMatches + 1,
                            goalsFor = cur.goalsFor + r.renterAScore,
                            goalsAgainst = cur.goalsAgainst + r.renterBScore
                        )
                    }
                    r.loserRenterId?.let { renterId ->
                        val cur = stats[renterId] ?: LeaderboardEntry(renterId = renterId)
                        stats[renterId] = cur.copy(
                            draws = cur.draws + 1,
                            totalMatches = cur.totalMatches + 1,
                            goalsFor = cur.goalsFor + r.renterBScore,
                            goalsAgainst = cur.goalsAgainst + r.renterAScore
                        )
                    }
                } else {
                    r.winnerRenterId?.let { renterId ->
                        val cur = stats[renterId] ?: LeaderboardEntry(renterId = renterId)
                        stats[renterId] = cur.copy(
                            wins = cur.wins + 1,
                            totalMatches = cur.totalMatches + 1,
                            goalsFor = cur.goalsFor + if (r.winnerSide == "A") r.renterAScore else r.renterBScore,
                            goalsAgainst = cur.goalsAgainst + if (r.winnerSide == "A") r.renterBScore else r.renterAScore
                        )
                    }
                    r.loserRenterId?.let { renterId ->
                        val cur = stats[renterId] ?: LeaderboardEntry(renterId = renterId)
                        stats[renterId] = cur.copy(
                            losses = cur.losses + 1,
                            totalMatches = cur.totalMatches + 1,
                            goalsFor = cur.goalsFor + if (r.winnerSide == "A") r.renterBScore else r.renterAScore,
                            goalsAgainst = cur.goalsAgainst + if (r.winnerSide == "A") r.renterAScore else r.renterBScore
                        )
                    }
                }
            }

            val entries = stats.values.map { e ->
                val winPercent = if (e.totalMatches > 0) e.wins.toFloat() / e.totalMatches * 100f else 0f
                e.copy(winPercent = winPercent)
            }.sortedByDescending { it.winPercent }
                .mapIndexed { index, e -> e.copy(rank = index + 1) }

            val leaderboard = FieldLeaderboard(
                fieldId = fieldId,
                updatedAt = System.currentTimeMillis(),
                entries = entries
            )

            firestore.collection(COLLECTION).document(fieldId).set(leaderboard).await()

            Result.success(leaderboard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to real-time changes of match results for a given field and invoke [onChange].
     * The caller is responsible for removing the returned [ListenerRegistration] when disposed.
     */
    fun addMatchResultsListener(fieldId: String, onChange: () -> Unit): ListenerRegistration {
        return firestore.collection(MATCH_RESULTS)
            .whereEqualTo("fieldId", fieldId)
            .addSnapshotListener { _, _ ->
                // We don't differentiate added/modified/removed here; any change triggers recompute in UI layer
                onChange()
            }
    }
}



