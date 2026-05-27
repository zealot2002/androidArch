package com.joy.featuregoods.ui

import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetailProductSectionState
import com.joy.featuregoods.model.GoodsDetailReviewState
import com.joy.featuregoods.model.GoodsDetailShopState

sealed class GoodsDetailListItem {
    data class PriceMarketing(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data class ProductTitle(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data class ServiceSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data class AfterSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data class SpecSelection(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data class PurchaseQuantity(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()

    data object SectionDivider : GoodsDetailListItem()

    data class Review(val state: GoodsDetailReviewState) : GoodsDetailListItem()

    data class Shop(val state: GoodsDetailShopState) : GoodsDetailListItem()

    data object DetailsTitle : GoodsDetailListItem()

    data class DetailImage(val imageUrl: String) : GoodsDetailListItem()

    data object RecommendTitle : GoodsDetailListItem()

    data class RecommendProduct(val product: BrowseProduct) : GoodsDetailListItem()

    data object ListFooter : GoodsDetailListItem()
}
