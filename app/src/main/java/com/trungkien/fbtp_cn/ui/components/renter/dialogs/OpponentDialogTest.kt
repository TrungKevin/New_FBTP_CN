package com.trungkien.fbtp_cn.ui.components.renter.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Test component để kiểm tra OpponentConfirmationDialog
 */
@Composable
fun OpponentDialogTestScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Test Opponent Confirmation Dialog",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Button(
            onClick = { showDialog = true }
        ) {
            Text("Show Custom Dialog")
        }
        
        Button(
            onClick = { showAlertDialog = true }
        ) {
            Text("Show Alert Dialog")
        }
        
        // Custom Dialog Test
        OpponentConfirmationDialog(
            isVisible = showDialog,
            opponentName = "Nguyễn Văn A",
            timeSlot = "20:00 - 22:30",
            date = "28/09/2025",
            onConfirm = {
                showDialog = false
                // Simulate booking success
            },
            onCancel = {
                showDialog = false
            }
        )
        
        // Alert Dialog Test
        OpponentConfirmationAlertDialog(
            isVisible = showAlertDialog,
            opponentName = "Trần Thị B",
            onConfirm = {
                showAlertDialog = false
                // Simulate booking success
            },
            onCancel = {
                showAlertDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OpponentDialogTestScreenPreview() {
    MaterialTheme {
        OpponentDialogTestScreen()
    }
}
