package com.trungkien.fbtp_cn.ui.components.renter.fieldsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.model.FieldImages
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64

@Composable
fun RenterSearchResultCard(
    field: SearchResultField,
    onFieldClick: (String) -> Unit = {},
    onFavoriteClick: (String) -> Unit = {},
    onBookClick: (String) -> Unit = {},
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable { onFieldClick(field.id) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section - Field image (50% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                // Field image
                val rawImage = field.imageUrl ?: field.fieldImages?.let { images ->
                    listOf(
                        images.mainImage,
                        images.image1,
                        images.image2,
                        images.image3
                    ).firstOrNull { it.isNotBlank() }
                }
                
                if (!rawImage.isNullOrBlank()) {
                    val decodedImage = remember(rawImage) {
                        try {
                            val base64String = if (rawImage.startsWith("data:image", ignoreCase = true)) {
                                rawImage.substringAfter(",")
                            } else {
                                rawImage
                            }
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            bitmap
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (decodedImage != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decodedImage.asImageBitmap(),
                            contentDescription = "Field image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.court1),
                            contentDescription = "Field image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.court1),
                        contentDescription = "Field image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Top section content
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top row with rating and actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Rating badge
                        Surface(
                            shape = RoundedCornerShape(25.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%.1f", field.rating),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (field.isAvailable) Color(0xFF00C853) else Color(0xFFFF5252)
                        ) {
                            Text(
                                text = if (field.isAvailable) "Đang hoạt động" else "Tạm ngưng",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Field type badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF7C4DFF)
                        ) {
                            Text(
                                text = "1 loại sân",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            // Bottom section - Info (50% height)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header row with avatar and field name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Owner avatar
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            if (!field.ownerAvatarUrl.isNullOrBlank()) {
                                if (field.ownerAvatarUrl.startsWith("data:image", ignoreCase = true)) {
                                    val bitmap = remember(field.ownerAvatarUrl) {
                                        try {
                                            val base64 = field.ownerAvatarUrl.substringAfter(",")
                                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        } catch (e: Exception) { null }
                                    }
                                    if (bitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Owner avatar",
                                            modifier = Modifier
                                                .size(42.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = field.ownerAvatarUrl,
                                            contentDescription = "Owner avatar",
                                            modifier = Modifier
                                                .size(42.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = field.ownerAvatarUrl,
                                        contentDescription = "Owner avatar",
                                        modifier = Modifier
                                            .size(42.dp)
                                            .padding(2.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Owner avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = field.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = field.ownerName.ifEmpty { "Chủ sân" },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                // Status indicator
                                Surface(
                                    shape = CircleShape,
                                    color = if (field.isAvailable) Color(0xFF00C853) else Color(0xFFFF5252),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                
                                Text(
                                    text = if (field.isAvailable) "Hoạt động" else "Tạm ngưng",
                                    fontSize = 11.sp,
                                    color = if (field.isAvailable) Color(0xFF00C853) else Color(0xFFFF5252),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Book button
                        Button(
                            onClick = { onBookClick(field.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "ĐẶT LỊCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Info grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // First row: Location and Phone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoItem(
                                icon = Icons.Default.LocationOn,
                                text = field.address.ifEmpty { field.location },
                                modifier = Modifier.weight(1f)
                            )
                            InfoItem(
                                icon = Icons.Default.Phone,
                                text = field.contactPhone,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Second row: Operating hours and Field type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoItemWithDrawable(
                                iconRes = R.drawable.schedule,
                                text = field.openHours,
                                modifier = Modifier.weight(1f)
                            )
                            InfoItemWithDrawable(
                                iconRes = R.drawable.stadium,
                                text = field.type,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Third row: Rating
                        InfoItemWithDrawable(
                            iconRes = R.drawable.star,
                            text = "${field.rating}/5.0 (${field.totalReviews} đánh giá)",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Fourth row: Price
                        if (field.price.isNotEmpty()) {
                            InfoItemWithDrawable(
                                iconRes = R.drawable.payments,
                                text = field.price,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Fifth row: Address and amenities
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (field.address.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = field.address,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            if (field.amenities.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = field.amenities.joinToString(", "),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoItemWithDrawable(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RenterSearchResultCardPreview() {
    FBTP_CNTheme {
        RenterSearchResultCard(
            field = SearchResultField(
                id = "1",
                name = "TP-Tennis",
                type = "TENNIS",
                price = "150,000 VND/giờ",
                location = "71 duong 10",
                rating = 4.5f,
                distance = "2.5 km",
                isAvailable = true,
                imageUrl = null,
                ownerName = "Kien",
                ownerAvatarUrl = null,
                ownerPhone = "0921483538",
                fieldImages = FieldImages(
                    mainImage = "",
                    image1 = "",
                    image2 = "",
                    image3 = ""
                ),
                address = "123 ABC Street, Quận 1, TP.HCM",
                openHours = "06:00 - 22:00",
                amenities = listOf("PARKING", "SHOWER", "EQUIPMENT"),
                totalReviews = 128,
                contactPhone = "0123456789",
                description = "Sân Tennis chất lượng cao"
            )
        )
    }
}
