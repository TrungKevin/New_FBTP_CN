package com.trungkien.fbtp_cn.ui.screens.renter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.ui.components.notification.NotificationCard
import com.trungkien.fbtp_cn.ui.components.notification.NotificationHeader
import com.trungkien.fbtp_cn.ui.components.notification.NotificationScreenContent
import com.trungkien.fbtp_cn.ui.components.notification.EmptyNotificationState
import com.trungkien.fbtp_cn.ui.theme.FBTP_CNTheme
import com.trungkien.fbtp_cn.repository.NotificationRepository
import com.trungkien.fbtp_cn.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterNotificationScreen(
    onBackClick: () -> Unit,
    onNavigateToBooking: () -> Unit = {},
    onNavigateToField: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToFieldDetail: (fieldId: String, initialTab: String) -> Unit = { _, _ -> },
    userId: String = "",
    modifier: Modifier = Modifier
) {
    val notificationRepository = NotificationRepository()
    val notificationViewModel: NotificationViewModel = viewModel { 
        NotificationViewModel(notificationRepository) 
    }
    val uiState by notificationViewModel.uiState.collectAsState()
    val matchRepo = remember { com.trungkien.fbtp_cn.repository.MatchRequestRepository() }
    val acceptOrRejectScope = rememberCoroutineScope()
    val currentUserName = remember { mutableStateOf("Bạn") }
    val scope = rememberCoroutineScope()
    
    // Date filter state
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    
    // Load notifications when screen opens
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            notificationViewModel.handle(
                com.trungkien.fbtp_cn.viewmodel.NotificationEvent.LoadNotifications(userId)
            )
        }
    }
    
    Scaffold(
        topBar = {
            NotificationHeader(
                onBackClick = onBackClick,
                unreadCount = uiState.unreadCount,
                onMarkAllAsRead = {
                    scope.launch {
                        notificationViewModel.handle(
                            com.trungkien.fbtp_cn.viewmodel.NotificationEvent.MarkAllAsRead
                        )
                    }
                },
                onCalendarClick = { showDatePicker = true }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Có lỗi xảy ra",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (userId.isNotBlank()) {
                                    notificationViewModel.handle(
                                        com.trungkien.fbtp_cn.viewmodel.NotificationEvent.LoadNotifications(userId)
                                    )
                                }
                            }
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            
            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmptyNotificationState()
                }
            }
            
            else -> {
                // Lọc theo ngày
                val selectedLocalDate: LocalDate? = selectedDateMillis?.let { ms ->
                    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                
                val selectedDateString = selectedLocalDate?.let { date ->
                    "${date.dayOfMonth}/${date.monthValue}/${date.year}"
                }
                
                NotificationScreenContent(
                    notifications = uiState.notifications,
                    onItemClick = { notification ->
                        notificationViewModel.handle(
                            com.trungkien.fbtp_cn.viewmodel.NotificationEvent.MarkAsRead(notification.notificationId)
                        )

                        // Điều hướng theo loại
                        when (notification.type) {
                            "BOOKING_CREATED", "BOOKING_SUCCESS", "BOOKING_CANCELLED", "BOOKING_CONFIRMED", "BOOKING_CANCELLED_BY_OWNER", "OPPONENT_JOINED", "OPPONENT_MATCHED", "MATCH_RESULT" -> {
                                onNavigateToBooking()
                            }
                            "FIELD_UPDATED" -> {
                                onNavigateToField()
                            }
                            "OPPONENT_AVAILABLE" -> {
                                val fieldId = notification.data.fieldId
                                if (!fieldId.isNullOrBlank()) {
                                    onNavigateToFieldDetail(fieldId, "booking")
                                } else {
                                    onNavigateToBooking()
                                }
                            }
                            "REVIEW_ADDED", "REVIEW_REPLY" -> {
                                val fieldId = notification.data.fieldId
                                if (!fieldId.isNullOrBlank()) {
                                    onNavigateToFieldDetail(fieldId, "reviews")
                                } else {
                                    onNavigateToProfile()
                                }
                            }
                            else -> onNavigateToBooking()
                        }
                    },
                    selectedDate = selectedDateString,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onAcceptInvite = { inviteId, fieldId ->
                        if (inviteId.isBlank()) return@NotificationScreenContent
                        acceptOrRejectScope.launch {
                            matchRepo.acceptInvite(inviteId, currentUserName.value)
                                .onSuccess {
                                    // mark as read and navigate
                                    // Tìm lại notification theo inviteId để mark nếu cần, ở đây đã mark khi click card
                                    val destFieldId = fieldId ?: ""
                                    if (destFieldId.isNotBlank()) onNavigateToFieldDetail(destFieldId, "booking")
                                    else onNavigateToBooking()
                                }
                                .onFailure { _ -> }
                        }
                    },
                    onRejectInvite = { inviteId ->
                        if (inviteId.isBlank()) return@NotificationScreenContent
                        acceptOrRejectScope.launch {
                            matchRepo.rejectInvite(inviteId, currentUserName.value)
                                .onSuccess { }
                                .onFailure { _ -> }
                        }
                    }
                )
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("Lọc") }
            },
            dismissButton = {
                TextButton(onClick = {
                    // Bỏ lọc
                    selectedDateMillis = null
                    showDatePicker = false
                }) { Text("Bỏ lọc") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RenterNotificationScreenPreview() {
    FBTP_CNTheme {
        RenterNotificationScreen(
            onBackClick = {},
            userId = "user1"
        )
    }
}
