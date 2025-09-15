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
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterReviewsSection
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterReview
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterServiceItem
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.RenterServicesSection
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.model.Field

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
    LaunchedEffect(fieldId) { fieldViewModel.handleEvent(FieldEvent.LoadFieldById(fieldId)) }
    // Tìm field từ allFields nếu currentField chưa load được
    val currentField: Field? = uiState.currentField ?: uiState.allFields.find { it.fieldId == fieldId }
    // Lấy hình ảnh thực tế từ dữ liệu sân - tối đa 4 ảnh (tương tự OwnerFieldDetailScreen)
    val fieldImages = remember(currentField?.images, currentField?.fieldId, fieldId) {
        println("🔄 DEBUG: RenterOrderDetailScreen - Building fieldImages")
        println("🔄 DEBUG: - fieldId: $fieldId")
        println("🔄 DEBUG: - currentField: ${currentField?.name}")
        println("🔄 DEBUG: - mainImage: ${currentField?.images?.mainImage?.take(50)}...")
        println("🔄 DEBUG: - image1: ${currentField?.images?.image1?.take(50)}...")
        println("🔄 DEBUG: - image2: ${currentField?.images?.image2?.take(50)}...")
        println("🔄 DEBUG: - image3: ${currentField?.images?.image3?.take(50)}...")
        
        if (currentField != null) {
            buildList<Any> {
                // Thêm mainImage nếu có (ưu tiên cao nhất)
                if (currentField.images.mainImage.isNotEmpty()) {
                    add(currentField.images.mainImage)
                    println("🔄 DEBUG: - Added mainImage")
                }
                // Thêm các ảnh chi tiết nếu có
                if (currentField.images.image1.isNotEmpty()) {
                    add(currentField.images.image1)
                    println("🔄 DEBUG: - Added image1")
                }
                if (currentField.images.image2.isNotEmpty()) {
                    add(currentField.images.image2)
                    println("🔄 DEBUG: - Added image2")
                }
                if (currentField.images.image3.isNotEmpty()) {
                    add(currentField.images.image3)
                    println("🔄 DEBUG: - Added image3")
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
                println("🔄 DEBUG: - Final fieldImages size: $size")
            }
        } else {
            // Nếu chưa có dữ liệu từ Firebase, sử dụng ảnh mặc định
            println("🔄 DEBUG: - Using default images (no field data)")
            listOf<Any>(R.drawable.court1, R.drawable.court2, R.drawable.court4, R.drawable.court5)
        }
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
                        "Chi tiết sân",
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
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tổng tạm tính", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onBookNow) { Text("Đặt lịch ngay") }
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

            // Content per tab (make each page scrollable)
            HorizontalPager(
                state = tabPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp)
            ) { page ->
                when (page) {
                    0 -> {
                        val f = currentField
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            RenterFieldInfoSection(
                                name = f?.name ?: "",
                                type = f?.sports?.firstOrNull() ?: "",
                                price = (uiState.pricingRules.firstOrNull()?.price ?: 0L).toInt(),
                                address = f?.address ?: "",
                                operatingHours = "${f?.openHours?.start ?: ""} - ${f?.openHours?.end ?: ""}",
                                contactPhone = f?.contactPhone ?: "",
                                distance = "",
                                rating = f?.averageRating ?: 0f,
                                amenities = f?.amenities ?: emptyList(),
                                description = f?.description ?: ""
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            val services = uiState.fieldServices.map { RenterServiceItem(it.fieldServiceId, it.name, it.price.toInt()) }
                            RenterServicesSection(
                                services = services,
                                selected = selectedServices,
                                onToggle = { id -> selectedServices = selectedServices.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }.toSet() }
                            )
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            RenterReviewsSection(
                                reviews = listOf(
                                    RenterReview("Nguyễn A", 5, "Sân rất tốt!"),
                                    RenterReview("Trần B", 4, "Ổn áp")
                                )
                            )
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


