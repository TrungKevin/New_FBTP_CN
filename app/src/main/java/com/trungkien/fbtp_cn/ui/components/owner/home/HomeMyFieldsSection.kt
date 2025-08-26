package com.trungkien.fbtp_cn.ui.components.owner.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.FieldImages
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
    fun HomeMyFieldsSection(
    fields: List<Field>,
    onFieldClick: (Field) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Header
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stadium), // Sử dụng icon event
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Sân của tôi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "${fields.size} sân",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.background, // Chữ màu trắng 2 sân
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (fields.isEmpty()) {
            EmptyFieldsCard()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fields.forEach { field ->
                    FieldCard(
                        field = field, 
                        onClick = onFieldClick,
                        onViewDetailsClick = { onFieldClick(field) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeMyFieldsSection() {
    FBTP_CNTheme {
        HomeMyFieldsSection(
            fields = listOf(
                Field(
                    fieldId = "1",
                    ownerId = "owner123",
                    name = "POC Pickleball",
                    sports = listOf("PICKLEBALL"),
                    address = "25 Tú Xương, TP. Thủ Đức",
                    geo = GeoLocation(lat = 10.7769, lng = 106.7009),
                    images = FieldImages(
                        mainImage = "",
                        image1 = "",
                        image2 = "",
                        image3 = ""
                    ),
                    slotMinutes = 30,
                    openHours = OpenHours(start = "05:00", end = "23:00", isOpen24h = false),
                    amenities = listOf("PARKING", "SHOWER"),
                    description = "Sân Pickleball chất lượng cao",
                    contactPhone = "0926666357",
                    averageRating = 4.5f,
                    totalReviews = 128,
                    isActive = true
                ),
                Field(
                    fieldId = "2",
                    name = "Sân Cầu Lông ABC",
                    sports = listOf("BADMINTON"),
                    address = "Quận 1, TP.HCM",
                    openHours = OpenHours(start = "06:00", end = "22:00"),
                    contactPhone = "0901234567",
                    geo = GeoLocation(lat = 10.7829, lng = 106.6992),
                    averageRating = 4.2f,
                    totalReviews = 89
                )
            ),
            onFieldClick = {}
        )
    }
}

@Composable
private fun EmptyFieldsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏟️",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Chưa có sân nào",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Hãy thêm sân đầu tiên của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}