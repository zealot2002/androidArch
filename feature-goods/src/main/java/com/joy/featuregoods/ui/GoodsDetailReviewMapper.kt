package com.joy.featuregoods.ui

import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailReviewPreview
import com.joy.featuregoods.model.GoodsDetailReviewState
import com.joy.featuregoods.model.GoodsDetailReviewTag

object GoodsDetailReviewMapper {

    fun from(detail: GoodsDetail): GoodsDetailReviewState {
        val previewImages = buildPreviewImages(detail)
        val totalCount = detail.reviewTotalCount.takeIf { it > 0 } ?: DEFAULT_TOTAL_COUNT
        val positiveRate = detail.reviewPositiveRate.ifBlank { DEFAULT_POSITIVE_RATE }
        return GoodsDetailReviewState(
            totalCount = totalCount,
            positiveRate = positiveRate,
            tags = listOf(
                GoodsDetailReviewTag("味道赞", 120),
                GoodsDetailReviewTag("包装严实", 85),
                GoodsDetailReviewTag("送货快", 210),
                GoodsDetailReviewTag("非常新鲜", 342),
            ),
            preview = GoodsDetailReviewPreview(
                userName = detail.reviewPreviewUser.ifBlank { "张*三" },
                avatarUrl = detail.reviewPreviewAvatarUrl.ifBlank { PREVIEW_AVATAR_URL },
                starCount = 5,
                content = detail.reviewPreviewContent.ifBlank {
                    "味道真的很赞，非常新鲜，顺丰快递送到家还是冰凉的，口感软糯。"
                },
                imageUrls = previewImages,
            ),
        )
    }

    private fun buildPreviewImages(detail: GoodsDetail): List<String> {
        val fromDetail = detail.bannerImages.filter { it.isNotBlank() }.take(3)
        if (fromDetail.isNotEmpty()) return fromDetail
        return DEFAULT_PREVIEW_IMAGES
    }

    private const val DEFAULT_TOTAL_COUNT = 856
    private const val DEFAULT_POSITIVE_RATE = "98%"
    private const val PREVIEW_AVATAR_URL = "https://picsum.photos/id/64/200/200"

    private val DEFAULT_PREVIEW_IMAGES = listOf(
        "https://picsum.photos/id/292/480/480",
        "https://picsum.photos/id/312/480/480",
        "https://picsum.photos/id/326/480/480",
    )
}
