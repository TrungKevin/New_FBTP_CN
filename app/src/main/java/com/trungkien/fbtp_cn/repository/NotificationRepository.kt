package com.trungkien.fbtp_cn.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.model.NotificationData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")
    private val userDevicesCollection = firestore.collection("user_devices")

    /**
     * Lắng nghe thông báo của user theo thời gian thực
     */
    fun listenNotificationsByUser(userId: String): Flow<List<Notification>> = callbackFlow {
        // Tránh composite index: chỉ whereEqualTo, không orderBy
        val listener = notificationsCollection
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ ERROR: NotificationRepository.listenNotificationsByUser - ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Notification::class.java)?.copy(
                                notificationId = doc.id
                            )
                        } catch (e: Exception) {
                            println("❌ ERROR: NotificationRepository.listenNotificationsByUser - Failed to parse notification: ${e.message}")
                            null
                        }
                    }
                    // Sắp xếp client-side theo createdAt desc và giới hạn 50
                    val sorted = notifications
                        .sortedByDescending { it.createdAt }
                        .take(50)
                    println("✅ DEBUG: NotificationRepository.listenNotificationsByUser - Loaded ${sorted.size} notifications for user $userId")
                    trySend(sorted)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Đếm số thông báo chưa đọc của user
     */
    fun listenUnreadNotificationCount(userId: String): Flow<Int> = callbackFlow {
        // Tránh composite index: lắng nghe tất cả rồi đếm client-side
        val listener = notificationsCollection
            .whereEqualTo("toUserId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ ERROR: NotificationRepository.listenUnreadNotificationCount - ${error.message}")
                    trySend(0)
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.count { doc ->
                    (doc.getBoolean("isRead") ?: false).not()
                } ?: 0
                println("🔔 DEBUG: NotificationRepository.listenUnreadNotificationCount - User $userId has $count unread notifications")
                trySend(count)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Lấy danh sách thông báo cho một user
     */
    suspend fun getNotificationsForUser(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            val notifications = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Notification::class.java)?.copy(
                        notificationId = doc.id
                    )
                } catch (e: Exception) {
                    println("❌ ERROR: NotificationRepository.getNotificationsForUser - Failed to parse notification: ${e.message}")
                    null
                }
            }
            val sorted = notifications.sortedByDescending { it.createdAt }
            println("✅ DEBUG: NotificationRepository.getNotificationsForUser - Fetched ${sorted.size} notifications for user $userId")
            Result.success(sorted)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.getNotificationsForUser - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Đánh dấu thông báo là đã đọc
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true, "readAt", System.currentTimeMillis())
                .await()
            
            println("✅ DEBUG: NotificationRepository.markAsRead - Marked notification $notificationId as read")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.markAsRead - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val notifications = notificationsCollection
                .whereEqualTo("toUserId", userId)
                .get()
                .await()

            notifications.documents.filter { (it.getBoolean("isRead") ?: false).not() }
                .forEach { doc ->
                batch.update(doc.reference, "isRead", true, "readAt", System.currentTimeMillis())
            }

            batch.commit().await()
            
            println("✅ DEBUG: NotificationRepository.markAllAsRead - Marked notifications as read for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.markAllAsRead - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tạo thông báo
     */
    suspend fun createNotification(
        toUserId: String,
        type: String,
        title: String,
        body: String,
        data: NotificationData,
        priority: String = "NORMAL"
    ): Result<String> {
        return try {
            val notificationId = UUID.randomUUID().toString()
            val notification = Notification(
                notificationId = notificationId,
                toUserId = toUserId,
                type = type,
                title = title,
                body = body,
                data = data,
                priority = priority,
                createdAt = System.currentTimeMillis()
            )

            notificationsCollection.document(notificationId)
                .set(notification)
                .await()

            println("✅ DEBUG: NotificationRepository.createNotification - Created notification $notificationId for user $toUserId")
            Result.success(notificationId)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.createNotification - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tạo notification từ Notification object
     */
    suspend fun createNotification(notification: Notification): Result<String> {
        return try {
            notificationsCollection.document(notification.notificationId)
                .set(notification)
                .await()

            println("✅ DEBUG: NotificationRepository.createNotification - Created notification ${notification.notificationId} for user ${notification.toUserId}")
            Result.success(notification.notificationId)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.createNotification - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Lưu FCM token của user device
     */
    suspend fun saveUserDevice(
        userId: String,
        fcmToken: String,
        deviceModel: String = "",
        appVersion: String = ""
    ): Result<Unit> {
        return try {
            val deviceId = UUID.randomUUID().toString()
            val userDevice = com.trungkien.fbtp_cn.model.UserDevice(
                deviceId = deviceId,
                userId = userId,
                fcmToken = fcmToken,
                platform = "ANDROID",
                lastSeenAt = System.currentTimeMillis(),
                deviceModel = deviceModel,
                appVersion = appVersion,
                isActive = true
            )

            userDevicesCollection.document(deviceId)
                .set(userDevice)
                .await()

            println("✅ DEBUG: NotificationRepository.saveUserDevice - Saved device $deviceId for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.saveUserDevice - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách FCM tokens của user để gửi push notification
     */
    suspend fun getUserFcmTokens(userId: String): Result<List<String>> {
        return try {
            val devices = userDevicesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val tokens = devices.documents.mapNotNull { doc ->
                try {
                    doc.toObject(com.trungkien.fbtp_cn.model.UserDevice::class.java)?.fcmToken
                } catch (e: Exception) {
                    null
                }
            }

            println("✅ DEBUG: NotificationRepository.getUserFcmTokens - Found ${tokens.size} FCM tokens for user $userId")
            Result.success(tokens)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.getUserFcmTokens - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Xóa thông báo cũ (giữ lại 100 thông báo gần nhất)
     */
    suspend fun cleanupOldNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("toUserId", userId)
                .get()
                .await()

            val docs = snapshot.documents
                .mapNotNull { it.toObject(Notification::class.java)?.copy(notificationId = it.id) }
                .sortedByDescending { it.createdAt }
            val toDelete = if (docs.size > 100) docs.drop(100) else emptyList()

            if (toDelete.isNotEmpty()) {
                val batch = firestore.batch()
                toDelete.forEach { n ->
                    batch.delete(notificationsCollection.document(n.notificationId))
                }
                batch.commit().await()
                println("✅ DEBUG: NotificationRepository.cleanupOldNotifications - Deleted ${toDelete.size} old notifications for user $userId")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ ERROR: NotificationRepository.cleanupOldNotifications - ${e.message}")
            Result.failure(e)
        }
    }
}
