package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Booking(
    val bookingId: String,
    val renterId: String,
    val ownerId: String,
    val fieldId: String,
    val date: String, // "2024-01-15"
    val startAt: String, // "18:00"
    val endAt: String, // "19:00"
    val slotsCount: Int,
    val minutes: Int,
    val matchId: String? = null,
    val matchSide: String? = null, // "A" | "B"
    val opponentMode: String? = null, // "WAITING_OPPONENT" | "LOCKED_FULL"
    val basePrice: Long,
    val serviceLines: List<ServiceLine> = emptyList(),
    val servicePrice: Long,
    val totalPrice: Long,
    val status: String, // "PENDING" | "PAID" | "CANCELLED" | "DONE"
    val notes: String? = null,
    val paymentStatus: String? = "PENDING", // "PENDING" | "PAID" | "REFUNDED"
    val paymentMethod: String? = null, // "CASH" | "BANK_TRANSFER" | "MOMO" | "VNPAY"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Keep
data class ServiceLine(
    val serviceId: String,
    val name: String,
    val billingType: String,
    val price: Long,
    val quantity: Int,
    val lineTotal: Long
)


