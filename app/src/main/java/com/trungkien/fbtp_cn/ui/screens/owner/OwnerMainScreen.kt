package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trungkien.fbtp_cn.ui.components.owner.OwnerBottomNavBar
import com.trungkien.fbtp_cn.ui.components.owner.OwnerTopAppBar
import com.trungkien.fbtp_cn.ui.components.owner.OwnerNavScreen
import com.trungkien.fbtp_cn.ui.screens.ModernEditProfileScreen
import com.trungkien.fbtp_cn.ui.screens.owner.AddFieldScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.ui.graphics.Color
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun OwnerMainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onLogoutToSplash: () -> Unit = {}
) {
    // State để quản lý tab đang được chọn
    var currentScreen by remember { mutableStateOf(OwnerNavScreen.Home) }
    
    // State để quản lý hiển thị TopAppBar (ẩn khi ở màn hình detail)
    var showTopAppBar by remember { mutableStateOf(true) }
    
    // State để quản lý hiển thị BottomNavBar (ẩn khi ở màn hình detail)
    var showBottomNavBar by remember { mutableStateOf(true) }
    
    // Shared FieldViewModel để chia sẻ dữ liệu fields giữa các màn hình
    val fieldViewModel: FieldViewModel = viewModel()
    val uiState by fieldViewModel.uiState.collectAsState()
    
    // AuthViewModel để lấy thông tin user (scoped theo Activity để chia sẻ giữa các màn)
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Debug logs để kiểm tra currentUser
    LaunchedEffect(currentUser) {
        println("🔄 DEBUG: OwnerMainScreen - currentUser changed")
        println("🔄 DEBUG: - currentUser: ${currentUser?.name}")
        println("🔄 DEBUG: - avatarUrl: ${currentUser?.avatarUrl?.take(50)}...")
        println("🔄 DEBUG: - avatarUrl length: ${currentUser?.avatarUrl?.length}")
        println("🔄 DEBUG: - authViewModel instance: ${authViewModel.hashCode()}")
    }
    
    // Debug logs để kiểm tra AuthViewModel instance
    LaunchedEffect(authViewModel) {
        println("🔄 DEBUG: OwnerMainScreen - AuthViewModel instance: ${authViewModel.hashCode()}")
    }
    
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
            println("🔄 OwnerMainScreen - Loading fields for ownerId: $ownerId")
            fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
        }
    }
    
    // 🔄 ĐỒNG BỘ DỮ LIỆU KHI CÓ THAY ĐỔI
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            if (success.contains("Thêm sân thành công") || 
                success.contains("Xóa sân thành công") ||
                success.contains("Cập nhật sân thành công")) {
                currentUser?.userId?.let { ownerId ->
                    println("🔄 OwnerMainScreen - Reloading fields after success: $success")
                    // Reload ngay lập tức không delay để đồng bộ
                    fieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
                }
            }
        }
    }
    
    Scaffold(
        modifier = modifier,
        containerColor = Color.White, // Thêm background màu trắng
        topBar = {
            if (showTopAppBar) {
                val currentUserForTopBar = authViewModel.currentUser.collectAsState().value
                println("🔄 DEBUG: OwnerMainScreen topBar - currentUserForTopBar: ${currentUserForTopBar?.name}")
                println("🔄 DEBUG: OwnerMainScreen topBar - avatarUrl: ${currentUserForTopBar?.avatarUrl?.take(50)}...")
                OwnerTopAppBar(
                    onMenuClick = { /* TODO: Xử lý menu */ },
                    onProfileClick = { 
                        currentScreen = OwnerNavScreen.Profile
                        navController.navigate("owner_profile") {
                            popUpTo("owner_home") { inclusive = true }
                        }
                    },
                    avatarUrl = currentUserForTopBar?.avatarUrl
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
            composable("owner_booking_list") {
                OwnerBookingListScreen(
                    onBookingClick = { bookingId ->
                        navController.navigate("owner_booking_detail/$bookingId")
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
            composable("owner_field_detail/{fieldId}") { backStackEntry ->
                val fieldId = backStackEntry.arguments?.getString("fieldId") ?: ""
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
                    fieldViewModel = fieldViewModel // TRUYỀN VIEWMODEL ĐỂ CHIA SẺ DỮ LIỆU
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