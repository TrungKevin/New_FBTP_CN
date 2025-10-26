package com.trungkien.fbtp_cn.ui.components.renter.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.repository.FieldRepository
import com.trungkien.fbtp_cn.ui.components.owner.map.SportMarkerIcon
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.UUID

/**
 * Google Map View cho Renter
 * Hiển thị tất cả các sân với marker tương ứng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterGoogleMapView(
    onFieldClick: (Field) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var markers by remember { mutableStateOf<List<Marker>>(emptyList()) }
    
    // Default address: Đại Học Hutech Khu E, Song Hành, Tân Phú, Thủ Đức
    val defaultAddress = "Đại Học Hutech Khu E, Song Hành Xa Lộ Hà Nội, Phường Tân Phú, Quận Thủ Đức, Thành phố Hồ Chí Minh"
    // Tọa độ chính xác theo yêu cầu user
    var currentLocation by remember { 
        mutableStateOf<LatLng?>(LatLng(10.8535, 106.7859)) // Tọa độ user cung cấp
    }
    
    var currentLocationMarker by remember { mutableStateOf<Marker?>(null) }
    
    var fields by remember { mutableStateOf<List<Field>>(emptyList()) }
    var isLoadingFields by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    
    // State for bottomsheet
    var selectedField by remember { mutableStateOf<Field?>(null) }
    
    val fusedLocationClient: FusedLocationProviderClient = 
        remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            println("✅ Location permission granted")
        } else {
            println("❌ Location permission denied")
        }
    }
    
    // Không cần geocode nữa vì đã hard-code tọa độ chính xác
    println("📍 Using hard-coded default location: lat=10.8535, lng=106.7859")
    
    // Check and request location permission
    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        if (permission == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            println("✅ Location permission already granted")
        } else {
            println("🔍 Requesting location permission...")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    // Load fields from database
    LaunchedEffect(Unit) {
        isLoadingFields = true
        try {
            val fieldRepository = FieldRepository()
            val result = fieldRepository.getAllFields()
            
            if (result.isSuccess) {
                val loadedFields = result.getOrNull() ?: emptyList()
                println("✅ Loaded ${loadedFields.size} fields for map")
                loadedFields.forEachIndexed { index, field ->
                    println("  [$index] fieldId: ${field.fieldId}, name: ${field.name}, lat: ${field.geo.lat}, lng: ${field.geo.lng}, sports: ${field.sports}")
                }
                fields = loadedFields
            } else {
                println("❌ Failed to load fields: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("❌ Error loading fields: ${e.message}")
        } finally {
            isLoadingFields = false
        }
    }
    
    // Update markers when fields or map changes
    LaunchedEffect(fields, googleMap) {
        val map = googleMap
        if (fields.isNotEmpty() && map != null) {
            println("🔄 LaunchedEffect triggered - fields size: ${fields.size}")
            
            // Create markers logic inline
            println("🏷️ Creating markers for ${fields.size} fields...")
            map.clear()
            markers.forEach { it.remove() }
            
            markers = fields.mapNotNull { field ->
                if (field.geo.lat != 0.0 && field.geo.lng != 0.0) {
                    try {
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(field.geo.lat, field.geo.lng))
                                .title(field.name)
                                .snippet("${field.sports.firstOrNull() ?: "Thể thao"} • ${field.averageRating}⭐")
                                .icon(
                                    com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(
                                        SportMarkerIconHelper.createMarkerBitmap(
                                            context,
                                            field.sports.firstOrNull() ?: "OTHER",
                                            120
                                        )
                                    )
                                )
                        )
                        
                        marker?.let { m ->
                            m.tag = field
                        }
                        
                        marker
                    } catch (e: Exception) {
                        println("❌ Error creating marker for field ${field.name}: ${e.message}")
                        null
                    }
                } else {
                    println("⚠️ Field ${field.name} has invalid coordinates: ${field.geo.lat}, ${field.geo.lng}")
                    null
                }
            }.filterNotNull()
            
            println("✅ Created ${markers.size} markers on map out of ${fields.size} fields")
            
            // Set marker click listener
            map.setOnMarkerClickListener { marker ->
                val field = marker.tag as? Field
                field?.let {
                    selectedField = it
                    true
                } ?: false
            }
        }
    }
    
    // Get current location
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                println("🔍 Getting device location...")
                val cancellationToken = CancellationTokenSource()
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { location ->
                    location?.let {
                        val lat = it.latitude
                        val lng = it.longitude
                        println("✅ Got device location: lat=$lat, lng=$lng")
                        
                        // CHỈ CẬP NHẬT NẾU Ở VIỆT NAM, NẾU KHÔNG DÙNG DEFAULT VIỆT NAM
                        val isInVietnam = lat >= 8.5 && lat <= 23.5 && lng >= 102.0 && lng <= 110.0
                        
                        if (isInVietnam) {
                            println("📍 Device location is in Vietnam - using it")
                            val newLocation = LatLng(lat, lng)
                            currentLocation = newLocation
                        } else {
                            println("⚠️ Device location is OUTSIDE Vietnam ($lat, $lng) - using default Vietnam location")
                            // Dùng vị trí mặc định Việt Nam thay vì location giả lập ở Mỹ
                            currentLocation = LatLng(10.8535, 106.7859)
                        }
                    } ?: run {
                        println("⚠️ Device location is null")
                        // Dùng default location nếu không lấy được
                        currentLocation = LatLng(10.8535, 106.7859)
                    }
                }.addOnFailureListener { e ->
                    println("❌ Failed to get device location: ${e.message}")
                    println("⚠️ Using default location: lat=10.8535, lng=106.7859")
                }
            } catch (e: Exception) {
                println("❌ Error getting device location: ${e.message}")
            }
        } else {
            println("⚠️ Location permission not granted, using default: lat=10.8535, lng=106.7859")
        }
    }
    
    // Update camera and marker position when currentLocation changes
    LaunchedEffect(currentLocation, googleMap) {
        val location = currentLocation
        val map = googleMap
        
        if (location != null && map != null) {
            // CHỈ CẬP NHẬT NẾU LOCATION Ở VIỆT NAM
            val isInVietnam = location.latitude >= 8.5 && location.latitude <= 23.5 && 
                              location.longitude >= 102.0 && location.longitude <= 110.0
            
            if (isInVietnam) {
                println("🔄 Updating camera and marker to VIETNAM location: lat=${location.latitude}, lng=${location.longitude}")
                
                // Cập nhật vị trí marker
                currentLocationMarker?.let { marker ->
                    marker.position = location
                    println("📍 Updated blue dot marker position")
                }
                
                // Cập nhật camera
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(location, 15f)
                )
            } else {
                println("⚠️ Ignored location OUTSIDE Vietnam: lat=${location.latitude}, lng=${location.longitude}")
                // Không update camera/marker nếu location không ở Việt Nam
            }
        }
    }
    
    // Handle map lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Google Map
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    onCreate(null)
                    getMapAsync { map ->
                        googleMap = map
                        
                        // Enable my location (hiển thị blue dot)
                        try {
                            map.isMyLocationEnabled = locationPermissionGranted
                        } catch (e: Exception) {
                            println("⚠️ Could not enable my location: ${e.message}")
                        }
                        
                        // Disable gesture for drawer when touching map
                        map.uiSettings.isZoomControlsEnabled = false // Use custom zoom controls
                        map.uiSettings.isMyLocationButtonEnabled = false // Don't show default my location button (use custom)
                        map.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
                        map.uiSettings.isCompassEnabled = true
                        map.uiSettings.isMapToolbarEnabled = false
                        
                        // Note: Markers will be created by LaunchedEffect when fields are loaded
                        println("🗺️ GoogleMap ready, waiting for fields to load...")
                        
                        // Add current location marker (chấm xanh) - Đại Học Hutech Khu E
                        val defaultLocation = LatLng(10.8535, 106.7859) // Tọa độ user cung cấp
                        val locationToShow = currentLocation ?: defaultLocation
                        
                        // Tạo custom marker màu xanh (giống My Location)
                        val blueDotMarker = createBlueDotMarker(context)
                        
                        // Tạo marker cho vị trí hiện tại
                        currentLocationMarker = map.addMarker(
                            MarkerOptions()
                                .position(locationToShow)
                                .title("Vị trí của bạn")
                                .snippet(defaultAddress)
                                .icon(blueDotMarker)
                                .anchor(0.5f, 0.5f) // Center the marker
                        )
                        
                        println("📍 Added blue dot marker at: lat=${locationToShow.latitude}, lng=${locationToShow.longitude}")
                        
                        // Zoom đến chấm xanh ngay lập tức để user thấy được
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(locationToShow, 16f)
                        )
                        println("🗺️ Map centered at: lat=${locationToShow.latitude}, lng=${locationToShow.longitude}")
                        
                        // Đảm bảo marker visible bằng cách đặt nó lên trên cùng
                        currentLocationMarker?.let { marker ->
                            marker.isVisible = true
                            marker.isFlat = false // 3D marker, không flat
                        }
                    }
                }
            },
            update = { view ->
                // Update markers when fields change - this will be handled by LaunchedEffect
                println("🔄 Update called - fields size: ${fields.size}")
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading indicator
        if (isLoadingFields) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = "Đang tải bản đồ...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Info about fields loaded
        if (!isLoadingFields && fields.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "📍 ${fields.size} sân gần bạn",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Custom Zoom Controls - Góc trên bên phải
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // My Location Button - ĐẶT LÊN TRÊN CÙNG
            FloatingActionButton(
                onClick = {
                    googleMap?.let { map ->
                        // Luôn zoom về vị trí mặc định khi bấm My Location
                        val location = currentLocation ?: LatLng(10.8535, 106.7859)
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(location, 16f)
                        )
                        println("📍 My Location button clicked - centering on default location")
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Vị trí của tôi",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Zoom In Button
            FloatingActionButton(
                onClick = {
                    googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Zoom Out Button
            FloatingActionButton(
                onClick = {
                    googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Field Info BottomSheet
    FieldInfoBottomSheet(
        field = selectedField,
        onDismiss = { selectedField = null },
        onViewMoreClick = { fieldId ->
            selectedField?.let { field ->
                onFieldClick(field)
            }
            selectedField = null
        }
    )
}

/**
 * Helper function to create blue dot marker (giống My Location)
 */
fun createBlueDotMarker(context: android.content.Context): com.google.android.gms.maps.model.BitmapDescriptor {
    val size = 40
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }
    
    // Vẽ outer circle (màu trắng nhạt)
    paint.color = android.graphics.Color.parseColor("#40FFFFFF")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint)
    
    // Vẽ inner circle (màu xanh My Location)
    paint.color = android.graphics.Color.parseColor("#2196F3")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6, paint)
    
    // Vẽ core (chấm trắng)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, 4f, paint)
    
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Helper object to create marker bitmap from SportMarkerIcon
 */
object SportMarkerIconHelper {
    fun createMarkerBitmap(
        context: android.content.Context,
        sportType: String,
        size: Int
    ): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val marker = SportMarkerIcon(context, sportType, size)
        marker.setBounds(0, 0, size, size)
        marker.draw(canvas)
        
        return bitmap
    }
}

