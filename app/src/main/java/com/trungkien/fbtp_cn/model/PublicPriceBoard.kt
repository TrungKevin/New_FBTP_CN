package com.trungkien.fbtp_cn.model

data class PublicPriceBoard(
    val boardId: String,
    val fieldId: String,
    val previewRules: List<PricePreviewRule>,
    val previewServices: List<PricePreviewService>,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PricePreviewRule(
    val slots: Int,
    val minutes: Int,
    val price: Long
)

data class PricePreviewService(
    val name: String,
    val price: Long,
    val billingType: String
)
