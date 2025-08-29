package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep // để Firebase không xoá khi build và đảm bảo tên trường không bị thay đổi
data class User(
    val userId: String,
    val role: String = "RENTER", // "OWNER" | "RENTER"
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Thông tin bổ sung (optional)
    val address: String = "",
    val dateOfBirth: String = "",
    val gender: String = "", // "MALE" | "FEMALE" | "OTHER"
    val isVerified: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
