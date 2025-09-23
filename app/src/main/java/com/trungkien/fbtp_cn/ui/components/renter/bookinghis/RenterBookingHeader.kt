package com.trungkien.fbtp_cn.ui.components.renter.bookinghis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
private fun formatDateForDisplay(dateString: String?): String {
    if (dateString == null) return "Tất cả từ hôm nay"
    
    return try {
        val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)
        
        when {
            date == today -> "Hôm nay"
            date == yesterday -> "Hôm qua"
            date == tomorrow -> "Ngày mai"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                date.format(formatter)
            }
        }
    } catch (e: DateTimeParseException) {
        dateString
    }
}

@Composable
fun RenterBookingHeader(
    selectedDateLabel: String?,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Lịch đặt sân của bạn",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = onCalendarClick,
                label = {
                    Text(
                        text = formatDateForDisplay(selectedDateLabel),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Lọc theo ngày",
                        tint = GreenPrimary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedDateLabel != null) {
                        GreenPrimary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    labelColor = if (selectedDateLabel != null) {
                        GreenPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            )
            
            // Hiển thị thông tin về việc lọc
            if (selectedDateLabel != null) {
                Text(
                    text = "Đang lọc theo ngày",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Hiển thị từ hôm nay",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun RenterBookingHeaderPreview() {
    FBTP_CNTheme {
        RenterBookingHeader(
            selectedDateLabel = "2024-01-15",
            onCalendarClick = {}
        )
    }
}



