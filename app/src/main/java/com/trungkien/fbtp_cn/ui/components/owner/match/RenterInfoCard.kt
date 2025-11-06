package com.trungkien.fbtp_cn.ui.components.owner.match

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.trungkien.fbtp_cn.model.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.trungkien.fbtp_cn.model.ServiceLine

@Composable
fun RenterInfoCard(
    renter: User,
    side: String,
    isSelected: Boolean,
    isMatchFinished: Boolean,
    onWinnerSelected: () -> Unit,
    score: Int = 0,
    onScoreChanged: (Int) -> Unit = {},
    opponentScore: Int = 0,
    isDraw: Boolean = false,
    renterNote: String? = null,
    onNoteChanged: (String) -> Unit = {},
    serviceLines: List<ServiceLine> = emptyList(),
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected && isMatchFinished) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = modifier.border(
            width = if (isSelected && isMatchFinished) 2.dp else 1.dp,
            color = if (isSelected && isMatchFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(16.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên renter với checkbox và ô nhập tỉ số
            RenterHeaderRow(
                renter = renter,
                side = side,
                isSelected = isSelected,
                isMatchFinished = isMatchFinished,
                onWinnerSelected = onWinnerSelected,
                score = score,
                onScoreChanged = onScoreChanged,
                opponentScore = opponentScore,
                isDraw = isDraw
            )
            
            // Hiển thị trạng thái thắng/thua/hòa
            if (isMatchFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = when {
                            isDraw -> "Hòa"
                            isSelected -> "Thắng"
                            else -> "Thua"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isDraw -> Color(0xFF4CAF50) // Xanh lá
                            isSelected -> Color(0xFFFFD700) // Vàng
                            else -> Color(0xFF808080) // Xám
                        }
                    )
                }
            }
            
            // Số điện thoại (đồng bộ style EnhancedInfoRow)
            EnhancedInfoRowLocal(
                icon = Icons.Filled.Phone,
                label = "Số điện thoại",
                value = renter.phone
            )
            
            // Email (đồng bộ style EnhancedInfoRow)
            EnhancedInfoRowLocal(
                icon = Icons.Filled.Email,
                label = "Email",
                value = renter.email
            )
            
            // ✅ Ghi chú riêng của renter - CHỈ HIỂN THỊ DỮ LIỆU CÓ SẴN
            // - Renter A: lấy từ Match.noteA (ghi chú khi đặt khe giờ đầu tiên)
            // - Renter B: lấy từ Match.noteB (ghi chú khi match vào làm đối thủ)
            // Luôn hiển thị ghi chú có sẵn từ dữ liệu, không cho nhập
            val displayNote = when {
                renterNote.isNullOrBlank() -> "Chưa có ghi chú"
                else -> renterNote
            }
            EnhancedInfoRowLocal(
                icon = Icons.Filled.Edit,
                label = "Ghi chú của ${renter.name.ifBlank { "Renter $side" }}",
                value = displayNote
            )
            
            // ✅ Dịch vụ thêm của renter - lấy từ Booking.serviceLines
            // ServiceLines được lưu khi renter chọn dịch vụ lúc đặt giờ (từ FieldService)
            // Mỗi renter có booking riêng, serviceLines được lấy từ booking của renter đó
            RenterServicesSection(
                serviceLines = serviceLines,
                renterName = renter.name.ifBlank { "Renter $side" }
            )
        }
    }
}

@Composable
private fun RenterHeaderRow(
    renter: User,
    side: String,
    isSelected: Boolean,
    isMatchFinished: Boolean,
    onWinnerSelected: () -> Unit,
    score: Int = 0,
    onScoreChanged: (Int) -> Unit = {},
    opponentScore: Int = 0,
    isDraw: Boolean = false
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar hoặc icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val data = renter.avatarUrl
            var rendered = false
            if (data.isNotBlank()) {
                val decodedBmp = try {
                    val base = if (data.startsWith("data:image")) data.substringAfter(",") else data
                    val compact = base.replace("\n", "").replace("\r", "").trim()
                    val bytes = Base64.decode(compact, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) { null }
                if (decodedBmp != null) {
                    androidx.compose.foundation.Image(
                        bitmap = decodedBmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    rendered = true
                } else if (data.startsWith("http", true) || data.startsWith("data:image", true)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(data)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    rendered = true
                }
            }
            if (!rendered) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_myplaces),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Text(
        text = renter.name.ifBlank { "Renter $side" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // Ô nhập tỉ số (chỉ hiển thị khi trận đấu kết thúc)
        if (isMatchFinished) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tỉ số:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                OutlinedTextField(
                    value = score.toString(),
                    onValueChange = { newValue ->
                        val newScore = newValue.toIntOrNull() ?: 0
                        if (newScore >= 0 && newScore <= 99) {
                            onScoreChanged(newScore)
                        }
                    },
                    enabled = isMatchFinished,
                    modifier = Modifier.width(60.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        // Checkbox để chọn đội thắng
        Checkbox(
            checked = isSelected || isDraw,
            onCheckedChange = { 
                // Kiểm tra validation tỉ số
                if (isMatchFinished) {
                    val isValidSelection = when {
                        isDraw -> true // Hòa thì luôn hợp lệ (bao gồm cả 0-0)
                        score > opponentScore -> true // Người có tỉ số cao hơn được chọn thắng
                        score < opponentScore -> false // Người có tỉ số thấp hơn không được chọn thắng
                        else -> true // Tỉ số bằng nhau thì có thể chọn hòa
                    }
                    
                    if (isValidSelection) {
                        onWinnerSelected()
                    } else {
                        // Hiển thị toast lỗi - sẽ được xử lý ở parent component
                        onWinnerSelected() // Vẫn gọi để parent có thể hiển thị toast
                    }
                } else {
                    onWinnerSelected()
                }
            },
            enabled = isMatchFinished,
            colors = CheckboxDefaults.colors(
                checkedColor = if (isDraw) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun EnhancedInfoRowLocal(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RenterServicesSection(
    serviceLines: List<ServiceLine>,
    renterName: String
) {
    if (serviceLines.isEmpty()) {
        // Nếu không có dịch vụ, hiển thị "Chưa có dịch vụ thêm"
        EnhancedInfoRowLocal(
            icon = Icons.Filled.ShoppingCart,
            label = "Dịch vụ thêm của $renterName",
            value = "Chưa có dịch vụ thêm"
        )
    } else {
        // Nếu có dịch vụ, hiển thị danh sách dịch vụ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Dịch vụ thêm của $renterName",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Danh sách dịch vụ
            serviceLines.forEach { service ->
                ServiceItemRow(service = service)
            }
            
            // Tổng tiền dịch vụ
            val totalServicePrice = serviceLines.sumOf { it.lineTotal }
            if (totalServicePrice > 0) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tổng tiền dịch vụ:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = "${String.format("%,d", totalServicePrice)}₫",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceItemRow(service: ServiceLine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = service.name.ifBlank { "Dịch vụ không xác định" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (service.quantity > 1) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Số lượng: ${service.quantity} × ${String.format("%,d", service.price)}₫",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%,d", service.price)}₫",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = "${String.format("%,d", service.lineTotal)}₫",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
