package com.joy.featuregoods.ui

import com.joy.featuregoods.R
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailProductSectionState
import java.util.Locale

object GoodsDetailProductSectionMapper {

    fun from(
        detail: GoodsDetail,
        shipFromCity: String?,
        selectedWeightIndex: Int,
        selectedFlavorIndex: Int,
        quantity: Int,
    ): GoodsDetailProductSectionState {
        val selectedSku = detail.skus.getOrNull(selectedWeightIndex) ?: detail.skus.firstOrNull()
        val priceYuan = selectedSku?.priceYuan ?: "98.00"
        val formattedPrice = formatDisplayPrice(priceYuan)
        val originalPrice = formatOriginalPrice(formattedPrice)
        val weightSpecs = detail.skus.map { it.name }.ifEmpty { defaultWeightSpecs() }
        val flavorSpecs = detail.colors.map { it.name }.ifEmpty { defaultFlavorSpecs() }
        val guarantees = detail.services.ifEmpty { defaultGuarantees() }
        val recommendLabel = detail.tags.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: "推荐"
        return GoodsDetailProductSectionState(
            priceYuan = formattedPrice,
            originalPriceYuan = originalPrice,
            statusTag = detail.tags.getOrNull(1)?.takeIf { it.isNotBlank() }
                ?: "热销中",
            couponText = "满199减20",
            title = detail.title,
            recommendLabel = "推荐：$recommendLabel",
            monthlySales = "月销量 2453+",
            shipFrom = "发货：${shipFromCity?.takeIf { it.isNotBlank() } ?: "上海市"}",
            guarantees = guarantees,
            weightSpecs = weightSpecs,
            flavorSpecs = flavorSpecs,
            selectedWeightIndex = selectedWeightIndex.coerceIn(0, weightSpecs.lastIndex.coerceAtLeast(0)),
            selectedFlavorIndex = selectedFlavorIndex.coerceIn(0, flavorSpecs.lastIndex.coerceAtLeast(0)),
            quantity = quantity.coerceIn(1, 99),
        )
    }

    private fun formatDisplayPrice(raw: String): String {
        val num = raw.trim().removePrefix("¥").removePrefix("￥").toDoubleOrNull()
        return if (num != null) {
            String.format(Locale.US, "%.2f", num)
        } else {
            raw.trim().ifBlank { "98.00" }
        }
    }

    private fun formatOriginalPrice(priceYuan: String): String {
        val num = priceYuan.toDoubleOrNull()
        return if (num != null) {
            val original = num * (158.0 / 98.0)
            "¥${String.format(Locale.US, "%.2f", original)}"
        } else {
            "¥158.00"
        }
    }

    private fun defaultWeightSpecs(): List<String> {
        return listOf("200g/盒", "400g/盒", "500g/盒")
    }

    private fun defaultFlavorSpecs(): List<String> {
        return listOf("原味", "烟熏味", "柠檬味")
    }

    private fun defaultGuarantees(): List<String> {
        return listOf("正品保证", "7天无理由退换", "顺丰冷链")
    }
}
