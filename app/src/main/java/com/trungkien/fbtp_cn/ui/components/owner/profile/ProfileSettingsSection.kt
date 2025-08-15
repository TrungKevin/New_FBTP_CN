package com.trungkien.fbtp_cn.ui.components.owner.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun ProfileSettingsSection(
    onAccountSettings: () -> Unit,
    onPrivacySettings: () -> Unit,
    onHelpSupport: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
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
                .padding(20.dp)
        ) {
            Text(
                text = "Cài đặt & Hỗ trợ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings items
            SettingsItem(
                icon = Icons.Default.Settings,
                title = "Cài đặt tài khoản",
                subtitle = "Thay đổi thông tin cá nhân",
                onClick = onAccountSettings,
                color = Color(0xFF607D8B),
                isVectorIcon = true
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )
            
            SettingsItem(
                icon = painterResource(id = R.drawable.privaci),
                title = "Quyền riêng tư",
                subtitle = "Cài đặt bảo mật",
                onClick = onPrivacySettings,
                color = Color(0xFF795548),
                isVectorIcon = false
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )
            
            SettingsItem(
                icon = painterResource(id = R.drawable.help),
                title = "Trợ giúp & Hỗ trợ",
                subtitle = "Liên hệ hỗ trợ khách hàng",
                onClick = onHelpSupport,
                color = Color(0xFF607D8B),
                isVectorIcon = false
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFE0E0E0)
            )
            
            // Logout button với màu đặc biệt
            SettingsItem(
                icon = painterResource(id = R.drawable.logout),
                title = "Đăng xuất",
                subtitle = "Thoát khỏi tài khoản",
                onClick = onLogout,
                color = Color(0xFFF44336),
                isLogout = true,
                isVectorIcon = false
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: Any, // Có thể là Painter hoặc ImageVector
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    color: Color,
    isLogout: Boolean = false,
    isVectorIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isLogout) {
                        Color(0xFFF44336).copy(alpha = 0.1f)
                    } else {
                        color.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isVectorIcon && icon is ImageVector) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isLogout) {
                        Color(0xFFF44336)
                    } else {
                        color
                    },
                    modifier = Modifier.size(20.dp)
                )
            } else if (!isVectorIcon && icon is Painter) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = if (isLogout) {
                        Color(0xFFF44336)
                    } else {
                        color
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isLogout) {
                    Color(0xFFF44336)
                } else {
                    Color(0xFF263238)
                }
            )
            
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = if (isLogout) {
                    Color(0xFFF44336).copy(alpha = 0.7f)
                } else {
                    Color(0xFF757575)
                }
            )
        }
        
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            tint = if (isLogout) {
                Color(0xFFF44336).copy(alpha = 0.5f)
            } else {
                Color(0xFFBDBDBD)
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview
@Composable
fun ProfileSettingsSectionPreview() {
    FBTP_CNTheme {
        ProfileSettingsSection(
            onAccountSettings = {},
            onPrivacySettings = {},
            onHelpSupport = {},
            onLogout = {}
        )
    }
}
