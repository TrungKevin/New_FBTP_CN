package com.trungkien.fbtp_cn.ui.components.renter.fieldsearch

import com.trungkien.fbtp_cn.model.FieldImages

data class SearchResultField(
    val id: String,
    val name: String,
    val type: String,
    val price: String,
    val location: String,
    val rating: Float,
    val distance: String,
    val isAvailable: Boolean,
    val imageUrl: String?,
    val ownerName: String,
    val ownerAvatarUrl: String?,
    val ownerPhone: String,
    val fieldImages: FieldImages?,
    val address: String,
    val openHours: String,
    val amenities: List<String>,
    val totalReviews: Int,
    val contactPhone: String,
    val description: String
)
