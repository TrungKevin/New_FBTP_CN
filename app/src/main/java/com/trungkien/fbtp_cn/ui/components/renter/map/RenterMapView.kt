package com.trungkien.fbtp_cn.ui.components.renter.map

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.theme.GreenPrimary

@Composable
fun RenterMapView(
    fields: List<Field>,
    currentLocation: GeoPoint?,
    onFieldClick: (Field) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultLocation = GeoPoint(10.8231, 106.6297) // HCMC coordinates
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
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    
                    // Set initial position
                    controller.setZoom(13.0)
                    controller.setCenter(currentLocation ?: defaultLocation)
                    
                    // Add markers for fields
                    fields.forEach { field ->
                        field.latitude?.let { lat ->
                            field.longitude?.let { lng ->
                                val marker = Marker(this).apply {
                                    position = GeoPoint(lat, lng)
                                    title = field.name
                                    snippet = "${field.type} • ${field.price}₫/giờ"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    
                                    // Handle marker click
                                    setOnMarkerClickListener { _, _ ->
                                        onFieldClick(field)
                                        true
                                    }
                                }
                                overlays.add(marker)
                            }
                        }
                    }
                    
                    // Add current location marker
                    currentLocation?.let { location ->
                        val currentMarker = Marker(this).apply {
                            position = location
                            title = "Vị trí của bạn"
                            snippet = "Bạn đang ở đây"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        overlays.add(currentMarker)
                    }
                    
                    invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Draggable Map Info Overlay
        var isInfoVisible by remember { mutableStateOf(true) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        
        if (isInfoVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = with(LocalDensity.current) { offsetX.toDp() },
                        y = with(LocalDensity.current) { offsetY.toDp() }
                    )
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Box {
                    // Close Button
                    IconButton(
                        onClick = { isInfoVisible = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Info Content
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🗺️ OpenStreetMap",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "• Hiển thị sân gần bạn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "• Marker với giá/đánh giá",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "• Click vào marker xem chi tiết sân",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "• Kéo để di chuyển thông tin này",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RenterMapViewPreview() {
    FBTP_CNTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Text(
                text = "🗺️ Google Maps Preview\n(Requires Google Maps API)",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
