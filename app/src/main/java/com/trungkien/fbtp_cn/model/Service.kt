package com.trungkien.fbtp_cn.model

data class Service(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Int,
    val unit: String, // "Cái", "Chai", "Giờ", etc.
    val category: String, // "Equipment", "Beverage", "Training", etc.
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val fieldId: String = ""
)

data class ServiceOrder(
    val id: String,
    val serviceId: String,
    val serviceName: String,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int = quantity * price
)
