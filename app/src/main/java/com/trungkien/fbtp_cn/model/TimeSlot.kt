package com.trungkien.fbtp_cn.model

import java.time.LocalTime

data class TimeSlot(
    val id: String,
    val fieldId: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAvailable: Boolean = true,
    val price: Int,
    val isBooked: Boolean = false,
    val bookingId: String? = null
)

data class TimeSlotGroup(
    val date: String, // "2024-01-15"
    val dayOfWeek: String, // "Thứ 2", "Thứ 3", etc.
    val timeSlots: List<TimeSlot>
)
