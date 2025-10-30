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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpponentDetailSheet(
    renterId: String,
    aiScore: Int,
    winRate: Float,
    totalMatches: Int,
    fieldName: String,
    fieldsLoading: Boolean,
    onDismiss: () -> Unit,
    onInvite: (date: String, timeRange: String, phone: String, note: String) -> Unit
) {
    val userRepo = remember { com.trungkien.fbtp_cn.repository.UserRepository() }
    var name by remember { mutableStateOf("Đối thủ") }
    var avatar by remember { mutableStateOf("") }
    var last5Wins by remember { mutableStateOf(0) }
    var last5Total by remember { mutableStateOf(0) }

    // Load profile + recent results
    LaunchedEffect(renterId) {
        userRepo.getUserById(renterId, onSuccess = { u ->
            name = u.name
            avatar = u.avatarUrl ?: ""
        }, onError = { })

        try {
            val db = FirebaseFirestore.getInstance()
            val winSnap = db.collection("match_results").whereEqualTo("winnerRenterId", renterId).get().await()
            val loseSnap = db.collection("match_results").whereEqualTo("loserRenterId", renterId).get().await()
            val all = (winSnap.documents.map { true } + loseSnap.documents.map { false })
            val take5 = all.take(5)
            last5Total = take5.size
            last5Wins = take5.count { it }
        } catch (_: Exception) {}
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
                        AssistChip(onClick = {}, label = { Text("AI ${aiScore}/100") })
                        AssistChip(onClick = {}, label = { Text("Win ${"%.1f".format(winRate)}% • Trận $totalMatches") })
                    }
                }
            }

            val summary = if (last5Total > 0) "Gần đây: thắng $last5Wins/$last5Total trận" else "Chưa có dữ liệu gần đây"
            Text(text = "AI nhận định: ${summary}.", color = MaterialTheme.colorScheme.onSurfaceVariant)

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


