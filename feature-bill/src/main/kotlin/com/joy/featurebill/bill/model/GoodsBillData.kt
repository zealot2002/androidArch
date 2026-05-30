package com.joy.featurebill.bill.model

data class GoodsBillData(
    val spuId: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val tag: String?,
    val tips: String,
    val shopName: String,
    val imageUrl: String,
)
