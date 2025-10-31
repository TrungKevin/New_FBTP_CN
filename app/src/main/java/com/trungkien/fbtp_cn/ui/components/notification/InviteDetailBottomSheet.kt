package com.trungkien.fbtp_cn.ui.components.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteDetailBottomSheet(
    fieldName: String,
    date: String,
    timeRange: String,
    fromName: String,
    fromRenterId: String?,
    fromPhone: String,
    note: String,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        val displayNameState = remember { mutableStateOf(fromName) }
        LaunchedEffect(fromName, fromRenterId) {
            if (displayNameState.value.isBlank() && !fromRenterId.isNullOrBlank()) {
                try {
                    com.trungkien.fbtp_cn.repository.UserRepository().getUserById(
                        fromRenterId,
                        onSuccess = { u -> displayNameState.value = u.name },
                        onError = { }
                    )
                } catch (_: Exception) { }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Chi tiết lời mời thách đấu", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Sân: $fieldName", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Ngày: $date", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Giờ: $timeRange", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Người gửi: ${displayNameState.value.ifBlank { "Đối thủ" }}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Số điện thoại: $fromPhone", style = MaterialTheme.typography.bodyMedium)
            if (note.isNotBlank()) {
                Text(text = "Ghi chú: $note", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (onAccept != null || onReject != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onReject != null) {
                        OutlinedButton(onClick = onReject) { Text("Từ chối") }
                    }
                    if (onAccept != null) {
                        Button(onClick = onAccept) { Text("Đồng ý") }
                    }
                }
            }
        }
    }
}


