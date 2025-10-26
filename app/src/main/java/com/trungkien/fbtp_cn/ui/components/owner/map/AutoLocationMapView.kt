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
import com.trungkien.fbtp_cn.ui.components.owner.map.MapMarkerUtils

/**
 * Component hiển thị Google Maps với marker tự động khi có địa chỉ
 * Sử dụng Google Maps API thay vì OpenStreetMap
 */
@Composable
fun AutoLocationMapView(
    field: Field,
    onLocationSelected: (GeoLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var currentLocation by remember { mutableStateOf(field.geo) }
    var isLoadingGeocoding by remember { mutableStateOf(false) }
    var geocodingError by remember { mutableStateOf<String?>(null) }
    
    val geocodingService = remember { GeocodingService() }

    // Tự động geocoding khi có địa chỉ
    LaunchedEffect(field.address) {
        if (field.address.isNotEmpty() && (currentLocation.lat == 0.0 || currentLocation.lng == 0.0)) {
            isLoadingGeocoding = true
            geocodingError = null
            
            try {
                println("🗺️ AutoLocationMapView - Auto geocoding address: ${field.address}")
                val result = geocodingService.geocodeAddress(field.address)
                if (result != null) {
                    println("🗺️ AutoLocationMapView - Geocoding success: lat=${result.lat}, lng=${result.lng}")
                    currentLocation = result
                    onLocationSelected(result)

                    // Cập nhật map ngay lập tức với marker mới
                    googleMap?.let { map ->
                        val geoPoint = LatLng(result.lat, result.lng)
                        
                        // Xóa marker cũ
                        marker?.remove()
                        
                        // Tạo marker mới
                        marker = map.addMarker(
                            MarkerOptions()
                                .position(geoPoint)
                                .title(field.name)
                                .snippet(field.address)
                        )
                        
                        // Center map on marker
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                        
                        println("🗺️ AutoLocationMapView - Geocoding complete: Marker centered at lat=${result.lat}, lng=${result.lng}")
                    }
                } else {
                    geocodingError = "Không tìm thấy vị trí cho địa chỉ này"
                    println("🗺️ AutoLocationMapView - Geocoding failed for: ${field.address}")
                }
            } catch (e: Exception) {
                geocodingError = "Lỗi khi tìm vị trí: ${e.message}"
                println("🗺️ AutoLocationMapView - Geocoding error: ${e.message}")
            } finally {
                isLoadingGeocoding = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Map View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { map ->
                            googleMap = map
                            
                            // Cấu hình map
                            map.uiSettings.isZoomControlsEnabled = true
                            map.uiSettings.isCompassEnabled = true
                            map.uiSettings.isMapToolbarEnabled = false
                            
                            val initialLocation = if (currentLocation.lat != 0.0 && currentLocation.lng != 0.0) {
                                LatLng(currentLocation.lat, currentLocation.lng)
                            } else {
                                LatLng(10.8231, 106.6297) // HCMC coordinates
                            }
                            
                            // Tạo marker ban đầu với icon tương ứng loại sân
                            marker = map.addMarker(
                                MarkerOptions()
                                    .position(initialLocation)
                                    .title(field.name)
                                    .snippet(field.address)
                                    .icon(MapMarkerUtils.getSportMarkerBitmapDescriptor(context, field.sports.firstOrNull() ?: "TENNIS", 150))
                                    .anchor(0.5f, 0.5f) // Center anchor
                            )
                            
                            // Center map on marker
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 16f))
                            
                            println("🗺️ AutoLocationMapView - Map initialized with marker at: lat=${initialLocation.latitude}, lng=${initialLocation.longitude}")
                        }
                        
                        mapView = this
                    }
                },
                update = { mapView ->
                    // Update marker when currentLocation changes
                    if (MapMarkerUtils.isValidLocation(currentLocation)) {
                        googleMap?.let { map ->
                            val geoPoint = LatLng(currentLocation.lat, currentLocation.lng)
                            
                            // Chỉ cập nhật nếu vị trí marker thực sự thay đổi
                            if (marker?.position != geoPoint) {
                                marker?.position = geoPoint
                                marker?.title = field.name
                                marker?.snippet = field.address
                                marker?.setIcon(MapMarkerUtils.getSportMarkerBitmapDescriptor(mapView.context, field.sports.firstOrNull() ?: "TENNIS", 200))
                                
                                // Center map on marker
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                                
                                println("🗺️ AutoLocationMapView - Marker position updated to: lat=${currentLocation.lat}, lng=${currentLocation.lng}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Center Location Button
            if (currentLocation.lat != 0.0 && currentLocation.lng != 0.0) {
                FloatingActionButton(
                    onClick = {
                        googleMap?.let { map ->
                            val geoPoint = LatLng(currentLocation.lat, currentLocation.lng)
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
        }
        
        // Location info overlay
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header với icon và trạng thái
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoadingGeocoding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Đang tìm vị trí...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (geocodingError != null) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Lỗi tìm vị trí",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Vị trí đã chọn",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Vị trí đã được xác định",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                    Text(
                        text = "Đang tìm tọa độ cho địa chỉ...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else if (geocodingError != null) {
                    Text(
                        text = geocodingError ?: "Lỗi geocoding",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Tọa độ: ${String.format("%.6f", currentLocation.lat)}, ${String.format("%.6f", currentLocation.lng)}",
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
                        isLoadingGeocoding -> "⏳ Đang tự động tìm vị trí..."
                        geocodingError != null -> "❌ Không thể tìm vị trí tự động. Vui lòng kiểm tra lại địa chỉ."
                        currentLocation.lat != 0.0 && currentLocation.lng != 0.0 -> "✅ Vị trí đã được xác định tự động từ địa chỉ."
                        else -> "📍 Vị trí sẽ được xác định từ địa chỉ đã nhập"
                    },
                    fontSize = 12.sp,
                    color = when {
                        isLoadingGeocoding -> MaterialTheme.colorScheme.primary
                        geocodingError != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}