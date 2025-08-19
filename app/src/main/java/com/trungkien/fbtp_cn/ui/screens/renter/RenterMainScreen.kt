package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.RenterNavScreen
import com.trungkien.fbtp_cn.ui.components.renter.RenterBottomNavBar
import com.trungkien.fbtp_cn.ui.components.renter.RenterTopAppBar
import com.trungkien.fbtp_cn.ui.screens.renter.RenterHomeScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterMainScreen(
    modifier: Modifier = Modifier
) {
    var selectedScreen by remember { mutableStateOf(RenterNavScreen.Home) }
    
    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            // Luôn giữ một TopAppBar giống nhau trên mọi tab như Owner
            RenterTopAppBar(
                onMenuClick = { /* TODO open drawer or menu */ },
                onProfileClick = { selectedScreen = RenterNavScreen.Profile }
            )
        },
        bottomBar = {
            RenterBottomNavBar(
                currentScreen = selectedScreen,
                onTabSelected = { screen ->
                    selectedScreen = screen
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                RenterNavScreen.Home -> {
                    RenterHomeScreen(
                        onFieldClick = { fieldId ->
                            // TODO: Navigate to field detail
                        },
                        onSearchClick = {
                            selectedScreen = RenterNavScreen.Search
                        },
                        onMapClick = {
                            selectedScreen = RenterNavScreen.Map
                        }
                    )
                }
                RenterNavScreen.Search -> {
                    var showOrder by remember { mutableStateOf<String?>(null) }
                    if (showOrder == null) {
                        RenterFieldSearchScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBookClick = { fieldId -> showOrder = fieldId }
                        )
                    } else {
                        RenterOrderDetailScreen(
                            fieldId = showOrder!!,
                            onBackClick = { showOrder = null },
                            onBookNow = { /* TODO: proceed booking */ }
                        )
                    }
                }
                RenterNavScreen.Map -> {
                    RenterMapScreen(
                        onBackClick = { selectedScreen = RenterNavScreen.Home },
                        onFieldClick = { field ->
                            // TODO: Navigate to field detail
                        },
                        modifier = Modifier.fillMaxSize(),
                        showHeader = false // Không hiển thị header riêng vì đã có RenterTopAppBar
                    )
                }
                RenterNavScreen.Booking -> {
                    RenterBookingScreen(modifier = Modifier.fillMaxSize())
                }
                RenterNavScreen.Profile -> {
                    RenterProfileScreen(
                        modifier = Modifier.fillMaxSize(),
                        onEditProfileClick = { /* TODO: Navigate to edit profile */ },
                        onLogoutClick = { /* TODO: logout */ }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = true)
@Composable
fun RenterMainScreenPreview() {
    FBTP_CNTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RenterMainScreen()
        }
    }
}
