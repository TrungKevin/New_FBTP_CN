package com.trungkien.fbtp_cn.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.viewmodel.FieldEvent
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldUiState
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.ui.components.common.LoadingDialog
import com.trungkien.fbtp_cn.utils.ImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldScreen(
    onBackClick: () -> Unit,
    onFieldAdded: (String) -> Unit, // fieldId
    modifier: Modifier = Modifier
) {
    val fieldViewModel: FieldViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by fieldViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Focus management for keyboard hiding
    val focusManager = LocalFocusManager.current
    
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4
    
    // Field information
    var fieldName by remember { mutableStateOf("") }
    var fieldAddress by remember { mutableStateOf("") }
    var selectedSports by remember { mutableStateOf(listOf<String>()) }
    var fieldDescription by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    
    // ✅ FIX: Thêm state cho loại sân bóng đá
    var selectedFootballFieldType by remember { mutableStateOf<String?>(null) }
    
    // Images - Sử dụng Uri thay vì String
    var mainImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var image1Uri by remember { mutableStateOf<android.net.Uri?>(null) }
    var image2Uri by remember { mutableStateOf<android.net.Uri?>(null) }
    var image3Uri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // Operating hours
    var startTime by remember { mutableStateOf("06:00") }
    var endTime by remember { mutableStateOf("23:00") }
    var isOpen24h by remember { mutableStateOf(false) }
    
    // Amenities
    var selectedAmenities by remember { mutableStateOf(listOf<String>()) }
    
    // Pricing rules
    var weekdayPrice by remember { mutableStateOf("") }
    var weekendPrice by remember { mutableStateOf("") }
    
    // Services
    var fieldServices by remember { mutableStateOf(listOf<FieldService>()) }
    
    // Auto-fetch user profile when screen initializes
    LaunchedEffect(Unit) {
        authViewModel.fetchProfile()
    }
    
    // Handle success and error states
    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            // Extract fieldId from success message
            val fieldId = success.substringAfter("ID: ").substringBefore("!")
            onFieldAdded(fieldId)
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Show error toast or snackbar
            println("Error: $error")
        }
    }
    
    // Function to create Field object and submit
    fun submitField() {
        // Validate required fields
        if (fieldName.isBlank() || fieldAddress.isBlank() || selectedSports.isEmpty()) {
            // Show validation error
            return
        }
        
        // Validate images - đảm bảo có đủ 4 ảnh
        val images = listOfNotNull(mainImageUri, image1Uri, image2Uri, image3Uri)
        if (images.size < 4) {
            // Show validation error for images
            println("Cần upload đủ 4 ảnh: ${images.size}/4")
            return
        }
        
        // Validate pricing
        if (weekdayPrice.isBlank() || weekendPrice.isBlank()) {
            println("Cần nhập giá ngày thường và cuối tuần")
            return
        }
        
        // Create Field object
        val field = Field(
            fieldId = "",
            ownerId = currentUser?.userId ?: "", // Get real ownerId from current user
            name = fieldName,
            address = fieldAddress,
            geo = GeoLocation(), // TODO: Get from location picker
            sports = selectedSports,
            images = FieldImages(), // Will be updated by repository with base64 strings
            slotMinutes = 30,
            openHours = OpenHours(
                start = startTime,
                end = endTime,
                isOpen24h = isOpen24h
            ),
            amenities = selectedAmenities,
            description = fieldDescription,
            contactPhone = currentUser?.phone ?: contactPhone, // Use current user's phone
            averageRating = 0f,
            totalReviews = 0,
            isActive = true,
            // ✅ FIX: Thêm footballFieldType nếu có chọn FOOTBALL
            footballFieldType = if (selectedSports.contains("FOOTBALL")) selectedFootballFieldType else null
        )
        
        // Create pricing rules
        val pricingRules = fieldViewModel.createDefaultPricingRules(
            weekdayPrice.toIntOrNull() ?: 0,
            weekendPrice.toIntOrNull() ?: 0
        )
        
        // Submit to ViewModel - ảnh sẽ được convert thành base64 và lưu trực tiếp vào Firestore
        fieldViewModel.handleEvent(
            FieldEvent.AddField(field, images, pricingRules, fieldServices)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm sân mới",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF00C853)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            item {
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / totalSteps,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF00C853),
                    trackColor = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bước ${currentStep + 1}/$totalSteps",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
            
            when (currentStep) {
                0 -> {
                    // Step 1: Basic Information
                    item {
                        Text(
                            text = "Thông tin cơ bản",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                    }
                    
                    item {
                        BasicInfoStep(
                            fieldName = fieldName,
                            onFieldNameChange = { fieldName = it },
                            fieldAddress = fieldAddress,
                            onFieldAddressChange = { fieldAddress = it },
                            selectedSports = selectedSports,
                            onSportsChange = { selectedSports = it },
                            fieldDescription = fieldDescription,
                            onDescriptionChange = { fieldDescription = it },
                            contactPhone = currentUser?.phone ?: "",
                            onContactPhoneChange = { contactPhone = it },
                            focusManager = focusManager,
                            isPhoneEditable = false, // Phone is read-only, taken from user profile
                            // ✅ FIX: Thêm props cho football field type
                            selectedFootballFieldType = selectedFootballFieldType,
                            onFootballFieldTypeChange = { selectedFootballFieldType = it }
                        )
                    }
                }
                
                1 -> {
                    // Step 2: Images
                    item {
                        Text(
                            text = "Hình ảnh sân",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                    }
                    
                    item {
                        ImagesStep(
                            mainImageUri = mainImageUri,
                            onMainImageChange = { mainImageUri = it },
                            image1Uri = image1Uri,
                            onImage1Change = { image1Uri = it },
                            image2Uri = image2Uri,
                            onImage2Change = { image2Uri = it },
                            image3Uri = image3Uri,
                            onImage3Change = { image3Uri = it }
                        )
                    }
                }
                
                2 -> {
                    // Step 3: Operating Hours & Amenities
                    item {
                        Text(
                            text = "Giờ hoạt động & Tiện ích",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                    }
                    
                    item {
                        OperatingHoursStep(
                            startTime = startTime,
                            onStartTimeChange = { startTime = it },
                            endTime = endTime,
                            onEndTimeChange = { endTime = it },
                            isOpen24h = isOpen24h,
                            onOpen24hChange = { isOpen24h = it },
                            selectedAmenities = selectedAmenities,
                            onAmenitiesChange = { selectedAmenities = it }
                        )
                    }
                }
                
                3 -> {
                    // Step 4: Pricing & Services
                    item {
                        Text(
                            text = "Bảng giá & Dịch vụ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238)
                        )
                    }
                    
                    item {
                        PricingServicesStep(
                            weekdayPrice = weekdayPrice,
                            onWeekdayPriceChange = { weekdayPrice = it },
                            weekendPrice = weekendPrice,
                            onWeekendPriceChange = { weekendPrice = it },
                            fieldServices = fieldServices,
                            onServicesChange = { fieldServices = it }
                        )
                    }
                }
            }
            
            // Navigation buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Quay lại")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = {
                            if (currentStep < totalSteps - 1) {
                                currentStep++
                            } else {
                                // Submit field using ViewModel
                                submitField()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isStepValid(
                            currentStep, 
                            fieldName, 
                            fieldAddress, 
                            selectedSports,
                            mainImageUri,
                            image1Uri,
                            image2Uri,
                            image3Uri,
                            weekdayPrice,
                            weekendPrice,
                            // ✅ FIX: Thêm selectedFootballFieldType
                            selectedFootballFieldType
                        ) && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text(if (currentStep < totalSteps - 1) "Tiếp theo" else "Hoàn thành")
                        }
                    }
                }
            }
        }
    }
    
    // Loading Dialog
    if (uiState.isLoading) {
        LoadingDialog()
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            // TODO: Implement proper error display
        }
    }
}

