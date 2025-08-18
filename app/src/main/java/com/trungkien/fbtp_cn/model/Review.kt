package com.trungkien.fbtp_cn.model

import java.time.LocalDateTime

data class Review(
    val id: String,
    val fieldId: String,
    val fieldName: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val rating: Float, // 1.0 - 5.0
    val comment: String,
    val createdAt: LocalDateTime,
    val images: List<String> = emptyList(),
    val likes: Int = 0,
    val replies: List<Reply> = emptyList()
)

data class Reply(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val comment: String,
    val createdAt: LocalDateTime,
    val isOwner: Boolean = false
)
