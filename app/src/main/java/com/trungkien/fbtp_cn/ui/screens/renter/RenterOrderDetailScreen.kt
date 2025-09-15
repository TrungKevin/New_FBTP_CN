package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trungkien.fbtp_cn.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterFieldInfoSection
import com.trungkien.fbtp_cn.ui.components.renter.reviews.RenterReviewsSection
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterServiceItem
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterServicesSection
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.model.FieldImages
import com.trungkien.fbtp_cn.model.OpenHours

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RenterOrderDetailScreen(
    fieldId: String,
    onBackClick: () -> Unit,
    onBookNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedServices by remember { mutableStateOf(setOf<String>()) }
    val fieldViewModel: FieldViewModel = viewModel()
    val uiState = fieldViewModel.uiState.collectAsState().value
    // Load field data từ Firebase khi có fieldId
    LaunchedEffect(fieldId) {
        if (fieldId.isNotEmpty()) {
            println("🔄 DEBUG: RenterOrderDetailScreen - Loading field data for fieldId: $fieldId")
            fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId))
            // Load pricing rules cho field này
            fieldViewModel.handleEvent(FieldEvent.LoadPricingRulesByFieldId(fieldId))
        }
    }
    // Lấy field data từ ViewModel (giống hệt OwnerFieldDetailScreen)
    val currentField = uiState.currentField ?: Field(
        fieldId = fieldId,
        ownerId = "",
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

    // Lấy hình ảnh thực tế từ dữ liệu sân - tối đa 4 ảnh (giống hệt OwnerFieldDetailScreen)
    val fieldImages = remember(uiState.currentField?.images, uiState.currentField?.fieldId) {
        val field = uiState.currentField
        if (field != null) {
            buildList<Any> {
                // Thêm ảnh thực từ dữ liệu
                if (field.images.mainImage.isNotEmpty()) add(field.images.mainImage)
                if (field.images.image1.isNotEmpty()) add(field.images.image1)
                if (field.images.image2.isNotEmpty()) add(field.images.image2)
                if (field.images.image3.isNotEmpty()) add(field.images.image3)

                // Nếu đã có ảnh thực nhưng < 4, lặp lại ảnh có sẵn để đủ 4
                if (isNotEmpty()) {
                    var index = 0
                    while (size < 4) {
                        add(this[index % this.size])
                        index++
                    }
                } else {
                    // Không có ảnh trong dữ liệu, dùng ảnh mặc định
                    add(R.drawable.court1)
                    add(R.drawable.court2)
                    add(R.drawable.court4)
                    add(R.drawable.court5)
                }
            }
        } else {
            // Nếu chưa có dữ liệu từ Firebase, sử dụng ảnh mặc định
            listOf<Any>(R.drawable.court1, R.drawable.court2, R.drawable.court4, R.drawable.court5)
        }
    }
    
    // Cập nhật fieldImages khi dữ liệu thay đổi
    LaunchedEffect(uiState.currentField, uiState.isLoading, uiState.error) {
        // FieldImages sẽ tự động cập nhật khi currentField thay đổi
    }
    
    val imagePager = rememberPagerState(pageCount = { fieldImages.size })

    // Tabs
    val tabs = listOf("Thông tin", "Dịch vụ", "Đánh giá")
    val tabPagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentField.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
        bottomBar = {
            // Ẩn bottom bar khi đang ở tab "Đánh giá" để có không gian rộng hơn
            if (tabPagerState.currentPage != 2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Giá từ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%,d", uiState.pricingRules.firstOrNull()?.price ?: 0L)} VND/giờ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { onBookNow() },
                            modifier = Modifier
                                .height(50.dp)
                                .width(150.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "ĐẶT LỊCH", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val layoutDirection =LocalLayoutDirection.current
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero images (fallback local if no images)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                HorizontalPager(state = imagePager, modifier = Modifier.fillMaxSize()) { page ->
                    FieldImage(
                        imageSource = fieldImages[page],
                        contentDescription = "Field Image ${page + 1}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                                    color = if (imagePager.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = tabPagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(
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
                        onClick = { coroutineScope.launch { tabPagerState.animateScrollToPage(index) } },
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

            // Tab Content
            HorizontalPager(
                state = tabPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            RenterFieldInfoSection(
                                name = currentField.name,
                                type = currentField.sports.firstOrNull() ?: "",
                                price = (uiState.pricingRules.firstOrNull()?.price ?: 0L).toInt(),
                                address = currentField.address,
                                operatingHours = "${currentField.openHours.start} - ${currentField.openHours.end}",
                                contactPhone = currentField.contactPhone,
                                distance = "",
                                rating = currentField.averageRating,
                                amenities = currentField.amenities,
                                description = currentField.description,
                                totalReviews = currentField.totalReviews,
                                slotMinutes = currentField.slotMinutes,
                                latitude = currentField.geo.lat,
                                longitude = currentField.geo.lng,
                                isActive = currentField.isActive
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            RenterServicesSection(
                                pricingRules = uiState.pricingRules,
                                fieldServices = uiState.fieldServices,
                                selected = selectedServices,
                                onToggle = { id -> selectedServices = selectedServices.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }.toSet() }
                            )
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            RenterReviewsSection(fieldId = currentField.fieldId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldImage(
    imageSource: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    when (imageSource) {
        is String -> {
            if (imageSource.isNotEmpty()) {
                val context = LocalContext.current
                // Ưu tiên decode Base64 giống OwnerFieldDetailScreen
                val base64Data = remember(imageSource) {
                    if (imageSource.startsWith("data:image", ignoreCase = true)) {
                        imageSource.substringAfter(",")
                    } else if (imageSource.startsWith("http", ignoreCase = true)) {
                        null
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

@Preview(showBackground = true)
@Composable
private fun RenterOrderDetailScreenPreview() {
    FBTP_CNTheme {
        RenterOrderDetailScreen(fieldId = "field1", onBackClick = {}, onBookNow = {})
    }
}


