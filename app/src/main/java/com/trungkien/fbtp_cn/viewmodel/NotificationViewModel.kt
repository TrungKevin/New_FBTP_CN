package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.model.NotificationData
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class NotificationEvent {
    data class LoadNotifications(val userId: String) : NotificationEvent()
    data class MarkAsRead(val notificationId: String) : NotificationEvent()
    object MarkAllAsRead : NotificationEvent()
    data class CreateNotification(
        val toUserId: String,
        val type: String,
        val title: String,
        val body: String,
        val data: NotificationData,
        val priority: String = "NORMAL"
    ) : NotificationEvent()
    data class SaveFcmToken(
        val userId: String,
        val fcmToken: String,
        val deviceModel: String = "",
        val appVersion: String = ""
    ) : NotificationEvent()
    object ClearError : NotificationEvent()
}

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun handle(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.LoadNotifications -> {
                loadNotifications(event.userId)
            }
            is NotificationEvent.MarkAsRead -> {
                markAsRead(event.notificationId)
            }
            is NotificationEvent.MarkAllAsRead -> {
                markAllAsRead()
            }
            is NotificationEvent.CreateNotification -> {
                createNotification(
                    event.toUserId,
                    event.type,
                    event.title,
                    event.body,
                    event.data,
                    event.priority
                )
            }
            is NotificationEvent.SaveFcmToken -> {
                saveFcmToken(event.userId, event.fcmToken, event.deviceModel, event.appVersion)
            }
            is NotificationEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadNotifications(userId: String) {
        currentUserId = userId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Lắng nghe danh sách thông báo
                notificationRepository.listenNotificationsByUser(userId)
                    .catch { error ->
                        println("❌ ERROR: NotificationViewModel.loadNotifications - ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                    .collect { notifications ->
                        _uiState.value = _uiState.value.copy(
                            notifications = notifications,
                            isLoading = false
                        )
                        println("✅ DEBUG: NotificationViewModel.loadNotifications - Loaded ${notifications.size} notifications")
                    }
            } catch (e: Exception) {
                println("❌ ERROR: NotificationViewModel.loadNotifications - ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }

        // Lắng nghe số lượng thông báo chưa đọc
        viewModelScope.launch {
            notificationRepository.listenUnreadNotificationCount(userId)
                .catch { error ->
                    println("❌ ERROR: NotificationViewModel.listenUnreadCount - ${error.message}")
                }
                .collect { count ->
                    _uiState.value = _uiState.value.copy(unreadCount = count)
                    println("🔔 DEBUG: NotificationViewModel.listenUnreadCount - Unread count: $count")
                }
        }
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess {
                    println("✅ DEBUG: NotificationViewModel.markAsRead - Success")
                }
                .onFailure { error ->
                    println("❌ ERROR: NotificationViewModel.markAsRead - ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    private fun markAllAsRead() {
        val userId = currentUserId ?: return
        
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
                .onSuccess {
                    println("✅ DEBUG: NotificationViewModel.markAllAsRead - Success")
                }
                .onFailure { error ->
                    println("❌ ERROR: NotificationViewModel.markAllAsRead - ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    private fun createNotification(
        toUserId: String,
        type: String,
        title: String,
        body: String,
        data: NotificationData,
        priority: String
    ) {
        viewModelScope.launch {
            notificationRepository.createNotification(
                toUserId = toUserId,
                type = type,
                title = title,
                body = body,
                data = data,
                priority = priority
            )
                .onSuccess { notificationId ->
                    println("✅ DEBUG: NotificationViewModel.createNotification - Created notification $notificationId")
                }
                .onFailure { error ->
                    println("❌ ERROR: NotificationViewModel.createNotification - ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    private fun saveFcmToken(
        userId: String,
        fcmToken: String,
        deviceModel: String,
        appVersion: String
    ) {
        viewModelScope.launch {
            notificationRepository.saveUserDevice(userId, fcmToken, deviceModel, appVersion)
                .onSuccess {
                    println("✅ DEBUG: NotificationViewModel.saveFcmToken - Success")
                }
                .onFailure { error ->
                    println("❌ ERROR: NotificationViewModel.saveFcmToken - ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    /**
     * Helper functions để tạo thông báo cho các sự kiện cụ thể
     */
    fun notifyBookingCreated(ownerId: String, renterName: String, fieldName: String, date: String, time: String) {
        val notificationData = NotificationData(
            fieldId = null, // Sẽ được set khi có fieldId
            bookingId = null // Sẽ được set khi có bookingId
        )
        
        handle(NotificationEvent.CreateNotification(
            toUserId = ownerId,
            type = "BOOKING_CREATED",
            title = "Có đặt sân mới",
            body = "$renterName đã đặt sân $fieldName vào $date lúc $time",
            data = notificationData,
            priority = "HIGH"
        ))
    }

    fun notifyOpponentJoined(renterAId: String, opponentName: String, fieldName: String, date: String, time: String) {
        val notificationData = NotificationData(
            fieldId = null,
            matchId = null
        )
        
        handle(NotificationEvent.CreateNotification(
            toUserId = renterAId,
            type = "OPPONENT_JOINED",
            title = "Có đối thủ tham gia",
            body = "$opponentName đã tham gia trận đấu tại $fieldName vào $date lúc $time",
            data = notificationData,
            priority = "HIGH"
        ))
    }

    fun notifyMatchResult(renterId: String, fieldName: String, result: String) {
        val notificationData = NotificationData(
            fieldId = null,
            matchId = null
        )
        
        handle(NotificationEvent.CreateNotification(
            toUserId = renterId,
            type = "MATCH_RESULT",
            title = "Kết quả trận đấu",
            body = "Kết quả trận đấu tại $fieldName: $result",
            data = notificationData,
            priority = "NORMAL"
        ))
    }

    fun notifyFieldUpdated(renterId: String, fieldName: String) {
        val notificationData = NotificationData(
            fieldId = null
        )
        
        handle(NotificationEvent.CreateNotification(
            toUserId = renterId,
            type = "FIELD_UPDATED",
            title = "Sân đã được cập nhật",
            body = "Sân $fieldName đã được cập nhật thông tin",
            data = notificationData,
            priority = "NORMAL"
        ))
    }

    fun notifyOpponentSearch(renterId: String, renterName: String, fieldName: String, date: String, time: String) {
        val notificationData = NotificationData(
            fieldId = null,
            bookingId = null
        )
        
        handle(NotificationEvent.CreateNotification(
            toUserId = renterId,
            type = "OPPONENT_SEARCH",
            title = "Tìm đối thủ",
            body = "$renterName đang tìm đối thủ tại $fieldName vào $date lúc $time",
            data = notificationData,
            priority = "NORMAL"
        ))
    }
}
