package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trungkien.fbtp_cn.ui.components.owner.OwnerBottomNavBar
import com.trungkien.fbtp_cn.ui.components.owner.OwnerTopAppBar
import com.trungkien.fbtp_cn.ui.components.owner.OwnerDrawerContent
import com.trungkien.fbtp_cn.ui.components.owner.OwnerNavScreen
import com.trungkien.fbtp_cn.ui.screens.ModernEditProfileScreen
import com.trungkien.fbtp_cn.ui.screens.owner.AddFieldScreen
import com.trungkien.fbtp_cn.ui.screens.owner.OwnerMapScreen
import com.trungkien.fbtp_cn.ui.screens.common.SimpleNotificationScreen
import com.trungkien.fbtp_cn.viewmodel.NotificationViewModel
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OwnerMainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onLogoutToSplash: () -> Unit = {}
) {
    val drawerState =
        rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNotificationScreen by remember { mutableStateOf(false) }
    // State để quản lý tab đang được chọn
    var currentScreen by remember { mutableStateOf(OwnerNavScreen.Home) }

    // State để quản lý hiển thị TopAppBar (ẩn khi ở màn hình detail)
    var showTopAppBar by remember { mutableStateOf(true) }

    // State để quản lý hiển thị BottomNavBar (ẩn khi ở màn hình detail)
    var showBottomNavBar by remember { mutableStateOf(true) }
    
    // State để track current route và disable drawer khi ở map screen
    var currentRoute by remember { mutableStateOf("") }
    val isMapScreen = currentRoute.startsWith("owner_field_map/")

    // Shared FieldViewModel để chia sẻ dữ liệu fields giữa các màn hình
    val fieldViewModel: FieldViewModel = viewModel()
    val uiState by fieldViewModel.uiState.collectAsState()

    // AuthViewModel để lấy thông tin user (scoped theo Activity để chia sẻ giữa các màn)
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    // 🔔 Unread notification count (realtime)
    val notificationRepository = remember { NotificationRepository() }
    var unreadCount by remember { mutableStateOf(0) }
    
    // Track current route để disable drawer khi ở map screen
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: ""
        }
    }
    
    LaunchedEffect(currentUser?.userId) {
        val uid = currentUser?.userId
        if (!uid.isNullOrBlank()) {
            notificationRepository.listenUnreadNotificationCount(uid).collectLatest { count ->
                unreadCount = count
            }
        } else {
            unreadCount = 0
        }
    }

    // Refresh current user UI-related state silently
    LaunchedEffect(currentUser) { /* no-op debug removed */ }

    // Refresh profile on resume to ensure latest avatar
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                authViewModel.fetchProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 🔥 TẬP TRUNG VIỆC LOAD DỮ LIỆU TẠI ĐÂY
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { ownerId ->
            fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
        }
    }

    // 🔄 ĐỒNG BỘ DỮ LIỆU KHI CÓ THAY ĐỔI
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            if (success.contains("Thêm sân thành công") ||
                success.contains("Xóa sân thành công") ||
                success.contains("Cập nhật sân thành công")
            ) {
                currentUser?.userId?.let { ownerId ->
                    // Reload ngay lập tức không delay để đồng bộ
                    fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
                }
            }
        }
    }

    // Hiển thị NotificationScreen nếu cần - KHÔNG có Scaffold chính
    if (showNotificationScreen) {
        SimpleNotificationScreen(
            onBackClick = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
            },
            onNavigateToHome = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Home
                navController.navigate("owner_home") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
            onNavigateToBooking = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Booking
                navController.navigate("owner_booking_list") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
            onNavigateToField = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Field
                navController.navigate("owner_field_list") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
            onNavigateToStats = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Stats
                navController.navigate("owner_stats") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
            onNavigateToProfile = {
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Profile
                navController.navigate("owner_profile") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
                   onNavigateToFieldDetail = { fieldId, initialTab ->
                       // Từ Notification vào chi tiết sân: ẩn TopAppBar & BottomBar
                       showNotificationScreen = false
                       showTopAppBar = false
                       showBottomNavBar = false
                       currentScreen = OwnerNavScreen.Field
                       navController.navigate("owner_field_detail/$fieldId?tab=$initialTab")
                   },
            onNavigateToMatches = {
                // ✅ NEW: Navigate to Matches tab specifically
                showNotificationScreen = false
                showTopAppBar = true
                showBottomNavBar = true
                currentScreen = OwnerNavScreen.Booking
                navController.navigate("owner_booking_list?tab=matches") {
                    popUpTo("owner_home") { inclusive = true }
                }
            },
            userId = currentUser?.userId ?: ""
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isMapScreen, // Disable drawer gestures khi ở map screen
            drawerContent = {
                OwnerDrawerContent(
                    avatarUrl = currentUser?.avatarUrl,
                    userName = currentUser?.name ?: "Owner",
                    unreadNotificationCount = unreadCount,
                    onNotificationClick = {
                        showNotificationScreen = true
                        showTopAppBar = false
                        showBottomNavBar = false
                    },
                    onProfileClick = {
                        currentScreen = OwnerNavScreen.Profile
                        navController.navigate("owner_profile") {
                            popUpTo("owner_home") { inclusive = true }
                        }
                    },
                    onLogoutClick = {
                        onLogoutToSplash()
                        scope.launch { drawerState.close() }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                modifier = modifier,
                containerColor = Color.White, // Thêm background màu trắng
                topBar = {
                    if (showTopAppBar) {
                        val currentUserForTopBar = authViewModel.currentUser.collectAsState().value
                        OwnerTopAppBar(
                            onMenuClick = {
                                if (!isMapScreen) { // Chỉ cho phép mở drawer khi không ở map screen
                                    scope.launch { drawerState.open() }
                                }
                            },
                            onProfileClick = {
                                currentScreen = OwnerNavScreen.Profile
                                navController.navigate("owner_profile") {
                                    popUpTo("owner_home") { inclusive = true }
                                }
                            },
                            onNotificationClick = {
                                showNotificationScreen = true
                            },
                            avatarUrl = currentUserForTopBar?.avatarUrl,
                            unreadNotificationCount = unreadCount
                        )
                    }
                },
                bottomBar = {
                    if (showBottomNavBar) {
                        OwnerBottomNavBar(
                            currentScreen = currentScreen,
                            onTabSelected = { screen ->
                                currentScreen = screen
                                when (screen) {
                                    OwnerNavScreen.Home -> {
                                        navController.navigate("owner_home") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }

                                    OwnerNavScreen.Field -> {
                                        navController.navigate("owner_field_list") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }

                                    OwnerNavScreen.Booking -> {
                                        navController.navigate("owner_booking_list") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }

                                    OwnerNavScreen.Stats -> {
                                        navController.navigate("owner_stats") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }

                                    OwnerNavScreen.Profile -> {
                                        navController.navigate("owner_profile") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "owner_home",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // Màn hình trang chủ
                    composable("owner_home") {
                        OwnerHomeScreen(
                            onNavigateToFieldDetail = { fieldId ->
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_field_detail/$fieldId")
                            },
                            onNavigateToAddField = {
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_add_field")
                            },
                            onNavigateToFieldList = {
                                currentScreen = OwnerNavScreen.Field
                                navController.navigate("owner_field_list") {
                                    popUpTo("owner_home") { inclusive = true }
                                }
                            },
                            onNavigateToBookingList = {
                                currentScreen = OwnerNavScreen.Booking
                                navController.navigate("owner_booking_list") {
                                    popUpTo("owner_home") { inclusive = true }
                                }
                            },
                            onNavigateToStats = {
                                currentScreen = OwnerNavScreen.Stats
                                navController.navigate("owner_stats") {
                                    popUpTo("owner_home") { inclusive = true }
                                }
                            },
                            fieldViewModel = fieldViewModel // TRUYỀN VIEWMODEL ĐỂ CHIA SẺ DỮ LIỆU
                        )
                    }

                    // Màn hình quản lý sân - TỰ ĐỘNG LOAD DỮ LIỆU TỪ FIREBASE
                    composable("owner_field_list") {
                        OwnerFieldManagementScreen(
                            onFieldClick = { fieldId ->
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_field_detail/$fieldId")
                            },
                            onAddFieldClick = {
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_add_field")
                            },
                            fieldViewModel = fieldViewModel // TRUYỀN VIEWMODEL ĐỂ LOAD DỮ LIỆU
                        )
                    }

                    // Màn hình danh sách đặt sân
                    composable("owner_booking_list") { backStackEntry ->
                        val tabParam = backStackEntry.arguments?.getString("tab")
                        val initialTab = if (tabParam == "matches") {
                            com.trungkien.fbtp_cn.ui.screens.owner.MainTab.Matches
                        } else {
                            com.trungkien.fbtp_cn.ui.screens.owner.MainTab.Bookings
                        }
                        
                        OwnerBookingListScreen(
                            onBookingClick = { bookingId ->
                                navController.navigate("owner_booking_detail/$bookingId")
                            },
                            onMatchClick = { matchId ->
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_match_detail/$matchId")
                            },
                            initialTab = initialTab
                        )
                    }

                    // Màn hình chi tiết trận đấu
                    composable("owner_match_detail/{matchId}") { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                        OwnerMatchDetailScreen(
                            matchId = matchId,
                            navController = navController,
                            onRestoreBars = {
                                showTopAppBar = true
                                showBottomNavBar = true
                            }
                        )
                    }

                    // Màn hình thống kê
                    composable("owner_stats") {
                        OwnerStatisticsScreen()
                    }

                    // Màn hình hồ sơ
                    composable("owner_profile") {
                        OwnerProfileScreen(
                            onEditProfileClick = {
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_edit_profile")
                            },
                            onNavigateToFieldList = {
                                currentScreen = OwnerNavScreen.Field
                                navController.navigate("owner_field_list") {
                                    popUpTo("owner_profile") { inclusive = true }
                                }
                            },
                            onNavigateToBookingList = {
                                currentScreen = OwnerNavScreen.Booking
                                navController.navigate("owner_booking_list") {
                                    popUpTo("owner_profile") { inclusive = true }
                                }
                            },
                            onNavigateToStats = {
                                currentScreen = OwnerNavScreen.Stats
                                navController.navigate("owner_stats") {
                                    popUpTo("owner_profile") { inclusive = true }
                                }
                            },
                            onLogoutClick = onLogoutToSplash
                        )
                    }

                    // Màn hình chỉnh sửa hồ sơ
                    composable("owner_edit_profile") {
                        ModernEditProfileScreen(
                            onBackClick = {
                                showTopAppBar = true
                                showBottomNavBar = true
                                // Refresh profile to ensure latest avatar is loaded
                                authViewModel.fetchProfile()
                                // ✅ FIX: Sử dụng popBackStack thay vì navigateUp để tránh lỗi back stack
                                try {
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    println("❌ ERROR: Navigation error: ${e.message}")
                                    // Fallback: navigate to profile
                                    navController.navigate("owner_profile") {
                                        popUpTo("owner_home") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    // Màn hình chi tiết sân
                    composable("owner_field_detail/{fieldId}?tab={tab}") { backStackEntry ->
                        val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                        val initialTab = backStackEntry.arguments?.getString("tab") ?: "info"
                        OwnerFieldDetailScreen(
                            fieldId = fieldId,
                            onBackClick = {
                                // Reset về màn hình quản lý sân và hiển thị lại TopAppBar + BottomBar
                                currentScreen = OwnerNavScreen.Field
                                showTopAppBar = true
                                showBottomNavBar = true
                                // ✅ FIX: Sử dụng popBackStack thay vì navigateUp để tránh lỗi back stack
                                try {
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    println("❌ ERROR: Navigation error: ${e.message}")
                                    // Fallback: navigate to field list
                                    navController.navigate("owner_field_list") {
                                        popUpTo("owner_home") { inclusive = true }
                                    }
                                }
                            },
                            fieldViewModel = fieldViewModel, // TRUYỀN VIEWMODEL ĐỂ CHIA SẺ DỮ LIỆU
                            initialTab = initialTab,
                            onLocationClick = {
                                // Navigate đến map screen
                                showTopAppBar = false
                                showBottomNavBar = false
                                navController.navigate("owner_field_map/$fieldId")
                            }
                        )
                    }

                    // Màn hình chi tiết đặt sân
                    composable("owner_booking_detail/{bookingId}") { backStackEntry ->
                        val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                        // TODO: Tạo OwnerBookingDetailScreen
                        // OwnerBookingDetailScreen(
                        //     bookingId = bookingId,
                        //     onBackClick = {
                        //         navController.navigateUp()
                        //     }
                        // )
                    }

                    // Màn hình thêm sân mới
                    composable("owner_add_field") {
                        AddFieldScreen(
                            onBackClick = {
                                showTopAppBar = true
                                showBottomNavBar = true
                                // ✅ FIX: Sử dụng popBackStack thay vì navigateUp để tránh lỗi back stack
                                try {
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    println("❌ ERROR: Navigation error: ${e.message}")
                                    // Fallback: navigate to field list
                                    navController.navigate("owner_field_list") {
                                        popUpTo("owner_home") { inclusive = true }
                                    }
                                }
                            },
                            onFieldAdded = { fieldId ->
                                // Sau khi thêm sân thành công, chuyển về màn hình quản lý sân
                                showTopAppBar = true
                                showBottomNavBar = true
                                currentScreen = OwnerNavScreen.Field
                                navController.navigate("owner_field_list") {
                                    popUpTo("owner_home") { inclusive = true }
                                }
                            },
                            fieldViewModel = fieldViewModel // TRUYỀN VIEWMODEL ĐỂ CHIA SẺ DỮ LIỆU
                        )
                    }

                    // Màn hình xem vị trí sân trên bản đồ
                    composable("owner_field_map/{fieldId}") { backStackEntry ->
                        val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
                        val field = uiState.fields.find { it.fieldId == fieldId }
                        
                        if (field != null) {
                            OwnerMapScreen(
                                field = field,
                                onBackClick = {
                                    // Không reset showTopAppBar và showBottomNavBar - giữ nguyên trạng thái ẩn
                                    // ✅ FIX: Sử dụng popBackStack thay vì navigateUp để tránh lỗi back stack
                                    try {
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        println("❌ ERROR: Navigation error: ${e.message}")
                                        // Fallback: navigate to field detail
                                        navController.navigate("owner_field_detail/$fieldId?tab=info") {
                                            popUpTo("owner_home") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        } else {
                            // Fallback nếu không tìm thấy field
                            Text("Không tìm thấy thông tin sân")
                        }
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnerMainScreenPreview() {
    FBTP_CNTheme {
        OwnerMainScreen()
    }
}
