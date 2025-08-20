package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun BookingNotes(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "Ghi chú", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp),
                placeholder = { Text("Ghi chú cho chủ sân...") },
                singleLine = false,
                minLines = 4,
                maxLines = 8
            )
        }
    }
}

@Preview
@Composable
private fun BookingNotesPreview() {
    FBTP_CNTheme {
        BookingNotes(notes = "", onNotesChange = {})
    }
}


