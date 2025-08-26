package com.trungkien.fbtp_cn.model

data class Field(
    val fieldId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val address: String = "",
    val geo: GeoLocation = GeoLocation(),
    val sports: List<String> = emptyList(), // ["TENNIS", "BADMINTON"]
    
    // HÌNH ẢNH - BẮT BUỘC 4 ẢNH
    val images: FieldImages = FieldImages(),
    
    val slotMinutes: Int = 30, // Số phút mỗi khe giờ (mặc định 30 phút)
    val openHours: OpenHours = OpenHours(),
    val amenities: List<String> = emptyList(), // ["PARKING", "SHOWER", "EQUIPMENT"]
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    
    // Thông tin bổ sung
    val description: String = "",
    val contactPhone: String = "",
    val averageRating: Float = 0f,
    val totalReviews: Int = 0
)

data class GeoLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val geohash: String = ""
)

data class FieldImages(
    val mainImage: String = "", // Ảnh chính (hiển thị trên thẻ)
    val image1: String = "", // Ảnh chi tiết 1
    val image2: String = "", // Ảnh chi tiết 2
    val image3: String = "" // Ảnh chi tiết 3
)

data class OpenHours(
    val start: String = "06:00", // Giờ mở cửa
    val end: String = "23:00", // Giờ đóng cửa
    val isOpen24h: Boolean = false // Có mở 24/24 không
)

// Giữ lại các class cũ để tương thích ngược
data class PriceRange(
    val weekday: PriceDetail = PriceDetail(),
    val weekend: PriceDetail = PriceDetail()
)

data class PriceDetail(
    val morning: Int = 0, // 5h-9h
    val afternoon: Int = 0, // 9h-17h
    val evening: Int = 0 // 17h-23h
) 