@Composable
private fun BasicInfoStep(
    fieldName: String,
    onFieldNameChange: (String) -> Unit,
    fieldAddress: String,
    onFieldAddressChange: (String) -> Unit,
    selectedSports: List<String>,
    onSportsChange: (List<String>) -> Unit,
    fieldDescription: String,
    onDescriptionChange: (String) -> Unit,
    contactPhone: String,
    onContactPhoneChange: (String) -> Unit,
    focusManager: FocusManager,
    isPhoneEditable: Boolean = true,
    // ✅ FIX: Thêm props cho football field type
    selectedFootballFieldType: String? = null,
    onFootballFieldTypeChange: (String?) -> Unit = {}
) {
    val availableSports = listOf("TENNIS", "BADMINTON", "FOOTBALL", "PICKLEBALL")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = fieldName,
            onValueChange = onFieldNameChange,
            label = { Text("Tên sân") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        
        OutlinedTextField(
            value = fieldAddress,
            onValueChange = onFieldAddressChange,
            label = { Text("Địa chỉ") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        
        Text(
            text = "Môn thể thao",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableSports) { sport ->
                FilterChip(
                    selected = selectedSports.contains(sport),
                    onClick = {
                        if (selectedSports.contains(sport)) {
                            onSportsChange(selectedSports - sport)
                            // ✅ FIX: Reset football field type khi bỏ chọn FOOTBALL
                            if (sport == "FOOTBALL") {
                                onFootballFieldTypeChange(null)
                            }
                        } else {
                            onSportsChange(selectedSports + sport)
                        }
                    },
                    label = { Text(sport) }
                )
            }
        }
        
        // ✅ FIX: Hiển thị lựa chọn loại sân bóng đá khi chọn FOOTBALL
        if (selectedSports.contains("FOOTBALL")) {
            Text(
                text = "Loại sân bóng đá",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("5_PLAYERS", "7_PLAYERS", "11_PLAYERS")) { fieldType ->
                    FilterChip(
                        selected = selectedFootballFieldType == fieldType,
                        onClick = {
                            onFootballFieldTypeChange(if (selectedFootballFieldType == fieldType) null else fieldType)
                        },
                        label = { 
                            Text(
                                when (fieldType) {
                                    "5_PLAYERS" -> "Sân 5 người"
                                    "7_PLAYERS" -> "Sân 7 người"
                                    "11_PLAYERS" -> "Sân 11 người"
                                    else -> fieldType
                                }
                            ) 
                        }
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = fieldDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Mô tả") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
        
        OutlinedTextField(
            value = contactPhone,
            onValueChange = onContactPhoneChange,
            label = { Text("Số điện thoại liên hệ") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isPhoneEditable,
            readOnly = !isPhoneEditable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isPhoneEditable) Color(0xFF00C853) else Color(0xFFBDBDBD),
                unfocusedBorderColor = if (isPhoneEditable) Color(0xFFE0E0E0) else Color(0xFFBDBDBD),
                disabledBorderColor = Color(0xFFBDBDBD),
                disabledLabelColor = Color(0xFF757575),
                disabledTextColor = Color(0xFF757575)
            ),
            trailingIcon = if (!isPhoneEditable) {
                {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Số điện thoại từ hồ sơ cá nhân",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null
        )
        
        // Show info text when phone is read-only
        if (!isPhoneEditable) {
            Text(
                text = "Số điện thoại được lấy từ hồ sơ cá nhân. Bạn có thể cập nhật trong phần Cài đặt > Hồ sơ.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun ImagesStep(
    mainImageUri: android.net.Uri?,
    onMainImageChange: (android.net.Uri?) -> Unit,
    image1Uri: android.net.Uri?,
    onImage1Change: (android.net.Uri?) -> Unit,
    image2Uri: android.net.Uri?,
    onImage2Change: (android.net.Uri?) -> Unit,
    image3Uri: android.net.Uri?,
    onImage3Change: (android.net.Uri?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ảnh chính (bắt buộc)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        ImageUploadCard(
            imageUri = mainImageUri,
            onImageChange = onMainImageChange,
            label = "Ảnh chính",
            isRequired = true
        )
        
        Text(
            text = "Ảnh chi tiết (bắt buộc)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageUploadCard(
                imageUri = image1Uri,
                onImageChange = onImage1Change,
                label = "Ảnh 1",
                isRequired = true,
                modifier = Modifier.weight(1f)
            )
            ImageUploadCard(
                imageUri = image2Uri,
                onImageChange = onImage2Change,
                label = "Ảnh 2",
                isRequired = true,
                modifier = Modifier.weight(1f)
            )
            ImageUploadCard(
                imageUri = image3Uri,
                onImageChange = onImage3Change,
                label = "Ảnh 3",
                isRequired = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ImageUploadCard(
    imageUri: android.net.Uri?,
    onImageChange: (android.net.Uri?) -> Unit,
    label: String,
    isRequired: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageChange(it) }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Camera result will be handled by ImagePicker
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Chọn nguồn ảnh") },
            text = { Text("Bạn muốn lấy ảnh từ đâu?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Thư viện ảnh")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        // Use ImagePicker for camera
                        val imagePicker = ImagePicker(context as androidx.fragment.app.FragmentActivity)
                        imagePicker.pickImage(fromCamera = true, onImageSelected = onImageChange)
                    }
                ) {
                    Text("Máy ảnh")
                }
            }
        )
    }
    
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { showImageSourceDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = if (imageUri != null) Color(0xFFE8F5E8) else Color(0xFFF5F5F5)
        ),
        border = if (isRequired && imageUri == null) {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                // Display actual image
                AsyncImage(
                    model = imageUri,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                // Remove button
                IconButton(
                    onClick = { onImageChange(null) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "Nhấn để chọn ảnh",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperatingHoursStep(
    startTime: String,
    onStartTimeChange: (String) -> Unit,
    endTime: String,
    onEndTimeChange: (String) -> Unit,
    isOpen24h: Boolean,
    onOpen24hChange: (Boolean) -> Unit,
    selectedAmenities: List<String>,
    onAmenitiesChange: (List<String>) -> Unit
) {
    val availableAmenities = listOf(
        "PARKING", "SHOWER", "EQUIPMENT", "LIGHTING", 
        "AIR_CONDITIONING", "LOCKER_ROOM", "CAFE", "WIFI"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Giờ hoạt động",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = onStartTimeChange,
                label = { Text("Giờ mở cửa") },
                modifier = Modifier.weight(1f),
                enabled = !isOpen24h,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            
            OutlinedTextField(
                value = endTime,
                onValueChange = onEndTimeChange,
                label = { Text("Giờ đóng cửa") },
                modifier = Modifier.weight(1f),
                enabled = !isOpen24h,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isOpen24h,
                onCheckedChange = onOpen24hChange
            )
            Text("Mở cửa 24/24")
        }
        
        Text(
            text = "Tiện ích",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableAmenities) { amenity ->
                FilterChip(
                    selected = selectedAmenities.contains(amenity),
                    onClick = {
                        if (selectedAmenities.contains(amenity)) {
                            onAmenitiesChange(selectedAmenities - amenity)
                        } else {
                            onAmenitiesChange(selectedAmenities + amenity)
                        }
                    },
                    label = { Text(amenity) }
                )
            }
        }
    }
}

@Composable
private fun PricingServicesStep(
    weekdayPrice: String,
    onWeekdayPriceChange: (String) -> Unit,
    weekendPrice: String,
    onWeekendPriceChange: (String) -> Unit,
    fieldServices: List<FieldService>,
    onServicesChange: (List<FieldService>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bảng giá (VNĐ/30 phút)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = weekdayPrice,
                onValueChange = onWeekdayPriceChange,
                label = { Text("Giá ngày thường") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            
            OutlinedTextField(
                value = weekendPrice,
                onValueChange = onWeekendPriceChange,
                label = { Text("Giá cuối tuần") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00C853),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
        }
        
        Text(
            text = "Dịch vụ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        // TODO: Add service management UI
        Text(
            text = "Chức năng quản lý dịch vụ sẽ được thêm sau",
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
    }
}

private fun isStepValid(
    step: Int, 
    fieldName: String, 
    fieldAddress: String, 
    selectedSports: List<String>,
    mainImageUri: android.net.Uri?,
    image1Uri: android.net.Uri?,
    image2Uri: android.net.Uri?,
    image3Uri: android.net.Uri?,
    weekdayPrice: String,
    weekendPrice: String,
    // ✅ FIX: Thêm parameter cho football field type
    selectedFootballFieldType: String? = null
): Boolean {
    return when (step) {
        0 -> {
            val basicValid = fieldName.isNotEmpty() && fieldAddress.isNotEmpty() && selectedSports.isNotEmpty()
            // ✅ FIX: Kiểm tra football field type nếu chọn FOOTBALL
            if (selectedSports.contains("FOOTBALL")) {
                basicValid && selectedFootballFieldType != null
            } else {
                basicValid
            }
        }
        1 -> {
            // Step 2: Images - đảm bảo có đủ 4 ảnh
            val images = listOfNotNull(mainImageUri, image1Uri, image2Uri, image3Uri)
            images.size >= 4
        }
        2 -> true // Step 3: Operating Hours & Amenities
        3 -> {
            // Step 4: Pricing & Services - đảm bảo có giá
            weekdayPrice.isNotBlank() && weekendPrice.isNotBlank()
        }
        else -> false
    }
}



@Preview
@Composable
private fun AddFieldScreenPreview() {
    AddFieldScreen(
        onBackClick = {},
        onFieldAdded = {}
    )
}

@Preview
@Composable
private fun BasicInfoStepPreview() {
    BasicInfoStep(
        fieldName = "TP-Arena",
        onFieldNameChange = {},
        fieldAddress = "71 Duong 10 Tang nhon Phu-Quan9",
        onFieldAddressChange = {},
        selectedSports = listOf("FOOTBALL"),
        onSportsChange = {},
        fieldDescription = "Wellcome",
        onDescriptionChange = {},
        contactPhone = "0921483538",
        onContactPhoneChange = {},
        focusManager = LocalFocusManager.current,
        isPhoneEditable = false,
        // ✅ FIX: Thêm preview cho football field type
        selectedFootballFieldType = "5_PLAYERS",
        onFootballFieldTypeChange = {}
    )
}
