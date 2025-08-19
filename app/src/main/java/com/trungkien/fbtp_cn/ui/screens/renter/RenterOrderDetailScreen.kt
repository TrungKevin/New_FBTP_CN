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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RenterOrderDetailScreen(
    fieldId: String,
    onBackClick: () -> Unit,
    onBookNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedServices by remember { mutableStateOf(setOf<String>()) }

    // Mock images like owner
    val fieldImages = listOf(
        R.drawable.court1,
        R.drawable.court2,
        R.drawable.court4,
        R.drawable.court5
    )
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
                .padding(
                    innerPadding
                )
                .verticalScroll(rememberScrollState())
        ) {
            // Hero images
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                HorizontalPager(state = imagePager, modifier = Modifier.fillMaxSize()) { page ->
                    Image(
                        painter = painterResource(id = fieldImages[page]),
                        contentDescription = "Field Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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

            // Content per tab
            HorizontalPager(
                state = tabPagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp)
            ) { page ->
                when (page) {
                    0 -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            RenterFieldInfoSection(
                                name = "POC Pickleball",
                                type = "Pickleball",
                                price = 150000,
                                address = "25 Tú Xương, TP. Thủ Đức",
                                operatingHours = "05:00 - 23:00",
                                contactPhone = "0926666357",
                                distance = "835.3m",
                                rating = 4.8f
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            RenterServicesSection(
                                services = listOf(
                                    RenterServiceItem("1", "Thuê vợt", 20000),
                                    RenterServiceItem("2", "Nước uống", 15000)
                                ),
                                selected = selectedServices,
                                onToggle = { id -> selectedServices = selectedServices.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }.toSet() }
                            )
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.padding(16.dp)) {
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


