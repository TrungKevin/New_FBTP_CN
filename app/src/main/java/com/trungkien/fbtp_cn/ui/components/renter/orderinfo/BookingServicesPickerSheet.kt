package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun BookingServicesPickerSheet(
    services: List<RenterServiceItem>,
    initial: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Int>) -> Unit
) {
    val quantities = remember { mutableStateMapOf<String, Int>().apply { putAll(initial) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn dịch vụ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                services.forEach { s ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = s.name)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val current = quantities[s.id] ?: 0
                                if (current > 0) quantities[s.id] = current - 1
                            }) { Icon(Icons.Default.Close, contentDescription = null) }
                            Text(text = (quantities[s.id] ?: 0).toString(), modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = {
                                val current = quantities[s.id] ?: 0
                                quantities[s.id] = current + 1
                            }) { Icon(Icons.Default.Add, contentDescription = null) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantities) }) { Text("Xác nhận") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Huỷ") }
        }
    )
}

@Preview
@Composable
private fun BookingServicesPickerSheetPreview() {
    FBTP_CNTheme {
        BookingServicesPickerSheet(
            services = listOf(
                RenterServiceItem("1", "Thuê vợt", 20000),
                RenterServiceItem("2", "Nước uống", 15000)
            ),
            initial = emptyMap(),
            onDismiss = {},
            onConfirm = {}
        )
    }
}


