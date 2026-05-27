package com.joy.featuregoods.data

import com.joy.featuregoods.model.GoodsColor
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsParam
import com.joy.featuregoods.model.GoodsSku

object GoodsDetailMockCatalog {

    const val MOCK_SALMON = "mock-salmon"

    fun buildRandom(productId: Int): GoodsDetail {
        return buildSalmonDetail(productId)
    }

    private fun picsum(seed: String, w: Int = 1200, h: Int = 800): String {
        return "https://picsum.photos/seed/$seed/$w/$h"
    }

    private fun buildSalmonDetail(productId: Int): GoodsDetail {
        return GoodsDetail(
            productId = productId,
            spuId = MOCK_SALMON,
            title = "挪威进口顶级三文鱼刺身(200g/盒) 顺丰冷链直达",
            subtitle = "推荐：顺丰冷链 · 冰鲜直达",
            bannerImages = listOf(
                picsum("salmon1", w = 1200, h = 800),
                picsum("salmon2", w = 1200, h = 800),
                picsum("salmon3", w = 1200, h = 800),
            ),
            detailImages = listOf(
                picsum("salmon-detail1", w = 1200, h = 1600),
                picsum("salmon-detail2", w = 1200, h = 1500),
                picsum("salmon-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("顺丰冷链", "热销中"),
            skus = listOf(
                GoodsSku(productId + 0, "200g/盒", "98.00", 0, ""),
                GoodsSku(productId + 1, "400g/2盒装", "168.00", 0, ""),
                GoodsSku(productId + 2, "600g家庭装", "238.00", 0, ""),
            ),
            colors = listOf(GoodsColor("经典原味", ""), GoodsColor("淡盐轻腌", "")),
            highlights = listOf(
                "挪威直采 · 冰鲜锁鲜运输",
                "刺身级品质 · 脂肪分布均匀",
                "全程冷链 · 签收仍保持冰凉",
            ),
            services = listOf("极速退款", "七天无理由", "冷链配送"),
            params = listOf(
                GoodsParam("产地", "挪威"),
                GoodsParam("储存", "-18℃冷冻 / 0-4℃冷藏"),
                GoodsParam("保质期", "7天（冷藏）"),
            ),
            shipFromCity = "上海市",
            shopLogoUrl = picsum("shop1", w = 200, h = 200),
            shopName = "鲜生优选旗舰店",
            selfOperatedTag = "自营",
            shopFollowerCount = "12.5万",
            shopItemCount = "156",
            reviewTotalCount = 856,
            reviewPositiveRate = "98%",
            reviewPreviewUser = "张*三",
            reviewPreviewContent = "味道真的很赞，非常新鲜，顺丰送到家还是冰凉的，口感软糯。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/64/200/200",
        )
    }
}
