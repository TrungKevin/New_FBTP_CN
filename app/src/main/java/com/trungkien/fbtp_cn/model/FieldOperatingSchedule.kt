package com.trungkien.fbtp_cn.model

data class FieldOperatingSchedule(
    val scheduleId: String,
    val fieldId: String,
    val dayOfWeek: Int, // 1 | 2 | 3 | 4 | 5 | 6 | 7 (1=Chủ nhật, 2=Thứ 2,...)
    val isOpen: Boolean,
    val openTime: String, // "06:00"
    val closeTime: String, // "23:00"
    val isSpecialDay: Boolean, // Ngày đặc biệt (lễ, tết)
    val specialNote: String? = null, // Ghi chú ngày đặc biệt
    val createdAt: Long = System.currentTimeMillis()
)
