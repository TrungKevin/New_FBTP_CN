package com.trungkien.fbtp_cn.ui.components.owner // Package cho các component của owner

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTopAppBar(
    onMenuClick: () -> Unit = {}, // Callback khi nhấn menu
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    unreadNotificationCount: Int = 0
) {
    // Debug logs để kiểm tra avatarUrl
    LaunchedEffect(avatarUrl) {
        println("🔄 DEBUG: OwnerTopAppBar - avatarUrl changed")
        println("🔄 DEBUG: - avatarUrl: ${avatarUrl?.take(50)}...")
        println("🔄 DEBUG: - avatarUrl length: ${avatarUrl?.length}")
        println("🔄 DEBUG: - avatarUrl.isNullOrEmpty(): ${avatarUrl.isNullOrEmpty()}")
        println("🔄 DEBUG: - avatarUrl starts with data:image: ${avatarUrl?.startsWith("data:image", ignoreCase = true)}")
    }
    CenterAlignedTopAppBar( // Thanh ứng dụng căn giữa
        modifier = modifier, // Modifier tùy chỉnh
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                // Hiển thị chấm đỏ khi có thông báo chưa đọc
                androidx.compose.material3.BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            androidx.compose.material3.Badge(
                                containerColor = Color.Red
                            ) {}
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = { // Tiêu đề (logo)
            Image( // Component hình ảnh
                painter = painterResource(id = R.drawable.title), // Sử dụng logo
                contentDescription = "Logo", // Mô tả cho accessibility
                modifier = Modifier // Modifier cho logo
                    .height(40.dp) // Chiều cao hợp lý hơn
                    .width(120.dp) // Chiều rộng tỷ lệ hợp lý
                    .clip(RoundedCornerShape(12.dp)) // Bo góc nhỏ hơn
            )
        },
        actions = { // Các hành động (chỉ profile)
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                if (!avatarUrl.isNullOrEmpty()) {
                    println("🔄 DEBUG: OwnerTopAppBar - Displaying avatar")
                    if (avatarUrl.startsWith("data:image", ignoreCase = true)) {
                        println("🔄 DEBUG: OwnerTopAppBar - Processing base64 avatar")
                        val bitmap = remember(avatarUrl) {
                            try {
                                val base64 = avatarUrl.substringAfter(",")
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                println("🔄 DEBUG: OwnerTopAppBar - Bitmap decoded: ${decodedBitmap != null}")
                                decodedBitmap
                            } catch (e: Exception) { 
                                println("❌ DEBUG: OwnerTopAppBar - Error decoding bitmap: ${e.message}")
                                null 
                            }
                        }
                        if (bitmap != null) {
                            println("🔄 DEBUG: OwnerTopAppBar - Displaying decoded bitmap")
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile avatar",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            println("🔄 DEBUG: OwnerTopAppBar - Fallback to AsyncImage")
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile avatar",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        println("🔄 DEBUG: OwnerTopAppBar - Displaying URL avatar")
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    println("🔄 DEBUG: OwnerTopAppBar - No avatar, showing default icon")
                    Icon( // Component icon
                        imageVector = Icons.Default.AccountCircle, // Icon profile
                        contentDescription = "Profile", // Mô tả cho accessibility
                        tint = Color(0xFF00C853), // Màu xanh lá đậm cho icon
                        modifier = Modifier.size(24.dp) // Kích thước icon chuẩn
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors( // Tùy chỉnh màu sắc
            containerColor = Color.White, // Màu nền trắng
            navigationIconContentColor = Color(0xFF00C853), // Màu icon navigation
            actionIconContentColor = Color(0xFF00C853), // Màu icon action
            titleContentColor = Color.Transparent // Màu title (transparent vì dùng Image)
        )
    )
}

@Preview // Đánh dấu đây là hàm preview
@Composable // Đánh dấu đây là một composable function
fun OwnerTopAppBarPreview() { // Hàm preview cho thanh ứng dụng trên
    FBTP_CNTheme { // Sử dụng theme tùy chỉnh
        OwnerTopAppBar(
            onMenuClick = {},
            onProfileClick = {}
        )
    }
}