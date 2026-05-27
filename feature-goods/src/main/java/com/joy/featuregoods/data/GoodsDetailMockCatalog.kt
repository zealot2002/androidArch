package com.joy.featuregoods.data

import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsColor
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsParam
import com.joy.featuregoods.model.GoodsSku
import java.util.concurrent.atomic.AtomicInteger

object GoodsDetailMockCatalog {

    const val MOCK_SALMON = "mock-salmon"
    const val MOCK_SNEAKERS = "mock-sneakers"
    const val MOCK_WATCH = "mock-watch"
    const val MOCK_HEADPHONES = "mock-headphones"
    const val MOCK_COFFEE = "mock-coffee"
    const val MOCK_PLANTS = "mock-plants"

    val MOCK_SPU_IDS: List<String> = listOf(
        MOCK_SALMON,
        MOCK_SNEAKERS,
        MOCK_WATCH,
        MOCK_HEADPHONES,
        MOCK_COFFEE,
        MOCK_PLANTS,
    )

    private val nextProfileIndex = AtomicInteger(0)

    fun buildRandom(productId: Int): GoodsDetail {
        val index = Math.floorMod(nextProfileIndex.getAndIncrement(), MOCK_SPU_IDS.size)
        val key = MOCK_SPU_IDS[index]
        return PROFILES.getValue(key).toGoodsDetail(requestedSpuId = key, productId = productId)
    }

    private fun picsum(seed: String, w: Int = 1200, h: Int = 800): String {
        return "https://picsum.photos/seed/$seed/$w/$h"
    }

    private data class Profile(
        val spuId: String,
        val title: String,
        val subtitle: String,
        val bannerImages: List<String>,
        val detailImageUrls: List<String>,
        val tags: List<String>,
        val skus: List<Pair<String, String>>,
        val colors: List<String>,
        val highlights: List<String>,
        val services: List<String>,
        val params: List<Pair<String, String>>,
        val shopLogoUrl: String,
        val shopName: String,
        val selfOperatedTag: String?,
        val shopFollowerCount: String,
        val shopItemCount: String,
        val reviewTotalCount: Int,
        val reviewPositiveRate: String,
        val reviewPreviewUser: String,
        val reviewPreviewContent: String,
        val reviewPreviewAvatarUrl: String,
    ) {
        fun toGoodsDetail(requestedSpuId: String, productId: Int): GoodsDetail {
            return GoodsDetail(
                productId = productId,
                spuId = requestedSpuId,
                title = title,
                subtitle = subtitle,
                bannerImages = bannerImages,
                tags = tags,
                skus = skus.mapIndexed { index, (name, price) ->
                    GoodsSku(
                        skuId = productId + index,
                        name = name,
                        priceYuan = price,
                        rangeKm = 0,
                        accelSec = "",
                    )
                },
                colors = colors.map { GoodsColor(it, "") },
                highlights = highlights,
                services = services,
                params = params.map { (key, value) -> GoodsParam(key, value) },
                detailImageUrls = detailImageUrls,
                shopLogoUrl = shopLogoUrl,
                shopName = shopName,
                selfOperatedTag = selfOperatedTag,
                shopFollowerCount = shopFollowerCount,
                shopItemCount = shopItemCount,
                reviewTotalCount = reviewTotalCount,
                reviewPositiveRate = reviewPositiveRate,
                reviewPreviewUser = reviewPreviewUser,
                reviewPreviewContent = reviewPreviewContent,
                reviewPreviewAvatarUrl = reviewPreviewAvatarUrl,
            )
        }
    }

