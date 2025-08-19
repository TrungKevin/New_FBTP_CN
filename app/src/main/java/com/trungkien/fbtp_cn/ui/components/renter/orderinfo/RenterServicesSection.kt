package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

data class RenterServiceItem(val id: String, val name: String, val price: Int)

@Composable
fun RenterServicesSection(
    services: List<RenterServiceItem>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // BẢNG GIÁ SÂN (giống CourtService)
        Text(
            text = "BẢNG GIÁ SÂN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CourtPriceTableCompact()

        // DANH SÁCH DỊCH VỤ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Dịch vụ", style = MaterialTheme.typography.titleMedium)
                services.forEach { s ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = s.name, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${String.format("%,d", s.price)}₫", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = selected.contains(s.id), onCheckedChange = { onToggle(s.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourtPriceTableCompact() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
            ) {
                HeaderCell("Thứ", modifier = Modifier.weight(1f))
                HeaderCell("Khung giờ", modifier = Modifier.weight(1f))
                HeaderCell("Giá", modifier = Modifier.weight(1f))
            }

            PriceRow("T2 - T6", "5h - 9h", "120.000 ₫")
            PriceRow("", "9h - 17h", "120.000 ₫")
            PriceRow("", "17h - 23h", "170.000 ₫")
            PriceRow("T7 - CN", "5h - 9h", "120.000 ₫")
            PriceRow("", "9h - 17h", "120.000 ₫")
            PriceRow("", "17h - 23h", "170.000 ₫")
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PriceRow(day: String, slot: String, price: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        CellText(day, modifier = Modifier.weight(1f))
        CellText(slot, modifier = Modifier.weight(1f))
        CellText(price, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CellText(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun RenterServicesSectionPreview() {
    FBTP_CNTheme {
        RenterServicesSection(
            services = listOf(
                RenterServiceItem("1", "Thuê vợt", 20000),
                RenterServiceItem("2", "Nước uống", 15000)
            ),
            selected = setOf("1"),
            onToggle = {}
        )
    }
}


