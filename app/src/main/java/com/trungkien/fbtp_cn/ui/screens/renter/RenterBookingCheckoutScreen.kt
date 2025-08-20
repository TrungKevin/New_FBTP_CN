package com.trungkien.fbtp_cn.ui.screens.renter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungkien.fbtp_cn.ui.components.renter.orderinfo.*
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RenterBookingCheckoutScreen(
    fieldId: String,
    basePricePerHour: Int,
    onBackClick: () -> Unit,
    onConfirmBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }

    // Service quantities map (serviceId -> qty)
    var servicesQuantity by remember { mutableStateOf(mapOf<String, Int>()) }
    var showServicePicker by remember { mutableStateOf(false) }

    // Mock services
    val allServices = listOf(
        RenterServiceItem("1", "Thuê vợt", 20000),
        RenterServiceItem("2", "Nước uống", 15000),
        RenterServiceItem("3", "Khăn lạnh", 5000)
    )

    val servicesTotal = servicesQuantity.entries.sumOf { entry ->
        val price = allServices.firstOrNull { it.id == entry.key }?.price ?: 0
        price * entry.value
    }
    val hours = selectedSlots.size
    val fieldTotal = basePricePerHour * hours
    val grandTotal = fieldTotal + servicesTotal

    if (showServicePicker) {
        BookingServicesPickerSheet(
            services = allServices,
            initial = servicesQuantity,
            onDismiss = { showServicePicker = false },
            onConfirm = { map ->
                servicesQuantity = map
                showServicePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đặt sân") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tổng: ${String.format("%,d", grandTotal)}₫", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onConfirmBooking, enabled = hours > 0) { Text("Xác nhận đặt") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookingDatePicker(selectedDate = selectedDate, onDateChange = { selectedDate = it })
            BookingTimeSlotGrid(selectedDate = selectedDate, selected = selectedSlots, onToggle = { slot ->
                selectedSlots = selectedSlots.toMutableSet().apply { if (contains(slot)) remove(slot) else add(slot) }.toSet()
            })

            BookingServicesPicker(
                servicesTotal = servicesTotal,
                onAddServicesClick = { showServicePicker = true }
            )

            BookingNotes(notes = notes, onNotesChange = { notes = it })

            BookingSummaryCard(
                hours = hours,
                pricePerHour = basePricePerHour,
                servicesTotal = servicesTotal
            )

            // Extra spacer so the last card is not hidden behind bottom bar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun RenterBookingCheckoutScreenPreview() {
    FBTP_CNTheme {
        RenterBookingCheckoutScreen(
            fieldId = "field1",
            basePricePerHour = 150000,
            onBackClick = {},
            onConfirmBooking = {}
        )
    }
}


