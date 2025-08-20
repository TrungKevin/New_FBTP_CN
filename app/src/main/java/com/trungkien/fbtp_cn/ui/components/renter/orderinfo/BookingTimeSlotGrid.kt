package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingTimeSlotGrid(
    selectedDate: LocalDate,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    startHour: Int = 6,
    endHour: Int = 22,
    modifier: Modifier = Modifier
) {
    val slots = (startHour * 2..endHour * 2).map { halfHour ->
        val hour = halfHour / 2
        val minute = if (halfHour % 2 == 0) 0 else 30
        String.format("%02d:%02d", hour, minute)
    }

    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Khung giờ ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

            LazyVerticalGrid(
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(slots) { slot ->
                    val hour = slot.substring(0,2).toInt()
                    val isAvailable = hour in startHour until endHour
                    val isSelected = selected.contains(slot)

                    val bg = when {
                        !isAvailable -> Color.Gray.copy(alpha = 0.3f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val fg = when {
                        !isAvailable -> Color.Gray
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(
                                if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .let { if (isAvailable) it.then(Modifier) else it }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .then(if (isAvailable) Modifier else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slot, style = MaterialTheme.typography.labelSmall, color = fg)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
private fun BookingTimeSlotGridPreview() {
    val selectedSlots = setOf("08:00", "10:00")
    BookingTimeSlotGrid(selectedDate = LocalDate.now(), selected = emptySet(), onToggle = {})
}


