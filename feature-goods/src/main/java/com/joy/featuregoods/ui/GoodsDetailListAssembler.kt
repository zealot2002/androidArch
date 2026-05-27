package com.joy.featuregoods.ui

import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailProductSectionState

object GoodsDetailListAssembler {

    //根据不同商品，数据有所不同，分割线的数量也不同，需要动态规划
    fun build(
        productSection: GoodsDetailProductSectionState,
        detail: GoodsDetail,
        detailImageUrls: List<String>,
        recommendProducts: List<BrowseProduct>,
        showListEndFooter: Boolean,
    ): List<GoodsDetailListItem> {
        val items = mutableListOf<GoodsDetailListItem>()

        items += GoodsDetailListItem.PriceMarketing(productSection)
        items += GoodsDetailListItem.ProductTitle(productSection)
        items += GoodsDetailListItem.ServiceSales(productSection)
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.AfterSales(productSection)
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.SpecSelection(productSection)
        items += GoodsDetailListItem.PurchaseQuantity(productSection)

        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.Review(GoodsDetailReviewMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider

        items += GoodsDetailListItem.Shop(GoodsDetailShopMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.DetailsTitle
        detailImageUrls.forEach { url ->
            items += GoodsDetailListItem.DetailImage(url)
        }

        items += GoodsDetailListItem.RecommendTitle
        recommendProducts.forEach { product ->
            items += GoodsDetailListItem.RecommendProduct(product)
        }

        if (showListEndFooter) {
            items += GoodsDetailListItem.ListFooter
        }

        return items
    }
}
