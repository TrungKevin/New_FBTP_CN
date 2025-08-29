package com.trungkien.fbtp_cn.model

import androidx.annotation.Keep

@Keep
data class Payment(
    val paymentId: String,
    val bookingId: String,
    val userId: String,
    val amount: Long,
    val currency: String = "VND",
    val paymentMethod: String, // "CASH" | "BANK_TRANSFER" | "MOMO" | "VNPAY" | "ZALOPAY"
    val status: String, // "PENDING" | "SUCCESS" | "FAILED" | "REFUNDED"
    val transactionId: String? = null,
    val paymentDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Thông tin bổ sung
    val gatewayResponse: String? = null, // Phản hồi từ cổng thanh toán
    val refundReason: String? = null, // Lý do hoàn tiền
    val adminNote: String? = null // Ghi chú admin
)
