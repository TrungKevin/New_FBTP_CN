package com.trungkien.fbtp_cn.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trungkien.fbtp_cn.MainActivity
import com.trungkien.fbtp_cn.R
import com.trungkien.fbtp_cn.model.Notification
import com.trungkien.fbtp_cn.model.NotificationData
import com.trungkien.fbtp_cn.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth

class FBTPNotificationService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "fbtp_notifications"
        private const val CHANNEL_NAME = "FBTP Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for field bookings and matches"
        
        // Notification types
        const val TYPE_BOOKING_CREATED = "BOOKING_CREATED"
        const val TYPE_OPPONENT_JOINED = "OPPONENT_JOINED"
        const val TYPE_MATCH_RESULT = "MATCH_RESULT"
        const val TYPE_FIELD_UPDATED = "FIELD_UPDATED"
        const val TYPE_OPPONENT_SEARCH = "OPPONENT_SEARCH"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val notificationRepository by lazy { NotificationRepository() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        println("🔔 DEBUG: FBTPNotificationService.onMessageReceived - Received message")
        println("🔔 DEBUG: - From: ${remoteMessage.from}")
        println("🔔 DEBUG: - Data: ${remoteMessage.data}")
        println("🔔 DEBUG: - Notification: ${remoteMessage.notification}")

        // Xử lý data payload
        val data = remoteMessage.data
        val type = data["type"] ?: "UNKNOWN"
        val title = data["title"] ?: (remoteMessage.notification?.title ?: "Thông báo")
        val body = data["body"] ?: (remoteMessage.notification?.body ?: "Bạn có thông báo mới")
        val bookingId = data["bookingId"]
        val fieldId = data["fieldId"]
        val matchId = data["matchId"]
        val toUserId = data["toUserId"] ?: data["userId"] ?: "" // fallback

        // Lưu notification vào Firestore nếu có toUserId
        if (toUserId.isNotBlank()) {
            val notification = Notification(
                notificationId = UUID.randomUUID().toString(),
                toUserId = toUserId,
                type = type,
                title = title,
                body = body,
                data = NotificationData(
                    bookingId = bookingId,
                    fieldId = fieldId,
                    matchId = matchId,
                    userId = toUserId,
                    customData = data.filterKeys { key ->
                        key !in setOf("type", "title", "body", "bookingId", "fieldId", "matchId", "toUserId", "userId")
                    }
                )
            )
            serviceScope.launch {
                val result = notificationRepository.createNotification(notification)
                println(
                    if (result.isSuccess) "✅ DEBUG: Saved incoming notification: ${notification.notificationId}"
                    else "❌ ERROR: Failed to save incoming notification: ${result.exceptionOrNull()?.message}"
                )
            }
        } else {
            println("⚠️ WARN: onMessageReceived - Missing toUserId, skip persisting notification")
        }

        // Tạo notification hệ thống để hiển thị ngay
        showNotification(
            type = type,
            title = title,
            body = body,
            bookingId = bookingId,
            fieldId = fieldId,
            matchId = matchId
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        println("🔔 DEBUG: FBTPNotificationService.onNewToken - New FCM token: ${token.take(20)}...")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            serviceScope.launch {
                val result = notificationRepository.saveUserDevice(
                    userId = uid,
                    fcmToken = token,
                    deviceModel = android.os.Build.MODEL ?: "",
                    appVersion = try { packageManager.getPackageInfo(packageName, 0).versionName ?: "" } catch (e: Exception) { "" }
                )
                println(
                    if (result.isSuccess) "✅ DEBUG: Saved FCM token for user $uid"
                    else "❌ ERROR: Failed to save FCM token: ${result.exceptionOrNull()?.message}"
                )
            }
        } else {
            println("⚠️ WARN: onNewToken - No authenticated user, skip saving token")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            println("✅ DEBUG: FBTPNotificationService.createNotificationChannel - Created notification channel")
        }
    }

    private fun showNotification(
        type: String,
        title: String,
        body: String,
        bookingId: String?,
        fieldId: String?,
        matchId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Thêm data để xử lý khi user tap vào notification
            putExtra("notification_type", type)
            putExtra("booking_id", bookingId)
            putExtra("field_id", fieldId)
            putExtra("match_id", matchId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        
        println("✅ DEBUG: FBTPNotificationService.showNotification - Showed notification: $title")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
