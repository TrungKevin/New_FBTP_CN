package com.trungkien.fbtp_cn.ui.components.owner.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.trungkien.fbtp_cn.model.Field
import com.trungkien.fbtp_cn.viewmodel.FieldViewModel
import com.trungkien.fbtp_cn.viewmodel.FieldEvent

@Composable
fun DeleteFieldDialog(
    field: Field,
    fieldViewModel: FieldViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by fieldViewModel.uiState.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Xác nhận xóa sân",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Bạn có chắc chắn muốn xóa sân \"${field.name}\" không?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Cảnh báo:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Hành động này sẽ xóa:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Thông tin sân và hình ảnh\n• Bảng giá và dịch vụ\n• Đánh giá và phản hồi\n• Khe giờ đã đặt (slots)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ Giữ lại:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Lịch sử đặt sân (bookings)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Lưu ý: Nếu có khách hàng đã đặt và chưa qua thời gian sử dụng, bạn sẽ không thể xóa sân.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    fieldViewModel.handleEvent(FieldEvent.DeleteField(field.fieldId))
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
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
                    text = if (uiState.isLoading) "Đang xóa..." else "Xóa sân",
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
                onConfirm() // Quay lại màn hình trước khi xóa thành công
            }
            uiState.error != null -> {
                // Error sẽ được hiển thị trong UI chính
            }
        }
    }
}

@Preview
@Composable
fun DeleteFieldDialogPreview() {
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Xác nhận xóa sân",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Text("Bạn có chắc chắn muốn xóa sân \"Sân bóng đá ABC\" không?")
            },
            confirmButton = {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa", color = Color.White)
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
