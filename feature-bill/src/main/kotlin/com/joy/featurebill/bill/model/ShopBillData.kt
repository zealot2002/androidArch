package com.joy.featurebill.bill.model

data class ShopBillData(
    val shopId: String,
    val name: String,
    val featureDesc: String?,
    val onSellDesc: String?,
    val imageUrl: String,
    val miniProgramCodeUrl: String,
)
