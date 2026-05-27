package com.joy.featuregoods.model

data class GoodsDetailReviewTag(
    val label: String,
    val count: Int,
)

data class GoodsDetailReviewPreview(
    val userName: String,
    val avatarUrl: String,
    val starCount: Int,
    val content: String,
    val imageUrls: List<String>,
)

data class GoodsDetailReviewState(
    val totalCount: Int,
    val positiveRate: String,
    val tags: List<GoodsDetailReviewTag>,
    val preview: GoodsDetailReviewPreview,
)