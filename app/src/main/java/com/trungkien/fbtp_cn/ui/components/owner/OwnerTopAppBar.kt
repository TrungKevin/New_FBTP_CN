package com.trungkien.fbtp_cn.ui.components.owner // Package cho các component của owner

import androidx.compose.foundation.Image // Import Image
import androidx.compose.foundation.layout.height // Import height layout
import androidx.compose.foundation.layout.width // Import width layout
import androidx.compose.foundation.layout.padding // Import padding layout
import androidx.compose.foundation.layout.size // Import size layout
import androidx.compose.foundation.shape.RoundedCornerShape // Import RoundedCornerShape
import androidx.compose.material.icons.Icons // Import Icons
import androidx.compose.material.icons.filled.AccountCircle // Import AccountCircle icon
import androidx.compose.material.icons.filled.Menu // Import Menu icon
import androidx.compose.material3.CenterAlignedTopAppBar // Import CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api // Import ExperimentalMaterial3Api
import androidx.compose.material3.Icon // Import Icon
import androidx.compose.material3.IconButton // Import IconButton
import androidx.compose.material3.MaterialTheme // Import MaterialTheme
import androidx.compose.material3.TopAppBarDefaults // Import TopAppBarDefaults
import androidx.compose.runtime.Composable // Import Composable
import androidx.compose.ui.Modifier // Import Modifier
import androidx.compose.ui.draw.clip // Import clip
import androidx.compose.ui.graphics.Color // Import Color
import androidx.compose.ui.res.painterResource // Import painterResource
import androidx.compose.ui.tooling.preview.Preview // Import Preview
import androidx.compose.ui.unit.dp // Import dp
import com.trungkien.fbtp_cn.R // Import R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme // Import FBTP_CNTheme

@OptIn(ExperimentalMaterial3Api::class) // Sử dụng API thực nghiệm của Material3
@Composable // Đánh dấu đây là một composable function
fun OwnerTopAppBar( // Hàm tạo thanh ứng dụng trên cho owner
    onMenuClick: () -> Unit, // Callback khi nhấn menu
    onProfileClick: () -> Unit, // Callback khi nhấn profile
    modifier: Modifier = Modifier // Modifier tùy chỉnh
) {
    CenterAlignedTopAppBar( // Thanh ứng dụng căn giữa
        modifier = modifier, // Modifier tùy chỉnh
        navigationIcon = { // Icon điều hướng (menu)
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp) // Kích thước chuẩn cho icon button
                    .padding(4.dp) // Padding nhỏ để tạo khoảng cách
            ) {
                Icon( // Component icon
                    imageVector = Icons.Default.Menu, // Icon menu
                    contentDescription = "Menu", // Mô tả cho accessibility
                    tint = Color(0xFF00C853), // Màu xanh lá đậm cho icon
                    modifier = Modifier.size(24.dp) // Kích thước icon chuẩn
                )
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
        actions = { // Các hành động (profile)
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(48.dp) // Kích thước chuẩn cho icon button
                    .padding(4.dp) // Padding nhỏ để tạo khoảng cách
            ) {
                Icon( // Component icon
                    imageVector = Icons.Default.AccountCircle, // Icon profile
                    contentDescription = "Profile", // Mô tả cho accessibility
                    tint = Color(0xFF00C853), // Màu xanh lá đậm cho icon
                    modifier = Modifier.size(24.dp) // Kích thước icon chuẩn
                )
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
        OwnerTopAppBar( // Gọi component thanh ứng dụng trên
            onMenuClick = {}, // Callback rỗng cho preview
            onProfileClick = {} // Callback rỗng cho preview
        )
    }
}