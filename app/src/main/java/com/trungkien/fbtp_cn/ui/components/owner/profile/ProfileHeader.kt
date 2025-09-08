package com.trungkien.fbtp_cn.ui.components.owner.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.remember

@Composable
fun ProfileHeader(
    ownerName: String,
    ownerEmail: String,
    ownerPhone: String,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar và tên
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C853)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !avatarUrl.isNullOrEmpty() -> {
                        if (avatarUrl.startsWith("data:image", ignoreCase = true)) {
                            val bitmap = remember(avatarUrl) {
                                try {
                                    val base64 = avatarUrl.substringAfter(",")
                                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) { null }
                            }
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Owner Avatar",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Owner Avatar",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Owner Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = ownerName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = ownerName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Thông tin liên hệ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Email
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ownerEmail,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
                
                // Phone
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ownerPhone,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nút chỉnh sửa
            OutlinedButton(
                onClick = onEditProfile,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF00C853)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF00C853)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chỉnh sửa hồ sơ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun ProfileHeaderPreview() {
    FBTP_CNTheme {
        ProfileHeader(
            ownerName = "Kien",
            ownerEmail = "kien@example.com",
            ownerPhone = "0926666357",
            onEditProfile = {}
        )
    }
}
