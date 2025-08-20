package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun BookingSummaryCard(
    hours: Int,
    pricePerHour: Int,
    servicesTotal: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Tổng tạm tính", style = MaterialTheme.typography.titleMedium)
            Text(text = "Số giờ: $hours")
            Text(text = "Tiền sân: ${String.format("%,d", pricePerHour * hours)}₫")
            Text(text = "Dịch vụ: ${String.format("%,d", servicesTotal)}₫")
            Divider()
            Text(text = "Tổng cộng: ${String.format("%,d", pricePerHour * hours + servicesTotal)}₫", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview
@Composable
private fun BookingSummaryCardPreview() {
    FBTP_CNTheme {
        BookingSummaryCard(hours = 2, pricePerHour = 150000, servicesTotal = 35000)
    }
}


