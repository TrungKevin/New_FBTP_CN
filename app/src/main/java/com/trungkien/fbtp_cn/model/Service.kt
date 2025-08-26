package com.trungkien.fbtp_cn.model

data class Service(
    val serviceId: String,
    val name: String,
    val defaultBillingType: String, // "FLAT_PER_BOOKING" | "PER_HOUR" | "PER_SLOT" | "PER_MINUTE" | "PER_UNIT"
    val defaultAllowQuantity: Boolean,
    val description: String = "",
    val category: String = "", // ["EQUIPMENT", "BEVERAGE", "TRAINING"]
    val isActive: Boolean = true
)

data class ServiceOrder(
    val id: String,
    val serviceId: String,
    val serviceName: String,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int = quantity * price
)
