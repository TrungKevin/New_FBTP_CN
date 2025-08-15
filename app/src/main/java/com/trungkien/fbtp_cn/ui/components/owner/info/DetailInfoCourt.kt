package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Field

@Composable
fun DetailInfoCourt(field: Field, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(18.dp)) {
        // Thông tin cơ bản
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp), // Hình tròn góc
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),// Màu nền của Card
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp) // Độ cao của Card
        ) {
            Column(Modifier.padding(22.dp)) {// Padding bên trong Card
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
                    value = field.type
                )
                InfoRowItem(
                    label = "Giá thuê",
                    value = "${String.format("%,d", field.price)} VND/giờ",
                    valueColor = MaterialTheme.colorScheme.primary,
                    isPrice = true
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))

        // Thông tin liên hệ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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
                if (field.address.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.LocationOn,
                        label = "Địa chỉ",
                        value = field.address
                    )
                }
                if (field.operatingHours.isNotEmpty()) {
                    InfoRowItem(
                        painter = painterResource(id = R.drawable.schedule),
                        label = "Giờ hoạt động",
                        value = field.operatingHours
                    )
                }
                if (field.contactPhone.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = field.contactPhone
                    )
                }
                if (field.distance.isNotEmpty()) {
                    InfoRowItem(
                        icon = Icons.Default.LocationOn,
                        label = "Khoảng cách",
                        value = field.distance
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun InfoRowItem(// Hàm để hiển thị một dòng thông tin
    icon: ImageVector? = null, // Biểu tượng để hiển thị, nếu có
    painter: Painter? = null,// Biểu tượng hoặc hình ảnh để hiển thị
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface, // Màu sắc của giá trị
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
                Text(text = "💰", fontSize = 16.sp, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
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
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview
@Composable
fun DetailInfoCourtPreview() {
    DetailInfoCourt(
        field = Field(
            id = "1",
            name = "Sân bóng đá ABC",
            type = "Sân cỏ nhân tạo",
            price = 200000,
            address = "123 Đường ABC, Quận 1, TP.HCM",
            operatingHours = "08:00 - 22:00",
            contactPhone = "0123456789",
            imageUrl = TODO(),
            status = TODO(),
            isAvailable = TODO(),
            distance = TODO()
        )
    )
}


