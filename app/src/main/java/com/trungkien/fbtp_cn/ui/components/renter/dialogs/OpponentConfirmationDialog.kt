package com.trungkien.fbtp_cn.ui.components.renter.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Dialog xác nhận khi renter sau muốn đặt vào khung giờ WAITING_OPPONENT
 * Hiển thị thông tin đối thủ đã đặt trước đó
 */
@Composable
fun OpponentConfirmationDialog(
    isVisible: Boolean,
    opponentName: String,
    timeSlot: String,
    date: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon/Emoji
                Text(
                    text = "🤝",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Tiêu đề chính
                Text(
                    text = "Bạn sẽ là đối thủ của $opponentName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Thông tin chi tiết
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Khung giờ: $timeSlot",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Ngày: $date",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Mô tả
                Text(
                    text = "Xác nhận để ghép cặp và hoàn tất đặt lịch cho khung giờ này.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Nút hành động
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Nút xác nhận
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Xác nhận đặt lịch",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Nút hủy
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hủy",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Phiên bản AlertDialog đơn giản hơn (tương thích với code hiện tại)
 */
@Composable
fun OpponentConfirmationAlertDialog(
    isVisible: Boolean,
    opponentName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (!isVisible) return
    
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = onConfirm) { 
                Text("Xác nhận") 
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { 
                Text("Hủy") 
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🤝", 
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Bạn sẽ là đối thủ của $opponentName",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Xác nhận để ghép cặp và hoàn tất đặt lịch cho khung giờ này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun OpponentConfirmationDialogPreview() {
    MaterialTheme {
        OpponentConfirmationDialog(
            isVisible = true,
            opponentName = "Nguyễn Văn A",
            timeSlot = "20:00 - 22:30",
            date = "28/09/2025",
            onConfirm = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OpponentConfirmationAlertDialogPreview() {
    MaterialTheme {
        OpponentConfirmationAlertDialog(
            isVisible = true,
            opponentName = "Nguyễn Văn A",
            onConfirm = {},
            onCancel = {}
        )
    }
}
