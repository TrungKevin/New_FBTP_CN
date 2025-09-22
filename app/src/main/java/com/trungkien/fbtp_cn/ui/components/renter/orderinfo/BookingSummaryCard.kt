package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

// ✅ NEW: Function để format giờ và phút
private fun formatHoursAndMinutes(hours: Double): String {
    if (hours <= 0) return "0 phút"
    
    val totalMinutes = (hours * 60).toInt()
    val hoursPart = totalMinutes / 60
    val minutesPart = totalMinutes % 60
    
    return when {
        hoursPart == 0 -> "$minutesPart phút"
        minutesPart == 0 -> "$hoursPart giờ"
        else -> "$hoursPart giờ $minutesPart phút"
    }
}

@Composable
fun BookingSummaryCard(
    hours: Double, // ✅ FIX: Đổi từ Int sang Double để hiển thị số giờ chính xác
    pricePerHour: Int,
    servicesTotal: Int,
    modifier: Modifier = Modifier,
    // ✅ NEW: Thêm tham số để hiển thị chi tiết hơn
    fieldTotal: Int = (pricePerHour * hours).toInt(),
    averagePricePerHour: Int = pricePerHour
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Tổng tạm tính", style = MaterialTheme.typography.titleMedium)
            
            // ✅ FIX: Hiển thị chi tiết số giờ và tiền sân (bỏ giá sân)
            Text(text = "Số giờ: ${formatHoursAndMinutes(hours)}")
            Text(text = "Tiền sân: ${String.format("%,d", fieldTotal)}₫")
            
            // ✅ NEW: Hiển thị dịch vụ nếu có
            if (servicesTotal > 0) {
                Text(text = "Tiền dịch vụ: ${String.format("%,d", servicesTotal)}₫")
            }
            
            Divider()
            
            // ✅ NEW: Tổng cộng = Tiền sân + Tiền dịch vụ
            val grandTotal = fieldTotal + servicesTotal
            Text(
                text = "Tổng cộng: ${String.format("%,d", grandTotal)}₫", 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
private fun BookingSummaryCardPreview() {
    FBTP_CNTheme {
        BookingSummaryCard(hours = 1.5, pricePerHour = 150000, servicesTotal = 35000)
    }
}


