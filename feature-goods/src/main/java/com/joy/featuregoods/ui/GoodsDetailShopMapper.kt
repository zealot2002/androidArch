package com.joy.featuregoods.ui

import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailShopRating
import com.joy.featuregoods.model.GoodsDetailShopState

object GoodsDetailShopMapper {

    fun from(detail: GoodsDetail): GoodsDetailShopState {
        return GoodsDetailShopState(
            logoUrl = detail.shopLogoUrl.ifBlank { DEFAULT_LOGO_URL },
            shopName = detail.shopName.ifBlank { "易店园官方旗舰店" },
            selfOperatedTag = detail.selfOperatedTag ?: "自营".takeIf { detail.shopName.isBlank() },
            followerCount = detail.shopFollowerCount.ifBlank { "12.5万" },
            itemCount = detail.shopItemCount.ifBlank { "156" },
            enterShopText = "进店逛逛",
            ratings = listOf(
                GoodsDetailShopRating(label = "宝贝描述", score = "4.9", levelLabel = "高"),
                GoodsDetailShopRating(label = "卖家服务", score = "4.8", levelLabel = "高"),
                GoodsDetailShopRating(label = "物流服务", score = "4.9", levelLabel = "高"),
            ),
        )
    }

    private const val DEFAULT_LOGO_URL = "https://picsum.photos/id/119/200/200"
}
