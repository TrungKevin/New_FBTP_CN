package com.trungkien.fbtp_cn.ui.screens.owner

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.model.FieldImages
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import com.trungkien.fbtp_cn.ui.components.owner.info.CourtService
import com.trungkien.fbtp_cn.ui.components.owner.info.DetailInfoCourt
import com.trungkien.fbtp_cn.ui.components.owner.info.EvaluateCourt
import com.trungkien.fbtp_cn.ui.components.owner.info.TimeSlots
import com.trungkien.fbtp_cn.ui.components.owner.dialogs.DeleteFieldDialog
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OwnerFieldDetailScreen(
    fieldId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    fieldViewModel: FieldViewModel? = null // NHẬN VIEWMODEL TỪ PARENT
) {
    // Lấy dữ liệu thực từ Firebase thay vì mock data
    val localFieldViewModel: FieldViewModel = fieldViewModel ?: viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by localFieldViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Fetch current user profile if not loaded
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            println("🔄 DEBUG: OwnerFieldDetailScreen - Fetching current user profile...")
            authViewModel.fetchProfile()
        }
    }
    
    // Debug currentUser state
    LaunchedEffect(currentUser) {
        println("🔄 DEBUG: OwnerFieldDetailScreen - currentUser: ${currentUser?.name}")
        println("🔄 DEBUG: OwnerFieldDetailScreen - currentUser?.userId: ${currentUser?.userId}")
    }
    
    val context = LocalContext.current

    // Load field data từ Firebase khi có fieldId
    LaunchedEffect(fieldId) {
        if (fieldId.isNotEmpty()) {
            // Loading field details from Firebase
            localFieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
        }
    }
    
    // Xử lý success message và hiển thị Toast
    LaunchedEffect(uiState.success) {
        uiState.success?.let { successMessage ->
            Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
        }
    }

    // Lấy field data từ ViewModel
    val field = uiState.currentField ?: Field(
        fieldId = fieldId,
        ownerId = currentUser?.userId ?: "",
        name = "Đang tải...",
        address = "",
        geo = GeoLocation(),
        sports = emptyList(),
        images = FieldImages(),
        slotMinutes = 30,
        openHours = OpenHours(),
        amenities = emptyList(),
        description = "",
        contactPhone = "",
        averageRating = 0f,
        totalReviews = 0
    )

    // Lấy hình ảnh thực tế từ dữ liệu sân - tối đa 4 ảnh (ưu tiên ảnh Firebase, lặp lại nếu ít hơn 4)
    val fieldImages = remember(uiState.currentField?.images, uiState.currentField?.fieldId) {
        val currentField = uiState.currentField
        if (currentField != null) {
            buildList<Any> {
                if (currentField.images.mainImage.isNotEmpty()) add(currentField.images.mainImage)
                if (currentField.images.image1.isNotEmpty()) add(currentField.images.image1)
                if (currentField.images.image2.isNotEmpty()) add(currentField.images.image2)
                if (currentField.images.image3.isNotEmpty()) add(currentField.images.image3)

                if (isNotEmpty()) {
                    var index = 0
                    while (size < 4) {
                        add(this[index % this.size])
                        index++
                    }
                } else {
                    add(R.drawable.court1)
                    add(R.drawable.court2)
                    add(R.drawable.court4)
                    add(R.drawable.court5)
                }
            }
        } else {
            listOf<Any>(R.drawable.court1, R.drawable.court2, R.drawable.court4, R.drawable.court5)
        }
    }

    // Cập nhật fieldImages khi dữ liệu thay đổi
    LaunchedEffect(uiState.currentField, uiState.isLoading, uiState.error) {
        // FieldImages sẽ tự động cập nhật khi currentField thay đổi
    }

    val pagerState = rememberPagerState(pageCount = { fieldImages.size })
    // Tabs + swipe state - Đơn giản hóa logic
    val tabs = listOf("Thông tin", "Dịch vụ", "Đánh giá", "Khung giờ")
    val tabPagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    // State cho dialog xóa
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Hiển thị loading state nếu đang tải dữ liệu
    if (uiState.isLoading) {
        LoadingDialog(message = "Đang tải thông tin sân...")
    } else if (uiState.error != null) {
        // Hiển thị error state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "❌ Lỗi tải dữ liệu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = uiState.error ?: "Không thể tải thông tin sân",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Button(
                    onClick = {
                        localFieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
                    }
                ) {
                    Text("Thử lại")
                }
            }
        }
    } else {
        // Hiển thị nội dung sân khi đã load thành công
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            field.name, // Hiển thị tên sân thực tế
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { onBackClick() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                // Kiểm tra điều kiện trước khi hiển thị dialog xác nhận xóa
                                if (field.fieldId.isNotEmpty()) {
                                    showDeleteDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Hero Image Section with Image Carousel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    // Image Carousel với Pager
                    HorizontalPager( // Sử dụng HorizontalPager để tạo carousel
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        FieldImage(
                            imageSource = fieldImages[page],
                            contentDescription = "Field Image ${page + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )

                    // Page indicator dots
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(fieldImages.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .background(
                                        color = if (pagerState.currentPage == index)
                                            Color.White
                                        else
                                            Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    


                    // Status badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (field.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (field.isActive) "Hoạt động" else "Không hoạt động",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Field name overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                    }
                }

                // Tabs + content
                TabRow(
                    selectedTabIndex = tabPagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { positions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(positions[tabPagerState.currentPage])
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = tabPagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    tabPagerState.animateScrollToPage(index)
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Swipeable content for tabs
                HorizontalPager( // Sử dụng HorizontalPager để tạo nội dung swipeable cho các tab
                    state = tabPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp) // Tăng height từ 400.dp lên 600.dp để hiển thị đầy đủ khung giờ
                ) { page ->
                    when (page) {
                        0 -> DetailInfoCourt(
                            field = field,
                            fieldViewModel = fieldViewModel,
                            onBackClick = onBackClick
                        )// Hiển thị thông tin chi tiết sân

                        1 -> {
                            // Hiển thị dịch vụ sân với FieldViewModel được chia sẻ
                            CourtService(
                                field = field, 
                                fieldViewModel = localFieldViewModel
                            )
                        }

                        2 -> EvaluateCourt(
                            fieldId = field.fieldId,
                            currentUser = currentUser,
                            isOwner = true,
                            viewModel = viewModel()
                        )// Hiển thị đánh giá sân

                        3 -> {
                            // Debug: Kiểm tra xem có vào được case này không
                            Column {

                                // ✅ FIX: Truyền field object vào TimeSlots
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    field?.let { fieldData ->
                                        TimeSlots(
                                            field = fieldData,
                                            fieldViewModel = localFieldViewModel
                                        )
                                    } ?: run {
                                        // Hiển thị loading nếu chưa có field data
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Khung giờ chỉ hỗ trợ từ Android 8.0 trở lên",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        else -> Text("Tab $page content") // Mặc định nếu có tab mới
                    }
                }
            }
        } // Scaffold
        
        // Dialog xác nhận xóa sân - chỉ hiển thị khi đã xác nhận
        if (showDeleteDialog) {
            DeleteFieldDialog(
                field = field,
                fieldViewModel = localFieldViewModel,
                onDismiss = { 
                    showDeleteDialog = false
                    println("DEBUG: ❌ User cancelled field deletion")
                },
                onConfirm = { 
                    showDeleteDialog = false
                    println("DEBUG: ✅ User confirmed field deletion for field: ${field.fieldId}")
                    
                    // Thực hiện xóa sân sau khi xác nhận
                    localFieldViewModel.handleEvent(FieldEvent.DeleteField(field.fieldId))
                    
                    // Đợi một chút để đảm bảo xóa hoàn tất trước khi navigate back
                    coroutineScope.launch {
                        delay(1500) // Đợi 1.5 giây để đảm bảo UI cập nhật hoàn toàn
                        onBackClick()
                    }
                }
            )
        }
        
    } // else
}

// Component để hiển thị hình ảnh từ Firebase hoặc resource
@Composable
fun FieldImage(
    imageSource: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    when (imageSource) {
        is String -> {
            if (imageSource.isNotEmpty()) {
                val context = LocalContext.current
                // Ưu tiên decode Base64 thủ công giống FieldCard để đảm bảo hiển thị
                val base64Data = remember(imageSource) {
                    if (imageSource.startsWith("data:image", ignoreCase = true)) {
                        imageSource.substringAfter(",")
                    } else if (imageSource.startsWith("http", ignoreCase = true)) {
                        null // Không decode khi là URL
                    } else {
                        imageSource
                    }
                }
                val decodedBitmap = remember(base64Data) {
                    try {
                        if (base64Data != null && base64Data.isNotEmpty()) {
                            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } else {
                            null
                        }
                    } catch (_: Exception) { null }
                }
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = contentDescription,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback dùng Coil cho URL hoặc khi decode thất bại
                    val dataString = when {
                        imageSource.startsWith("http", ignoreCase = true) -> imageSource
                        imageSource.startsWith("data:image", ignoreCase = true) -> imageSource
                        else -> "data:image/jpeg;base64,$imageSource"
                    }
                    val model = ImageRequest.Builder(context)
                        .data(dataString)
                        .crossfade(true)
                        .allowHardware(false)
                        .placeholder(R.drawable.court1)
                        .error(R.drawable.court1)
                        .build()
                    AsyncImage(
                        model = model,
                        contentDescription = contentDescription,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // String rỗng, hiển thị ảnh mặc định
                Image(
                    painter = painterResource(id = R.drawable.court1),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            }
        }
        is Int -> {
            // Nếu là resource ID (ảnh mặc định)
            Image(
                painter = painterResource(id = imageSource),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            // Fallback
            Image(
                painter = painterResource(id = R.drawable.court1),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview
@Composable
fun PreviewOwnerFieldDetailScreen() {
    FBTP_CNTheme {
        OwnerFieldDetailScreen(
            fieldId = "1",
            onBackClick = {}
        )
    }
}

@Composable
fun InfoRowItem( // Hàm Composable để hiển thị một dòng thông tin với biểu tượng, nhãn và giá trị
    icon: ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isPrice: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        when {
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            painter != null -> {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            else -> {
                // Money icon for price
                Text(
                    text = "💰",
                    fontSize = 16.sp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = if (isPrice) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

