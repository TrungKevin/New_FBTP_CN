package com.trungkien.fbtp_cn.model

data class PricingRule(
    val ruleId: String,
    val fieldId: String,
    val dayType: String, // "WEEKDAY" | "WEEKEND" | "HOLIDAY"
    val slots: Int,
    val minutes: Int,
    val price: Long,
    val calcMode: String, // "CEIL_TO_RULE" | "LINEAR"
    val effectiveFrom: Long? = null, // Thời điểm có hiệu lực từ
    val effectiveTo: Long? = null, // Thời điểm hết hiệu lực
    
    // Thông tin bổ sung
    val description: String = "",
    val isActive: Boolean = true
)
