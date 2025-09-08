package com.trungkien.fbtp_cn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
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
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.viewmodel.AuthViewModel
import com.trungkien.fbtp_cn.ui.components.owner.profile.ImageUploadService
import android.widget.Toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser = authViewModel.currentUser.collectAsState().value
    val context = LocalContext.current
    var ownerName by remember { mutableStateOf(currentUser?.name ?: "") }
    var ownerEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var ownerPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chỉnh sửa hồ sơ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Avatar section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar với nút chỉnh sửa
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ownerName.take(1).uppercase(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { /* TODO: Handle avatar change */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00C853)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = Color(0xFF00C853)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Change Avatar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thay đổi ảnh đại diện",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Personal Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Thông tin cá nhân",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Họ và tên
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = {
                            Text(
                                text = "Họ và tên",
                                color = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email
                    OutlinedTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = {
                            Text(
                                text = "Email",
                                color = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Số điện thoại
                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = {
                            Text(
                                text = "Số điện thoại",
                                color = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedLabelColor = Color(0xFF00C853)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    // removed address and bio
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
                        width = 1.dp,
                        color = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Hủy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = {
                        if (ownerName.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (ownerEmail.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        authViewModel.updateProfile(
                            name = ownerName,
                            email = ownerEmail,
                            phone = ownerPhone
                        ) { ok, msg ->
                            if (ok) {
                                Toast.makeText(context, "Đã lưu hồ sơ", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            } else {
                                Toast.makeText(context, msg ?: "Lưu thất bại", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
    FBTP_CNTheme {
        EditProfileScreen(onBackClick = {})
    }
}
