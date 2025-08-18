package com.trungkien.fbtp_cn.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatar: String = "",
    val address: String = "",
    val totalBookings: Int = 0,
    val totalReviews: Int = 0,
    val averageRating: Float = 0f
)
