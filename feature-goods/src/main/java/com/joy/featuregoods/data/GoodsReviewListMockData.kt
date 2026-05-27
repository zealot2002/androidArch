package com.joy.featuregoods.data

import com.joy.featuregoods.model.GoodsDetailReviewListItem

object GoodsReviewListMockData {

    fun reviews(): List<GoodsDetailReviewListItem> = listOf(
        GoodsDetailReviewListItem(
            userName = "张*三",
            avatarUrl = "https://picsum.photos/id/64/200/200",
            starCount = 5,
            content = "味道真的很赞，非常新鲜，顺丰快递送到家还是冰凉的，口感软糯。",
            imageUrls = listOf(
                "https://picsum.photos/id/292/480/480",
                "https://picsum.photos/id/312/480/480",
                "https://picsum.photos/id/326/480/480",
            ),
            specLabel = "200g 刺身级",
            timeLabel = "2025-03-12",
        ),
        GoodsDetailReviewListItem(
            userName = "李*明",
            avatarUrl = "https://picsum.photos/id/91/200/200",
            starCount = 5,
            content = "回购第三次了，品质稳定，孩子也很爱吃，刺身做寿司都合适。",
            imageUrls = listOf(
                "https://picsum.photos/id/108/480/480",
                "https://picsum.photos/id/119/480/480",
            ),
            specLabel = "300g 刺身级",
            timeLabel = "2025-03-08",
        ),
        GoodsDetailReviewListItem(
            userName = "王*芳",
            avatarUrl = "https://picsum.photos/id/177/200/200",
            starCount = 4,
            content = "包装很严实，冰袋没化，就是比超市略贵一点，但新鲜度确实更好。",
            imageUrls = emptyList(),
            specLabel = "200g 刺身级",
            timeLabel = "2025-02-28",
        ),
        GoodsDetailReviewListItem(
            userName = "陈*浩",
            avatarUrl = "https://picsum.photos/id/338/200/200",
            starCount = 5,
            content = "送货很快，早上下单下午就到。鱼肉颜色漂亮，没有腥味，推荐。",
            imageUrls = listOf(
                "https://picsum.photos/id/160/480/480",
            ),
            specLabel = "500g 家庭装",
            timeLabel = "2025-02-20",
        ),
        GoodsDetailReviewListItem(
            userName = "赵*静",
            avatarUrl = "https://picsum.photos/id/399/200/200",
            starCount = 5,
            content = "做寿喜锅特别合适，厚度刚好，解冻后擦干水分煎一下更香。",
            imageUrls = listOf(
                "https://picsum.photos/id/225/480/480",
                "https://picsum.photos/id/237/480/480",
            ),
            specLabel = "300g 刺身级",
            timeLabel = "2025-02-15",
        ),
        GoodsDetailReviewListItem(
            userName = "刘*洋",
            avatarUrl = "https://picsum.photos/id/429/200/200",
            starCount = 4,
            content = "整体满意，有一片边缘略深，客服处理很及时，会再试试其他规格。",
            imageUrls = emptyList(),
            specLabel = "200g 刺身级",
            timeLabel = "2025-02-06",
        ),
        GoodsDetailReviewListItem(
            userName = "周*婷",
            avatarUrl = "https://picsum.photos/id/475/200/200",
            starCount = 5,
            content = "闺蜜推荐的，做三文鱼饭团超方便，米饭趁热卷上就行，全家好评。",
            imageUrls = listOf(
                "https://picsum.photos/id/281/480/480",
                "https://picsum.photos/id/287/480/480",
                "https://picsum.photos/id/296/480/480",
            ),
            specLabel = "200g 刺身级",
            timeLabel = "2025-01-30",
        ),
        GoodsDetailReviewListItem(
            userName = "孙*磊",
            avatarUrl = "https://picsum.photos/id/532/200/200",
            starCount = 5,
            content = "冷链靠谱，到手还是硬邦邦的。脂肪线清晰，入口即化，值这个价。",
            imageUrls = listOf(
                "https://picsum.photos/id/318/480/480",
            ),
            specLabel = "500g 家庭装",
            timeLabel = "2025-01-22",
        ),
    )
}
