package com.trungkien.fbtp_cn.ui.components.renter.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.res.painterResource

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

@Composable
fun RenterMapHeader(
    isListView: Boolean,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    onViewToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = GreenPrimary
                )
            }

            // Title
            Text(
                text = "Bản đồ",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Right side buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter button
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Bộ lọc",
                        tint = GreenPrimary
                    )
                }

                // View toggle button
                IconButton(onClick = onViewToggleClick) {
                    if (isListView) {
                        Icon(
                            painter = painterResource(id = R.drawable.map),
                            contentDescription = "Chuyển sang bản đồ",
                            tint = GreenPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Chuyển sang danh sách",
                            tint = GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RenterMapHeaderPreview() {
    FBTP_CNTheme {
        RenterMapHeader(
            isListView = false,
            onBackClick = {},
            onFilterClick = {},
            onViewToggleClick = {}
        )
    }
}
