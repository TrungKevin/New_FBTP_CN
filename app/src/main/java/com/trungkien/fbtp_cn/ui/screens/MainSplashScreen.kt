package com.trungkien.fbtp_cn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.trungkien.fbtp_cn.ui.components.auth.LoginBottomSheet
import com.trungkien.fbtp_cn.ui.components.auth.RegisterBottomSheet
import com.trungkien.fbtp_cn.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.trungkien.fbtp_cn.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.AuthEvent
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSplashScreen(
    onNavigateToOwner: () -> Unit = {},
    onNavigateToRenter: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var showLoginSheet by remember { mutableStateOf(false) }
    var showRegisterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Observe auth state
    val authState by authViewModel.authState.collectAsState()
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when {
            authState.isSuccess -> {
                showRegisterSheet = false
                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                // Không tự đăng nhập; chỉ mở Login sheet để người dùng thao tác tiếp
                authViewModel.handleEvent(AuthEvent.ResetState)
                showLoginSheet = true
            }
            authState.error != null -> {
                Toast.makeText(context, authState.error ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                authViewModel.handleEvent(AuthEvent.ResetState)
            }
        }
    }

    // Main splash screen (logo is drawn inside LogoBreathing)
    SplashScreen(
        onLoginClick = { showLoginSheet = true },
        onRegisterClick = { showRegisterSheet = true },
        onCustomerServiceClick = { /* Handle customer service */ },
        onDownloadAppClick = { /* Handle download app */ },
        onTryPlayingClick = { /* Handle try playing */ },
        onComputerVersionClick = { /* Handle computer version */ }
    )

    // Login bottom sheet via component
    if (showLoginSheet) {
        LoginBottomSheet(
            onDismiss = { showLoginSheet = false },
            onLogin = { email, password ->
                authViewModel.handleEvent(AuthEvent.Login(email = email, password = password))
            },
            onGoogleLogin = {},
            onForgotPassword = {},
            onSwitchToRegister = {
                showLoginSheet = false
                showRegisterSheet = true
            }
        )
    }

    // Register bottom sheet via component
    if (showRegisterSheet) {
        RegisterBottomSheet(
            onDismiss = { showRegisterSheet = false },
            onRegister = { username, password, email, phone, role ->
                // Handle registration through ViewModel
                authViewModel.handleEvent(
                    AuthEvent.Register(
                        username = username,
                        password = password,
                        email = email,
                        phone = phone,
                        role = role
                    )
                )
            },
            onSwitchToLogin = {
                showRegisterSheet = false
                showLoginSheet = true
            },
            isLoading = authState.isLoading
        )
    }
    
    // Global loading dialog
    if (authState.isLoading) {
        LoadingDialog(message = "Đang xử lý...")
    }

    // Navigate by role after login success
    LaunchedEffect(authState.isSuccess, authState.role, authState.op) {
        if (authState.isSuccess && authState.role != null && authState.op == "LOGIN") {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            val role = authState.role!!.uppercase()
            showLoginSheet = false
            if (role == "OWNER") onNavigateToOwner() else onNavigateToRenter()
            authViewModel.handleEvent(AuthEvent.ResetState)
        }
        if (!authState.isLoading && authState.error != null && authState.op == "LOGIN") {
            Toast.makeText(context, authState.error ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}

@Preview
@Composable
fun MainSplashScreenPreview() {
    com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme {
        MainSplashScreen(
            onNavigateToOwner = { },
            onNavigateToRenter = { }
        )
    }
}
