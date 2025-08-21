package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileHeader
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileStats
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileMenuSection
import com.trungkien.fbtp_cn.ui.components.owner.profile.ProfileSettingsSection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun OwnerProfileScreen(
    onEditProfileClick: () -> Unit,
    onNavigateToFieldList: () -> Unit,
    onNavigateToBookingList: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val user = authViewModel.currentUser.collectAsState().value
    LaunchedEffect(Unit) {
        if (user == null) authViewModel.fetchProfile()
    }
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(
                    ownerName = user?.name ?: "",
                    ownerEmail = user?.email ?: "",
                    ownerPhone = user?.phone ?: "",
                    onEditProfile = onEditProfileClick
                )
            }
            
            item {
                ProfileStats(
                    totalFields = 2,
                    totalBookings = 15,
                    totalRevenue = 2500000,
                    onViewDetails = { /* TODO: Navigate to detailed stats */ }
                )
            }
            
            item {
                ProfileMenuSection(
                    onMyFields = onNavigateToFieldList,
                    onMyBookings = onNavigateToBookingList,
                    onStatistics = onNavigateToStats,
                    onNotifications = { /* TODO: Navigate to notifications */ }
                )
            }
            
            item {
                ProfileSettingsSection(
                    onAccountSettings = { /* TODO: Navigate to account settings */ },
                    onPrivacySettings = { /* TODO: Navigate to privacy settings */ },
                    onHelpSupport = { /* TODO: Navigate to help support */ },
                    onLogout = onLogoutClick
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewOwnerProfileScreen() {
    FBTP_CNTheme {
        OwnerProfileScreen(
            onEditProfileClick = { /* Preview callback */ },
            onNavigateToFieldList = { /* Preview callback */ },
            onNavigateToBookingList = { /* Preview callback */ },
            onNavigateToStats = { /* Preview callback */ }
        )
    }
}
