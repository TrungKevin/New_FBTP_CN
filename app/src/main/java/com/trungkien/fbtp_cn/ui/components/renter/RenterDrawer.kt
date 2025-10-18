package com.trungkien.fbtp_cn.ui.components.renter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.trungkien.fbtp_cn.model.User
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import coil.compose.AsyncImage
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterDrawer(
    currentUser: User?,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMapClick: () -> Unit,
    onBookingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RenterDrawerContent(
                avatarUrl = currentUser?.avatarUrl,
                userName = currentUser?.name ?: "Renter",
                unreadNotificationCount = unreadNotificationCount,
                onNotificationClick = {
                    onNotificationClick()
                    scope.launch { drawerState.close() }
                },
                onProfileClick = {
                    onProfileClick()
                    scope.launch { drawerState.close() }
                },
                onLogoutClick = {
                    onLogoutClick()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        // Content sẽ được wrap bởi ModalNavigationDrawer
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterDrawerContent(
    avatarUrl: String? = null,
    userName: String = "Renter",
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header với avatar và tên
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                if (avatarUrl.startsWith("data:image", ignoreCase = true)) {
                    val bitmap = remember(avatarUrl) {
                        try {
                            val base64 = avatarUrl.substringAfter(",")
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Người thuê sân",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color(0xFFE0E0E0)
        )
        
        // Notification Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNotificationClick),
            colors = CardDefaults.cardColors(
                containerColor = if (unreadNotificationCount > 0) 
                    Color(0xFFE8F5E8) else Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Thông báo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = if (unreadNotificationCount > 0) 
                            "$unreadNotificationCount thông báo mới" else "Không có thông báo mới",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unreadNotificationCount > 0) 
                            Color(0xFF00C853) else Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Hồ sơ cá nhân",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = "Xem và chỉnh sửa thông tin",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogoutClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Đăng xuất",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Thoát khỏi tài khoản",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer
        Text(
            text = "FBTP v3.0.2",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RenterDrawerContentPreview() {
    FBTP_CNTheme {
        RenterDrawerContent(
            userName = "Nguyễn Văn A",
            unreadNotificationCount = 5
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RenterDrawerContentNoNotificationPreview() {
    FBTP_CNTheme {
        RenterDrawerContent(
            userName = "Nguyễn Văn A",
            unreadNotificationCount = 0
        )
    }
}
