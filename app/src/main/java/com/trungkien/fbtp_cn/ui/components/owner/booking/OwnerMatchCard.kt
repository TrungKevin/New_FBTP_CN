package com.trungkien.fbtp_cn.ui.components.owner.booking

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.trungkien.fbtp_cn.model.Match
import com.trungkien.fbtp_cn.model.MatchParticipant
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun OwnerMatchCard(
    match: Match,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val fieldRepo = remember { FieldRepository() }
    val userRepo = remember { UserRepository() }

    var fieldName by remember(match.fieldId) { mutableStateOf<String?>(null) }
    var fieldAddress by remember(match.fieldId) { mutableStateOf<String?>(null) }
    LaunchedEffect(match.fieldId) {
        try { 
            fieldRepo.getFieldById(match.fieldId).getOrNull()?.let { f -> 
                fieldName = f.name
                fieldAddress = f.address
            } 
        } catch (_: Exception) {}
    }

    // Resolve participant A/B
    val participantA: MatchParticipant? = match.participants.firstOrNull { it.side == "A" } ?: match.participants.getOrNull(0)
    val participantB: MatchParticipant? = match.participants.firstOrNull { it.side == "B" } ?: match.participants.getOrNull(1)

    var aName by remember { mutableStateOf("") }
    var aPhone by remember { mutableStateOf("") }
    var aAvatar by remember { mutableStateOf("") }

    var bName by remember { mutableStateOf("") }
    var bPhone by remember { mutableStateOf("") }
    var bAvatar by remember { mutableStateOf("") }

    LaunchedEffect(participantA?.renterId) {
        participantA?.renterId?.let { rid ->
            userRepo.getUserById(rid,
                onSuccess = { u ->
                    aName = u.name
                    aPhone = u.phone
                    aAvatar = u.avatarUrl ?: ""
                },
                onError = { })
        }
    }
    LaunchedEffect(participantB?.renterId) {
        participantB?.renterId?.let { rid ->
            userRepo.getUserById(rid,
                onSuccess = { u ->
                    bName = u.name
                    bPhone = u.phone
                    bAvatar = u.avatarUrl ?: ""
                },
                onError = { })
        }
    }

    val statusColor = when (match.status.uppercase()) {
        "WAITING_OPPONENT" -> Color(0xFFFF9800)
        "FULL" -> MaterialTheme.colorScheme.primary
        "CONFIRMED" -> Color(0xFF2E7D32)
        "CANCELLED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val statusLabel = when (match.status.uppercase()) {
        "WAITING_OPPONENT" -> "ĐANG CHỜ ĐỐI THỦ"
        "FULL" -> "ĐÃ GHÉP ĐÔI"
        "CONFIRMED" -> "ĐÃ XÁC NHẬN"
        "CANCELLED" -> "ĐÃ HỦY"
        else -> match.status
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // Header với tên sân và trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sân ${fieldName ?: match.fieldId}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(Modifier.height(6.dp))
                    
                    // Thông tin ngày giờ
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = match.date,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${match.startAt} - ${match.endAt}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Địa chỉ sân
                    if (!fieldAddress.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                            Text(
                                text = fieldAddress ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Status badge
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Participants Section
            Text(
                text = "Người tham gia",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Participant A (Người đặt sân)
            ParticipantCard(
                label = "Người đặt sân",
                name = aName,
                phone = aPhone,
                avatarData = aAvatar,
                context = context
            )

            Spacer(Modifier.height(8.dp))

            // Participant B (Đối thủ) - hiển thị nếu đã có dữ liệu B, không phụ thuộc trạng thái
            if (participantB != null) {
                ParticipantCard(
                    label = "Đối thủ",
                    name = bName,
                    phone = bPhone,
                    avatarData = bAvatar,
                    context = context
                )
            } else {
                // Placeholder cho đối thủ chưa có
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), 
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Nút theo trạng thái
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (match.status.equals("FULL", ignoreCase = true) && onConfirm != null) {
                Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("Xác nhận") }
            }
            if (!match.status.equals("CANCELLED", ignoreCase = true) && onCancel != null) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Hủy") }
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    label: String,
    name: String,
    phone: String,
    avatarData: String,
    context: android.content.Context
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), 
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Avatar
        val rendered = if (avatarData.isNotBlank()) {
            val decoded = try {
                val base = if (avatarData.startsWith("data:image")) avatarData.substringAfter(",") else avatarData
                val bytes = Base64.decode(base, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
            if (decoded != null) {
                androidx.compose.foundation.Image(
                    bitmap = decoded.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                true
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (avatarData.startsWith("http") || avatarData.startsWith("data:image")) avatarData else "data:image/jpeg;base64,$avatarData")
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                true
            }
        } else false

        if (!rendered) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(), 
                    color = MaterialTheme.colorScheme.primary, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Thông tin người dùng
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (name.isNotBlank()) name else "Chưa có thông tin",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (phone.isNotBlank()) {
                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewOwnerMatchCard() {
    FBTP_CNTheme {
        OwnerMatchCard(
            match = Match(
                rangeKey = "field1_2025-10-01_1800_2000",
                fieldId = "field1",
                date = "2025-10-01",
                startAt = "18:00",
                endAt = "20:00",
                capacity = 2,
                occupiedCount = 1,
                participants = listOf(MatchParticipant(bookingId = "bA", renterId = "uA", side = "A")),
                price = 0,
                totalPrice = 0,
                status = "WAITING_OPPONENT",
                matchType = "SINGLE"
            )
        )
    }
}


