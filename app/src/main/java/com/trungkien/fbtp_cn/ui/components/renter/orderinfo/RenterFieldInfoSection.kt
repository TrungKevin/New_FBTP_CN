package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterFieldInfoSection(
    name: String,
    type: String,
    price: Int,
    address: String,
    operatingHours: String,
    contactPhone: String,
    distance: String,
    rating: Float,
    amenities: List<String> = emptyList(),
    description: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(18.dp)) {
        // Thông tin cơ bản
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    text = "Thông tin cơ bản",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                InfoRowItem(
                    painter = painterResource(id = R.drawable.stadium),
                    label = "Loại sân",
                    value = type
                )
                InfoRowItem(
                    label = "Giá thuê",
                    value = "${String.format("%,d", price)} VND/giờ",
                    valueColor = MaterialTheme.colorScheme.primary,
                    isPrice = true
                )
                InfoRowItem(
                    icon = Icons.Default.Star,
                    label = "Đánh giá",
                    value = "$rating / 5.0"
                )
                if (amenities.isNotEmpty()) {
                    InfoRowItem(
                        painter = painterResource(id = R.drawable.stadium),
                        label = "Tiện ích",
                        value = amenities.joinToString(", ")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Thông tin liên hệ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    text = "Thông tin liên hệ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (address.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.LocationOn,
                        label = "Địa chỉ",
                        value = address
                    )
                }
                if (operatingHours.isNotEmpty()) {
                    InfoRowItem(
                        painter = painterResource(id = R.drawable.event),
                        label = "Giờ hoạt động",
                        value = operatingHours
                    )
                }
                if (contactPhone.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = contactPhone
                    )
                }
                if (distance.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.LocationOn,
                        label = "Khoảng cách",
                        value = distance
                    )
                }
            }
        }

        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        text = "Mô tả",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun InfoRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isPrice: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        when {
            icon != null -> {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            painter != null -> {
                Icon(painter = painter, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            else -> {
                Text(text = "💰", fontSize = 16.sp, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor, fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Preview
@Composable
private fun RenterFieldInfoSectionPreview() {
    FBTP_CNTheme {
        RenterFieldInfoSection(
            name = "POC Pickleball",
            type = "Pickleball",
            price = 150000,
            address = "25 Tú Xương, TP. Thủ Đức",
            operatingHours = "05:00 - 23:00",
            contactPhone = "0926666357",
            distance = "835.3m",
            rating = 4.8f
        )
    }
}

