package com.trungkien.fbtp_cn.ui.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.trungkien.fbtp_cn.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.trungkien.fbtp_cn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSwitchToRegister: () -> Unit,
    errorMessage: String? = null,
    onErrorDismiss: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    var username by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberAccount by remember { mutableStateOf(prefs.getBoolean("remember", true)) }
    var previousUsername by remember { mutableStateOf(username) }
    var previousPassword by remember { mutableStateOf(password) }
    
    // Reset error when user starts typing
    LaunchedEffect(username, password) {
        if (errorMessage != null && (username != previousUsername || password != previousPassword)) {
            onErrorDismiss?.invoke()
        }
        previousUsername = username
        previousPassword = password
    }
    
    val hasError = errorMessage != null && errorMessage.isNotBlank()
    
    // When there's any login error, highlight both fields in red
    val emailError = hasError
    val passwordError = hasError

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Background,
        scrimColor = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDismiss() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Quay", color = OnSecondary, fontSize = 16.sp)
                }
                Image(
                    painter = painterResource(id = R.drawable.title),
                    contentDescription = "Title",
                    modifier = Modifier.height(40.dp)
                )
                Text(
                    text = "Đăng ký",
                    color = OnSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }

            // Email
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        if (errorMessage != null) {
                            onErrorDismiss?.invoke()
                        }
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = OnSecondary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (emailError) Color(0xFFD32F2F) else GreenPrimary,
                        unfocusedBorderColor = if (emailError) Color(0xFFD32F2F) else OnSecondary.copy(alpha = 0.25f),
                        errorBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = if (emailError) Color(0xFFD32F2F) else GreenPrimary,
                        unfocusedLabelColor = if (emailError) Color(0xFFD32F2F) else OnSecondary.copy(alpha = 0.8f),
                        errorLabelColor = Color(0xFFD32F2F),
                        focusedTextColor = OnSecondary,
                        unfocusedTextColor = OnSecondary,
                        errorTextColor = OnSecondary,
                        cursorColor = if (emailError) Color(0xFFD32F2F) else GreenPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                if (emailError && errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // Password
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (errorMessage != null) {
                            onErrorDismiss?.invoke()
                        }
                    },
                    label = { Text("Mật khẩu") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = OnSecondary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = OnSecondary.copy(alpha = 0.8f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (passwordError) Color(0xFFD32F2F) else GreenPrimary,
                        unfocusedBorderColor = if (passwordError) Color(0xFFD32F2F) else OnSecondary.copy(alpha = 0.25f),
                        errorBorderColor = Color(0xFFD32F2F),
                        focusedLabelColor = if (passwordError) Color(0xFFD32F2F) else GreenPrimary,
                        unfocusedLabelColor = if (passwordError) Color(0xFFD32F2F) else OnSecondary.copy(alpha = 0.8f),
                        errorLabelColor = Color(0xFFD32F2F),
                        focusedTextColor = OnSecondary,
                        unfocusedTextColor = OnSecondary,
                        errorTextColor = OnSecondary,
                        cursorColor = if (passwordError) Color(0xFFD32F2F) else GreenPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )
                if (passwordError && errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // Remember + Forgot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberAccount,
                        onCheckedChange = { rememberAccount = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GreenPrimary,
                            uncheckedColor = OnSecondary.copy(alpha = 0.4f)
                        )
                    )
                    Text(text = "Nhớ tài khoản và mật khẩu", color = OnSecondary, fontSize = 14.sp)
                }
                Text(
                    text = "Quên mật khẩu?",
                    color = OnSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        if (username.matches(Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$"))) {
                            // Dùng callback để tầng trên gửi email reset
                            Toast.makeText(context, "Đang gửi email khôi phục...", Toast.LENGTH_SHORT).show()
                            // Tạm dùng onLogin callback đặc thù? Không, ta không có callback email riêng ở đây.
                            // Gọi broadcast qua LocalContext? Thay vào đó, đơn giản ta mở intent email reset bằng AuthEvent từ trên.
                            // Để không đổi API, ta phát một local broadcast bằng Compose không tiện; nên hiển thị toast hướng dẫn.
                        } else {
                            Toast.makeText(context, "Nhập email hợp lệ để khôi phục", Toast.LENGTH_SHORT).show()
                        }
                        onForgotPassword()
                    }
                )
            }

            // Login button
            Button(
                onClick = {
                    val emailOk = username.matches(Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$"))
                    val passwordOk = password.isNotBlank()
                    when {
                        username.isBlank() -> Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                        !emailOk -> Toast.makeText(context, "Email phải có định dạng hợp lệ @gmail.com", Toast.LENGTH_SHORT).show()
                        !passwordOk -> Toast.makeText(context, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
                        else -> {
                            if (rememberAccount) {
                                prefs.edit()
                                    .putBoolean("remember", true)
                                    .putString("email", username)
                                    .putString("password", password)
                                    .apply()
                            } else {
                                prefs.edit().clear().apply()
                            }
                            onLogin(username, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Click để đăng nhập",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginBottomSheetPreview() {
    LoginBottomSheet(
        onDismiss = {},
        onLogin = { _, _ -> },
        onGoogleLogin = {},
        onForgotPassword = {},
        onSwitchToRegister = {},
        errorMessage = null,
        onErrorDismiss = null
    )
}


