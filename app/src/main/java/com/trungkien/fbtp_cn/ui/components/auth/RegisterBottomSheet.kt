package com.trungkien.fbtp_cn.ui.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.runtime.saveable.rememberSaveable
import com.trungkien.fbtp_cn.ui.theme.*
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.trungkien.fbtp_cn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBottomSheet(
    onDismiss: () -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit,
    isLoading: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    
    // Optimize state management with rememberSaveable for better performance
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var agreeTerms by rememberSaveable { mutableStateOf(true) }
    
    // Move roles outside composable to avoid recreation
    val roles = remember { listOf("Chủ sân", "Khách hàng") }
    var selectedRole by rememberSaveable { mutableStateOf(roles[1]) }
    var roleExpanded by rememberSaveable { mutableStateOf(false) }
    
    // Pre-compile regex patterns for better performance
    val phoneRegex = remember { Regex("^\\d{10,11}$") }
    val emailRegex = remember { Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$") }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onSwitchToLogin() }
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập") },
                leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                enabled = !isLoading
            )

            // (Moved) Role selector will be placed near register button

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                enabled = !isLoading
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Số điện thoại") },
                leadingIcon = { Icon(Icons.Filled.Phone, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                enabled = !isLoading
            )

            // Password
            OutlinedTextField( // dùng để nhập mật khẩu và kiểm tra mật khẩu hiển thị
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Color.White.copy(alpha = 0.8f))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                enabled = !isLoading
            )

            // Confirm password
            OutlinedTextField( // dùng để nhập lại mật khẩu và kiểm tra mật khẩu hiển thị
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Nhập lại mật khẩu") },
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Color.White.copy(alpha = 0.8f))
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                enabled = !isLoading
            )

            // Role selector (above checkbox)
            ExposedDropdownMenuBox( // dùng để chọn vai trò người dùng
                expanded = roleExpanded,
                onExpandedChange = { roleExpanded = !roleExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                TextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vai trò") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background,
                        focusedIndicatorColor = GreenPrimary,
                        unfocusedIndicatorColor = OnSecondary.copy(alpha = 0.25f),
                        focusedLabelColor = GreenPrimary,
                        unfocusedLabelColor = OnSecondary.copy(alpha = 0.8f),
                        focusedTextColor = OnSecondary,
                        unfocusedTextColor = OnSecondary,
                        cursorColor = GreenPrimary
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = !isLoading
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role) },
                            onClick = {
                                selectedRole = role
                                roleExpanded = false
                            }
                        )
                    }
                }
            }

            // Agree terms (label on the right reflects the purpose)
            Row( // dùng để đồng ý với điều khoản sử dụng
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = { agreeTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = OnSecondary.copy(alpha = 0.4f)),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (agreeTerms) "Đã đồng ý điều khoản sử dụng" else "Tôi đồng ý với điều khoản sử dụng", color = OnSecondary, fontSize = 14.sp)
            }

            // Register button
            Button( // dùng để đăng ký tài khoản
                onClick = {
                    // Use pre-compiled regex patterns for validation
                    val phoneOk = phoneRegex.matches(phone)
                    val emailOk = emailRegex.matches(email)
                    val passwordOk = password.length >= 6
                    val confirmOk = confirm == password

                    when {
                        username.isBlank() -> {
                            Toast.makeText(context, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show()
                        }
                        !emailOk -> {
                            Toast.makeText(context, "Email phải đúng định dạng và có đuôi @gmail.com", Toast.LENGTH_SHORT).show()
                        }
                        !phoneOk -> {
                            Toast.makeText(context, "Số điện thoại phải là 10-11 chữ số", Toast.LENGTH_SHORT).show()
                        }
                        !passwordOk -> {
                            Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                        }
                        !confirmOk -> {
                            Toast.makeText(context, "Nhập lại mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                        }
                        !agreeTerms -> {
                            Toast.makeText(context, "Bạn cần đồng ý điều khoản sử dụng", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Call registration in a coroutine to avoid blocking UI
                            onRegister(username, password, email, phone, selectedRole)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Đang đăng ký...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = "Click để đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
private fun RegisterBottomSheetPreview() {
    RegisterBottomSheet(
        onDismiss = {}, 
        onRegister = { _, _, _, _, _ -> }, 
        onSwitchToLogin = {},
        isLoading = false
    )
}


