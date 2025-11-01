package com.trungkien.fbtp_cn.ui.components.renter.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.trungkien.fbtp_cn.model.LeaderboardEntry
import com.trungkien.fbtp_cn.repository.LeaderboardRepository
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.repository.MatchRequestRepository
import com.trungkien.fbtp_cn.repository.MatchRequestResult
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.border
import kotlin.math.roundToInt

@Composable
fun AIArenaTab(
    fieldId: String,
    renterId: String,
    modifier: Modifier = Modifier
) {
    val repo = remember { LeaderboardRepository() }
    val aiAgent = remember { com.trungkien.fbtp_cn.repository.AIAgent() }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var myWeighted by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(fieldId, renterId) {
        isLoading = true
        
        println("🔍 DEBUG: AIArenaTab - Loading opponents for fieldId: $fieldId, renterId: $renterId")
        
        // ✅ CRITICAL: Lấy leaderboard từ SÂN HIỆN TẠI (fieldId cụ thể)
        val lb = repo.getLeaderboard(fieldId).getOrNull()
            ?: repo.recomputeAndSave(fieldId).getOrNull()
        var list = lb?.entries.orEmpty()
        
        println("  - Leaderboard entries from field $fieldId: ${list.size}")
        
        // ✅ CRITICAL: Kỹ năng của chính mình: ưu tiên từ leaderboard của sân hiện tại, fallback về AiProfile của sân đó
        myWeighted = lb?.entries?.firstOrNull { it.renterId == renterId }?.weightedWinRate
        if (myWeighted == null && fieldId.isNotBlank() && renterId.isNotEmpty()) {
            try {
                val aiProfileRepo = com.trungkien.fbtp_cn.repository.AiProfileRepository()
                val profileResult = aiProfileRepo.getAiProfile(renterId, fieldId)
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull()
                    myWeighted = profile?.skill
                    println("  - My weighted winRate from AiProfile (field $fieldId): $myWeighted")
                }
            } catch (e: Exception) {
                println("  - Failed to get AiProfile: ${e.message}")
                // Nếu không lấy được, để null để dùng median
            }
        } else {
            println("  - My weighted winRate from leaderboard (field $fieldId): $myWeighted")
        }
        
        // ✅ Loại chính mình nếu có
        if (renterId.isNotEmpty()) list = list.filter { it.renterId != renterId }
        // ✅ CRITICAL FIX: Fallback 1 - Tính trực tiếp từ match_results của SÂN HIỆN TẠI (fieldId)
        if (list.isEmpty()) {
            println("  - Leaderboard empty, computing from match_results for field: $fieldId")
            try {
                val snap = FirebaseFirestore.getInstance().collection("match_results")
                    .whereEqualTo("fieldId", fieldId) // ✅ CHỈ lấy từ sân hiện tại
                    .get().await()
                
                println("  - Found ${snap.size()} match_results for field: $fieldId")

                val stats = mutableMapOf<String, LeaderboardEntry>()
                snap.documents.forEach { d ->
                    val winnerId = d.getString("winnerRenterId")
                    val loserId = d.getString("loserRenterId")
                    val isDraw = d.getBoolean("isDraw") ?: false
                    if (isDraw) {
                        listOfNotNull(winnerId, loserId).forEach { id ->
                            val cur = stats[id] ?: LeaderboardEntry(renterId = id)
                            stats[id] = cur.copy(draws = cur.draws + 1, totalMatches = cur.totalMatches + 1)
                        }
                    } else {
                        if (!winnerId.isNullOrBlank()) {
                            val cur = stats[winnerId] ?: LeaderboardEntry(renterId = winnerId)
                            stats[winnerId] = cur.copy(wins = cur.wins + 1, totalMatches = cur.totalMatches + 1)
                        }
                        if (!loserId.isNullOrBlank()) {
                            val cur = stats[loserId] ?: LeaderboardEntry(renterId = loserId)
                            stats[loserId] = cur.copy(losses = cur.losses + 1, totalMatches = cur.totalMatches + 1)
                        }
                    }
                }

                val C = 10f
                val computed = stats.values.map { e ->
                    val winRate = if (e.totalMatches > 0) e.wins.toFloat() / e.totalMatches else 0f
                    val weighted = if (e.totalMatches > 0) winRate * (e.totalMatches / (e.totalMatches + C)) else 0f
                    e.copy(winPercent = winRate * 100f, weightedWinRate = weighted)
                }
                list = computed.filter { it.renterId.isNotBlank() && it.renterId != renterId }
                println("  - Computed ${list.size} opponents from match_results for field: $fieldId")
            } catch (e: Exception) {
                println("  - Error computing from match_results: ${e.message}")
            }
        }
        // ✅ CRITICAL FIX: Fallback 2 - Lấy đối thủ từ bookings DUO tại CÙNG SÂN (fieldId)
        if (list.isEmpty()) {
            println("  - Still empty, trying bookings DUO for field: $fieldId")
            try {
                val bs = FirebaseFirestore.getInstance().collection("bookings")
                    .whereEqualTo("fieldId", fieldId) // ✅ CHỈ lấy từ sân hiện tại
                    .whereEqualTo("bookingType", "DUO")
                    .get().await()
                
                println("  - Found ${bs.size()} DUO bookings for field: $fieldId")
                val opponentIds = mutableListOf<String>()
                bs.documents.forEach { d ->
                    listOfNotNull(
                        d.getString("renterId"),
                        d.getString("renterA"),
                        d.getString("renterB"),
                        d.getString("opponentId")
                    ).forEach { id ->
                        if (!id.isNullOrBlank() && id != renterId) opponentIds.add(id)
                    }
                }
                val distinctIds = opponentIds.distinct()

                // Ưu tiên các booking có status PAID/CONFIRMED để đẩy score cao hơn
                val statusScore = mapOf(
                    "PAID" to 0.7f,
                    "CONFIRMED" to 0.65f,
                    "PENDING" to 0.5f,
                    "CANCELLED" to 0.35f
                )
                val idToScore = distinctIds.associateWith { id ->
                    val related = bs.documents.filter { d ->
                        val ids = listOf(
                            d.getString("renterId"),
                            d.getString("renterA"),
                            d.getString("renterB"),
                            d.getString("opponentId")
                        )
                        ids.contains(id)
                    }
                    if (related.isEmpty()) 0.5f else related
                        .map { statusScore[it.getString("status") ?: "PENDING"] ?: 0.5f }
                        .average().toFloat()
                }

                list = idToScore.map { (id, score) ->
                    LeaderboardEntry(
                        renterId = id,
                        totalMatches = 1,
                        winPercent = score * 100f,
                        weightedWinRate = score
                    )
                }
                println("  - Computed ${list.size} opponents from bookings for field: $fieldId")
            } catch (e: Exception) {
                println("  - Error computing from bookings: ${e.message}")
            }
        }

        // ✅ CRITICAL: Không có Fallback 3 - Đảm bảo chỉ tính toán theo sân hiện tại (fieldId)
        // Không fallback về toàn bộ hệ thống để đảm bảo AI chỉ tính theo từng sân
        entries = list
        
        println("  - Final entries count for field $fieldId: ${entries.size}")
        println("  - Strong opponents (weightedWinRate >= 0.5): ${entries.count { it.weightedWinRate >= 0.5f }}")
        println("  - Balanced opponents (weightedWinRate < 0.5): ${entries.count { it.weightedWinRate < 0.5f }}")
        
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    // Ngưỡng tham số hóa
    var c by remember { mutableStateOf(10f) }
    var strongThreshold by remember { mutableStateOf(0.5f) }
    // Mở rộng tab "Đối khá" để bao phủ mọi đối thủ dưới ngưỡng mạnh
    var balancedMin by remember { mutableStateOf(0f) }
    val tabs = listOf("Gợi ý", "Đối mạnh", "Đối khá")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        // ✅ CRITICAL FIX: Sắp xếp và phân loại dựa trên weightedWinRate từ SÂN HIỆN TẠI
        // Comparator: sắp xếp theo weightedWinRate giảm dần, sau đó theo totalMatches giảm dần
        val comparator = compareByDescending<LeaderboardEntry> { it.weightedWinRate }
            .thenByDescending { it.totalMatches }
        
        // ✅ Phân loại tab "Đối mạnh" và "Đối khá" dựa trên weightedWinRate từ sân hiện tại
        // - "Đối mạnh": weightedWinRate >= 0.5 (50%)
        // - "Đối khá": weightedWinRate < 0.5 (50%)
        val strongIds = entries.filter { it.weightedWinRate >= 0.5f }.map { it.renterId }.toSet()
        val strong = entries.filter { it.weightedWinRate >= 0.5f }.sortedWith(comparator)
        val balanced = entries.filter { it.weightedWinRate < 0.5f && it.renterId !in strongIds }.sortedWith(comparator)
        
        println("🔍 DEBUG: AIArenaTab - Tab classification (field: $fieldId)")
        println("  - Strong tab: ${strong.size} opponents")
        println("  - Balanced tab: ${balanced.size} opponents")
        val suggestions: List<LeaderboardEntry> = remember(entries, myWeighted) {
            val sug = aiAgent.suggestOpponents(myWeighted, entries, limit = 5)
            val idSet = sug.map { it.renterId }.toSet()
            entries.filter { it.renterId in idSet }
                .sortedByDescending { e -> sug.firstOrNull { it.renterId == e.renterId }?.score ?: 0f }
        }

        // Nếu tab Đối mạnh trống nhưng Đối khá có dữ liệu, tự chuyển sang Đối khá để người dùng thấy danh sách
        LaunchedEffect(entries, strong.size, balanced.size) {
            if (selectedTab == 0 && suggestions.isNotEmpty()) return@LaunchedEffect
            if (selectedTab == 1 && strong.isEmpty() && balanced.isNotEmpty()) selectedTab = 2
        }

        val list = when (selectedTab) {
            0 -> suggestions
            1 -> strong
            else -> balanced
        }

        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đối thủ phù hợp")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { e ->
                    if (e.renterId.isNotBlank() && e.renterId.lowercase() != "unknown" && e.renterId.lowercase() != "đối thủ") {
                        val prob = if (selectedTab == 0) {
                            val baseMy = myWeighted ?: run {
                                val arr = entries.map { it.weightedWinRate }.sorted()
                                if (arr.isEmpty()) 0.5f else arr[arr.size / 2]
                            }
                            aiAgent.estimateOutcomeProbabilities(baseMy, e.weightedWinRate)
                        } else null
                        OpponentCard(fieldId, renterId, e, prob)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpponentCard(
    fieldId: String,
    renterAId: String,
    entry: LeaderboardEntry,
    outcomeProb: com.trungkien.fbtp_cn.repository.OutcomeProbabilities? = null
) {
    val userRepo = remember { UserRepository() }
    val matchRepo = remember { MatchRequestRepository() }
    var name by remember { mutableStateOf("Đối thủ") }
    // Đồng bộ với cách lấy avatar của RenterReviewCard: luôn lấy mới từ UserRepository
    var showInvite by remember { mutableStateOf(false) }
    var facilityId by remember { mutableStateOf<String?>(null) }
    var courtId by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf("") }
    var timeRange by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<com.trungkien.fbtp_cn.repository.SlotSuggestion>>(emptyList()) }
    var sending by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    var fieldName by remember { mutableStateOf<String?>(null) }
    var fieldLoading by remember { mutableStateOf(false) }

    // Fetch field khi cần show detail
    LaunchedEffect(showDetail) {
        if (showDetail && fieldName == null) {
            fieldLoading = true
            try {
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("fields")
                    .document(fieldId)
                    .get()
                    .await()
                fieldName = doc.getString("name") ?: "--"
            } catch (_: Exception) {
                fieldName = "--"
            }
            fieldLoading = false
        }
    }

    val avatarData by produceState(initialValue = "", key1 = entry.renterId) {
        if (entry.renterId.isNotBlank()) {
            userRepo.getUserById(
                entry.renterId,
                onSuccess = { u ->
            name = u.name
                    value = u.avatarUrl ?: ""
                },
                onError = { _ -> value = "" }
            )
        } else {
            value = ""
        }
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar trái (ẩn hoàn toàn nếu không có avatar)
            if (avatarData.isNotBlank()) {
                if (avatarData.startsWith("data:image", true)) {
                    val base = avatarData.substringAfter(",")
                    val bytes = try { android.util.Base64.decode(base, android.util.Base64.DEFAULT) } catch (_: Exception) { null }
                    val bmp = bytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) }
                    if (bmp != null) Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(54.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
                } else {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(avatarData).allowHardware(false).build(), contentDescription = null, modifier = Modifier.size(54.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
                }
                Spacer(modifier = Modifier.width(14.dp))
            } else {
                Spacer(modifier = Modifier.width(8.dp)) // spacing khi không có avatar
            }
            // Cột thông tin giữa
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text("AI ${((entry.weightedWinRate * 100).coerceIn(0f, 100f)).toInt()}/100", style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(28.dp)
                    )
                    if (outcomeProb == null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Win ${"%.1f".format(entry.winPercent)}%", style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    } else {
                        val wInt = (outcomeProb.pWin * 100f).roundToInt()
                        val dInt = (outcomeProb.pDraw * 100f).roundToInt()
                        var lInt = 100 - wInt - dInt
                        if (lInt < 0) lInt = 0
                        AssistChip(
                            onClick = {},
                            label = { Text("pWin ${wInt}%", style = MaterialTheme.typography.labelMedium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                }
                Spacer(modifier = Modifier.height(6.dp))
                val desc = if (outcomeProb != null) {
                    val wInt = (outcomeProb.pWin * 100f).roundToInt()
                    val dInt = (outcomeProb.pDraw * 100f).roundToInt()
                    var lInt = 100 - wInt - dInt
                    if (lInt < 0) lInt = 0
                    "AI dự đoán: Thắng ${wInt}% • Hòa ${dInt}% • Thua ${lInt}%"
                } else if (entry.weightedWinRate >= 0.5f) {
                    "AI nhận định: phong độ cao, phù hợp để thử thách."
                } else {
                    "AI nhận định: phong độ ổn định, thích hợp cho trận cân bằng."
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Nút Xem chi tiết
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                TextButton(onClick = { showDetail = true }) {
                    Text("Xem chi tiết", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
        }
        }
    }

    if (showDetail) {
        OpponentDetailSheet(
            renterId = entry.renterId,
            aiScore = ((entry.weightedWinRate * 100).coerceIn(0f, 100f)).toInt(),
            winRate = entry.winPercent,
            totalMatches = entry.totalMatches,
            fieldId = fieldId, // Truyền fieldId để lấy dữ liệu theo sân
            fieldName = fieldName ?: "Đang tải...",
            fieldsLoading = fieldLoading,
            onDismiss = { showDetail = false },
            onInvite = { selectedDate, selectedTimeRange, selectedPhone, selectedNote ->
                date = selectedDate
                timeRange = selectedTimeRange
                phone = selectedPhone
                note = selectedNote
                showDetail = false
                showInvite = true
            }
        )
    }

    if (showInvite) {
        val scope = rememberCoroutineScope()
        val fieldNameShow = fieldName ?: "--"
        val facilityIdField = (facilityId ?: "").ifBlank { fieldId }
        val courtIdField = (courtId ?: "")
        val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }
        val timeRegex = Regex("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")
        val isFormValid = isPhoneValid && date.isNotBlank() && timeRange.matches(timeRegex)
        val ctx = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { if (!sending) showInvite = false },
            confirmButton = {
                androidx.compose.material3.Button(enabled = !sending && isFormValid, onClick = {
                    sending = true
                    scope.launch {
                        when (val res = matchRepo.sendMatchRequestFull(
                            renterAId = renterAId,
                            renterBId = entry.renterId,
                            facilityId = facilityIdField,
                            courtId = courtIdField,
                            fieldName = fieldNameShow,
                            date = date,
                            timeRange = timeRange,
                            phone = phone,
                            note = note
                        )) {
                            is MatchRequestResult.Booked -> {
                                sending = false
                                showInvite = false
                                android.widget.Toast.makeText(ctx, "Đã gửi lời mời", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            is MatchRequestResult.NeedAlternative -> {
                                suggestions = res.suggestions
                                sending = false
                                android.widget.Toast.makeText(ctx, "Khung giờ đã kín, xem gợi ý", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            is MatchRequestResult.Error -> {
                                sending = false
                                val msg = (res as MatchRequestResult.Error).message
                                android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { androidx.compose.material3.Text("Gửi") }
            },
            dismissButton = { TextButton(enabled = !sending, onClick = { showInvite = false }) { Text("Hủy") } },
            title = { Text("Gửi lời mời giao hữu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sân thi đấu: $fieldNameShow", style = MaterialTheme.typography.bodyLarge)
                    Text("Ngày: $date", style = MaterialTheme.typography.bodyMedium)
                    Text("Khung giờ: $timeRange", style = MaterialTheme.typography.bodyMedium)
                    Text("Số điện thoại: $phone", style = MaterialTheme.typography.bodyMedium)
                    if (note.isNotBlank()) Text("Ghi chú: $note", style = MaterialTheme.typography.bodyMedium)
                    if (suggestions.isNotEmpty()) {
                        Text("Khung giờ đã kín. Gợi ý:")
                        suggestions.forEach { s ->
                            TextButton(onClick = {
                                date = s.date
                                timeRange = s.timeRange
                            }) {
                                Text("${s.date} • ${s.timeRange}")
                            }
                        }
                    }
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun OpponentCardPreview() {
    FBTP_CNTheme {
        Surface {
            OpponentCard(
                fieldId = "field_sample",
                renterAId = "renter_A",
                entry = LeaderboardEntry(
                    renterId = "renter_B",
                    wins = 8,
                    losses = 2,
                    draws = 1,
                    totalMatches = 11,
                    winPercent = 72f,
                    weightedWinRate = 0.68f
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OpponentListPreview() {
    val samples = listOf(
        LeaderboardEntry(renterId = "b1", wins = 10, losses = 3, draws = 0, totalMatches = 13, winPercent = 76.9f, weightedWinRate = 0.70f),
        LeaderboardEntry(renterId = "b2", wins = 6, losses = 4, draws = 2, totalMatches = 12, winPercent = 50f, weightedWinRate = 0.55f),
        LeaderboardEntry(renterId = "b3", wins = 3, losses = 6, draws = 1, totalMatches = 10, winPercent = 30f, weightedWinRate = 0.40f)
    )
    FBTP_CNTheme {
        Surface {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(samples) { e ->
                    OpponentCard(fieldId = "field_sample", renterAId = "renter_A", entry = e)
                }
            }
        }
    }
}

