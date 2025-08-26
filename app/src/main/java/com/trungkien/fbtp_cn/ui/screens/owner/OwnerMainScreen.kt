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
import com.trungkien.fbtp_cn.ui.screens.EditProfileScreen
import com.trungkien.fbtp_cn.ui.screens.owner.AddFieldScreen
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.ui.graphics.Color
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
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
    
    Scaffold(
        modifier = modifier,
        containerColor = Color.White, // Thêm background màu trắng
        topBar = {
            if (showTopAppBar) {
                OwnerTopAppBar(
                    onMenuClick = { /* TODO: Xử lý menu */ },
                    onProfileClick = { 
                        currentScreen = OwnerNavScreen.Profile
                        navController.navigate("owner_profile") {
                            popUpTo("owner_home") { inclusive = true }
                        }
                    }
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
                    }
                )
            }
            
            // Màn hình quản lý sân - SỬ DỤNG CÙNG FIELDVIEWMODEL VỚI OWNERHOMESCREEN
            composable("owner_field_list") {
                // Sử dụng shared FieldViewModel để lấy dữ liệu fields từ Firebase
                val uiState by fieldViewModel.uiState.collectAsState()
                val fields = uiState.fields // Lấy fields từ Firebase
                
                // Lấy AuthViewModel trong composable context
                val authViewModel: com.trungkien.fbtp_cn.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()
                
                // Load fields khi màn hình được navigate đến (nếu chưa có dữ liệu)
                LaunchedEffect(currentUser?.userId, fields.isEmpty()) {
                    val userId = currentUser?.userId
                    if (userId != null && fields.isEmpty()) {
                        println("DEBUG: Loading fields for ownerId: $userId in OwnerMainScreen (fields was empty)")
                        fieldViewModel.handleEvent(com.trungkien.fbtp_cn.viewmodel.FieldEvent.LoadFieldsByOwner(userId))
                    }
                }
                
                // Debug logging
                LaunchedEffect(fields) {
                    println("DEBUG: OwnerMainScreen - fields count: ${fields.size}")
                    if (fields.isNotEmpty()) {
                        println("DEBUG: OwnerMainScreen - first field: ${fields.first().name}")
                    }
                }
                
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
                    fields = fields, // TRUYỀN DỮ LIỆU FIELDS TỪ FIREBASE
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
                EditProfileScreen(
                    onBackClick = {
                        showTopAppBar = true
                        showBottomNavBar = true
                        navController.navigateUp()
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
                        navController.navigateUp()
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
                        navController.navigateUp()
                    },
                    onFieldAdded = { fieldId ->
                        // Sau khi thêm sân thành công, chuyển về màn hình quản lý sân
                        showTopAppBar = true
                        showBottomNavBar = true
                        currentScreen = OwnerNavScreen.Field
                        navController.navigate("owner_field_list") {
                            popUpTo("owner_home") { inclusive = true }
                        }
                    }
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