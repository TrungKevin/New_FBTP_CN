package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List

import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint
import com.trungkien.fbtp_cn.data.MockData
import com.trungkien.fbtp_cn.ui.components.renter.map.RenterLocationInput
import com.trungkien.fbtp_cn.ui.components.renter.map.RenterMapHeader
import com.trungkien.fbtp_cn.ui.components.renter.map.RenterMapView
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun RenterMapScreen(
    onBackClick: () -> Unit,
    onFieldClick: (com.trungkien.fbtp_cn.model.Field) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    var isListView by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf("123 ABC Street, District 1, HCMC") }
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
                // Map View (full screen)
                RenterMapView(
                    fields = try {
                        MockData.mockFields
                    } catch (e: Exception) {
                        emptyList()
                    },
                    currentLocation = currentLocation,
                    onFieldClick = onFieldClick,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Mini Header for Map Controls (when no main header)
                if (!showHeader) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // View Toggle Button
                            IconButton(
                                onClick = { isListView = !isListView },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isListView) Icons.Default.LocationOn else Icons.Default.List,
                                    contentDescription = if (isListView) "Chuyển sang bản đồ" else "Chuyển sang danh sách",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Filter Button
                            IconButton(
                                onClick = { /* TODO: Open filter dialog */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Bộ lọc",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Location Input (overlay on top)
                RenterLocationInput(
                    currentAddress = currentAddress,
                    onAddressChange = { address ->
                        currentAddress = address
                        // TODO: Geocode address to coordinates
                    },
                    onGpsClick = {
                        // TODO: Get current GPS location
                        currentLocation = GeoPoint(10.8231, 106.6297)
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .offset(y = (-8).dp)
                )
                
                // Quick Actions Overlay (bottom right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // My Location Button
                        FloatingActionButton(
                            onClick = {
                                // TODO: Center map to current location
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Vị trí của tôi"
                            )
                        }
                        
                        // Filter Button
                        FloatingActionButton(
                            onClick = {
                                // TODO: Open filter dialog
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Bộ lọc"
                            )
                        }
                    }
                }
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
