package com.trungkien.fbtp_cn.model

import java.time.LocalDateTime

data class Booking(
    val id: String,
    val fieldId: String,
    val fieldName: String,
    val timeRange: String,
    val status: String,
    // Thêm thông tin cho Renter
    val userId: String = "",
    val userName: String = "",
    val date: String = "", // "2024-01-15"
    val startTime: String = "", // "18:00"
    val endTime: String = "", // "19:00"
    val totalPrice: Int = 0,
    val fieldPrice: Int = 0,
    val servicesPrice: Int = 0,
    val services: List<ServiceOrder> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val fieldImage: String = "",
    val fieldAddress: String = "",
    val fieldType: String = ""
)


