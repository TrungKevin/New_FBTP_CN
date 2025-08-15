package com.trungkien.fbtp_cn.ui.components.owner.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Booking

@Composable
fun BookingListItem(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val statusColor = when (booking.status) {
        "Đã xác nhận" -> Color(0xFF4CAF50)
        "Chờ xác nhận" -> Color(0xFFFF9800)
        "Đã hủy" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.fieldName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.schedule),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = booking.timeRange,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = booking.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BookingListItemPreview() {
    val booking = Booking(
        id = "1",
        fieldName = "Sân bóng A",
        timeRange = "18:00 - 19:00",
        status = "Đã xác nhận",
        fieldId = TODO()
    )
    BookingListItem(booking = booking, modifier = Modifier.padding(16.dp))
}


