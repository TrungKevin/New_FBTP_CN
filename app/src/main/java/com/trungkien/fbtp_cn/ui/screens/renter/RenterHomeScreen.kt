package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.home.*
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.trungkien.fbtp_cn.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterHomeScreen(
    modifier: Modifier = Modifier,
    onFieldClick: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header với search bar
        RenterHomeHeader(
            onSearchClick = onSearchClick,
            onMapClick = onMapClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Map preview nhỏ
        RenterMapPreview(
            onMapClick = onMapClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sân nổi bật
        RenterFeaturedFields(
            onFieldClick = onFieldClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lịch sử đặt sân gần đây
        RenterRecentBookings(
            onFieldClick = onFieldClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick actions
        RenterQuickActions(
            onSearchClick = onSearchClick,
            onMapClick = onMapClick
        )
        
        Spacer(modifier = Modifier.height(100.dp)) // Space cho bottom navigation
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun RenterHomeScreenPreview() {
    FBTP_CNTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RenterHomeScreen(
                onFieldClick = { fieldId ->
                    // Preview callback
                },
                onSearchClick = {
                    // Preview callback
                },
                onMapClick = {
                    // Preview callback
                }
            )
        }
    }
}
