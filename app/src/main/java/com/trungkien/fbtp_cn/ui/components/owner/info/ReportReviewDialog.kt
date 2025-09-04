package com.trungkien.fbtp_cn.ui.components.owner.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * DIALOG BÁO CÁO REVIEW
 * 
 * Cho phép user:
 * - Chọn lý do báo cáo
 * - Nhập mô tả chi tiết (tùy chọn)
 */
@Composable
fun ReportReviewDialog(
    reviewId: String,
    onDismiss: () -> Unit,
    onReport: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReason by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Available report reasons
    val reportReasons = listOf(
        "Nội dung không phù hợp" to "Nội dung vi phạm quy tắc cộng đồng",
        "Spam hoặc quảng cáo" to "Nội dung spam, quảng cáo không mong muốn",
        "Thông tin sai lệch" to "Thông tin không chính xác, gây hiểu nhầm",
        "Quấy rối hoặc lạm dụng" to "Nội dung quấy rối, lạm dụng người khác",
        "Bản quyền" to "Vi phạm bản quyền, sao chép nội dung",
        "Khác" to "Lý do khác không có trong danh sách"
    )
    
    // Validation
    val isFormValid = selectedReason.isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Báo cáo đánh giá",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Hãy chọn lý do báo cáo để chúng tôi có thể xử lý tốt hơn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Report reasons
                ReportReasonsSection(
                    reportReasons = reportReasons,
                    selectedReason = selectedReason,
                    onReasonSelected = { selectedReason = it }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Description (optional)
                DescriptionSection(
                    description = description,
                    onDescriptionChanged = { description = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Warning
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Cảnh báo",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Báo cáo sai sự thật có thể dẫn đến việc tài khoản của bạn bị hạn chế.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        
                        onReport(selectedReason, description.trim())
                        isLoading = false
                        onDismiss()
                    }
                },
                enabled = isFormValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi báo cáo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        modifier = modifier
    )
}

/**
 * COMPONENT CHỌN LÝ DO BÁO CÁO
 */
@Composable
private fun ReportReasonsSection(
    reportReasons: List<Pair<String, String>>,
    selectedReason: String,
    onReasonSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Lý do báo cáo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Radio buttons for each reason
        reportReasons.forEach { (reason, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                RadioButton(
                    selected = selectedReason == reason,
                    onClick = { onReasonSelected(reason) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.error
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * COMPONENT NHẬP MÔ TẢ CHI TIẾT
 */
@Composable
private fun DescriptionSection(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Mô tả chi tiết (tùy chọn)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Hãy mô tả chi tiết lý do báo cáo...")
            },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.error,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        // Character count
        Text(
            text = "${description.length}/200",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
