package com.trungkien.fbtp_cn.ui.components.renter.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.PriceRange
import com.trungkien.fbtp_cn.model.PriceDetail
import androidx.compose.ui.res.painterResource
import com.trungkien.fbtp_cn.R

@Composable
fun RenterFeaturedFields(
    onFieldClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Featured",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Sân nổi bật",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            TextButton(
                onClick = { /* TODO: Navigate to all fields */ }
            ) {
                Text(
                    text = "Xem tất cả",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Featured fields horizontal scroll
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RenterFieldCard(
                    fieldName = "Sân Tennis ABC",
                    fieldType = "Tennis",
                    price = "120k/giờ",
                    location = "Quận 1, TP.HCM",
                    rating = 4.8f,
                    isAvailable = true,
                    onClick = { onFieldClick("field1") }
                )
            }
            
            item {
                RenterFieldCard(
                    fieldName = "Sân Cầu lông XYZ",
                    fieldType = "Badminton",
                    price = "80k/giờ",
                    location = "Quận 2, TP.HCM",
                    rating = 4.6f,
                    isAvailable = true,
                    onClick = { onFieldClick("field2") }
                )
            }
            
            item {
                RenterFieldCard(
                    fieldName = "Sân Bóng đá DEF",
                    fieldType = "Football",
                    price = "200k/giờ",
                    location = "Quận 3, TP.HCM",
                    rating = 4.7f,
                    isAvailable = false,
                    onClick = { onFieldClick("field3") }
                )
            }
        }
    }
}

@Composable
fun RenterFieldCard(
    fieldName: String,
    fieldType: String,
    price: String,
    location: String,
    rating: Float,
    isAvailable: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Field image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Field type icon
                Icon(
                    painter = painterResource(
                        id = when (fieldType) {
                            "Tennis" -> R.drawable.stadium
                            "Badminton" -> R.drawable.stadium
                            "Football" -> R.drawable.stadium
                            else -> R.drawable.stadium
                        }
                    ),
                    contentDescription = "Field type",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                
                // Rating badge
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = rating.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Availability badge
                if (isAvailable) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Còn trống",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Field info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(16.dp)
            ) {
                Text(
                    text = fieldName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = fieldType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price and availability
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Từ $price",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!isAvailable) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Hết chỗ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RenterFeaturedFieldsPreview() {
    val mockFields = listOf(
        Field(
            id = "1",
            name = "Sân bóng đá ABC",
            type = "Bóng đá",
            imageUrl = "https://via.placeholder.com/200",
            averageRating = 4.5f,
            totalReviews = 120,
            distance = "2.5km",
            price = 50000,
            isFavorite = false,
            status = "Available",
            isAvailable = true,
            address = "123 Đường ABC, Quận 1, TP.HCM",
            operatingHours = "06:00 - 22:00",
            contactPhone = "0123456789",
            latitude = 10.762622,
            longitude = 106.660172,
            description = "Sân bóng đá chất lượng cao",
            facilities = listOf("Đèn chiếu sáng", "Chỗ để xe", "Nhà vệ sinh"),
            images = listOf("https://via.placeholder.com/400x300"),
            ownerId = "owner1",
            ownerName = "Chủ sân ABC",
            priceRange = PriceRange(
                weekday = PriceDetail(
                    morning = 50000,
                    afternoon = 60000,
                    evening = 70000
                ),
                weekend = PriceDetail(
                    morning = 60000,
                    afternoon = 70000,
                    evening = 80000
                )
            )
        ),
        Field(
            id = "2",
            name = "Sân tennis XYZ",
            type = "Tennis",
            imageUrl = "https://via.placeholder.com/200",
            averageRating = 4.8f,
            totalReviews = 80,
            distance = "1.2km",
            price = 70000,
            isFavorite = true,
            status = "Available",
            isAvailable = true,
            address = "456 Đường XYZ, Quận 2, TP.HCM",
            operatingHours = "07:00 - 21:00",
            contactPhone = "0987654321",
            latitude = 10.787989,
            longitude = 106.749810,
            description = "Sân tennis chuyên nghiệp",
            facilities = listOf("Đèn chiếu sáng", "Chỗ để xe", "Nhà vệ sinh", "Vợt thuê"),
            images = listOf("https://via.placeholder.com/400x300"),
            ownerId = "owner2",
            ownerName = "Chủ sân XYZ",
            priceRange = PriceRange(
                weekday = PriceDetail(
                    morning = 70000,
                    afternoon = 80000,
                    evening = 90000
                ),
                weekend = PriceDetail(
                    morning = 80000,
                    afternoon = 90000,
                    evening = 100000
                )
            )
        )
    )

    RenterFeaturedFields(
        onFieldClick = {}
    )
}
