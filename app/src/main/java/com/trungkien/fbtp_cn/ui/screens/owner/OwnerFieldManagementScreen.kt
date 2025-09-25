package com.trungkien.fbtp_cn.ui.screens.owner // Package màn hình phía owner

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.trungkien.fbtp_cn.ui.components.owner.FieldCard
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.model.GeoLocation
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.ui.components.owner.home.HomeSearchBar
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldUiState
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.repository.ReviewRepository
import com.trungkien.fbtp_cn.model.ReviewSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class) // Cho phép dùng API experimental của Material3
@Composable // Định nghĩa một composable function
fun OwnerFieldManagementScreen( // Màn hình quản lý sân của chủ sở hữu
    onFieldClick: (String) -> Unit, // Callback khi click vào sân
    onAddFieldClick: () -> Unit, // Callback khi click vào nút thêm sân
    modifier: Modifier = Modifier, // Modifier truyền từ ngoài vào
    testMode: Boolean = false, // Test mode để hiển thị mock data
    fieldViewModel: FieldViewModel? = null // NHẬN VIEWMODEL TỪ PARENT ĐỂ LOAD DỮ LIỆU
) {
    // CÁCH HOẠT ĐỘNG GIỐNG NHƯ OwnerHomeScreen:
    // 1. Sử dụng FieldViewModel để load dữ liệu từ Firebase
    // 2. LaunchedEffect để tự động load khi có user
    // 3. Hiển thị danh sách sân bằng FieldCard
    
    // Sử dụng ViewModel từ parent nếu có, nếu không thì tạo mới
    val localFieldViewModel: FieldViewModel = fieldViewModel ?: viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by localFieldViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
 
    // Refresh profile on resume to ensure latest avatar for FieldCard
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                authViewModel.fetchProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Lấy dữ liệu từ Firebase giống như OwnerHomeScreen
    val fields = if (testMode) getMockFields() else uiState.fields
    val isLoading = if (testMode) false else uiState.isLoading
    val error = if (testMode) null else uiState.error
    // Tải ReviewSummary theo từng sân để luôn cập nhật điểm trung bình thực
    var reviewSummaryMap by remember { mutableStateOf<Map<String, ReviewSummary>>(emptyMap()) }
    val reviewRepository = remember { ReviewRepository() }
    LaunchedEffect(fields) {
        if (fields.isNotEmpty()) {
            val summaries = mutableMapOf<String, ReviewSummary>()
            fields.forEach { field ->
                try {
                    val result = withContext(Dispatchers.IO) { reviewRepository.getReviewSummary(field.fieldId) }
                    result.getOrNull()?.let { summary -> summaries[field.fieldId] = summary }
                } catch (_: Exception) { }
            }
            reviewSummaryMap = summaries
        } else {
            reviewSummaryMap = emptyMap()
        }
    }
    
    // Debug để kiểm tra ViewModel được sử dụng
    LaunchedEffect(Unit) {
        println("DEBUG: 🔍 OwnerFieldManagementScreen - fieldViewModel from parent: ${fieldViewModel != null}")
        println("DEBUG: 🔍 OwnerFieldManagementScreen - localFieldViewModel: ${localFieldViewModel.hashCode()}")
        println("DEBUG: 🔍 OwnerFieldManagementScreen - uiState.fields count: ${uiState.fields.size}")
    }
    
    // 🔥 KHÔNG CẦN LOAD DỮ LIỆU TẠI ĐÂY NỮA - ĐÃ ĐƯỢC XỬ LÝ TẠI OWNERMAINSCREEN
    // Chỉ sử dụng dữ liệu từ parent ViewModel
    

    
    // Debug logging chi tiết để theo dõi việc load dữ liệu từ Firebase
    LaunchedEffect(uiState, fields) {
        println("=== 🔥 FIREBASE DIRECT LOADING DEBUG ===")
        println("DEBUG: 🚀 Test mode: $testMode")
        println("DEBUG: 👤 Current user: ${currentUser?.userId}")
        println("DEBUG: 📊 UI State - isLoading: ${uiState.isLoading}, fields count: ${uiState.fields.size}")
        println("DEBUG: 🎯 Display fields count: ${fields.size}")
        
        if (uiState.error != null) {
            println("DEBUG: ❌ Firebase Error: ${uiState.error}")
        }
        
        if (uiState.fields.isNotEmpty()) {
            println("DEBUG: ✅ Firebase fields loaded: ${uiState.fields.map { "${it.name} (${it.fieldId})" }}")
            println("DEBUG: 🎯 Using Firebase data for display")
        } else {
            println("DEBUG: ⚠️ No Firebase fields loaded yet")
        }
        
        if (fields.isNotEmpty()) {
            println("DEBUG: 🎉 Display fields ready: ${fields.map { "${it.name} (${it.fieldId})" }}")
        } else {
            println("DEBUG: 🔍 No display fields - waiting for Firebase data...")
        }
        println("=== END DEBUG ===")
    }
    
    // Debug currentUser
    LaunchedEffect(currentUser) {
        println("DEBUG: Current user updated - userId: ${currentUser?.userId}, name: ${currentUser?.name}")
    }

    var searchQuery by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        // Header với tiêu đề, số lượng sân và nút tìm kiếm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quản lý sân",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

            }

        }

        // Search bar hiển thị bên dưới tiêu đề quản lý sân
        HomeSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            onSearch = { q -> searchQuery = q }
        )

        // Nội dung chính
        if (isLoading) {
            LoadingDialog(message = "Đang tải danh sách sân...")
        } else if (error != null) {
            // Hiển thị error message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                        text = error ?: "Không thể tải danh sách sân",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = {
                            currentUser?.userId?.let { ownerId ->
                                localFieldViewModel.handleEvent(FieldEvent.LoadFieldsByOwner(ownerId))
                            }
                        }
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        } else if (fields.isEmpty()) { // Không có dữ liệu
            Box( // Hộp căn giữa
                modifier = Modifier
                    .fillMaxSize() // Chiếm toàn bộ màn hình
                    .padding(16.dp), // Áp dụng padding cố định
                contentAlignment = Alignment.Center // Căn giữa
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🏟️",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Chưa có sân nào", // Thông báo rỗng
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hãy thêm sân đầu tiên của bạn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Thông tin trạng thái Firebase
                    if (uiState.isLoading) {
                        LoadingDialog(message = "🔥 Đang tải dữ liệu từ Firebase...")
                    }
                    
                    if (uiState.error != null) {
                        Text(
                            text = "❌ Lỗi Firebase: ${uiState.error}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    if (!uiState.isLoading && uiState.fields.isEmpty() && uiState.error == null) {
                        Text(
                            text = "ℹ️ Chưa có sân nào trong Firebase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onAddFieldClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm sân đầu tiên")
                        }
                        

                    }
                }
            }
        } else { // Có dữ liệu - Hiển thị danh sách sân
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hiển thị thông báo thành công với thông tin cart chi tiết
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tìm thấy ${fields.size} sân",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Thông tin cart bổ sung
                        Spacer(modifier = Modifier.height(8.dp))
                        // Chỉ tính trên các sân thuộc account hiện tại
                        val statsFields = fields.filter { it.ownerId == currentUser?.userId }
                        val activeFields = statsFields.count { it.isActive }
                        val totalSports = statsFields.flatMap { it.sports }.distinct()
                        // Tổng sao = tổng tất cả điểm sao của mọi review trên các sân của account
                        val totalStars = statsFields.sumOf { field ->
                            val summary = reviewSummaryMap[field.fieldId]
                            val avg = (summary?.averageRating ?: field.averageRating).toDouble()
                            val count = (summary?.totalReviews ?: field.totalReviews).toDouble()
                            avg * count
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🟢 $activeFields hoạt động",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "⭐ Tổng ${String.format("%.0f", totalStars)} sao",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "🏟️ ${totalSports.size} loại",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Lọc theo tên sân gần đúng và loại môn thể thao
                val filteredFields = remember(fields, searchQuery) {
                    if (searchQuery.isBlank()) fields else fields.filter { f ->
                        val q = searchQuery.trim().lowercase()
                        val nameMatch = f.name.lowercase().contains(q)
                        val sportMatch = f.sports.any { it.lowercase().contains(q) }
                        nameMatch || sportMatch
                    }.sortedBy { f ->
                        // Sắp xếp xem gần giống nhất trước (độ ưu tiên: tên khớp, sau đó sport)
                        val q = searchQuery.trim().lowercase()
                        val nameIndex = f.name.lowercase().indexOf(q).let { if (it == -1) Int.MAX_VALUE else it }
                        val sportIndex = f.sports.minOfOrNull { it.lowercase().indexOf(q).let { i -> if (i == -1) Int.MAX_VALUE else i } } ?: Int.MAX_VALUE
                        minOf(nameIndex, sportIndex)
                    }
                }

                // Hiển thị danh sách sân bằng LazyColumn
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredFields) { field ->
                        val summary = reviewSummaryMap[field.fieldId]
                        val fieldWithLiveRating = field.copy(
                            averageRating = summary?.averageRating ?: field.averageRating,
                            totalReviews = summary?.totalReviews ?: field.totalReviews
                        )
                        FieldCard(
                            field = fieldWithLiveRating,
                            onClick = { clickedField -> onFieldClick(clickedField.fieldId) },
                            onViewDetailsClick = { onFieldClick(field.fieldId) },
                            ownerAvatarUrl = currentUser?.avatarUrl
                        )
                    }
                }
            }
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onAddFieldClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sân")
            }
        }
    }
}

