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

@Composable
fun AIArenaTab(
    fieldId: String,
    renterId: String,
    modifier: Modifier = Modifier
) {
    val repo = remember { LeaderboardRepository() }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(fieldId, renterId) {
        isLoading = true
        val lb = repo.getLeaderboard(fieldId).getOrNull()
            ?: repo.recomputeAndSave(fieldId).getOrNull()
        var list = lb?.entries.orEmpty()
        // loại chính mình nếu có
        if (renterId.isNotEmpty()) list = list.filter { it.renterId != renterId }
        // Fallback: nếu sân hiện tại không có dữ liệu leaderboard, thử tính trực tiếp từ match_results theo field
        if (list.isEmpty()) {
            try {
                val snap = FirebaseFirestore.getInstance().collection("match_results")
                    .whereEqualTo("fieldId", fieldId)
                    .get().await()

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
            } catch (_: Exception) {}
        }
        // Fallback 2: lấy đối thủ từ bookings DUO tại cùng field (không bắt buộc hasOpponent=true)
        if (list.isEmpty()) {
            try {
                val bs = FirebaseFirestore.getInstance().collection("bookings")
                    .whereEqualTo("fieldId", fieldId)
                    .whereEqualTo("bookingType", "DUO")
                    .get().await()
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
            } catch (_: Exception) {}
        }

        // Fallback 3: nếu vẫn rỗng, quét tất cả DUO bookings công khai (hasOpponent=true)
        if (list.isEmpty()) {
            try {
                val bsAll = FirebaseFirestore.getInstance().collection("bookings")
                    .whereEqualTo("bookingType", "DUO")
                    .whereEqualTo("hasOpponent", true)
                    .get().await()

                val opponentIds = mutableListOf<String>()
                bsAll.documents.forEach { d ->
                    listOfNotNull(
                        d.getString("renterId"),
                        d.getString("renterA"),
                        d.getString("renterB"),
                        d.getString("opponentId")
                    ).forEach { id -> if (!id.isNullOrBlank() && id != renterId) opponentIds.add(id) }
                }
                val distinctIds = opponentIds.distinct()

                val statusScore = mapOf(
                    "PAID" to 0.7f,
                    "CONFIRMED" to 0.65f,
                    "PENDING" to 0.5f,
                    "CANCELLED" to 0.35f
                )
                val idToScore = distinctIds.associateWith { id ->
                    val related = bsAll.documents.filter { d ->
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
            } catch (_: Exception) {}
        }
        entries = list
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
    val tabs = listOf("Đối mạnh", "Đối khá")

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

        val comparator = compareByDescending<LeaderboardEntry> { it.weightedWinRate }
            .thenByDescending { it.totalMatches }
        val strong = entries
            .filter { it.weightedWinRate >= strongThreshold }
            .sortedWith(comparator)
        val balanced = entries
            .filter { it.weightedWinRate in balancedMin..strongThreshold }
            .sortedWith(comparator)

        // Nếu tab Đối mạnh trống nhưng Đối khá có dữ liệu, tự chuyển sang Đối khá để người dùng thấy danh sách
        LaunchedEffect(entries, strong.size, balanced.size) {
            if (selectedTab == 0 && strong.isEmpty() && balanced.isNotEmpty()) {
                selectedTab = 1
            }
        }

        val list = if (selectedTab == 0) strong else balanced

        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đối thủ phù hợp")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { e -> OpponentCard(fieldId, renterId, e) }
            }
        }
    }
}

@Composable
private fun OpponentCard(fieldId: String, renterAId: String, entry: LeaderboardEntry) {
    val userRepo = remember { UserRepository() }
    val matchRepo = remember { MatchRequestRepository() }
    var name by remember { mutableStateOf("Đối thủ") }
    // Đồng bộ với cách lấy avatar của RenterReviewCard: luôn lấy mới từ UserRepository
    var showInvite by remember { mutableStateOf(false) }
    var facilityId by remember { mutableStateOf("") }
    var courtId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeRange by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<com.trungkien.fbtp_cn.repository.SlotSuggestion>>(emptyList()) }
    var sending by remember { mutableStateOf(false) }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val data = avatarData
            val context = LocalContext.current
            when {
                data.isBlank() -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                data.startsWith("data:image", ignoreCase = true) -> {
                    val base64 = data.substringAfter(",")
                    val bytes = try { Base64.decode(base64, Base64.DEFAULT) } catch (_: Exception) { null }
                    val bmp = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                data.startsWith("http", ignoreCase = true) -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(data)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                }
                else -> {
                    // base64 thô từ Firestore (không prefix), cố decode trực tiếp
                    val bytes = try { Base64.decode(data, Base64.DEFAULT) } catch (_: Exception) { null }
                    val bmp = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("data:image/jpeg;base64,$data")
                                .crossfade(true)
                                .allowHardware(false)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val aiScore = ((entry.weightedWinRate * 100).coerceIn(0f, 100f)).toInt()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("AI ${aiScore}/100") }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Win ${"%.1f".format(entry.winPercent)}%") }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Trận ${entry.totalMatches}") }
                    )
                }

                val desc = if (entry.weightedWinRate >= 0.5f) {
                    "AI nhận định: phong độ cao, phù hợp để thử thách."
                } else {
                    "AI nhận định: phong độ ổn định, thích hợp cho trận cân bằng."
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                TextButton(onClick = { /* TODO: mở trang chi tiết đội */ }) {
                    Text("Xem chi tiết")
                }
                Button(onClick = { showInvite = true }, shape = RoundedCornerShape(12.dp)) {
                    Text("Gửi lời mời")
                }
            }
        }
    }

    if (showInvite) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { if (!sending) showInvite = false },
            confirmButton = {
                TextButton(enabled = !sending, onClick = {
                    sending = true
                    scope.launch {
                        when (val res = matchRepo.sendMatchRequest(
                            renterAId = renterAId,
                            renterBId = entry.renterId,
                            facilityId = facilityId.ifBlank { fieldId },
                            courtId = courtId,
                            date = date,
                            timeRange = timeRange
                        )) {
                            is MatchRequestResult.Booked -> {
                                sending = false
                                showInvite = false
                            }
                            is MatchRequestResult.NeedAlternative -> {
                                suggestions = res.suggestions
                                sending = false
                            }
                            is MatchRequestResult.Error -> {
                                sending = false
                            }
                        }
                    }
                }) { Text("Gửi") }
            },
            dismissButton = { TextButton(enabled = !sending, onClick = { showInvite = false }) { Text("Hủy") } },
            title = { Text("Gửi lời mời giao hữu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = facilityId, onValueChange = { facilityId = it }, label = { Text("Cơ sở/Sân (FacilityId)") })
                    OutlinedTextField(value = courtId, onValueChange = { courtId = it }, label = { Text("Mã sân (CourtId)") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Ngày (yyyy-MM-dd)") })
                    OutlinedTextField(value = timeRange, onValueChange = { timeRange = it }, label = { Text("Khung giờ (HH:mm-HH:mm)") })
                    if (suggestions.isNotEmpty()) {
                        Text("Khung giờ đã kín. Gợi ý:")
                        suggestions.forEach { s ->
                            TextButton(onClick = {
                                facilityId = s.facilityId
                                courtId = s.courtId
                                date = s.date
                                timeRange = s.timeRange
                            }) {
                                Text("${s.facilityId} • ${s.courtId} • ${s.date} • ${s.timeRange}")
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

