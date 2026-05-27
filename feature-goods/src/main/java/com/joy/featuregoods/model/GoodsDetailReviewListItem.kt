package com.joy.featuregoods.model

data class GoodsDetailReviewListItem(
    val userName: String,
    val avatarUrl: String,
    val starCount: Int,
    val content: String,
    val imageUrls: List<String>,
    val specLabel: String = "",
    val timeLabel: String = "",
)
