package com.trungkien.fbtp_cn.service

import com.google.firebase.firestore.FirebaseFirestore
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Helper để gửi thông báo cho tất cả renter trong hệ thống
 */
class GlobalNotificationHelper(
    private val notificationRepository: NotificationRepository,
    private val notificationBuilder: NotificationBuilder = NotificationBuilder()
) {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Gửi thông báo cho tất cả renter khi có người chờ đối thủ
     */
    fun notifyAllRentersOpponentAvailable(
        waitingRenterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String,
        fieldId: String,
        excludeRenterId: String? = null // Loại trừ renter đã đặt sân
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("🔔 DEBUG: GlobalNotificationHelper - Sending opponent available notification to all renters")
                println("  - waitingRenterName: $waitingRenterName")
                println("  - fieldName: $fieldName")
                println("  - date: $date")
                println("  - time: $time")
                println("  - bookingId: $bookingId")
                println("  - fieldId: $fieldId")
                println("  - excludeRenterId: $excludeRenterId")

                // Lấy danh sách tất cả renter
                // Ưu tiên role = "RENTER" (đúng theo model), fallback role = "renter"
                val rentersUpper = firestore.collection("users")
                    .whereEqualTo("role", "RENTER")
                    .get()
                    .await()
                    .documents

                val rentersLower = if (rentersUpper.isEmpty()) {
                    firestore.collection("users")
                        .whereEqualTo("role", "renter")
                        .get()
                        .await()
                        .documents
                } else emptyList()

                var renterDocs = if (rentersUpper.isNotEmpty()) rentersUpper else rentersLower

                // Fallback cuối: nếu vẫn rỗng (thiếu field role trong DB), lấy toàn bộ users
                if (renterDocs.isEmpty()) {
                    println("⚠️ WARNING: No renters found by role. Falling back to all users…")
                    renterDocs = firestore.collection("users")
                        .get()
                        .await()
                        .documents
                }

                val renters = renterDocs.mapNotNull { doc ->
                    val userId = doc.id
                    val userName = doc.getString("name") ?: "Người chơi"
                    val role = (doc.getString("role") ?: "").uppercase()
                    // Chỉ gửi cho renter nếu có role, còn nếu fallback lấy toàn bộ thì vẫn gửi trừ người tạo
                    if (userId != excludeRenterId && userId.isNotBlank()) {
                        if (rentersUpper.isNotEmpty() || rentersLower.isNotEmpty()) {
                            if (role == "RENTER") Pair(userId, userName) else null
                        } else {
                            Pair(userId, userName)
                        }
                    } else null
                }

                println("🔔 DEBUG: Found ${renters.size} renters to notify")

                // Gửi notification cho từng renter
                renters.forEach { (renterId, renterName) ->
                    try {
                        val notification = notificationBuilder.buildOpponentAvailableNotification(
                            renterId = renterId,
                            waitingRenterName = waitingRenterName,
                            fieldName = fieldName,
                            date = date,
                            time = time,
                            bookingId = bookingId,
                            fieldId = fieldId
                        )
                        
                        // Lưu tuần tự để tránh giới hạn Firestore write burst
                        notificationRepository.createNotification(notification)
                        println("✅ DEBUG: Sent opponent available notification to renter: $renterName ($renterId)")
                    } catch (e: Exception) {
                        println("❌ ERROR: Failed to send notification to renter $renterId: ${e.message}")
                    }
                }

                println("🔔 DEBUG: GlobalNotificationHelper - Completed sending notifications to ${renters.size} renters")
            } catch (e: Exception) {
                println("❌ ERROR: GlobalNotificationHelper failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Gửi thông báo cho owner khi có renter đặt sân chờ đối thủ
     */
    fun notifyOwnerWaitingOpponent(
        ownerId: String,
        renterName: String,
        fieldName: String,
        date: String,
        time: String,
        bookingId: String,
        fieldId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("🔔 DEBUG: GlobalNotificationHelper - Sending waiting opponent notification to owner")
                println("  - ownerId: $ownerId")
                println("  - renterName: $renterName")
                println("  - fieldName: $fieldName")
                println("  - date: $date")
                println("  - time: $time")

                val notification = notificationBuilder.buildWaitingOpponentBookingNotification(
                    ownerId = ownerId,
                    renterName = renterName,
                    fieldName = fieldName,
                    date = date,
                    time = time,
                    bookingId = bookingId,
                    fieldId = fieldId
                )
                
                notificationRepository.createNotification(notification)
                println("✅ DEBUG: Sent waiting opponent notification to owner: $ownerId")
            } catch (e: Exception) {
                println("❌ ERROR: Failed to send waiting opponent notification to owner: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
