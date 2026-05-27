package com.joy.featuregoods.model

import java.io.Serializable

data class BrowseHistoryItem(
    val type: ItemType,
    val date: String? = null,
    val product: BrowseProduct? = null,
) : Serializable

enum class ItemType {
    TIME,
    PRODUCT
}

data class BrowseProduct(
    val spuId: String,
    val title: String,
    val price: String,
    val originalPrice: String,
    val imageUrl: String,
    val badge: String?,
    val sales: String,
) : Serializable