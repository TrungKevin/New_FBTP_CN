package com.trungkien.fbtp_cn.ui.components.owner.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

@Composable
fun OwnerMapView(
    field: Field,
    onMarkerClick: (() -> Unit)? = null, // Callback để hiển thị bottom sheet
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // OpenStreetMap
        var mapView by remember { mutableStateOf<MapView?>(null) }
        
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    
                    // Set field location as center
                    val fieldLocation = GeoPoint(field.geo.lat, field.geo.lng)
                    
                    controller.setZoom(18.0) // Zoom gần hơn để thấy rõ marker
                    controller.animateTo(fieldLocation) // Smooth animation đến vị trí marker
                    
                    mapView = this
                    
                    // Clear any existing overlays to remove unwanted markers
                    overlays.clear()
                    
                    // Debug: Log field information
                    println("🗺️ OwnerMapView - Field: ${field.name}")
                    println("🗺️ OwnerMapView - Coordinates: lat=${field.geo.lat}, lng=${field.geo.lng}")
                    println("🗺️ OwnerMapView - Sports: ${field.sports}")
                    
                    // Add field marker with click functionality
                    val sportType = field.sports.firstOrNull() ?: "TENNIS"
                    
                    // Custom marker với SportMarkerIcon
                    val marker = Marker(this).apply {
                        position = fieldLocation
                        title = field.name
                        snippet = "Chạm để xem chi tiết"
                        // Neo marker để chóp giọt nước trỏ đúng tọa độ
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        isDraggable = false
                        
                        // Custom marker icon theo loại sân - tăng kích thước để hiển thị rõ ràng
                        icon = SportMarkerIcon(context, sportType, 120)
                        
                        // Debug: Log marker creation
                        println("🗺️ OwnerMapView - Created marker for sport: $sportType")
                        println("🗺️ OwnerMapView - Marker position: lat=${fieldLocation.latitude}, lng=${fieldLocation.longitude}")
                        
                        // Add click listener để center map và hiển thị bottom sheet khi click marker
                        setOnMarkerClickListener { marker, mapView ->
                            
                            // Center map on marker position
                            mapView.controller.animateTo(marker.position)
                            
                            // Hiển thị bottom sheet nếu có callback
                            onMarkerClick?.invoke()
                            
                            // Show info window
                            mapView.invalidate()
                            true
                        }
                    }
                    overlays.add(marker)
                    
                    // Force map refresh để đảm bảo marker hiển thị
                    post {
                        invalidate()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Field Location Button - chỉ hiển thị khi có tọa độ
        if (field.geo.lat != 0.0 && field.geo.lng != 0.0) {
            FieldLocationButton(
                onClick = {
                    mapView?.let { map ->
                        val fieldLocation = GeoPoint(field.geo.lat, field.geo.lng)
                        map.controller.animateTo(fieldLocation)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}
