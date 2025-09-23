package com.trungkien.fbtp_cn.ui.components.renter.bookinghis

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64

@Composable
fun RenterBookingCard(
    booking: Booking,
    onDetailClick: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Load field thật từ Firestore với error handling
    var field by remember(booking.fieldId) { mutableStateOf<Field?>(null) }
    var isLoadingField by remember { mutableStateOf(true) }
    
    LaunchedEffect(booking.fieldId) {
        isLoadingField = true
        val repo = FieldRepository()
        repo.getFieldById(booking.fieldId)
            .onSuccess { f -> 
                field = f
                isLoadingField = false
            }
            .onFailure { 
                isLoadingField = false
                // Log error hoặc xử lý lỗi nếu cần
            }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Enhanced Banner with gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ),
                contentAlignment = Alignment.TopStart
            ) {
                // Floating rating chip with glassmorphism effect
                if (!isLoadingField && field != null) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .graphicsLayer {
                                alpha = 0.95f
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.star),
                                contentDescription = "Đánh giá",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = field?.let { f ->
                                    if (f.averageRating > 0) {
                                        String.format("%.1f", f.averageRating)
                                    } else {
                                        "0.0"
                                    }
                                } ?: "0.0",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Enhanced center image: show field mainImage as banner if available
                if (isLoadingField) {
                    // Loading state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (field?.images?.mainImage?.isNotEmpty() == true) {
                    // Show field image using base64 decode like RenterSearchResultCard
                    val currentField = field
                    val rawImage = currentField!!.images.mainImage
                    val decodedImage = remember(rawImage) {
                        try {
                            println("🖼️ DEBUG: RenterBookingCard - Decoding image for field: ${currentField.name}")
                            println("🖼️ DEBUG: Raw image length: ${rawImage.length}")
                            println("🖼️ DEBUG: Raw image starts with data:image: ${rawImage.startsWith("data:image", ignoreCase = true)}")
                            
                            val base64String = if (rawImage.startsWith("data:image", ignoreCase = true)) {
                                rawImage.substringAfter(",")
                            } else {
                                rawImage
                            }
                            println("🖼️ DEBUG: Base64 string length: ${base64String.length}")
                            
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            println("🖼️ DEBUG: Decoded bytes length: ${imageBytes.size}")
                            
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            println("🖼️ DEBUG: Bitmap created: ${bitmap != null}")
                            if (bitmap != null) {
                                println("🖼️ DEBUG: Bitmap size: ${bitmap.width}x${bitmap.height}")
                            }
                            bitmap
                        } catch (e: Exception) {
                            println("❌ DEBUG: Error decoding image: ${e.message}")
                            e.printStackTrace()
                            null
                        }
                    }
                    
                    if (decodedImage != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decodedImage.asImageBitmap(),
                            contentDescription = "Hình ảnh sân ${currentField.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        // Fallback icon
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.95f),
                                shadowElevation = 8.dp,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.stadium),
                                    contentDescription = "Sân thể thao",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Fallback icon
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.stadium),
                                contentDescription = "Sân thể thao",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // Decorative corner accent
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 16.dp)
                        .size(6.dp)
                        .background(
                            Color(0xFF00C853).copy(alpha = 0.8f),
                            CircleShape
                        )
                )
            }

            // Enhanced Info section with better spacing and typography
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = field?.name ?: booking.fieldId,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = field?.sports?.firstOrNull() ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    FilledTonalButton(
                        onClick = { onDetailClick(booking) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Color(0xFF00C853).copy(alpha = 0.3f)
                            ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Chi tiết",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Enhanced info pills with better visual hierarchy
                val address = field?.address ?: "—"
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedInfoPill(
                            icon = R.drawable.location,
                            text = address,
                            modifier = Modifier.weight(1f),
                            iconTint = Color(0xFF2196F3)
                        )
                        EnhancedInfoPill(
                            icon = R.drawable.calendar,
                            text = "${booking.date}",
                            modifier = Modifier.weight(1f),
                            iconTint = Color(0xFF9C27B0)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EnhancedInfoPill(
                            icon = R.drawable.schedule,
                            text = "${booking.startAt} - ${booking.endAt}",
                            modifier = Modifier.weight(1f),
                            iconTint = Color(0xFFFF9800)
                        )
                        EnhancedInfoPill(
                            icon = R.drawable.money,
                            text = "${booking.totalPrice}₫",
                            modifier = Modifier.weight(1f),
                            iconTint = Color(0xFF4CAF50),
                            isPrice = true
                        )
                    }

                    // Enhanced status chip positioned separately
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        EnhancedStatusChip(status = booking.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedStatusChip(status: String) {
    data class StatusStyle(val bg: Color, val fg: Color, val label: String, val iconRes: Int)
    val style = when (status.lowercase()) {
        "confirmed", "upcoming" -> StatusStyle(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            "Sắp diễn ra",
            R.drawable.event
        )
        "completed" -> StatusStyle(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            "Hoàn thành",
            R.drawable.star
        )
        else -> StatusStyle(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            "Đã hủy",
            R.drawable.bookmark
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = style.bg,
        modifier = Modifier.shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            spotColor = style.fg.copy(alpha = 0.2f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = style.iconRes),
                contentDescription = null,
                tint = style.fg,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = style.label,
                color = style.fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EnhancedInfoPill(
    icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    isPrice: Boolean = false
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(44.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = iconTint.copy(alpha = 0.1f)
            ),
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = if (isPrice) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
                color = if (isPrice) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
private fun RenterBookingCardPreview() {
    FBTP_CNTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenterBookingCard(
                booking = Booking(
                    bookingId = "b1",
                    renterId = "user_001",
                    ownerId = "owner_001",
                    fieldId = "f1",
                    date = "2024-01-15",
                    startAt = "18:00",
                    endAt = "19:00",
                    slotsCount = 2,
                    minutes = 60,
                    basePrice = 250000,
                    servicePrice = 0,
                    totalPrice = 250000,
                    status = "PAID"
                ),
                onDetailClick = {}
            )
            RenterBookingCard(
                booking = Booking(
                    bookingId = "b2",
                    renterId = "user_001",
                    ownerId = "owner_002",
                    fieldId = "f2",
                    date = "2024-01-10",
                    startAt = "20:00",
                    endAt = "22:00",
                    slotsCount = 2,
                    minutes = 120,
                    basePrice = 400000,
                    servicePrice = 0,
                    totalPrice = 400000,
                    status = "DONE"
                ),
                onDetailClick = {}
            )
        }
    }
}