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
import com.trungkien.fbtp_cn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBottomSheet(
    onDismiss: () -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(true) }
    val roles = listOf("Chủ sân", "Khách hàng")
    var selectedRole by remember { mutableStateOf(roles[1]) }
    var roleExpanded by remember { mutableStateOf(false) }

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
                    modifier = Modifier.clickable { onSwitchToLogin() }
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
                Text(
                    text = "Đăng Nhập",
                    color = OnSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
            )

            // Password
            OutlinedTextField(
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )

            // Confirm password
            OutlinedTextField(
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            // Role selector (above checkbox)
            ExposedDropdownMenuBox(
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeTerms,
                    onCheckedChange = { agreeTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = OnSecondary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (agreeTerms) "Đã đồng ý điều khoản sử dụng" else "Tôi đồng ý với điều khoản sử dụng", color = OnSecondary, fontSize = 14.sp)
            }

            // Register button
            Button(
                onClick = {
                    if (
                        username.isNotBlank() &&
                        email.isNotBlank() &&
                        phone.isNotBlank() &&
                        password.isNotEmpty() &&
                        password == confirm && agreeTerms
                    ) {
                        onRegister(username, password, email, phone, selectedRole)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Click để đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
private fun RegisterBottomSheetPreview() {
    RegisterBottomSheet(onDismiss = {}, onRegister = { _, _, _, _, _ -> }, onSwitchToLogin = {})
}


