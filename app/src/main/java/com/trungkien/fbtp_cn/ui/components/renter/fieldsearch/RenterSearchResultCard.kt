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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import android.util.Base64

data class SearchResultField(
    val id: String,
    val name: String,
    val type: String,
    val price: String,
    val location: String,
    val rating: Float,
    val distance: String,
    val isAvailable: Boolean,
    val imageUrl: String? = null,
    val ownerName: String = "",
    val ownerAvatarUrl: String? = null,
    val ownerPhone: String = "",
    val fieldImages: FieldImages? = null,
    val address: String = "",
    val openHours: String = "",
    val amenities: List<String> = emptyList(),
    val totalReviews: Int = 0,
    val contactPhone: String = "",
    val description: String = ""
)

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
            .height(320.dp)
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
            // Top section - Field image (40% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f, fill = true)
            ) {
                // Field image - sử dụng cách tương tự FieldCard
                val rawImage = field.imageUrl ?: field.fieldImages?.let { images ->
                    listOf(
                        images.mainImage,
                        images.image1,
                        images.image2,
                        images.image3
                    ).firstOrNull { it.isNotBlank() }
                }
                
                // Debug logs
                println("🔄 DEBUG: RenterSearchResultCard - Field: ${field.name}")
                println("🔄 DEBUG: - imageUrl: ${field.imageUrl?.take(50)}...")
                println("🔄 DEBUG: - fieldImages.mainImage: ${field.fieldImages?.mainImage?.take(50)}...")
                println("🔄 DEBUG: - rawImage: ${rawImage?.take(50)}...")
                println("🔄 DEBUG: - ownerAvatarUrl: ${field.ownerAvatarUrl?.take(50)}...")
                
                if (!rawImage.isNullOrBlank()) {
                    // Xử lý base64 image trực tiếp như FieldCard
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
                            println("DEBUG: Error decoding base64 image: ${e.message}")
                            null
                        }
                    }
                    
                    if (decodedImage != null) {
                        Image(
                            bitmap = decodedImage.asImageBitmap(),
                            contentDescription = "Field image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to default image if bitmap decode fails
                        Image(
                            painter = painterResource(id = R.drawable.court1),
                            contentDescription = "Field image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Fallback to default image
                    Image(
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
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // Top row with rating and actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Rating badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", field.rating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Favorite button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onFavoriteClick(field.id) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.fillMaxHeight())

                // Bottom badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Availability badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (field.isAvailable) Color(0xFF00C853) else Color(0xFFFF5252)
                ) {
                    Text(
                        text = if (field.isAvailable) "Còn trống" else "Hết chỗ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Field type badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF7C4DFF)
                    ) {
                        Text(
                            text = field.type,
                            fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    }
                }
            }
            
            // Bottom section - Field info (60% height)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f, fill = true),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row with owner avatar and field name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Owner avatar - sử dụng cách tương tự FieldCard
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            // Debug logs for owner avatar
                            println("🔄 DEBUG: RenterSearchResultCard - Owner Avatar Debug")
                            println("🔄 DEBUG: - field.ownerName: ${field.ownerName}")
                            println("🔄 DEBUG: - field.ownerAvatarUrl: ${field.ownerAvatarUrl?.take(50)}...")
                            println("🔄 DEBUG: - field.ownerAvatarUrl.isNullOrBlank(): ${field.ownerAvatarUrl.isNullOrBlank()}")
                            
                            if (!field.ownerAvatarUrl.isNullOrBlank()) {
                                if (field.ownerAvatarUrl.startsWith("data:image", ignoreCase = true)) {
                                    val bitmap = remember(field.ownerAvatarUrl) {
                                        try {
                                            val base64 = field.ownerAvatarUrl.substringAfter(",")
                                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        } catch (e: Exception) { 
                                            println("DEBUG: Error decoding owner avatar: ${e.message}")
                                            null 
                                        }
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Owner avatar",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Fallback to default avatar
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = field.ownerName.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // For URL images, use AsyncImage
                                    AsyncImage(
                                        model = field.ownerAvatarUrl,
                                        contentDescription = "Owner avatar",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(2.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                // Fallback avatar - hiển thị đẹp hơn
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Owner avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = field.ownerName.ifEmpty { "Chủ sân" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                                text = "Đặt lịch",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Field details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = field.address.ifEmpty { field.location },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Open hours
                        if (field.openHours.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.schedule),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                        Text(
                                    text = field.openHours,
                                    fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                        // Rating and reviews
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                        painter = painterResource(id = R.drawable.star),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${field.rating}/5.0 (${field.totalReviews} đánh giá)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Amenities
                        if (field.amenities.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                field.amenities.take(3).forEach { amenity ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                        Text(
                                            text = amenity,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
}

@Preview(showBackground = true)
@Composable
fun RenterSearchResultCardPreview() {
    FBTP_CNTheme {
        RenterSearchResultCard(
            field = SearchResultField(
                id = "field1",
                name = "Sân Tennis ABC",
                type = "Tennis",
                price = "120k/giờ",
                location = "Quận 1, TP.HCM",
                rating = 4.8f,
                distance = "2.5km",
                isAvailable = true,
                ownerName = "Nguyễn Văn A",
                ownerPhone = "0123456789",
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
