package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.ui.components.renter.map.RenterMapHeader
import com.trungkien.fbtp_cn.ui.components.renter.map.RenterGoogleMapView
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterMapScreen(
    onBackClick: () -> Unit,
    onFieldClick: (com.trungkien.fbtp_cn.model.Field) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    var isListView by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("Đang tải...") }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(GeoPoint(10.8231, 106.6297)) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header - chỉ hiển thị khi showHeader = true
        if (showHeader) {
            RenterMapHeader(
                isListView = isListView,
                onBackClick = onBackClick,
                onFilterClick = {
                    // TODO: Open filter dialog
                },
                onViewToggleClick = {
                    isListView = !isListView
                }
            )
        }

        if (isListView) {
            // List View - Enhanced with modern design
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // List Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "🗺️ Danh sách sân gần bạn",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tìm kiếm sân thể thao theo vị trí và khoảng cách",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // List Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) { index ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Field Icon
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "⚽",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Field Info
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Sân ${index + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Football • 2.5km",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "120.000₫/giờ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                                        // Action Button
                        Button(
                            onClick = { /* TODO: Book field */ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Đặt lịch")
                        }
                            }
                        }
                    }
                }
            }
        } else {
            // Map View with enhanced overlay layout
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Google Map View (full screen)
                RenterGoogleMapView(
                    onFieldClick = onFieldClick,
                    modifier = Modifier.fillMaxSize()
                )
                
                // NOTE: Location input và Quick Actions đã được xóa để tránh trùng lặp với RenterGoogleMapView
            }
        }
    }
}

@Preview
@Composable
private fun RenterMapScreenPreview() {
    FBTP_CNTheme {
        RenterMapScreen(
            onBackClick = {},
            onFieldClick = {}
        )
    }
}
