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
    selectedServices: Map<String, Int> = emptyMap(),
    allServices: List<RenterServiceItem> = emptyList(),
    onAddServicesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header chỉ có tiêu đề và nút thêm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dịch vụ thêm", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAddServicesClick) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm dịch vụ")
                }
            }
            
            // ✅ FIX: Hiển thị chi tiết các dịch vụ đã chọn
            if (selectedServices.isNotEmpty()) {
                selectedServices.forEach { (serviceId, quantity) ->
                    if (quantity > 0) {
                        val service = allServices.find { it.id == serviceId }
                        service?.let { serviceItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${serviceItem.name} x $quantity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format("%,d", serviceItem.price * quantity)}₫",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            // ✅ FIX: Tổng dịch vụ ở phía dưới cùng
            if (selectedServices.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tổng dịch vụ:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%,d", servicesTotal)}₫",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BookingServicesPickerPreview() {
    FBTP_CNTheme {
        val mockServices = listOf(
            RenterServiceItem("1", "Thuê vợt", 20000),
            RenterServiceItem("2", "Nước uống", 15000),
            RenterServiceItem("3", "Khăn lạnh", 5000)
        )
        val mockSelectedServices = mapOf(
            "1" to 2,  // Thuê vợt x 2
            "2" to 1   // Nước uống x 1
        )
        BookingServicesPicker(
            servicesTotal = 55000, // 2*20000 + 1*15000 = 55000
            selectedServices = mockSelectedServices,
            allServices = mockServices,
            onAddServicesClick = {}
        )
    }
}


