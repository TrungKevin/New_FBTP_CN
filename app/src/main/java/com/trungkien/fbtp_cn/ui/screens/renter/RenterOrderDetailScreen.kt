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
    val currentField: Field? = uiState.currentField
    val detailImages: List<String> = listOf(
        uiState.currentField?.images?.mainImage ?: "",
        uiState.currentField?.images?.image1 ?: "",
        uiState.currentField?.images?.image2 ?: "",
        uiState.currentField?.images?.image3 ?: ""
    ).filter { it.isNotBlank() }
    val imagePager = rememberPagerState(pageCount = { maxOf(detailImages.size, 1) })

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
                    val img = detailImages.getOrNull(page)
                    if (!img.isNullOrBlank()) {
                        val model = if (img.startsWith("http", true) || img.startsWith("data:image", true)) img else "data:image/jpeg;base64,$img"
                        coil.compose.AsyncImage(
                            model = model,
                            contentDescription = "Field Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.court1),
                            contentDescription = "Field Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
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
                    repeat(maxOf(detailImages.size, 1)) { index ->
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
                                price = (uiState.pricingRules.firstOrNull()?.price ?: 0).toInt(),
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

@Preview(showBackground = true)
@Composable
private fun RenterOrderDetailScreenPreview() {
    FBTP_CNTheme {
        RenterOrderDetailScreen(fieldId = "field1", onBackClick = {}, onBookNow = {})
    }
}