// Mock data cho testing - Dữ liệu mẫu để kiểm thử
private fun getMockFields(): List<Field> { // Tạo danh sách sân mẫu để hiển thị
    return listOf( // Trả về danh sách các phần tử Field
        Field( // Phần tử 1
            fieldId = "1", // Mã sân
            ownerId = "owner123",
            name = "POC Pickleball", // Tên sân
            sports = listOf("PICKLEBALL"), // Loại sân
            address = "25 Tú Xương, P. Tăng Nhơn Phú B, TP. Thủ Đức", // Địa chỉ
            geo = GeoLocation(lat = 10.7769, lng = 106.7009), // Vị trí
            images = com.trungkien.fbtp_cn.model.FieldImages(
                mainImage = "",
                image1 = "",
                image2 = "",
                image3 = ""
            ),
            slotMinutes = 30,
            openHours = OpenHours(start = "05:00", end = "23:00", isOpen24h = false), // Giờ mở cửa
            amenities = listOf("PARKING", "EQUIPMENT"),
            description = "Sân Pickleball chất lượng cao",
            contactPhone = "0926666357", // SĐT liên hệ
            averageRating = 4.5f, // Điểm đánh giá
            totalReviews = 128, // Số đánh giá
            isActive = true
        ),
        Field( // Phần tử 2
            fieldId = "2",
            ownerId = "owner123",
            name = "Sân Cầu Lông ABC",
            sports = listOf("BADMINTON"),
            address = "123 Đường XYZ, Quận 1, TP.HCM",
            geo = GeoLocation(lat = 10.7829, lng = 106.6992),
            images = com.trungkien.fbtp_cn.model.FieldImages(
                mainImage = "",
                image1 = "",
                image2 = "",
                image3 = ""
            ),
            slotMinutes = 30,
            openHours = OpenHours(start = "06:00", end = "22:00", isOpen24h = false),
            amenities = listOf("PARKING", "SHOWER"),
            description = "Sân cầu lông chuyên nghiệp",
            contactPhone = "0901234567",
            averageRating = 4.2f,
            totalReviews = 89,
            isActive = true
        ),
        Field( // Phần tử 3
            fieldId = "3",
            ownerId = "owner123",
            name = "Sân Bóng Đá Mini",
            sports = listOf("FOOTBALL"),
            address = "456 Đường QWE, Quận 7, TP.HCM",
            geo = GeoLocation(lat = 10.7308, lng = 106.7263),
            images = com.trungkien.fbtp_cn.model.FieldImages(
                mainImage = "",
                image1 = "",
                image2 = "",
                image3 = ""
            ),
            slotMinutes = 30,
            openHours = OpenHours(start = "07:00", end = "23:00", isOpen24h = false),
            amenities = listOf("PARKING", "EQUIPMENT"),
            description = "Sân bóng đá mini chất lượng cao",
            contactPhone = "0987654321",
            averageRating = 4.0f,
            totalReviews = 67,
            isActive = true
        )
    )
}

@Preview // Đánh dấu đây là hàm preview
@Composable // Đánh dấu đây là một composable function
fun OwnerFieldManagerPreview() { // Hàm xem trước UI màn hình quản lý sân
    FBTP_CNTheme { // Áp dụng theme
        OwnerFieldManagementScreen( // Gọi composable chính
            onFieldClick = { /* Preview callback */ },
            onAddFieldClick = { /* Preview callback */ },
            modifier = Modifier.fillMaxSize(), // Chiếm toàn bộ diện tích
            testMode = true // Sử dụng test mode để hiển thị mock data
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnerFieldManagerWithDataPreview() { // Preview với dữ liệu thực
    FBTP_CNTheme {
        OwnerFieldManagementScreen(
            onFieldClick = { /* Preview callback */ },
            onAddFieldClick = { /* Preview callback */ },
            modifier = Modifier.fillMaxSize(),
            testMode = false // Không dùng test mode
        )
    }
}