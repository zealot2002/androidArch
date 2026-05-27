package com.joy.featuregoods.model

import java.io.Serializable

data class GoodsDetail(
    val productId: Int,
    val spuId: String,
    val title: String,
    val subtitle: String,
    val bannerImages: List<String>,
    val detailImages: List<String> = emptyList(),
    val tags: List<String>,
    val skus: List<GoodsSku>,
    val colors: List<GoodsColor>,
    val highlights: List<String>,
    val services: List<String>,
    val params: List<GoodsParam>,
    val shipFromCity: String = "",
    val shopLogoUrl: String = "",
    val shopName: String = "",
    val selfOperatedTag: String? = null,
    val shopFollowerCount: String = "",
    val shopItemCount: String = "",
    val reviewTotalCount: Int = 0,
    val reviewPositiveRate: String = "",
    val reviewPreviewUser: String = "",
    val reviewPreviewContent: String = "",
    val reviewPreviewAvatarUrl: String = "",
) : Serializable

data class GoodsSku(
    val skuId: Int,
    val name: String,
    val priceYuan: String,
    val rangeKm: Int,
    val accelSec: String,
) : Serializable

data class GoodsColor(
    val name: String,
    val hex: String,
) : Serializable

data class GoodsParam(
    val key: String,
    val value: String,
) : Serializable