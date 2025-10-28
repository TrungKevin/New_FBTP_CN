package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileHeader
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileStats
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileMenuSection
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileSettingsSection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun OwnerProfileScreen(
    onEditProfileClick: () -> Unit,
    onNavigateToFieldList: () -> Unit,
    onNavigateToBookingList: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    val context = LocalContext.current
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (user == null) authViewModel.fetchProfile()
    }
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(
                    ownerName = user?.name ?: "",
                    ownerEmail = user?.email ?: "",
                    ownerPhone = user?.phone ?: "",
                    onEditProfile = onEditProfileClick,
                    avatarUrl = user?.avatarUrl
                )
            }

            
//            item {
//                ProfileMenuSection(
//                    onMyFields = onNavigateToFieldList,
//                    onMyBookings = onNavigateToBookingList,
//                    onStatistics = onNavigateToStats,
//                    onNotifications = { /* TODO: Navigate to notifications */ }
//                )
//            }
            
            item {
                ProfileSettingsSection(
                    onAccountSettings = { showAccountDialog = true },
                    onLogout = onLogoutClick
                )
            }
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Cài đặt tài khoản") },
            text = { Text("Bạn có thể đổi mật khẩu qua email hoặc xóa tài khoản.") },
            confirmButton = {
                Button(onClick = {
                    val email = user?.email ?: ""
                    if (email.isNotBlank()) {
                        authViewModel.handleEvent(com.trungkien.fbtp_cn.viewmodel.AuthEvent.ForgotPassword(email))
                        Toast.makeText(context, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Không tìm thấy email tài khoản", Toast.LENGTH_SHORT).show()
                    }
                    showAccountDialog = false
                }) { Text("Gửi email đổi mật khẩu") }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false; showDeleteConfirm = true }) { Text("Xóa tài khoản") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirm = false },
            title = { Text("Xác nhận xóa tài khoản") },
            text = { Text("Hành động này không thể hoàn tác. Bạn có chắc chắn?") },
            confirmButton = {
                Button(enabled = !isDeleting, onClick = {
                    isDeleting = true
                    authViewModel.deleteAccount { ok, msg ->
                        isDeleting = false
                        showDeleteConfirm = false
                        if (ok) {
                            Toast.makeText(context, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show()
                            onLogoutClick()
                        } else {
                            Toast.makeText(context, msg ?: "Xóa tài khoản thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text(if (isDeleting) "Đang xóa..." else "Xác nhận") }
            },
            dismissButton = { TextButton(enabled = !isDeleting, onClick = { showDeleteConfirm = false }) { Text("Hủy") } }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOwnerProfileScreen() {
    FBTP_CNTheme {
        OwnerProfileScreen(
            onEditProfileClick = { /* Preview callback */ },
            onNavigateToFieldList = { /* Preview callback */ },
            onNavigateToBookingList = { /* Preview callback */ },
            onNavigateToStats = { /* Preview callback */ }
        )
    }
}
