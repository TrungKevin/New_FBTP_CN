package com.trungkien.fbtp_cn.ui.components.owner.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.model.OpenHours
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldDialog(
    field: Field,
    fieldViewModel: FieldViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by fieldViewModel.uiState.collectAsState()
    
    // State cho các trường có thể chỉnh sửa
    var fieldName by remember { mutableStateOf(field.name) }
    var fieldAddress by remember { mutableStateOf(field.address) }
    var fieldDescription by remember { mutableStateOf(field.description) }
    var contactPhone by remember { mutableStateOf(field.contactPhone) }
    var openStart by remember { mutableStateOf(field.openHours.start) }
    var openEnd by remember { mutableStateOf(field.openHours.end) }
    var active by remember { mutableStateOf(field.active) }
    
    // Cập nhật state khi field thay đổi
    LaunchedEffect(field) {
        fieldName = field.name
        fieldAddress = field.address
        fieldDescription = field.description
        contactPhone = field.contactPhone
        openStart = field.openHours.start
        openEnd = field.openHours.end
        active = field.active
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Chỉnh sửa thông tin sân",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tên sân
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("Tên sân") },
                    leadingIcon = { Icon(Icons.Default.SportsSoccer, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !uiState.isLoading
                )
                
                // Địa chỉ
                OutlinedTextField(
                    value = fieldAddress,
                    onValueChange = { fieldAddress = it },
                    label = { Text("Địa chỉ") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !uiState.isLoading
                )
                
                // Mô tả
                OutlinedTextField(
                    value = fieldDescription,
                    onValueChange = { fieldDescription = it },
                    label = { Text("Mô tả") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !uiState.isLoading
                )
                
                // Số điện thoại
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    enabled = !uiState.isLoading
                )
                
                // Giờ hoạt động
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = openStart,
                        onValueChange = { openStart = it },
                        label = { Text("Giờ mở") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !uiState.isLoading
                    )
                    OutlinedTextField(
                        value = openEnd,
                        onValueChange = { openEnd = it },
                        label = { Text("Giờ đóng") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, null) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        enabled = !uiState.isLoading
                    )
                }
                
                // Trạng thái hoạt động
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Trạng thái hoạt động",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        enabled = !uiState.isLoading
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation
                    if (fieldName.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (fieldAddress.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Tạo field mới với thông tin đã chỉnh sửa
                    val updatedField = field.copy(
                        name = fieldName.trim(),
                        address = fieldAddress.trim(),
                        description = fieldDescription.trim(),
                        contactPhone = contactPhone.trim(),
                        openHours = OpenHours(
                            start = openStart.trim(),
                            end = openEnd.trim(),
                            open24h = false
                        ),
                        active = active
                    )
                    
                    // Lưu thay đổi
                    fieldViewModel.handleEvent(FieldEvent.UpdateField(updatedField))
                    onSave()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (uiState.isLoading) "Đang lưu..." else "Lưu thay đổi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Hủy",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        modifier = modifier
    )
    
    // Hiển thị thông báo kết quả
    LaunchedEffect(uiState.success, uiState.error) {
        when {
            uiState.success != null -> {
                Toast.makeText(context, uiState.success, Toast.LENGTH_SHORT).show()
                onSave() // Đóng dialog khi lưu thành công
            }
            uiState.error != null -> {
                Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Preview
@Composable
fun EditFieldDialogPreview() {
    MaterialTheme {
        // Preview không thể tạo ViewModel, chỉ hiển thị UI
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Chỉnh sửa thông tin sân",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = "Sân bóng đá ABC",
                        onValueChange = {},
                        label = { Text("Tên sân") },
                        leadingIcon = { Icon(Icons.Default.SportsSoccer, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = "123 Đường ABC, Quận 1, TP.HCM",
                        onValueChange = {},
                        label = { Text("Địa chỉ") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Lưu thay đổi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("Hủy")
                }
            }
        )
    }
}
