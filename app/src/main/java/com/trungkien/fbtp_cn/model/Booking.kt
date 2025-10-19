package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Booking(
    val bookingId: String = "",
    val renterId: String = "",
    val ownerId: String = "",
    val fieldId: String = "",
    val date: String = "", // "2024-01-15"
    val startAt: String = "", // "18:00"
    val endAt: String = "", // "19:00"
    val slotsCount: Int = 0,
    val minutes: Int = 0,
    val matchId: String? = null,
    val matchSide: String? = null, // "A" | "B"
    val opponentMode: String? = null, // "WAITING_OPPONENT" | "LOCKED_FULL"
    val basePrice: Long = 0,
    val serviceLines: List<ServiceLine> = emptyList(),
    val servicePrice: Long = 0,
    val totalPrice: Long = 0,
    val status: String = "PENDING", // "PENDING" | "CONFIRMED" | "CANCELLED" | "DONE"
    val notes: String? = null,
    val paymentStatus: String? = "PENDING", // "PENDING" | "PAID" | "REFUNDED"
    val paymentMethod: String? = null, // "CASH" | "BANK_TRANSFER" | "MOMO" | "VNPAY"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // ✅ NEW: Thêm các trường cho logic đối thủ
    val hasOpponent: Boolean = false, // runtime state: currently has opponent
    val opponentId: String? = null, // ID của đối thủ
    val opponentName: String? = null, // Tên đối thủ
    val opponentAvatar: String? = null, // Avatar đối thủ
    val bookingType: String = "SOLO", // "SOLO" | "DUO" - SOLO: tìm đối thủ, DUO: đã có đối thủ
    val consecutiveSlots: List<String> = emptyList(), // Danh sách các khung giờ liên tiếp
    // ✅ CRITICAL FIX: immutable origin flag, set when creating the booking
    val createdWithOpponent: Boolean = false // true only if user chose "Đã có đối thủ" at creation time
)

@Keep
data class ServiceLine(
    val serviceId: String = "",
    val name: String = "",
    val billingType: String = "UNIT",
    val price: Long = 0,
    val quantity: Int = 0,
    val lineTotal: Long = 0
)


