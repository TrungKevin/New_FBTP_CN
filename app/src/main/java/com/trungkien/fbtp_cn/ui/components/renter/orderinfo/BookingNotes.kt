package com.trungkien.fbtp_cn.ui.components.renter.orderinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme

@Composable
fun BookingNotes(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ FIX: FocusManager để ẩn bàn phím
    val focusManager: FocusManager = LocalFocusManager.current
    
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // ✅ FIX: Click ra ngoài để ẩn bàn phím
                    focusManager.clearFocus()
                }
        ) {
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
                maxLines = 8,
                // ✅ FIX: Thêm nút Done cho bàn phím
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        // ✅ FIX: Ẩn bàn phím khi nhấn Done
                        focusManager.clearFocus()
                    }
                )
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


