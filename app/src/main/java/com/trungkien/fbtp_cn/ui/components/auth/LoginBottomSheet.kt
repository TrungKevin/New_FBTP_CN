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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    var username by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberAccount by remember { mutableStateOf(prefs.getBoolean("remember", true)) }

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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Đăng ký",
                    color = OnSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }

            // Email
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = OnSecondary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = OnSecondary.copy(alpha = 0.25f),
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = OnSecondary.copy(alpha = 0.8f),
                    focusedTextColor = OnSecondary,
                    unfocusedTextColor = OnSecondary,
                    cursorColor = GreenPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = OnSecondary.copy(alpha = 0.25f),
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = OnSecondary.copy(alpha = 0.8f),
                    focusedTextColor = OnSecondary,
                    unfocusedTextColor = OnSecondary,
                    cursorColor = GreenPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

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
                    modifier = Modifier.clickable { onForgotPassword() }
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
        onSwitchToRegister = {}
    )
}


