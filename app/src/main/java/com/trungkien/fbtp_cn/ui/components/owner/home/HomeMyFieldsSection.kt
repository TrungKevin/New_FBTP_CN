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
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.R
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
                    FieldCard(field = field, onClick = onFieldClick)
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
                    id = "1",
                    name = "POC Pickleball",
                    type = "Pickleball",
                    price = 150000,
                    imageUrl = "",
                    status = "Available",
                    isAvailable = true,
                    address = "25 Tú Xương, TP. Thủ Đức",
                    operatingHours = "05:00 - 23:00",
                    contactPhone = "0926666357",
                    distance = "835m"
                ),
                Field(
                    id = "2",
                    name = "Sân Cầu Lông ABC",
                    type = "Cầu Lông",
                    price = 120000,
                    imageUrl = "",
                    status = "Booked",
                    isAvailable = false,
                    address = "Quận 1, TP.HCM",
                    operatingHours = "06:00 - 22:00",
                    contactPhone = "0901234567",
                    distance = "1.2km"
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