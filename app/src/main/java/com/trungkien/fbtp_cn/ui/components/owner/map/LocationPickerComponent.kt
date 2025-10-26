package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.service.GeocodingService
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Component đơn giản để chọn vị trí sân trên Google Maps
 */
@Composable
fun LocationPickerComponent(
    field: Field,
    onLocationSelected: (GeoLocation) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf(field.geo) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isLoadingGeocoding by remember { mutableStateOf(false) }
    var geocodingError by remember { mutableStateOf<String?>(null) }
    var isLocationConfirmed by remember { mutableStateOf(false) }
    var lastGeocodedAddress by remember { mutableStateOf("") }
    
    val geocodingService = remember { GeocodingService() }
    
    // ✅ FIX: Geocoding lại khi địa chỉ thay đổi (không chỉ khi tọa độ là 0)
    LaunchedEffect(field.address) {
        // Chỉ geocoding khi: 
        // 1. Địa chỉ không rỗng
        // 2. Địa chỉ thay đổi so với lần geocoding trước
        if (field.address.isNotEmpty() && field.address != lastGeocodedAddress) {
            isLoadingGeocoding = true
            geocodingError = null
            
            try {
                println("🗺️ LocationPicker - Geocoding address: ${field.address}")
                val result = geocodingService.geocodeAddress(field.address)
                if (result != null) {
                    println("🗺️ LocationPicker - Geocoding success: lat=${result.lat}, lng=${result.lng}")
                    selectedLocation = result
                    lastGeocodedAddress = field.address // Lưu địa chỉ đã geocoding
                    onLocationSelected(result)
                    
                    // Cập nhật marker position ngay lập tức
                    googleMap?.let { map ->
                        val geoPoint = LatLng(result.lat, result.lng)
                        marker?.remove()
                        marker = map.addMarker(
                            MarkerOptions()
                                .position(geoPoint)
                                .title(field.name)
                                .snippet(field.address)
                                .draggable(true)
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                    }
                } else {
                    geocodingError = "Không tìm thấy vị trí cho địa chỉ này"
                    println("🗺️ LocationPicker - Geocoding failed for: ${field.address}")
                }
            } catch (e: Exception) {
                geocodingError = "Lỗi khi tìm vị trí: ${e.message}"
                println("🗺️ LocationPicker - Geocoding error: ${e.message}")
            } finally {
                isLoadingGeocoding = false
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Map View - Bản đồ to hơn và rõ ràng hơn
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp) // Giảm padding để bản đồ to hơn
        ) {
            AndroidView(
                factory = { context ->
                    println("🗺️ LocationPicker - Creating Google MapView...")
                    MapView(context).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { map ->
                            googleMap = map
                            
                            // Cấu hình map
                            map.uiSettings.isZoomControlsEnabled = false
                            map.uiSettings.isCompassEnabled = false
                            map.uiSettings.isMapToolbarEnabled = false
                            
                            val initialLocation = if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                                LatLng(selectedLocation.lat, selectedLocation.lng)
                            } else {
                                LatLng(10.8231, 106.6297) // HCMC coordinates
                            }
                            
                            println("🗺️ LocationPicker - Initial location: lat=${initialLocation.latitude}, lng=${initialLocation.longitude}")
                            
                            // Tạo marker có thể kéo
                            marker = map.addMarker(
                                MarkerOptions()
                                    .position(initialLocation)
                                    .title(field.name)
                                    .snippet("Kéo marker để điều chỉnh vị trí chính xác")
                                    .draggable(true)
                            )
                            
                            // Center map on marker
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))
                            
                            // Xử lý khi marker được kéo
                            map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                                override fun onMarkerDragStart(marker: Marker) {
                                    println("🗺️ Marker drag started")
                                }
                                
                                override fun onMarkerDrag(marker: Marker) {
                                    // Cập nhật vị trí trong quá trình kéo
                                    selectedLocation = GeoLocation(
                                        lat = marker.position.latitude,
                                        lng = marker.position.longitude,
                                        geohash = ""
                                    )
                                    isLocationConfirmed = false // Reset confirmation khi kéo
                                }
                                
                                override fun onMarkerDragEnd(marker: Marker) {
                                    // Cập nhật vị trí cuối cùng
                                    selectedLocation = GeoLocation(
                                        lat = marker.position.latitude,
                                        lng = marker.position.longitude,
                                        geohash = ""
                                    )
                                    isLocationConfirmed = false
                                    println("🗺️ Marker drag ended at: ${marker.position.latitude}, ${marker.position.longitude}")
                                }
                            })
                            
                            // Handle map tap
                            map.setOnMapClickListener { latLng ->
                                println("🗺️ LocationPicker - Map tapped at: lat=${latLng.latitude}, lng=${latLng.longitude}")
                                
                                // Update selected location
                                selectedLocation = GeoLocation(
                                    lat = latLng.latitude,
                                    lng = latLng.longitude,
                                    geohash = ""
                                )
                                
                                // Update marker position
                                marker?.remove()
                                marker = map.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(field.name)
                                        .snippet("Kéo marker để điều chỉnh vị trí chính xác")
                                        .draggable(true)
                                )
                                
                                // Center map on new location
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                                
                                // Reset confirmation state when location changes
                                isLocationConfirmed = false
                                
                                println("🗺️ LocationPicker - Marker updated to: lat=${latLng.latitude}, lng=${latLng.longitude}")
                            }
                            
                            println("🗺️ LocationPicker - Map setup completed")
                        }
                        
                        mapView = this
                    }
                },
                update = { mapView ->
                    // Update marker when selectedLocation changes
                    if (MapMarkerUtils.isValidLocation(selectedLocation)) {
                        googleMap?.let { map ->
                            val geoPoint = LatLng(selectedLocation.lat, selectedLocation.lng)
                            marker?.remove()
                            marker = map.addMarker(
                                MarkerOptions()
                                    .position(geoPoint)
                                    .title(field.name)
                                    .snippet("Kéo marker để điều chỉnh vị trí chính xác")
                                    .draggable(true)
                            )
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // My Location Button
            if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                FloatingActionButton(
                    onClick = {
                        googleMap?.let { map ->
                            val geoPoint = LatLng(selectedLocation.lat, selectedLocation.lng)
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(start = 16.dp, end = 16.dp, bottom = 120.dp) // Di chuyển lên cao hơn để tránh đè zoom controls
                        .size(56.dp),
                    containerColor = GreenPrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Vị trí đã chọn",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Location info overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Vị trí đã chọn",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Vị trí đã chọn",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = field.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = field.address,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Hiển thị trạng thái geocoding
                    if (isLoadingGeocoding) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Đang tìm tọa độ...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (geocodingError != null) {
                        Text(
                            text = geocodingError ?: "Lỗi geocoding",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Tọa độ: ${String.format("%.6f", selectedLocation.lat)}, ${String.format("%.6f", selectedLocation.lng)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when {
                            isLocationConfirmed -> "✅ Vị trí đã được xác nhận"
                            selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0 -> "📍 Kéo marker hoặc chạm vào bản đồ để điều chỉnh vị trí"
                            else -> "📍 Chạm vào bản đồ để đánh dấu vị trí chính xác"
                        },
                        fontSize = 12.sp,
                        color = if (isLocationConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Action buttons - Logic button thông minh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isLocationConfirmed) {
                // Button "Xác nhận vị trí" khi chưa xác nhận
                Button(
                    onClick = { 
                        isLocationConfirmed = true
                        onLocationSelected(selectedLocation)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    ),
                    enabled = selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0
                ) {
                    Text("Xác nhận vị trí")
                }
            } else {
                // Button "Tiếp tục" khi đã xác nhận
                Button(
                    onClick = { onLocationSelected(selectedLocation) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    )
                ) {
                    Text("Tiếp tục")
                }
            }
        }
    }
}