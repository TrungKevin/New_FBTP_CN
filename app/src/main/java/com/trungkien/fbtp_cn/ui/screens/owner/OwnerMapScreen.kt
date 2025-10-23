package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.ui.components.owner.map.FieldLocationBottomSheet
import com.trungkien.fbtp_cn.ui.components.owner.map.LocationPickerComponent
import com.trungkien.fbtp_cn.ui.components.owner.map.OwnerLocationInfoCard
import com.trungkien.fbtp_cn.ui.components.owner.map.OwnerMapHeader
import com.trungkien.fbtp_cn.ui.components.owner.map.OwnerMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerMapScreen(
    field: Field,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentField by remember { mutableStateOf(field) }
    var isLocationPickerMode by remember { mutableStateOf(false) }
    var isAdjustmentMode by remember { mutableStateOf(false) } // Chế độ điều chỉnh vị trí marker
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) } // State để hiển thị bottom sheet
    var selectedLocation by remember { mutableStateOf<GeoLocation?>(null) } // Lưu vị trí được chọn
    
    val fieldRepository = remember { FieldRepository() }
    
    // Nếu chưa có tọa độ, tự động chuyển sang chế độ chọn vị trí
    LaunchedEffect(field.geo) {
        if (field.geo.lat == 0.0 && field.geo.lng == 0.0) {
            isLocationPickerMode = true
        }
    }
    
    if (isLocationPickerMode) {
        // Chế độ chọn vị trí - Sử dụng Scaffold để đảm bảo map nằm dưới header
        Scaffold(
            topBar = {
                OwnerMapHeader(
                    field = currentField,
                    onBackClick = {
                        isLocationPickerMode = false
                    },
                    onEditLocation = null // Không hiển thị nút edit trong chế độ picker
                )
            }
        ) { paddingValues ->
            LocationPickerComponent(
                field = currentField,
                onLocationSelected = { newLocation ->
                    selectedLocation = newLocation // Lưu vị trí được chọn
                    isLoading = true
                    errorMessage = null
                },
                onCancel = {
                    isLocationPickerMode = false
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
        
        // Xử lý cập nhật vị trí trong LaunchedEffect riêng biệt
        LaunchedEffect(isLoading, selectedLocation) {
            if (isLoading && selectedLocation != null) {
                try {
                    val newLocation = selectedLocation!!
                    
                    val result = fieldRepository.updateFieldLocation(currentField.fieldId, newLocation)
                    if (result.isSuccess) {
                        // Cập nhật local state
                        currentField = currentField.copy(geo = newLocation)
                        isLocationPickerMode = false
                        selectedLocation = null // Reset selectedLocation
                        
                    } else {
                        errorMessage = "Không thể cập nhật vị trí: ${result.exceptionOrNull()?.message}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
        
        // Hiển thị loading hoặc error
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lỗi",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Có lỗi xảy ra",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { errorMessage = null }
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
        }
    } else {
        // Chế độ xem vị trí
        Scaffold(
            topBar = {
                        OwnerMapHeader(
                            field = currentField,
                            onBackClick = onBackClick,
                            onEditLocation = {
                                isLocationPickerMode = true
                            },
                            onResetLocation = {
                                // Reset tọa độ về 0.0, 0.0 để buộc geocoding lại
                                val resetField = currentField.copy(geo = GeoLocation(lat = 0.0, lng = 0.0))
                                currentField = resetField
                                isLocationPickerMode = true
                            },
                            onAdjustLocation = {
                                // Chuyển sang chế độ điều chỉnh vị trí marker
                                isAdjustmentMode = true
                            }
                        )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Map View
                OwnerMapView(
                    field = currentField,
                    onMarkerClick = {
                        if (isAdjustmentMode) {
                            // Trong chế độ điều chỉnh, không hiển thị bottom sheet
                            return@OwnerMapView
                        }
                        showBottomSheet = true // Hiển thị bottom sheet khi click marker
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                
                // Hiển thị hướng dẫn khi ở chế độ điều chỉnh
                if (isAdjustmentMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Chế độ điều chỉnh vị trí",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chạm vào bản đồ để di chuyển marker đến vị trí chính xác",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isAdjustmentMode = false
                                    }
                                ) {
                                    Text("Hủy")
                                }
                                Button(
                                    onClick = {
                                        // TODO: Implement marker adjustment logic
                                        isAdjustmentMode = false
                                    }
                                ) {
                                    Text("Xác nhận")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom Sheet hiển thị khi click vào marker
        if (showBottomSheet) {
            FieldLocationBottomSheet(
                field = currentField,
                onDismiss = {
                    showBottomSheet = false
                }
            )
        }
    }
}
