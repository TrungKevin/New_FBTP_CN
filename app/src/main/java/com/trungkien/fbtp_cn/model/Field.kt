package com.trungkien.fbtp_cn.model

data class Field(
    val id: String,
    val name: String,
    val type: String,
    val price: Int,
    val imageUrl: String,
    val status: String,
    val isAvailable: Boolean,
    val address: String = "",
    val operatingHours: String = "",
    val contactPhone: String = "",
    val distance: String = ""
) 