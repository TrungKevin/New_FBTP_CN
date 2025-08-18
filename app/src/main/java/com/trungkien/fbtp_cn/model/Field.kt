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
    val distance: String = "",
    // Thêm thông tin cho Renter
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val description: String = "",
    val facilities: List<String> = emptyList(), // ["Parking", "Shower", "Equipment"]
    val images: List<String> = emptyList(),
    val ownerId: String = "",
    val ownerName: String = "",
    val priceRange: PriceRange = PriceRange(),
    val isFavorite: Boolean = false
)

data class PriceRange(
    val weekday: PriceDetail = PriceDetail(),
    val weekend: PriceDetail = PriceDetail()
)

data class PriceDetail(
    val morning: Int = 0, // 5h-9h
    val afternoon: Int = 0, // 9h-17h
    val evening: Int = 0 // 17h-23h
) 