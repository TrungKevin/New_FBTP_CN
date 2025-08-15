package com.trungkien.fbtp_cn.ui.components.owner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.CommonShadows
import kotlin.random.Random

// Helper function to get random avatar
private fun getRandomAvatar(): Int {
    return when (Random.nextInt(3)) {
        0 -> R.drawable.avta1
        1 -> R.drawable.avta2
        2 -> R.drawable.avta3
        else -> R.drawable.avta1
    }
}

// Enhanced colors
private val GreenAccent = Color(0xFF00C853)
private val PurpleAccent = Color(0xFF7C4DFF)
private val YellowAccent = Color(0xFFFFB300)
private val RedAccent = Color(0xFFFF5252)

@Composable
fun FieldCard(
    field: Field,
    ownerAvatar: Int = getRandomAvatar(),
    rating: Float = 5.0f,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onViewDetailsClick: () -> Unit = {},
    onClick: (Field) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    val favoriteIcon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
    val favoriteColor by animateColorAsState(
        targetValue = if (isFavorite) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "favoriteColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .scale(cardScale)
            .clickable {
                onClick(field)
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = CommonShadows.Card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section - Enhanced Image with better overlay (55% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f, fill = true)
            ) {
                // Background field image
                Image(
                    painter = painterResource(id = R.drawable.court1),
                    contentDescription = "Field image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Enhanced gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
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
                        // Enhanced rating badge
                        Surface(
                            shape = RoundedCornerShape(25.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = CommonShadows.Button,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = YellowAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%.1f", rating),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Enhanced action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Favorite button with animation
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shadowElevation = CommonShadows.Button,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onFavoriteClick() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = favoriteIcon,
                                        contentDescription = "Favorite",
                                        tint = favoriteColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Share button
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                shadowElevation = CommonShadows.Button,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onShareClick() }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                                         // Bottom section with badges
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(horizontal = 16.dp, vertical = 12.dp),
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         // Enhanced badges
                         Surface(
                             shape = RoundedCornerShape(20.dp),
                             color = GreenAccent,
                             shadowElevation = CommonShadows.Badge
                         ) {
                             Text(
                                 text = "Đơn ngày",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = Color.White,
                                 modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                             )
                         }

                         Surface(
                             shape = RoundedCornerShape(20.dp),
                             color = PurpleAccent,
                             shadowElevation = CommonShadows.Badge
                         ) {
                             Text(
                                 text = "Sự kiện",
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = Color.White,
                                 modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                             )
                         }
                     }
                }
            }

            // Bottom section - Enhanced Info (45% height)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f, fill = true),
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
                        // Enhanced owner avatar
                        Surface(
                            shape = CircleShape,
                            shadowElevation = CommonShadows.Button,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Image(
                                painter = painterResource(id = ownerAvatar),
                                contentDescription = "Owner avatar",
                                modifier = Modifier
                                    .size(42.dp)
                                    .padding(2.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
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
                             Text(
                                 text = field.type,
                                 fontSize = 13.sp,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                                 fontWeight = FontWeight.Medium,
                                 maxLines = 1,
                                 overflow = TextOverflow.Ellipsis
                             )
                         }

                        // Enhanced view details button
                        Button(
                            onClick = onViewDetailsClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowAccent
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = CommonShadows.Button),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "XEM CHI TIẾT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                                         // Enhanced info grid - Show all information clearly
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
                                 text = field.distance,
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
                                 text = field.operatingHours,
                                 modifier = Modifier.weight(1f)
                             )
                             InfoItemWithDrawable(
                                 iconRes = R.drawable.stadium,
                                 text = field.type,
                                 modifier = Modifier.weight(1f)
                             )
                         }

                         // Third row: Price (full width for emphasis)
                         InfoItemWithDrawable(
                             iconRes = R.drawable.payments,
                             text = "${String.format("%,d", field.price)} VND/giờ",
                             modifier = Modifier.fillMaxWidth()
                         )

                         // Fourth row: Address (full width for complete display)
                         InfoItem(
                             icon = Icons.Default.LocationOn,
                             text = field.address,
                             modifier = Modifier.fillMaxWidth()
                         )
                     }

                    // Action button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Enhanced view details button
                        Button(
                            onClick = onViewDetailsClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YellowAccent
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = CommonShadows.Button),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "XEM CHI TIẾT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            fontSize = if (iconRes == R.drawable.payments) 14.sp else 12.sp,
            color = if (iconRes == R.drawable.payments)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (iconRes == R.drawable.payments) FontWeight.Bold else FontWeight.Medium,
            maxLines = if (iconRes == R.drawable.payments) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EnhancedFieldCardPreview() {
    FBTP_CNTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FieldCard(
                field = Field(
                    id = "1",
                    name = "POC Pickleball Club",
                    type = "Pickleball Premium",
                    price = 150000,
                    imageUrl = "https://via.placeholder.com/400x200/0000FF/FFFFFF?text=PBL+Court",
                    status = "Available",
                    isAvailable = true,
                    address = "25 Tú Xương, P. Tăng Nhơn Phú B, TP. Thủ Đức",
                    operatingHours = "05:00 - 23:00",
                    contactPhone = "0926666357",
                    distance = "835.3m"
                ),
                ownerAvatar = R.drawable.avta1,
                rating = 4.8f,
                isFavorite = true
            )

            FieldCard(
                field = Field(
                    id = "2",
                    name = "Tennis Court Vinhomes",
                    type = "Tennis Standard",
                    price = 200000,
                    imageUrl = "https://via.placeholder.com/400x200/00FF00/FFFFFF?text=Tennis+Court",
                    status = "Booked",
                    isAvailable = false,
                    address = "Vinhomes Grand Park, TP. Thủ Đức",
                    operatingHours = "06:00 - 22:00",
                    contactPhone = "0901234567",
                    distance = "1.2km"
                ),
                ownerAvatar = R.drawable.avta2,
                rating = 4.5f,
                isFavorite = false
            )
        }
    }
}