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
    var username by remember { mutableStateOf("Roro123") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberAccount by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF96EC96), // Dark background color
        scrimColor = Color.Black.copy(alpha = 0.5f),
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Quay", color = Color.White, fontSize = 16.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = OrangeSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Đăng ký",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
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
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = GreenPrimary,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
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
                            checkedColor = OrangeSecondary,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text(text = "Nhớ tài khoản và mật khẩu", color = Color.White, fontSize = 14.sp)
                }
                Text(
                    text = "Quên mật khẩu?",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onForgotPassword() }
                )
            }

            // Login button
            Button(
                onClick = { onLogin(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeSecondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Click để đăng nhập",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
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


