package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Slot(
    val slotId: String,
    val fieldId: String,
    val date: String, // Ngày (yyyy-MM-dd)
    val startAt: String, // Giờ bắt đầu (HH:mm)
    val endAt: String, // Giờ kết thúc (HH:mm)
    val isAvailable: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    
    val price: Long? = null, // Giá của khe giờ này
    val isBooked: Boolean = false, // Đã được đặt chưa
    val bookingId: String? = null // ID booking nếu đã đặt
)
