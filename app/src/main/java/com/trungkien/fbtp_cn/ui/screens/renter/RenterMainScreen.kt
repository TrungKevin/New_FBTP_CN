package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.components.renter.RenterNavScreen
import com.trungkien.fbtp_cn.ui.components.renter.RenterBottomNavBar
import com.trungkien.fbtp_cn.ui.components.renter.RenterTopAppBar
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.ui.screens.ModernEditProfileScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.ui.graphics.Color

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterMainScreen(
    modifier: Modifier = Modifier,
    onLogoutToSplash: () -> Unit = {}
) {
    var selectedScreen by remember { mutableStateOf(RenterNavScreen.Home) }
    // If not null, we're viewing order detail inside Search tab
    var activeOrderDetailFieldId by remember { mutableStateOf<String?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }
    
    // FieldViewModel để load pricing rules
    val fieldViewModel: FieldViewModel = viewModel()
    
    // Load pricing rules when we have a fieldId
    LaunchedEffect(activeOrderDetailFieldId) {
        activeOrderDetailFieldId?.let { fieldId ->
            fieldViewModel.handleEvent(com.trungkien.fbtp_cn.viewmodel.FieldEvent.LoadPricingRulesByFieldId(fieldId))
        }
    }
    
    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            // Ẩn TopAppBar khi đang hiển thị RenterOrderDetailScreen trong tab Search
            val shouldShowTop = !(selectedScreen == RenterNavScreen.Search && activeOrderDetailFieldId != null) && !isEditingProfile
            val authViewModel: AuthViewModel = viewModel()
            val currentUser = authViewModel.currentUser.collectAsState().value
            if (shouldShowTop) {
                RenterTopAppBar(
                    onMenuClick = { /* TODO open drawer or menu */ },
                    onProfileClick = { selectedScreen = RenterNavScreen.Profile },
                    avatarUrl = currentUser?.avatarUrl
                )
            }
        },
        bottomBar = {
            if (!isEditingProfile) {
                RenterBottomNavBar(
                    currentScreen = selectedScreen,
                    onTabSelected = { screen ->
                        selectedScreen = screen
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isEditingProfile) {
                ModernEditProfileScreen(onBackClick = { isEditingProfile = false })
            } else when (selectedScreen) {
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
                    if (activeOrderDetailFieldId == null) {
                        RenterFieldSearchScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBookClick = { fieldId -> activeOrderDetailFieldId = fieldId }
                        )
                    } else {
                        var goCheckout by remember { mutableStateOf(false) }
                        if (!goCheckout) {
                            RenterOrderDetailScreen(
                                fieldId = activeOrderDetailFieldId!!,
                                onBackClick = { activeOrderDetailFieldId = null },
                                onBookNow = { goCheckout = true }
                            )
                        } else {
                            // Get actual price from pricing rules
                            val uiState by fieldViewModel.uiState.collectAsState()
                            val fieldPrice = uiState.pricingRules.firstOrNull()?.price?.toInt() ?: 150000
                            RenterBookingCheckoutScreen(
                                fieldId = activeOrderDetailFieldId!!,
                                basePricePerHour = fieldPrice,
                                onBackClick = { goCheckout = false },
                                onConfirmBooking = { /* TODO: finalize booking */ }
                            )
                        }
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
                        onEditProfileClick = { isEditingProfile = true },
                        onLogoutClick = onLogoutToSplash
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
