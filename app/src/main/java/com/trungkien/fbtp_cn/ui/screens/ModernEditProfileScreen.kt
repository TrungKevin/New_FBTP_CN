package com.trungkien.fbtp_cn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.ui.components.owner.profile.ImageUploadService
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernEditProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser = authViewModel.currentUser.collectAsState().value
    
    // Debug currentUser and fetch profile
    LaunchedEffect(Unit) {
        println("🔄 DEBUG: ModernEditProfileScreen - Fetching current user profile...")
        authViewModel.fetchProfile()
    }
    
    LaunchedEffect(currentUser) {
        println("🔄 DEBUG: ModernEditProfileScreen - currentUser: $currentUser")
        println("🔄 DEBUG: ModernEditProfileScreen - currentUser?.userId: ${currentUser?.userId}")
        println("🔄 DEBUG: ModernEditProfileScreen - currentUser?.name: ${currentUser?.name}")
    }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val imageUploadService = remember { ImageUploadService() }
    
    // State variables
    var ownerName by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var pendingAvatar: String? by remember { mutableStateOf(null) }
    var waitingForSync by remember { mutableStateOf(false) }
    
    // Initialize with current user data
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            ownerName = user.name ?: ""
            ownerEmail = user.email ?: ""
            ownerPhone = user.phone ?: ""
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        println("🔄 DEBUG: Image picker result: $uri")
        uri?.let {
            println("🔄 DEBUG: Setting selectedImageUri to: $it")
            selectedImageUri = it
            println("🔄 DEBUG: selectedImageUri set successfully")
        } ?: run {
            println("🔄 DEBUG: Image picker returned null")
        }
    }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chỉnh sửa hồ sơ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF00C853)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { keyboardController?.hide() },
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Avatar Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ảnh đại diện",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Avatar Display
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .border(
                                width = 3.dp,
                                color = Color(0xFF00C853),
                                shape = CircleShape
                            )
                            .clickable { 
                                println("🔄 DEBUG: Avatar clicked, launching image picker...")
                                imagePickerLauncher.launch("image/*") 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected Avatar",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            !currentUser?.avatarUrl.isNullOrEmpty() -> {
                                AsyncImage(
                                    model = currentUser?.avatarUrl,
                                    contentDescription = "Current Avatar",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF757575)
                                )
                            }
                        }
                        
                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Avatar",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Nhấn để thay đổi ảnh đại diện",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    
                    // Upload progress
                    if (isUploadingAvatar) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF00C853),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đang tải lên...",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
            
            // Personal Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Thông tin cá nhân",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Name Field
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = {
                            Text(
                                text = "Họ và tên",
                                color = Color(0xFF757575)
                            )
                        },
                        placeholder = {
                            Text(
                                text = currentUser?.name ?: "Nhập họ và tên",
                                color = Color(0xFFBDBDBD)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853),
                            cursorColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email Field
                    OutlinedTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = {
                            Text(
                                text = "Email",
                                color = Color(0xFF757575)
                            )
                        },
                        placeholder = {
                            Text(
                                text = currentUser?.email ?: "Nhập email",
                                color = Color(0xFFBDBDBD)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853),
                            cursorColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Phone Field
                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = {
                            Text(
                                text = "Số điện thoại",
                                color = Color(0xFF757575)
                            )
                        },
                        placeholder = {
                            Text(
                                text = currentUser?.phone ?: "Nhập số điện thoại",
                                color = Color(0xFFBDBDBD)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853),
                            cursorColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onBackClick() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF757575)
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Hủy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = {
                        println("🔄 DEBUG: Save button clicked")
                        println("🔄 DEBUG: selectedImageUri: $selectedImageUri")
                        println("🔄 DEBUG: currentUser?.userId: ${currentUser?.userId}")
                        println("🔄 DEBUG: ownerName: $ownerName")
                        println("🔄 DEBUG: ownerEmail: $ownerEmail")
                        println("🔄 DEBUG: ownerPhone: $ownerPhone")
                        
                        isLoading = true
                        
                        // Upload avatar if selected
                        if (selectedImageUri != null && currentUser?.userId != null) {
                            println("🔄 DEBUG: Starting avatar upload process...")
                            isUploadingAvatar = true
                            coroutineScope.launch {
                                try {
                                    val uploadResult = imageUploadService.uploadAvatar(
                                        context,
                                        selectedImageUri!!,
                                        currentUser.userId!!
                                    )
                                    
                                    uploadResult.fold(
                                        onSuccess = { avatarBase64 ->
                                            // Update profile with avatar
                                            authViewModel.updateProfile(
                                                name = if (ownerName.isBlank()) currentUser?.name ?: "" else ownerName,
                                                email = if (ownerEmail.isBlank()) currentUser?.email ?: "" else ownerEmail,
                                                phone = if (ownerPhone.isBlank()) currentUser?.phone ?: "" else ownerPhone,
                                                avatarUrl = avatarBase64
                                            ) { ok, msg ->
                                                if (ok) {
                                                    // Chờ đồng bộ user mới từ Firestore: bật waiting và ghi nhận avatar kỳ vọng
                                                    pendingAvatar = when {
                                                        avatarBase64.startsWith("data:image") -> avatarBase64
                                                        else -> "data:image/jpeg;base64,$avatarBase64"
                                                    }
                                                    waitingForSync = true
                                                    authViewModel.fetchProfile()
                                                } else {
                                                    isLoading = false
                                                    isUploadingAvatar = false
                                                    Toast.makeText(context, msg ?: "Lưu thất bại", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onFailure = { error ->
                                            isLoading = false
                                            isUploadingAvatar = false
                                            Toast.makeText(context, "Tải ảnh lên thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } catch (e: Exception) {
                                    isLoading = false
                                    isUploadingAvatar = false
                                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Update profile without avatar
                            println("🔄 DEBUG: No new image selected, updating profile without avatar")
                            println("🔄 DEBUG: selectedImageUri is null: ${selectedImageUri == null}")
                            println("🔄 DEBUG: currentUser?.userId is null: ${currentUser?.userId == null}")
                            
                            authViewModel.updateProfile(
                                name = if (ownerName.isBlank()) currentUser?.name ?: "" else ownerName,
                                email = if (ownerEmail.isBlank()) currentUser?.email ?: "" else ownerEmail,
                                phone = if (ownerPhone.isBlank()) currentUser?.phone ?: "" else ownerPhone
                            ) { ok, msg ->
                                println("🔄 DEBUG: Profile update result (no avatar): ok=$ok, msg=$msg")
                                if (ok) {
                                    waitingForSync = true
                                    pendingAvatar = currentUser?.avatarUrl
                                    authViewModel.fetchProfile()
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, msg ?: "Lưu thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Lưu thay đổi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading || isUploadingAvatar || waitingForSync) {
                com.trungkien.fbtp_cn.ui.components.common.LoadingDialog(message = "Đang lưu & đồng bộ hồ sơ...")
            }
        }
    }

    LaunchedEffect(currentUser?.avatarUrl, pendingAvatar) {
        if (waitingForSync && !pendingAvatar.isNullOrBlank()) {
            val cur = currentUser?.avatarUrl
            if (!cur.isNullOrBlank() && cur == pendingAvatar) {
                // Ensure optimistic avatar is applied across the app before closing
                authViewModel.applyOptimisticAvatar(pendingAvatar)
                waitingForSync = false
                isLoading = false
                onBackClick()
            }
        }
    }

    // Safety timeout: if Firestore sync is slow, close after 2500ms using optimistic avatar
    LaunchedEffect(waitingForSync) {
        if (waitingForSync) {
            delay(2500)
            if (waitingForSync) {
                // Timeout: still apply optimistic avatar so all screens update together
                authViewModel.applyOptimisticAvatar(pendingAvatar)
                waitingForSync = false
                isLoading = false
                onBackClick()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ModernEditProfileScreenPreview() {
    FBTP_CNTheme {
        ModernEditProfileScreen(onBackClick = {})
    }
}
