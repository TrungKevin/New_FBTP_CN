package com.trungkien.fbtp_cn.ui.components.owner.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.Match
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BookingInfoCard(
    field: Field?,
    match: Match,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên sân (đồng bộ màu/icon xanh)
            EnhancedInfoRowLocal(
                icon = Icons.Filled.Place,
                label = "Tên sân",
                value = field?.name ?: "Tên sân"
            )
            
            // Ngày đặt
            EnhancedInfoRowLocal(
                icon = Icons.Filled.DateRange,
                label = "Ngày đặt",
                value = formatDate(match.date)
            )
            
            // Khung giờ
            EnhancedInfoRowLocal(
                icon = Icons.Filled.AccessTime,
                label = "Khung giờ",
                value = "${match.startAt} - ${match.endAt}"
            )
            
            // Giá tiền
            EnhancedInfoRowLocal(
                icon = Icons.Filled.AttachMoney,
                label = "Giá tiền",
                value = formatPrice(match.totalPrice),
            )

        }
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

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        date.format(formatter)
    } catch (_: Exception) {
        dateString
    }
}

private fun formatPrice(price: Long): String {
    return "${price.toString().reversed().chunked(3).joinToString(".").reversed()} VND"
}
