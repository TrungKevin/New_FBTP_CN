package com.trungkien.fbtp_cn.model

data class FieldService(
    val fieldServiceId: String,
    val fieldId: String,
    val serviceId: String? = null, // ID dịch vụ master (liên kết Services, optional)
    val name: String,
    val price: Long,
    val billingType: String, // "FLAT_PER_BOOKING" | "PER_HOUR" | "PER_SLOT" | "PER_MINUTE" | "PER_UNIT"
    val allowQuantity: Boolean,
    val description: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val stockQuantity: Int? = null // Số lượng tồn kho (nếu có)
)
