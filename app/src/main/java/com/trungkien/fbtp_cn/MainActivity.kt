package com.trungkien.fbtp_cn // Package chính của ứng dụng

import android.os.Bundle // Import Bundle để lưu trữ dữ liệu
import android.view.View // Import View để điều khiển giao diện
import androidx.activity.ComponentActivity // Activity chính của Compose
import androidx.activity.compose.setContent // Hàm thiết lập nội dung Compose
import androidx.activity.enableEdgeToEdge // Bật chế độ edge-to-edge
import androidx.compose.foundation.layout.fillMaxSize // Layout lấp đầy màn hình
import androidx.compose.ui.Modifier // Modifier để tùy chỉnh UI
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme // Theme tùy chỉnh
import androidx.core.view.WindowCompat // Tương thích với window
import com.trungkien.fbtp_cn.ui.screens.owner.OwnerMainScreen // Màn hình chính của owner

class MainActivity : ComponentActivity() { // Activity chính của ứng dụng
    override fun onCreate(savedInstanceState: Bundle?) { // Hàm vòng đời khởi tạo activity
        super.onCreate(savedInstanceState) // Gọi khởi tạo lớp cha
        WindowCompat.setDecorFitsSystemWindows(window, false) // Hiển thị nội dung tràn viền (edge-to-edge)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN // Ẩn status bar để toàn màn hình
        actionBar?.hide() // Ẩn action bar mặc định
        enableEdgeToEdge() // Bật xử lý inset hệ thống cho Compose
        setContent { // Bắt đầu nội dung Compose
            FBTP_CNTheme { // Áp dụng theme ứng dụng
                OwnerMainScreen(
                    modifier = Modifier.fillMaxSize() // Sử dụng OwnerMainScreen thay vì OwnerHomeScreen
                )
            }
        }
    }
}

