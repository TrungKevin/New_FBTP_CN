package com.trungkien.fbtp_cn.ui.screens.owner

import android.os.Build
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
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OwnerFieldDetailScreen(
    fieldId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lấy dữ liệu thực từ Firebase thay vì mock data
    val fieldViewModel: FieldViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by fieldViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Load field data từ Firebase khi có fieldId
    LaunchedEffect(fieldId) {
        if (fieldId.isNotEmpty()) {
            println("DEBUG: 🔍 Loading field details for fieldId: $fieldId")
            fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
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

    // Lấy hình ảnh thực tế từ dữ liệu sân - tối đa 4 ảnh
    val fieldImages = remember(field.images, field.fieldId) {
        buildList {
            // Thêm mainImage nếu có (ưu tiên cao nhất)
            if (field.images.mainImage.isNotEmpty()) {
                add(field.images.mainImage)
            }
            // Thêm các ảnh chi tiết nếu có
            if (field.images.image1.isNotEmpty()) {
                add(field.images.image1)
            }
            if (field.images.image2.isNotEmpty()) {
                add(field.images.image2)
            }
            if (field.images.image3.isNotEmpty()) {
                add(field.images.image3)
            }
            
            // Đảm bảo luôn có ít nhất 4 ảnh để hiển thị
            while (size < 4) {
                when (size) {
                    0 -> add(R.drawable.court1)
                    1 -> add(R.drawable.court2)
                    2 -> add(R.drawable.court4)
                    3 -> add(R.drawable.court5)
                }
            }
        }
    }

    // Debug logging và cập nhật fieldImages khi dữ liệu thay đổi
    LaunchedEffect(field, uiState.isLoading, uiState.error) {
        println("DEBUG: 🏟️ OwnerFieldDetailScreen - fieldId: $fieldId")
        println("DEBUG: 🏟️ OwnerFieldDetailScreen - field loaded: ${field.name}")
        println("DEBUG: 🏟️ OwnerFieldDetailScreen - isLoading: ${uiState.isLoading}")
        println("DEBUG: 🏟️ OwnerFieldDetailScreen - error: ${uiState.error}")
        
        // Debug hình ảnh
        println("DEBUG: 🖼️ Field images from Firebase:")
        println("DEBUG: 🖼️ - mainImage: ${field.images.mainImage}")
        println("DEBUG: 🖼️ - image1: ${field.images.image1}")
        println("DEBUG: 🖼️ - image2: ${field.images.image2}")
        println("DEBUG: 🖼️ - image3: ${field.images.image3}")
        println("DEBUG: 🖼️ - Total fieldImages count: ${fieldImages.size}")
        println("DEBUG: 🖼️ - fieldImages: $fieldImages")
        
        // Kiểm tra xem có ảnh từ Firebase không
        val hasFirebaseImages = field.images.mainImage.isNotEmpty() || 
                               field.images.image1.isNotEmpty() || 
                               field.images.image2.isNotEmpty() || 
                               field.images.image3.isNotEmpty()
        
        if (hasFirebaseImages) {
            println("DEBUG: 🎉 Có ảnh từ Firebase - sẽ hiển thị ảnh thực tế!")
        } else {
            println("DEBUG: ⚠️ Không có ảnh từ Firebase - sử dụng ảnh mặc định")
        }
    }

    val pagerState = rememberPagerState(pageCount = { fieldImages.size })
    // Tabs + swipe state - Đơn giản hóa logic
    val tabs = listOf("Thông tin", "Dịch vụ", "Đánh giá", "Khung giờ")
    val tabPagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

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
                        fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
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
                            onClick = { /* Xử lý chỉnh sửa sân */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Chỉnh sửa",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { /* Xử lý xóa sân */ },
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
                    
                    // Thông báo về nguồn ảnh
                    val hasFirebaseImages = field.images.mainImage.isNotEmpty() || 
                                           field.images.image1.isNotEmpty() || 
                                           field.images.image2.isNotEmpty() || 
                                           field.images.image3.isNotEmpty()
                    
                    if (hasFirebaseImages) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "📸 Ảnh thực từ Firebase",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", field.averageRating)}/5.0",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
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
                        0 -> DetailInfoCourt(field = field)// Hiển thị thông tin chi tiết sân

                        1 -> CourtService(field = field)// Hiển thị dịch vụ sân

                        2 -> EvaluateCourt(field = field)// Hiển thị đánh giá sân

                        3 -> {
                            // Debug: Kiểm tra xem có vào được case này không
                            Column {

                                // Gọi trực tiếp TimeSlots thay vì qua wrapper
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    TimeSlots()
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
                if (imageSource.startsWith("http")) {
                    // TODO: Implement Coil image loading for Firebase URLs
                    // Tạm thời hiển thị placeholder với thông báo rõ ràng
                    Box(
                        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🖼️ Ảnh từ Firebase",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Đang tải ảnh...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "URL: ${imageSource.take(30)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
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

