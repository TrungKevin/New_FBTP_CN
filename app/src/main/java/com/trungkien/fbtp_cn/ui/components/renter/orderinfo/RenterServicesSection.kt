package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.model.PricingRule
import com.trungkien.fbtp_cn.model.FieldService

data class RenterServiceItem(val id: String, val name: String, val price: Int)

@Composable
fun RenterServicesSection(
    pricingRules: List<PricingRule>,
    fieldServices: List<FieldService>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isServicesCollapsed by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // BẢNG GIÁ SÂN (giống CourtService)
        Text(
            text = "BẢNG GIÁ SÂN",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        CourtPriceTableReadOnly(pricingRules)

        // DANH SÁCH DỊCH VỤ (giống CourtService)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DANH SÁCH DỊCH VỤ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isServicesCollapsed = !isServicesCollapsed }
            ) {
                Text(
                    text = if (isServicesCollapsed) "Mở rộng" else "Rút gọn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(
                            rotationZ = if (isServicesCollapsed) 0f else 90f
                        )
                )
            }
        }

        if (!isServicesCollapsed) {
            ServicesListRenter(fieldServices)
        }
    }
}

@Composable
private fun CourtPriceTableReadOnly(pricingRules: List<PricingRule>) {
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

            val weekday = pricingRules.filter { (it.dayType).contains("WEEKDAY", true) || (it.description).contains("T2", true) }
            val weekend = pricingRules.filter { (it.dayType).contains("WEEKEND", true) || (it.description).contains("T7", true) || (it.description).contains("CN", true) }

            fun findPrice(rules: List<PricingRule>, range: String): String {
                // Ưu tiên match theo description chứa đúng range, fallback theo index nếu không tìm thấy
                val matched = rules.firstOrNull { it.description.contains(range, ignoreCase = true) }
                val price = (matched?.price ?: 0L)
                return if (matched != null && price > 0) formatPrice(price) + " ₫" else "-"
            }

            // Hiển thị đầy đủ cột "Thứ" cho mọi hàng giống CourtService
            PriceRow("T2 - T6", "5h - 12h", findPrice(weekday, "5h - 12h"))
            PriceRow("T2 - T6", "12h - 18h", findPrice(weekday, "12h - 18h"))
            PriceRow("T2 - T6", "18h - 24h", findPrice(weekday, "18h - 24h"))
            PriceRow("T7 - CN", "5h - 12h", findPrice(weekend, "5h - 12h"))
            PriceRow("T7 - CN", "12h - 18h", findPrice(weekend, "12h - 18h"))
            PriceRow("T7 - CN", "18h - 24h", findPrice(weekend, "18h - 24h"))
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
            pricingRules = emptyList(),
            fieldServices = emptyList(),
            selected = setOf("1"),
            onToggle = {}
        )
    }
}

@Composable
private fun ServicesListRenter(services: List<FieldService>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ServiceCategoryRenter(
            title = "Dịch vụ tại sân",
            services = services.map { ServiceItemRenter(it.name ?: "Dịch vụ", formatPrice(it.price ?: 0) + " ₫") }
        )
    }
}

@Composable
private fun ServiceCategoryRenter(
    title: String,
    services: List<ServiceItemRenter>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }

            services.forEach { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = service.price,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private data class ServiceItemRenter(
    val name: String,
    val price: String
)

private fun formatPrice(value: Number): String {
    val v = value.toLong()
    return String.format("%,d", v).replace(',', '.')
}


