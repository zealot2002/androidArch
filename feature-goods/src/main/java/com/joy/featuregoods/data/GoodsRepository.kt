package com.joy.featuregoods.data

import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import kotlin.math.abs

class GoodsRepository {

    suspend fun fetchGoodsDetail(spuId: String): GoodsDetail {
        val id = resolveProductId(spuId)
        return mockGoodsDetail(productId = id)
    }

    suspend fun loadRecommended(): List<BrowseProduct> {
        return listOf(
            BrowseProduct(
                spuId = "mock-sneakers",
                title = "Nike Air Zoom 跑步鞋",
                price = "799.00",
                originalPrice = "899.00",
                imageUrl = "https://picsum.photos/seed/sneakers/200/200",
                badge = "热销",
                sales = "2.3万"
            ),
            BrowseProduct(
                spuId = "mock-watch",
                title = "Garmin 运动智能手表",
                price = "4280.00",
                originalPrice = "4580.00",
                imageUrl = "https://picsum.photos/seed/watch/200/200",
                badge = "自营",
                sales = "5600"
            ),
            BrowseProduct(
                spuId = "mock-headphones",
                title = "Sony 无线降噪耳机",
                price = "2499.00",
                originalPrice = "2699.00",
                imageUrl = "https://picsum.photos/seed/headphones/200/200",
                badge = "12期免息",
                sales = "1.8万"
            ),
            BrowseProduct(
                spuId = "mock-coffee",
                title = "耶加雪菲 精品咖啡豆",
                price = "68.00",
                originalPrice = "88.00",
                imageUrl = "https://picsum.photos/seed/coffee/200/200",
                badge = null,
                sales = "8900"
            ),
            BrowseProduct(
                spuId = "mock-plants",
                title = "龟背竹 室内绿植",
                price = "198.00",
                originalPrice = "238.00",
                imageUrl = "https://picsum.photos/seed/plants/200/200",
                badge = "破损包赔",
                sales = "3200"
            ),
        )
    }

    private fun resolveProductId(spuId: String): Int {
        spuId.toIntOrNull()?.let { return it }
        val h = spuId.hashCode()
        val positive = if (h == Int.MIN_VALUE) 0 else abs(h)
        return positive % 8_999_999 + 1
    }

    private fun mockGoodsDetail(productId: Int): GoodsDetail {
        return GoodsDetailMockCatalog.buildRandom(productId = productId)
    }
}