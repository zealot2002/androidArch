package com.joy.featuregoods.model

data class GoodsDetailShopRating(
    val label: String,
    val score: String,
    val levelLabel: String,
)

data class GoodsDetailShopState(
    val logoUrl: String,
    val shopName: String,
    val selfOperatedTag: String?,
    val followerCount: String,
    val itemCount: String,
    val enterShopText: String,
    val ratings: List<GoodsDetailShopRating>,
)