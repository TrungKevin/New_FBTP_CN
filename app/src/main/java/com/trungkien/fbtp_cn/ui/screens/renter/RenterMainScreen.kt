package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.components.renter.RenterNavScreen
import com.trungkien.fbtp_cn.ui.components.renter.RenterBottomNavBar
import com.trungkien.fbtp_cn.ui.components.renter.RenterTopAppBar
import com.trungkien.fbtp_cn.ui.components.renter.RenterDrawerContent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.repository.NotificationRepository
import com.trungkien.fbtp_cn.viewmodel.NotificationViewModel
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
    var showNotificationScreen by remember { mutableStateOf(false) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // ViewModels
    val fieldViewModel: FieldViewModel = viewModel()
    val bookingViewModel: com.trungkien.fbtp_cn.viewmodel.BookingViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val notificationRepository = NotificationRepository()
    val notificationViewModel: NotificationViewModel = viewModel { 
        NotificationViewModel(notificationRepository) 
    }
    
    val currentUser = authViewModel.currentUser.collectAsState().value
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    
    // Load pricing rules when we have a fieldId
    LaunchedEffect(activeOrderDetailFieldId) {
        activeOrderDetailFieldId?.let { fieldId ->
            fieldViewModel.handleEvent(com.trungkien.fbtp_cn.viewmodel.FieldEvent.LoadPricingRulesByFieldId(fieldId))
        }
    }
    
    // Load notifications when user is available
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            notificationViewModel.handle(
                com.trungkien.fbtp_cn.viewmodel.NotificationEvent.LoadNotifications(userId)
            )
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RenterDrawerContent(
                avatarUrl = currentUser?.avatarUrl,
                userName = currentUser?.name ?: "Renter",
                unreadNotificationCount = notificationUiState.unreadCount,
                onNotificationClick = {
                    showNotificationScreen = true
                    scope.launch { drawerState.close() }
                },
                onProfileClick = {
                    selectedScreen = RenterNavScreen.Profile
                    scope.launch { drawerState.close() }
                },
                onLogoutClick = {
                    onLogoutToSplash()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = Color.White,
            topBar = {
                // Ẩn TopAppBar khi đang hiển thị RenterOrderDetailScreen trong tab Search
                val shouldShowTop = !(selectedScreen == RenterNavScreen.Search && activeOrderDetailFieldId != null) && !isEditingProfile && !showNotificationScreen
                if (shouldShowTop) {
                    RenterTopAppBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onProfileClick = { selectedScreen = RenterNavScreen.Profile },
                        onNotificationClick = { showNotificationScreen = true },
                        avatarUrl = currentUser?.avatarUrl,
                        unreadNotificationCount = notificationUiState.unreadCount
                    )
                }
            },
            bottomBar = {
                if (!isEditingProfile && !showNotificationScreen) {
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
            } else if (showNotificationScreen) {
                RenterNotificationScreen(
                    onBackClick = { showNotificationScreen = false },
                    onNavigateToBooking = { 
                        showNotificationScreen = false
                        selectedScreen = RenterNavScreen.Booking
                    },
                    onNavigateToField = { 
                        showNotificationScreen = false
                        selectedScreen = RenterNavScreen.Search
                    },
                    onNavigateToProfile = { 
                        showNotificationScreen = false
                        selectedScreen = RenterNavScreen.Profile
                    },
                    onNavigateToFieldDetail = { fieldId, initialTab ->
                        showNotificationScreen = false
                        activeOrderDetailFieldId = fieldId
                        selectedScreen = RenterNavScreen.Search
                    },
                    userId = currentUser?.userId ?: ""
                )
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
                        },
                        onBookClick = { fieldId ->
                            // Khi bấm ĐẶT LỊCH ở Featured → mở chi tiết sân giống Search
                            activeOrderDetailFieldId = fieldId
                            selectedScreen = RenterNavScreen.Search
                        },
                        onHistoryClick = {
                            selectedScreen = RenterNavScreen.Booking
                        },
                        onProfileClick = {
                            selectedScreen = RenterNavScreen.Profile
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
                        var goCheckout by remember(activeOrderDetailFieldId) { mutableStateOf(false) }
                        if (!goCheckout) {
                            RenterOrderDetailScreen(
                                fieldId = activeOrderDetailFieldId!!,
                                onBackClick = { activeOrderDetailFieldId = null },
                                onBookNow = {
                                    // ✅ Reset cờ lastCreatedId để lần mở checkout mới không tự navigate
                                    bookingViewModel.handle(com.trungkien.fbtp_cn.viewmodel.BookingEvent.ResetLastCreatedId)
                                    // ✅ RESET navigation flags before opening checkout
                                    goCheckout = true
                                }
                            )
                        } else {
                            // Get actual price from pricing rules
                            val uiState by fieldViewModel.uiState.collectAsState()
                            val fieldPrice = uiState.pricingRules.firstOrNull()?.price?.toInt() ?: 150000
                            RenterBookingCheckoutScreen(
                                fieldId = activeOrderDetailFieldId!!,
                                basePricePerHour = fieldPrice,
                                onBackClick = {
                                    bookingViewModel.handle(com.trungkien.fbtp_cn.viewmodel.BookingEvent.ResetLastCreatedId)
                                    goCheckout = false
                                },
                                onConfirmBooking = {
                                    // Sau khi VM tạo booking thành công, chuyển sang tab Booking
                                    selectedScreen = RenterNavScreen.Booking
                                    activeOrderDetailFieldId = null
                                    goCheckout = false
                                    bookingViewModel.handle(com.trungkien.fbtp_cn.viewmodel.BookingEvent.ResetLastCreatedId)
                                }
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
