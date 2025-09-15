package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    totalReviews: Int = 0,
    slotMinutes: Int = 30,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Thông tin cơ bản
        InfoCard(
            title = "Thông tin cơ bản",
            icon = Icons.Default.Info
        ) {
            InfoRowItem(
                icon = Icons.Default.SportsSoccer,
                label = "Loại sân",
                value = type,
                valueColor = MaterialTheme.colorScheme.primary
            )
            InfoRowItem(
                icon = Icons.Default.AttachMoney,
                label = "Giá thuê",
                value = "${String.format("%,d", price)} VND/giờ",
                valueColor = MaterialTheme.colorScheme.primary,
                isPrice = true
            )
            InfoRowItem(
                icon = Icons.Default.Star,
                label = "Điểm đánh giá",
                value = "${String.format("%.1f", rating)}/5.0 (${totalReviews} đánh giá)",
                valueColor = Color(0xFFFFB800)
            )
            InfoRowItem(
                icon = Icons.Default.Schedule,
                label = "Thời gian slot",
                value = "${slotMinutes} phút"
            )
            if (description.isNotBlank()) {
                InfoRowItem(
                    icon = Icons.Default.Description,
                    label = "Mô tả",
                    value = description
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Thông tin liên hệ
        InfoCard(
            title = "Thông tin liên hệ",
            icon = Icons.Default.ContactPhone
        ) {
            InfoRowItem(
                icon = Icons.Default.LocationOn,
                label = "Địa chỉ",
                value = address
            )
            
            InfoRowItem(
                icon = Icons.Default.AccessTime,
                label = "Giờ hoạt động",
                value = operatingHours
            )
            
            InfoRowItem(
                icon = Icons.Default.Phone,
                label = "Số điện thoại",
                value = contactPhone,
                valueColor = MaterialTheme.colorScheme.primary
            )
            
            if (distance.isNotEmpty()) {
                InfoRowItem(
                    icon = Icons.Default.Navigation,
                    label = "Khoảng cách",
                    value = distance
                )
            }
            
            if (latitude != 0.0 && longitude != 0.0) {
                InfoRowItem(
                    icon = Icons.Default.MyLocation,
                    label = "Tọa độ",
                    value = "${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Tiện ích và dịch vụ
        if (amenities.isNotEmpty()) {
            InfoCard(
                title = "Tiện ích & Dịch vụ",
                icon = Icons.Default.LocalOffer
            ) {
                amenities.forEach { amenity ->
                    InfoRowItem(
                        icon = getAmenityIcon(amenity),
                        label = "Tiện ích",
                        value = getAmenityDisplayName(amenity),
                        valueColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Trạng thái hoạt động
        InfoCard(
            title = "Trạng thái",
            icon = Icons.Default.Circle
        ) {
            InfoRowItem(
                icon = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                label = "Trạng thái hoạt động",
                value = if (isActive) "Đang hoạt động" else "Tạm ngưng",
                valueColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            if (isActive) {
                InfoRowItem(
                    icon = Icons.Default.Visibility,
                    label = "Hiển thị công khai",
                    value = "Có",
                    valueColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoRowItem(
    icon: ImageVector? = null,
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            painter != null -> {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Text(
                    text = "💰",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

private fun getAmenityIcon(amenity: String): ImageVector {
    return when (amenity.uppercase()) {
        "PARKING" -> Icons.Default.LocalParking
        "SHOWER" -> Icons.Default.Shower
        "EQUIPMENT" -> Icons.Default.Sports
        "WIFI" -> Icons.Default.Wifi
        "AC" -> Icons.Default.AcUnit
        "FOOD" -> Icons.Default.Restaurant
        "DRINKS" -> Icons.Default.LocalCafe
        "LOCKER" -> Icons.Default.Lock
        else -> Icons.Default.Star
    }
}

private fun getAmenityDisplayName(amenity: String): String {
    return when (amenity.uppercase()) {
        "PARKING" -> "Bãi đỗ xe"
        "SHOWER" -> "Phòng tắm"
        "EQUIPMENT" -> "Thiết bị thể thao"
        "WIFI" -> "WiFi miễn phí"
        "AC" -> "Điều hòa"
        "FOOD" -> "Dịch vụ ăn uống"
        "DRINKS" -> "Nước giải khát"
        "LOCKER" -> "Tủ khóa"
        else -> amenity
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
            rating = 4.8f,
            amenities = listOf("PARKING", "SHOWER", "EQUIPMENT", "WIFI"),
            description = "Sân Pickleball chất lượng cao với đầy đủ tiện ích hiện đại, phù hợp cho các trận đấu giao hữu và tập luyện.",
            totalReviews = 128,
            slotMinutes = 30,
            latitude = 10.7829,
            longitude = 106.6992,
            isActive = true
        )
    }
}

