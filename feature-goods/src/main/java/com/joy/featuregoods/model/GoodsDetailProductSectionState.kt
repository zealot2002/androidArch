package com.joy.featuregoods.model

data class GoodsDetailProductSectionState(
    val priceYuan: String,
    val originalPriceYuan: String,
    val statusTag: String,
    val couponText: String,
    val title: String,
    val recommendLabel: String,
    val monthlySales: String,
    val shipFrom: String,
    val guarantees: List<String>,
    val weightSpecs: List<String>,
    val flavorSpecs: List<String>,
    val selectedWeightIndex: Int = 0,
    val selectedFlavorIndex: Int = 0,
    val quantity: Int = 1,
)