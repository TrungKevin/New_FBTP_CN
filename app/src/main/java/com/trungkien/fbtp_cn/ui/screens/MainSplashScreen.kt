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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSplashScreen(
    onNavigateToOwner: () -> Unit = {},
    onNavigateToRenter: () -> Unit = {}
) {
    var showLoginSheet by remember { mutableStateOf(false) }
    var showRegisterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            onLogin = { username, _ ->
                showLoginSheet = false
                if (username.contains("owner")) onNavigateToOwner() else onNavigateToRenter()
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
            onRegister = { _, _ ->
                showRegisterSheet = false
                onNavigateToRenter()
            },
            onSwitchToLogin = {
                showRegisterSheet = false
                showLoginSheet = true
            }
        )
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
