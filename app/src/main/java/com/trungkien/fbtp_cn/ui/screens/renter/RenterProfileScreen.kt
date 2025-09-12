package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.profile.MyReview
import com.trungkien.fbtp_cn.ui.components.renter.profile.RenterProfileHeader
import com.trungkien.fbtp_cn.ui.components.renter.profile.RenterProfileStats
import com.trungkien.fbtp_cn.ui.components.renter.profile.RenterProfileMenuSection
import com.trungkien.fbtp_cn.ui.components.renter.profile.RenterProfileSettingsSection
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RenterProfileScreen(
    onEditProfileClick: () -> Unit = {},
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
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RenterProfileHeader(
                    renterName = user?.name ?: "",
                    renterEmail = user?.email ?: "",
                    renterPhone = user?.phone ?: "",
                    renterAvatarUrl = user?.avatarUrl,
                    onEditProfile = onEditProfileClick
                )
            }

            item {
                RenterProfileStats(
                    totalBookings = 12,
                    favoriteCount = 4,
                    reviewsCount = 7,
                    onViewDetails = {}
                )
            }

            item {
                RenterProfileMenuSection(
                    onMyBookings = {},
                    onFavorites = {},
                    onNotifications = {}
                )
            }

            item {
                RenterProfileSettingsSection(
                    onAccountSettings = {},
                    onPrivacySettings = {},
                    onHelpSupport = {},
                    onLogout = onLogoutClick
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RenterProfileScreenPreview() {
    FBTP_CNTheme {
        RenterProfileScreen()
    }
}


