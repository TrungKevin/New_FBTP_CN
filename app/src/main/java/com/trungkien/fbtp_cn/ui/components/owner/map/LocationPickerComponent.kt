package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary
import com.trungkien.fbtp_cn.service.GeocodingService

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
    var isLoadingGeocoding by remember { mutableStateOf(false) }
    var geocodingError by remember { mutableStateOf<String?>(null) }
    
    val geocodingService = remember { GeocodingService() }
    
    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }
    
    // Tự động geocoding khi có địa chỉ nhưng chưa có tọa độ
    LaunchedEffect(field.address) {
        if (field.address.isNotEmpty() && (selectedLocation.lat == 0.0 || selectedLocation.lng == 0.0)) {
            isLoadingGeocoding = true
            geocodingError = null
            
            val geoLocation = geocodingService.geocodeAddress(field.address)
            if (geoLocation != null) {
                selectedLocation = geoLocation
            } else {
                geocodingError = "Không thể tìm thấy tọa độ cho địa chỉ này"
            }
            
            isLoadingGeocoding = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Map View - Không có header riêng vì đã có từ Scaffold
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        
                        // Set initial position (HCMC center nếu chưa có tọa độ)
                        val initialLocation = if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                            GeoPoint(selectedLocation.lat, selectedLocation.lng)
                        } else {
                            GeoPoint(10.8231, 106.6297) // HCMC coordinates
                        }
                        
                        
                        
                        controller.setZoom(15.0)
                        controller.setCenter(initialLocation)
                        
                        // Add marker for selected location with click functionality
                        val marker = Marker(this).apply {
                            position = initialLocation
                            title = field.name
                            snippet = "Chạm để xem chi tiết"
                            // Neo marker để chóp giọt nước trỏ đúng tọa độ
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            // Custom marker icon theo loại sân (kích thước lớn, dễ nhìn)
                            icon = SportMarkerIcon(context, field.sports.firstOrNull() ?: "TENNIS", 120)
                            
                            // Add click listener để center map khi click marker
                            setOnMarkerClickListener { marker, mapView ->
                                // Center map on marker position
                                mapView.controller.animateTo(marker.position)
                                
                                // Show info window
                                mapView.invalidate()
                                true
                            }
                        }
                        overlays.add(marker)
                        
                        // Handle map tap to select location - sử dụng GestureDetector
                        val gestureDetector = android.view.GestureDetector(context, object : android.view.GestureDetector.SimpleOnGestureListener() {
                            override fun onSingleTapUp(e: android.view.MotionEvent): Boolean {
                                val projection = this@apply.projection
                                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                                
                                
                                // Update selected location
                                selectedLocation = GeoLocation(
                                    lat = geoPoint.latitude,
                                    lng = geoPoint.longitude,
                                    geohash = ""
                                )
                                
                                // Update marker position
                                marker.position = geoPoint as org.osmdroid.util.GeoPoint
                                marker.title = field.name
                                marker.snippet = "Vị trí đã chọn"
                                // Đảm bảo anchor và icon đúng sau khi người dùng chọn điểm
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.icon = SportMarkerIcon(context, field.sports.firstOrNull() ?: "TENNIS", 120)
                                
                                // Center map on new location
                                controller.animateTo(geoPoint)
                                
                                invalidate()
                                return true
                            }
                        })
                        
                        // Set gesture detector for map view
                        this.setOnTouchListener { _, event ->
                            gestureDetector.onTouchEvent(event)
                            false
                        }
                        
                        mapView = this
                        invalidate()
                    }
                },
                update = { mapView ->
                    // Update marker when selectedLocation changes
                    if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                        val geoPoint = GeoPoint(selectedLocation.lat, selectedLocation.lng)
                        
                        // Update marker position
                        val marker = mapView.overlays.find { it is Marker } as? Marker
                        marker?.let {
                            it.position = geoPoint
                            it.title = field.name
                            it.snippet = "Chạm để xem chi tiết"
                            // Giữ anchor ở đáy và đồng bộ icon kích thước lớn
                            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            it.icon = SportMarkerIcon(context, field.sports.firstOrNull() ?: "TENNIS", 120)
                        }
                        
                        // Center map on location
                        mapView.controller.animateTo(geoPoint)
                        mapView.invalidate()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // My Location Button - để tái căn giữa về vị trí đã chọn
            if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                MyLocationButton(
                    onClick = {
                        mapView?.let { map ->
                            val geoPoint = GeoPoint(selectedLocation.lat, selectedLocation.lng)
                            map.controller.animateTo(geoPoint)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
            
            // Location info overlay - Fixed layout để không bị tràn
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
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = field.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                                text = if (selectedLocation.lat != 0.0 && selectedLocation.lng != 0.0) {
                                    "📍 Chạm vào bản đồ để chọn vị trí khác"
                                } else {
                                    "📍 Chạm vào bản đồ để đánh dấu vị trí chính xác"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                }
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text("Hủy")
            }
            
            Button(
                onClick = { onLocationSelected(selectedLocation) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary
                )
            ) {
                Text("Xác nhận vị trí")
            }
        }
    }
}
