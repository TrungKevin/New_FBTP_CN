package com.trungkien.fbtp_cn.ui.components.owner.booking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BookingFilterBar(
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { label ->
            val isSelected = label == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelectedChange(label) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookingFilterBarPreview() {
    var selectedOption by remember { mutableStateOf("Tất cả") }
    val options = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã hủy")

    BookingFilterBar(
        options = options,
        selected = selectedOption,
        onSelectedChange = { selectedOption = it },
        modifier = Modifier.padding(16.dp)
    )
}