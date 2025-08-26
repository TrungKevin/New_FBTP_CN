package com.trungkien.fbtp_cn.model

data class UserDevice(
    val deviceId: String,
    val userId: String,
    val fcmToken: String,
    val platform: String, // "ANDROID" | "IOS"
    val lastSeenAt: Long,
    
    // Thông tin bổ sung
    val deviceModel: String = "",
    val appVersion: String = "",
    val isActive: Boolean = true
)
