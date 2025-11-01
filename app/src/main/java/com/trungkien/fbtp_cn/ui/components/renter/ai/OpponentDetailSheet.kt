package com.trungkien.fbtp_cn.ui.components.renter.ai

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpponentDetailSheet(
    renterId: String,
    aiScore: Int,
    winRate: Float,
    totalMatches: Int,
    fieldId: String, // Thêm fieldId để lấy dữ liệu theo sân
    fieldName: String,
    fieldsLoading: Boolean,
    onDismiss: () -> Unit,
    onInvite: (date: String, timeRange: String, phone: String, note: String) -> Unit
) {
    val userRepo = remember { com.trungkien.fbtp_cn.repository.UserRepository() }
    val aiProfileRepo = remember { com.trungkien.fbtp_cn.repository.AiProfileRepository() }
    var name by remember { mutableStateOf("Đối thủ") }
    var avatar by remember { mutableStateOf("") }
    var recentForm by remember { mutableStateOf<List<String>>(emptyList()) } // ["W", "W", "L", "W", "D"]
    var calculatedTotalMatches by remember { mutableStateOf<Int?>(null) } // Tổng số trận đã chơi ở sân này
    var calculatedAiScore by remember { mutableStateOf<Int?>(null) } // AI score tính từ sân hiện tại
    var calculatedWinRate by remember { mutableStateOf<Float?>(null) } // Win rate tính từ sân hiện tại

    // ✅ FIX: Load profile + recent form và tính totalMatches từ match_results (theo fieldId)
    LaunchedEffect(renterId, fieldId) {
        userRepo.getUserById(renterId, onSuccess = { u ->
            name = u.name
            avatar = u.avatarUrl ?: ""
        }, onError = { })

        try {
            val db = FirebaseFirestore.getInstance()
            
            // ✅ FIX: Lấy tất cả match_results mà renter tham gia (chỉ ở sân hiện tại - fieldId)
            // Query winner matches
            val winnerQuery = db.collection("match_results")
                .whereEqualTo("winnerRenterId", renterId)
            val winnerSnap = if (fieldId.isNotBlank()) {
                winnerQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                winnerQuery.get().await()
            }
            
            // Query loser matches
            val loserQuery = db.collection("match_results")
                .whereEqualTo("loserRenterId", renterId)
            val loserSnap = if (fieldId.isNotBlank()) {
                loserQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                loserQuery.get().await()
            }
            
            // ✅ CRITICAL FIX: Query draw matches - phải filter thêm để đảm bảo renter tham gia
            // Firestore không hỗ trợ OR query, nên phải query tất cả draw ở sân này rồi filter trong memory
            val drawQuery = db.collection("match_results")
                .whereEqualTo("isDraw", true)
            val drawSnap = if (fieldId.isNotBlank()) {
                drawQuery.whereEqualTo("fieldId", fieldId).get().await()
            } else {
                drawQuery.get().await()
            }
            
            // ✅ Combine tất cả match results và loại bỏ duplicate
            val allMatches = mutableSetOf<String>() // Dùng Set để tránh duplicate resultId
            val matchResults = mutableListOf<com.trungkien.fbtp_cn.model.MatchResult>()
            
            // Thêm winner matches
            winnerSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(com.trungkien.fbtp_cn.model.MatchResult::class.java)
                    if (result != null && result.resultId.isNotBlank() && !allMatches.contains(result.resultId)) {
                        allMatches.add(result.resultId)
                        matchResults.add(result)
                    }
                } catch (_: Exception) {}
            }
            
            // Thêm loser matches (tránh duplicate)
            loserSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(com.trungkien.fbtp_cn.model.MatchResult::class.java)
                    if (result != null && result.resultId.isNotBlank() && !allMatches.contains(result.resultId)) {
                        allMatches.add(result.resultId)
                        matchResults.add(result)
                    }
                } catch (_: Exception) {}
            }
            
            // ✅ CRITICAL FIX: Thêm draw matches - CHỈ những match mà renter tham gia
            drawSnap.documents.forEach { doc ->
                try {
                    val result = doc.toObject(com.trungkien.fbtp_cn.model.MatchResult::class.java)
                    if (result != null && result.isDraw && result.resultId.isNotBlank()) {
                        // ✅ CHỈ thêm nếu renter là winner hoặc loser trong trận draw này
                        val renterParticipated = (result.winnerRenterId == renterId || result.loserRenterId == renterId)
                        if (renterParticipated && !allMatches.contains(result.resultId)) {
                            allMatches.add(result.resultId)
                            matchResults.add(result)
                        }
                    }
                } catch (_: Exception) {}
            }
            
            // ✅ Tính totalMatches: Tổng số trận đã chơi ở sân này (fieldId)
            calculatedTotalMatches = matchResults.size
            
            // ✅ CRITICAL FIX: Tính AI score và winRate từ match_results của sân hiện tại
            var wins = 0
            var losses = 0
            var draws = 0
            
            matchResults.forEach { match ->
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
            
            val total = wins + losses + draws
            if (total > 0) {
                // Tính winRate
                val winRateCalculated = wins.toFloat() / total.toFloat()
                calculatedWinRate = winRateCalculated
                
                // Tính weightedWinRate (skill) = winRate * (N / (N + C))
                val C = 10f
                val weightedWinRate = winRateCalculated * (total.toFloat() / (total.toFloat() + C))
                calculatedAiScore = ((weightedWinRate * 100).coerceIn(0f, 100f)).toInt()
            } else {
                calculatedWinRate = 0f
                calculatedAiScore = 0
            }
            
            println("🔍 DEBUG: OpponentDetailSheet - renterId: $renterId, fieldId: $fieldId")
            println("  - Total matches at this field: $calculatedTotalMatches")
            println("  - Wins: $wins, Losses: $losses, Draws: $draws")
            println("  - Calculated winRate: ${calculatedWinRate}, Calculated AI score: $calculatedAiScore")
            println("  - Passed from parent - winRate: $winRate, aiScore: $aiScore, totalMatches: $totalMatches")
            
            // ✅ FIX: Lấy 5 trận gần nhất từ SÂN HIỆN TẠI (cùng fieldId)
            // Cả totalMatches, recentForm, và AI score đều phải từ cùng một sân (fieldId)
            val recent5 = matchResults
                .distinctBy { it.resultId }
                .sortedByDescending { it.recordedAt }
                .take(5)
            
            println("  - Recent 5 matches (from current field) count: ${recent5.size}")
            
            // ✅ Parse thành W/L/D cho recentForm (5 trận gần nhất từ sân hiện tại)
            recentForm = recent5.map { match ->
                when {
                    match.isDraw -> "D"
                    match.winnerRenterId == renterId -> "W"
                    match.loserRenterId == renterId -> "L"
                    else -> "?" // Unknown
                }
            }.reversed() // Trận gần nhất ở cuối
            
            println("  - Recent form (from current field): ${recentForm.joinToString("-")}")
            
        } catch (e: Exception) {
            println("❌ ERROR: Failed to load recent form: ${e.message}")
            recentForm = emptyList()
            calculatedTotalMatches = null
            calculatedAiScore = null
            calculatedWinRate = null
        }
    }

    var date by remember { mutableStateOf("") }
    var timeRange by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current
                if (avatar.isBlank()) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                } else if (avatar.startsWith("data:image", true)) {
                    val base = avatar.substringAfter(",")
                    val bytes = try { Base64.decode(base, Base64.DEFAULT) } catch (_: Exception) { null }
                    val bmp = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    if (bmp != null) Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape)) else Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(56.dp))
                } else {
                    AsyncImage(model = ImageRequest.Builder(context).data(avatar).allowHardware(false).build(), contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // ✅ CRITICAL FIX: Hiển thị AI score tính từ sân hiện tại, nếu không có thì dùng giá trị truyền vào
                        val displayAiScore = calculatedAiScore ?: aiScore
                        AssistChip(onClick = {}, label = { Text("AI $displayAiScore/100") })
                        // ✅ FIX: Hiển thị số trận và winRate đã tính từ dữ liệu thực tế của sân hiện tại
                        val displayTotalMatches = calculatedTotalMatches ?: totalMatches
                        val displayWinRate = calculatedWinRate ?: winRate
                        AssistChip(onClick = {}, label = { Text("Win ${"%.1f".format(displayWinRate * 100)}% • Trận $displayTotalMatches") })
                    }
                }
            }

            // Hiển thị phong độ gần đây bằng vòng tròn màu W/L/D
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI nhận định:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (recentForm.isEmpty()) {
                    Text(
                        text = "Chưa có dữ liệu gần đây",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Hiển thị các vòng tròn màu
                    recentForm.forEach { result ->
                        val (color, text) = when (result) {
                            "W" -> Color(0xFF4CAF50) to "W" // Xanh lá cây
                            "L" -> Color(0xFFF44336) to "L" // Đỏ
                            "D" -> Color(0xFFFFC107) to "D" // Vàng
                            else -> MaterialTheme.colorScheme.onSurfaceVariant to "?"
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(2.dp, color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                color = color,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Thay bằng dòng này!
            Text("Sân thi đấu: $fieldName", style = MaterialTheme.typography.bodyLarge)
            if (fieldsLoading || fieldName == "Đang tải...") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(" Đang tải thông tin sân...")
                }
            } else {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Ngày yyyy-MM-dd") })
                OutlinedTextField(value = timeRange, onValueChange = { timeRange = it }, label = { Text("Khung giờ HH:mm-HH:mm") })
                OutlinedTextField(value = phone, onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } }, label = { Text("Số điện thoại (10 số)*") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = phone.isNotBlank() && !isPhoneValid, supportingText = { if (phone.isNotBlank() && !isPhoneValid) Text("Số điện thoại phải 10 số") else null })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú (không bắt buộc)") })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(enabled = isPhoneValid, onClick = { onInvite(date, timeRange, phone, note) }) { Text("Gửi lời mời") }
                }
            }
        }
    }
}


