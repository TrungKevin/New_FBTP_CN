package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.trungkien.fbtp_cn.ui.components.owner.map.MapMarkerUtils
import com.trungkien.fbtp_cn.service.GeocodingService

@Composable
fun OwnerMapView(
    field: Field,
    onMarkerClick: (() -> Unit)? = null, // Callback để hiển thị bottom sheet
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var currentLocation by remember { mutableStateOf(field.geo) }
    var isLoadingGeocoding by remember { mutableStateOf(false) }
    var geocodingError by remember { mutableStateOf<String?>(null) }
    var lastGeocodedAddress by remember { mutableStateOf("") }
    
    val geocodingService: GeocodingService = remember { GeocodingService() }

    // ✅ FIX: Geocoding lại khi địa chỉ thay đổi (không chỉ khi tọa độ là 0)
    LaunchedEffect(field.address) {
        // Chỉ geocoding khi: 
        // 1. Địa chỉ không rỗng
        // 2. Địa chỉ thay đổi so với lần geocoding trước
        if (field.address.isNotEmpty() && field.address != lastGeocodedAddress) {
            isLoadingGeocoding = true
            geocodingError = null
            
            try {
                println("🗺️ OwnerMapView - Auto geocoding address: ${field.address}")
                val result = geocodingService.geocodeAddress(field.address)
                if (result != null) {
                    println("🗺️ OwnerMapView - Geocoding success: lat=${result.lat}, lng=${result.lng}")
                    currentLocation = result
                    lastGeocodedAddress = field.address // Lưu địa chỉ đã geocoding
                    
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
                                .icon(MapMarkerUtils.getSportMarkerBitmapDescriptor(mapView?.context ?: context, field.sports.firstOrNull() ?: "TENNIS", 200))
                                .anchor(0.5f, 0.5f) // Center anchor
                        )
                        
                        // Center map on marker
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 16f))
                        
                        println("🗺️ OwnerMapView - Geocoding complete: Marker centered at lat=${result.lat}, lng=${result.lng}")
                    }
                } else {
                    geocodingError = "Không tìm thấy vị trí cho địa chỉ này"
                    println("🗺️ OwnerMapView - Geocoding failed for: ${field.address}")
                }
            } catch (e: Exception) {
                geocodingError = "Lỗi khi tìm vị trí: ${e.message}"
                println("🗺️ OwnerMapView - Geocoding error: ${e.message}")
            } finally {
                isLoadingGeocoding = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Google Maps
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
                        
                        val fieldLocation = LatLng(field.geo.lat, field.geo.lng)
                        
                        // Tạo marker cố định với icon tương ứng loại sân
                        marker = map.addMarker(
                            MarkerOptions()
                                .position(fieldLocation)
                                .title(field.name)
                                .snippet(field.address)
                                .icon(MapMarkerUtils.getSportMarkerBitmapDescriptor(context, field.sports.firstOrNull() ?: "TENNIS", 150))
                                .anchor(0.5f, 0.5f) // Center anchor
                        )
                        
                        // Center map on marker
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fieldLocation, 16f))
                        
                        // Thêm click listener cho marker
                        map.setOnMarkerClickListener { clickedMarker ->
                            onMarkerClick?.invoke()
                            true
                        }
                        
                        println("🗺️ OwnerMapView - Map initialized with marker at: lat=${field.geo.lat}, lng=${field.geo.lng}")
                    }
                    
                    mapView = this
                }
            },
            update = { mapView ->
                // Chỉ cập nhật khi field thực sự thay đổi
                googleMap?.let { map ->
                    val fieldLocation = LatLng(field.geo.lat, field.geo.lng)
                    
                    // Chỉ cập nhật nếu vị trí marker thực sự thay đổi
                    if (marker?.position != fieldLocation) {
                        marker?.position = fieldLocation
                        marker?.title = field.name
                        marker?.snippet = field.address
                        marker?.setIcon(MapMarkerUtils.getSportMarkerBitmapDescriptor(mapView.context, field.sports.firstOrNull() ?: "TENNIS", 150))
                        
                        // Center map on marker
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(fieldLocation, 16f))
                        
                        println("🗺️ OwnerMapView - Marker position updated to: lat=${field.geo.lat}, lng=${field.geo.lng}")
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Field Location Button - chỉ hiển thị khi có tọa độ
        if (field.geo.lat != 0.0 && field.geo.lng != 0.0) {
            OwnerFieldLocationButton(
                onClick = {
                    googleMap?.let { map ->
                        val fieldLocation = LatLng(field.geo.lat, field.geo.lng)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(fieldLocation, 16f))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(start = 16.dp, end = 16.dp, bottom = 120.dp) // Di chuyển lên cao hơn để tránh đè zoom controls
            )
        }
    }
}

/**
 * Button để center map về vị trí field
 */
@Composable
private fun OwnerFieldLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = GreenPrimary,
        contentColor = androidx.compose.ui.graphics.Color.White
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Vị trí sân",
            modifier = Modifier.size(24.dp)
        )
    }
}