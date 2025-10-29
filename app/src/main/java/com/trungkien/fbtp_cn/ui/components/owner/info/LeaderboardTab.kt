package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trungkien.fbtp_cn.model.FieldLeaderboard
import com.trungkien.fbtp_cn.model.LeaderboardEntry
import com.trungkien.fbtp_cn.repository.LeaderboardRepository
import com.trungkien.fbtp_cn.repository.UserRepository
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun LeaderboardTab(
    fieldId: String,
    modifier: Modifier = Modifier
) {
    val repo = remember { LeaderboardRepository() }
    val coroutineScope = rememberCoroutineScope()
    var leaderboard by remember { mutableStateOf<FieldLeaderboard?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fieldId) {
        isLoading = true
        error = null
        leaderboard = repo.getLeaderboard(fieldId).getOrElse { null }
        if (leaderboard == null) {
            leaderboard = repo.recomputeAndSave(fieldId).getOrNull()
        }
        isLoading = false
    }

    // Lắng nghe thay đổi realtime của match_results để cập nhật bảng xếp hạng
    DisposableEffect(fieldId) {
        val reg = repo.addMatchResultsListener(fieldId) {
            coroutineScope.launch {
                isLoading = true
                leaderboard = repo.recomputeAndSave(fieldId).getOrNull()
                isLoading = false
            }
        }
        onDispose { reg.remove() }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Hợp nhất trùng renterId và sắp xếp theo WeightedWinRate giống repository để đảm bảo 1 người chỉ xuất hiện 1 lần
    val rawEntries = leaderboard?.entries.orEmpty()
    val c = 10f
    val entries = rawEntries
        .groupBy { it.renterId }
        .values
        .map { same ->
            val first = same.first()
            val wins = same.sumOf { it.wins }
            val losses = same.sumOf { it.losses }
            val draws = same.sumOf { it.draws }
            val goalsFor = same.sumOf { it.goalsFor }
            val goalsAgainst = same.sumOf { it.goalsAgainst }
            val total = same.sumOf { it.totalMatches }
            val winRate = if (total > 0) wins.toFloat() / total.toFloat() else 0f
            val weighted = winRate * (total.toFloat() / (total.toFloat() + c))
            first.copy(
                wins = wins,
                losses = losses,
                draws = draws,
                goalsFor = goalsFor,
                goalsAgainst = goalsAgainst,
                totalMatches = total,
                winPercent = winRate * 100f,
                weightedWinRate = weighted
            )
        }
        .sortedByDescending { it.weightedWinRate }
        .mapIndexed { index, e -> e.copy(rank = index + 1) }

    if (entries.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().padding(16.dp)) {
            Text("Chưa có dữ liệu xếp hạng")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(entries) { _, entry ->
            LeaderboardRow(entry)
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val userRepo = remember { UserRepository() }
    var avatar by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf<String>("Người chơi") }

    LaunchedEffect(entry.renterId) {
        try {
            userRepo.getUserById(entry.renterId, onSuccess = { u ->
                avatar = u.avatarUrl
                name = u.name
            }, onError = { })
        } catch (_: Exception) {}
    }

    val isTop3 = entry.rank in 1..3
    val rankBorderColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isTop3) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(if (isTop3) 1.5.dp else 1.dp, rankBorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank: Top 3 hiển thị cúp, từ 4 trở đi hiển thị số thứ tự
            when (entry.rank) {
                1, 2, 3 -> {
                    val cupColor = when (entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        else -> Color(0xFFCD7F32) // Bronze
                    }
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = cupColor,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${entry.rank}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> Text(
                    text = "${entry.rank}.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )
            }

            // Avatar
            when {
                !avatar.isNullOrBlank() && avatar!!.startsWith("data:image", true) -> {
                    val bitmap = try {
                        val base64 = avatar!!.substringAfter(",")
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) { null }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                !avatar.isNullOrBlank() -> {
                    AsyncImage(
                        model = avatar,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {}
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Tăng cỡ chữ cho dễ nhìn
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Tỷ lệ thắng: ${"%.1f".format(entry.winPercent)}%  •  ${entry.wins}W-${entry.draws}D-${entry.losses}L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bỏ hiển thị Bàn thắng/bại theo yêu cầu
        }
    }
}