    private val PROFILES: Map<String, Profile> = mapOf(
        MOCK_SALMON to Profile(
            spuId = MOCK_SALMON,
            title = "挪威进口顶级三文鱼刺身(200g/盒) 顺丰冷链直达",
            subtitle = "推荐：顺丰冷链 · 冰鲜直达",
            bannerImages = listOf(
                picsum("salmon1", w = 1200, h = 800),
                picsum("salmon2", w = 1200, h = 800),
                picsum("salmon3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("salmon-detail1", w = 1200, h = 1600),
                picsum("salmon-detail2", w = 1200, h = 1500),
                picsum("salmon-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("顺丰冷链", "热销中"),
            skus = listOf(
                "200g/盒" to "98.00",
                "400g/2盒装" to "168.00",
                "600g家庭装" to "238.00",
            ),
            colors = listOf("经典原味", "淡盐轻腌"),
            highlights = listOf(
                "挪威直采 · 冰鲜锁鲜运输",
                "刺身级品质 · 脂肪分布均匀",
                "全程冷链 · 签收仍保持冰凉",
            ),
            services = listOf("极速退款", "七天无理由", "冷链配送"),
            params = listOf(
                "产地" to "挪威",
                "储存" to "-18℃冷冻 / 0-4℃冷藏",
                "保质期" to "7天（冷藏）",
            ),
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
        ),
        MOCK_SNEAKERS to Profile(
            spuId = MOCK_SNEAKERS,
            title = "Nike Air Zoom Pegasus 41 缓震跑步鞋 男款",
            subtitle = "推荐：轻弹回弹 · 日常训练",
            bannerImages = listOf(
                picsum("sneakers1", w = 1200, h = 800),
                picsum("sneakers2", w = 1200, h = 800),
                picsum("sneakers3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("sneakers-detail1", w = 1200, h = 1600),
                picsum("sneakers-detail2", w = 1200, h = 1500),
                picsum("sneakers-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("正品保障", "限时特惠"),
            skus = listOf(
                "40码" to "799.00",
                "41码" to "799.00",
                "42码" to "819.00",
            ),
            colors = listOf("黑白", "荧光绿"),
            highlights = listOf(
                "Zoom Air 缓震 · 长距离也舒适",
                "透气工程网面 · 夏季不闷脚",
                "耐磨橡胶大底 · 抓地稳定",
            ),
            services = listOf("正品鉴定", "七天无理由", "运费险"),
            params = listOf(
                "品牌" to "Nike",
                "适用场景" to "路跑 / 日常",
                "闭合方式" to "系带",
            ),
            shopLogoUrl = picsum("shop2", w = 200, h = 200),
            shopName = "潮鞋汇运动专营店",
            selfOperatedTag = null,
            shopFollowerCount = "8.6万",
            shopItemCount = "320",
            reviewTotalCount = 1240,
            reviewPositiveRate = "97%",
            reviewPreviewUser = "李*明",
            reviewPreviewContent = "上脚很轻，缓震明显，跑 10 公里脚也不酸，尺码正。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/65/200/200",
        ),
        MOCK_WATCH to Profile(
            spuId = MOCK_WATCH,
            title = "Garmin Forerunner 965 运动智能手表 钛合金表圈",
            subtitle = "推荐：双频 GPS · 超长续航",
            bannerImages = listOf(
                picsum("watch1", w = 1200, h = 800),
                picsum("watch2", w = 1200, h = 800),
                picsum("watch3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("watch-detail1", w = 1200, h = 1600),
                picsum("watch-detail2", w = 1200, h = 1500),
                picsum("watch-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("国行正品", "晒单有礼"),
            skus = listOf(
                "黑色表带" to "4280.00",
                "白色表带" to "4280.00",
                "钛灰套装" to "4680.00",
            ),
            colors = listOf("曜石黑", "云雾白"),
            highlights = listOf(
                "AMOLED 彩屏 · 户外强光也清晰",
                "多星定位 · 轨迹更精准",
                "典型续航 23 天 · 出差无忧",
            ),
            services = listOf("全国联保", "七天无理由", "顺丰包邮"),
            params = listOf(
                "防水等级" to "5 ATM",
                "屏幕" to "1.4 英寸 AMOLED",
                "重量" to "53g",
            ),
            shopLogoUrl = picsum("shop3", w = 200, h = 200),
            shopName = "时光精品数码馆",
            selfOperatedTag = "自营",
            shopFollowerCount = "5.2万",
            shopItemCount = "89",
            reviewTotalCount = 632,
            reviewPositiveRate = "99%",
            reviewPreviewUser = "王*芳",
            reviewPreviewContent = "GPS 很准，续航比预期还长，游泳也能记圈，表盘质感很好。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/91/200/200",
        ),
        MOCK_HEADPHONES to Profile(
            spuId = MOCK_HEADPHONES,
            title = "Sony WH-1000XM5 无线降噪耳机 铂金银",
            subtitle = "推荐：旗舰降噪 · Hi-Res 音质",
            bannerImages = listOf(
                picsum("headphones1", w = 1200, h = 800),
                picsum("headphones2", w = 1200, h = 800),
                picsum("headphones3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("headphones-detail1", w = 1200, h = 1600),
                picsum("headphones-detail2", w = 1200, h = 1500),
                picsum("headphones-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("官方授权", "12期免息"),
            skus = listOf(
                "铂金银" to "2499.00",
                "午夜黑" to "2499.00",
                "联名礼盒" to "2799.00",
            ),
            colors = listOf("铂金银", "午夜黑"),
            highlights = listOf(
                "双芯降噪 V2 · 通勤更安静",
                "30 小时续航 · 快充 3 分钟听 3 小时",
                "多点连接 · 手机电脑无缝切换",
            ),
            services = listOf("正品发票", "七天无理由", "以换代修"),
            params = listOf(
                "驱动单元" to "30mm",
                "蓝牙" to "5.2",
                "重量" to "约 250g",
            ),
            shopLogoUrl = picsum("shop4", w = 200, h = 200),
            shopName = "声学科技官方店",
            selfOperatedTag = "自营",
            shopFollowerCount = "18.3万",
            shopItemCount = "412",
            reviewTotalCount = 2108,
            reviewPositiveRate = "98%",
            reviewPreviewUser = "陈*浩",
            reviewPreviewContent = "降噪比上一代强，低音干净，长时间佩戴也不夹头。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/100/200/200",
        ),
        MOCK_COFFEE to Profile(
            spuId = MOCK_COFFEE,
            title = "埃塞俄比亚 耶加雪菲 水洗 G1 精品咖啡豆 227g",
            subtitle = "推荐：中浅烘 · 花香果酸",
            bannerImages = listOf(
                picsum("coffee1", w = 1200, h = 800),
                picsum("coffee2", w = 1200, h = 800),
                picsum("coffee3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("coffee-detail1", w = 1200, h = 1600),
                picsum("coffee-detail2", w = 1200, h = 1500),
                picsum("coffee-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("新鲜烘焙", "满减优惠"),
            skus = listOf(
                "227g 袋装" to "68.00",
                "454g 家庭装" to "118.00",
                "挂耳 10 包" to "45.00",
            ),
            colors = listOf("中浅烘", "中深烘"),
            highlights = listOf(
                "下单后 48 小时内烘焙发货",
                "水洗处理 · 柑橘与茉莉花香",
                "SCA 评分 86+ · 精品级",
            ),
            services = listOf("坏豆包赔", "七天无理由", "顺丰包邮"),
            params = listOf(
                "产区" to "埃塞俄比亚 耶加雪菲",
                "处理法" to "水洗",
                "建议萃取" to "手冲 / 意式",
            ),
            shopLogoUrl = picsum("shop5", w = 200, h = 200),
            shopName = "咖啡豆工坊",
            selfOperatedTag = null,
            shopFollowerCount = "3.8万",
            shopItemCount = "67",
            reviewTotalCount = 421,
            reviewPositiveRate = "96%",
            reviewPreviewUser = "赵*敏",
            reviewPreviewContent = "开袋就很香，冲出来果酸明亮，做拿铁也很平衡。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/177/200/200",
        ),
        MOCK_PLANTS to Profile(
            spuId = MOCK_PLANTS,
            title = "龟背竹 大型室内绿植 含陶瓷盆 约 80cm",
            subtitle = "推荐：耐阴好养 · 净化空气",
            bannerImages = listOf(
                picsum("plants1", w = 1200, h = 800),
                picsum("plants2", w = 1200, h = 800),
                picsum("plants3", w = 1200, h = 800),
            ),
            detailImageUrls = listOf(
                picsum("plants-detail1", w = 1200, h = 1600),
                picsum("plants-detail2", w = 1200, h = 1500),
                picsum("plants-detail3", w = 1200, h = 1600),
            ),
            tags = listOf("破损包赔", "养护指导"),
            skus = listOf(
                "60cm 入门" to "128.00",
                "80cm 标准" to "198.00",
                "100cm 大型" to "268.00",
            ),
            colors = listOf("原盆发货", "换盆升级"),
            highlights = listOf(
                "北欧风陶瓷盆 · 开箱即摆",
                "基地直发 · 专业打包防损",
                "附养护卡 · 新手也能养",
            ),
            services = listOf("破损补发", "七天无理由", "同城极速达"),
            params = listOf(
                "光照" to "散射光 / 耐阴",
                "浇水" to "见干见湿",
                "盆径" to "约 24cm",
            ),
            shopLogoUrl = picsum("shop6", w = 200, h = 200),
            shopName = "植愈生活馆",
            selfOperatedTag = null,
            shopFollowerCount = "6.1万",
            shopItemCount = "143",
            reviewTotalCount = 389,
            reviewPositiveRate = "97%",
            reviewPreviewUser = "周*婷",
            reviewPreviewContent = "包装很用心，叶子完好，放客厅一下子就有生气了。",
            reviewPreviewAvatarUrl = "https://picsum.photos/id/200/200/200",
        ),
    )
}