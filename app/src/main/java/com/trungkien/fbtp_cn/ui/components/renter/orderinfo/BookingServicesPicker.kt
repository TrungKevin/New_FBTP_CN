package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun BookingServicesPicker(
    servicesTotal: Int,
    onAddServicesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Dịch vụ thêm", style = MaterialTheme.typography.titleMedium)
                Text(text = "Tổng dịch vụ: ${String.format("%,d", servicesTotal)}₫", color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onAddServicesClick) {
                Icon(Icons.Default.Add, contentDescription = "Thêm dịch vụ")
            }
        }
    }
}

@Preview
@Composable
private fun BookingServicesPickerPreview() {
    FBTP_CNTheme {
        BookingServicesPicker(servicesTotal = 35000, onAddServicesClick = {})
    }
}


