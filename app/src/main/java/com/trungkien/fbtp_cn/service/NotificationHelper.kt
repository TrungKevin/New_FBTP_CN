package com.trungkien.fbtp_cn.service

import com.trungkien.fbtp_cn.model.*
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationHelper(
    private val notificationRepository: NotificationRepository
) {
    private val notificationBuilder = NotificationBuilder()
    
    /**
     * Gửi thông báo khi có đặt sân mới cho owner
     */
    fun notifyBookingCreated(
        ownerId: String,
        renterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildBookingCreatedNotification(
                ownerId = ownerId,
                renterName = renterName,
                fieldName = fieldName,
                date = date,
                time = time,
                bookingId = bookingId ?: "",
                fieldId = fieldId ?: ""
            )
            notificationRepository.createNotification(notification)
        }
    }

    /**
     * Gửi thông báo khi có đối thủ tham gia cho Renter A
     */
    fun notifyOpponentJoined(
        renterAId: String,
        opponentName: String,
        fieldName: String,
        date: String,
        time: String,
        matchId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildOpponentJoinedNotification(
                renterAId = renterAId,
                opponentName = opponentName,
                fieldName = fieldName,
                date = date,
                time = time,
                matchId = matchId,
                fieldId = fieldId
            )
            notificationRepository.createNotification(notification)
        }
    }

    /**
     * Gửi thông báo kết quả trận đấu cho cả hai renter
     */
    fun notifyMatchResult(
        renterAId: String,
        renterBId: String,
        fieldName: String,
        result: String,
        matchId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Gửi cho Renter A
            val notificationA = notificationBuilder.buildMatchResultNotification(
                renterAId = renterAId,
                renterBId = renterBId,
                fieldName = fieldName,
                result = result,
                matchId = matchId ?: "",
                fieldId = fieldId
            )
            notificationRepository.createNotification(notificationA)
            
            // Gửi cho Renter B (duplicate với userId khác)
            val notificationB = notificationA.copy(
                notificationId = java.util.UUID.randomUUID().toString(),
                toUserId = renterBId
            )
            notificationRepository.createNotification(notificationB)
        }
    }

    /**
     * Gửi thông báo khi sân được cập nhật cho các renter đã đặt sân
     */
    fun notifyFieldUpdated(
        renterIds: List<String>,
        fieldName: String,
        fieldId: String? = null,
        updateType: String = "thông tin"
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            renterIds.forEach { renterId ->
                val notification = notificationBuilder.buildFieldUpdatedNotification(
                    ownerId = renterId, // Trong trường hợp này renterIds là danh sách người nhận
                    fieldName = fieldName,
                    fieldId = fieldId ?: "",
                    updateType = updateType
                )
                notificationRepository.createNotification(notification)
            }
        }
    }

    /**
     * Gửi thông báo tìm đối thủ cho các renter khác
     */
    fun notifyOpponentSearch(
        renterIds: List<String>,
        renterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notificationData = NotificationData(
                bookingId = bookingId,
                fieldId = fieldId
            )
            
            renterIds.forEach { renterId ->
                notificationRepository.createNotification(
                    toUserId = renterId,
                    type = "OPPONENT_SEARCH",
                    title = "Tìm đối thủ",
                    body = "$renterName đang tìm đối thủ tại $fieldName vào $date lúc $time",
                    data = notificationData,
                    priority = "NORMAL"
                )
            }
        }
    }

    /**
     * Gửi thông báo khi đặt sân thành công cho renter
     */
    fun notifyBookingSuccess(
        renterId: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildBookingSuccessNotification(
                renterId = renterId,
                fieldName = fieldName,
                date = date,
                time = time,
                bookingId = bookingId ?: "",
                fieldId = fieldId ?: ""
            )
            notificationRepository.createNotification(notification)
        }
    }

    /**
     * Gửi thông báo khi đặt sân bị hủy
     */
    fun notifyBookingCancelled(
        renterId: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String? = null,
        fieldId: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val notification = notificationBuilder.buildBookingCancelledNotification(
                ownerId = renterId, // Trong trường hợp này renterId là người nhận
                renterName = "Bạn", // Self-cancellation
                fieldName = fieldName,
                date = date,
                time = time,
                bookingId = bookingId ?: "",
                fieldId = fieldId ?: ""
            )
            notificationRepository.createNotification(notification)
        }
    }
}
