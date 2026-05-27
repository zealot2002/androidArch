package com.joy.featuregoods.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joy.featuregoods.databinding.ItemGoodsDetailHighlightsBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailParamsBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailPriceBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailRecommendBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailReviewBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailServicesBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailShopBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailSpecBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailTitleBinding
import com.joy.featuregoods.databinding.ItemRecommendProductBinding
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail

class GoodsDetailAdapter(
    private val callbacks: Callbacks,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Callbacks {
        fun onRecommendProductClick(spuId: String)
        fun onEnterShopClick()
    }

    companion object {
        const val VIEW_TYPE_PRICE = 1
        const val VIEW_TYPE_TITLE = 2
        const val VIEW_TYPE_HIGHLIGHTS = 3
        const val VIEW_TYPE_SPEC = 4
        const val VIEW_TYPE_SERVICES = 5
        const val VIEW_TYPE_PARAMS = 6
        const val VIEW_TYPE_SHOP = 7
        const val VIEW_TYPE_REVIEW = 8
        const val VIEW_TYPE_RECOMMEND = 9
    }

    private var goodsDetail: GoodsDetail? = null
    private var recommendProducts: List<BrowseProduct> = emptyList()

    fun submit(detail: GoodsDetail, recommends: List<BrowseProduct>) {
        this.goodsDetail = detail
        this.recommendProducts = recommends
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_PRICE
            1 -> VIEW_TYPE_TITLE
            2 -> VIEW_TYPE_HIGHLIGHTS
            3 -> VIEW_TYPE_SPEC
            4 -> VIEW_TYPE_SERVICES
            5 -> VIEW_TYPE_PARAMS
            6 -> VIEW_TYPE_SHOP
            7 -> VIEW_TYPE_REVIEW
            8 -> VIEW_TYPE_RECOMMEND
            else -> VIEW_TYPE_PRICE
        }
    }

    override fun getItemCount(): Int {
        return if (goodsDetail == null) 0 else 9
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PRICE -> PriceVH(ItemGoodsDetailPriceBinding.inflate(inflater, parent, false))
            VIEW_TYPE_TITLE -> TitleVH(ItemGoodsDetailTitleBinding.inflate(inflater, parent, false))
            VIEW_TYPE_HIGHLIGHTS -> HighlightsVH(ItemGoodsDetailHighlightsBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SPEC -> SpecVH(ItemGoodsDetailSpecBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SERVICES -> ServicesVH(ItemGoodsDetailServicesBinding.inflate(inflater, parent, false))
            VIEW_TYPE_PARAMS -> ParamsVH(ItemGoodsDetailParamsBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SHOP -> ShopVH(ItemGoodsDetailShopBinding.inflate(inflater, parent, false))
            VIEW_TYPE_REVIEW -> ReviewVH(ItemGoodsDetailReviewBinding.inflate(inflater, parent, false))
            VIEW_TYPE_RECOMMEND -> RecommendVH(ItemGoodsDetailRecommendBinding.inflate(inflater, parent, false))
            else -> PriceVH(ItemGoodsDetailPriceBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val detail = goodsDetail ?: return
        when (holder) {
            is PriceVH -> holder.bind(detail)
            is TitleVH -> holder.bind(detail)
            is HighlightsVH -> holder.bind(detail)
            is SpecVH -> holder.bind(detail)
            is ServicesVH -> holder.bind(detail)
            is ParamsVH -> holder.bind(detail)
            is ShopVH -> holder.bind(detail)
            is ReviewVH -> holder.bind(detail)
            is RecommendVH -> holder.bind(recommendProducts)
        }
    }

    private inner class PriceVH(private val binding: ItemGoodsDetailPriceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            val sku = detail.skus.firstOrNull()
            binding.tvPrice.text = "¥${sku?.priceYuan ?: "0.00"}"
            val originalPrice = (sku?.priceYuan?.toDoubleOrNull() ?: 0.0) * 1.2
            binding.tvOriginalPrice.text = "¥%.2f".format(originalPrice)
            binding.tvOriginalPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvTags.text = detail.tags.joinToString(" ")
        }
    }

    private inner class TitleVH(private val binding: ItemGoodsDetailTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.tvTitle.text = detail.title
            binding.tvSubtitle.text = detail.subtitle
        }
    }

    private inner class HighlightsVH(private val binding: ItemGoodsDetailHighlightsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.llHighlights.removeAllViews()
            detail.highlights.forEach { highlight ->
                val textView = TextView(binding.root.context).apply {
                    text = "• $highlight"
                    textSize = 14f
                    setTextColor(binding.root.context.getColor(com.joy.appres.R.color.black_2))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 4
                    }
                }
                binding.llHighlights.addView(textView)
            }
        }
    }

    private inner class SpecVH(private val binding: ItemGoodsDetailSpecBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.llSkus.removeAllViews()
            detail.skus.forEachIndexed { index, sku ->
                val chip = TextView(binding.root.context).apply {
                    text = sku.name
                    textSize = 12f
                    setTextColor(binding.root.context.getColor(com.joy.appres.R.color.black_2))
                    background = binding.root.context.getDrawable(com.joy.appres.R.drawable.bg_gray_fill_interact_1)
                    setPadding(12, 6, 12, 6)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) marginStart = 8
                    }
                }
                binding.llSkus.addView(chip)
            }
        }
    }

    private inner class ServicesVH(private val binding: ItemGoodsDetailServicesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.llServices.removeAllViews()
            detail.services.forEachIndexed { index, service ->
                val chip = TextView(binding.root.context).apply {
                    text = service
                    textSize = 12f
                    setTextColor(binding.root.context.getColor(com.joy.appres.R.color.orange_1))
                    background = binding.root.context.getDrawable(com.joy.appres.R.drawable.bg_orange_fill_interact_3)
                    setPadding(10, 4, 10, 4)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (index > 0) marginStart = 8
                    }
                }
                binding.llServices.addView(chip)
            }
        }
    }

    private inner class ParamsVH(private val binding: ItemGoodsDetailParamsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.llParams.removeAllViews()
            detail.params.forEach { param ->
                val row = LinearLayout(binding.root.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8
                    }
                }
                val keyView = TextView(binding.root.context).apply {
                    text = param.key
                    textSize = 13f
                    setTextColor(binding.root.context.getColor(com.joy.appres.R.color.black_3))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f)
                }
                val valueView = TextView(binding.root.context).apply {
                    text = param.value
                    textSize = 13f
                    setTextColor(binding.root.context.getColor(com.joy.appres.R.color.black_2))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f)
                }
                row.addView(keyView)
                row.addView(valueView)
                binding.llParams.addView(row)
            }
        }
    }

    private inner class ShopVH(private val binding: ItemGoodsDetailShopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            Glide.with(binding.ivShopLogo.context)
                .load(detail.shopLogoUrl)
                .into(binding.ivShopLogo)
            binding.tvShopName.text = detail.shopName
            binding.tvShopInfo.text = "${detail.shopFollowerCount} 粉丝 | ${detail.shopItemCount} 商品"
            binding.tvEnterShop.setOnClickListener { callbacks.onEnterShopClick() }
        }
    }

    private inner class ReviewVH(private val binding: ItemGoodsDetailReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(detail: GoodsDetail) {
            binding.tvReviewCount.text = "(${detail.reviewTotalCount})"
            binding.tvPositiveRate.text = "好评率 ${detail.reviewPositiveRate}"
            binding.tvUserName.text = detail.reviewPreviewUser
            binding.tvReviewContent.text = detail.reviewPreviewContent
            Glide.with(binding.ivAvatar.context)
                .load(detail.reviewPreviewAvatarUrl)
                .into(binding.ivAvatar)
        }
    }

    private inner class RecommendVH(private val binding: ItemGoodsDetailRecommendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(products: List<BrowseProduct>) {
            binding.llRecommend.removeAllViews()
            products.take(3).forEachIndexed { index, product ->
                val itemBinding = ItemRecommendProductBinding.inflate(
                    LayoutInflater.from(binding.root.context),
                    binding.llRecommend,
                    false
                )
                Glide.with(itemBinding.ivProduct.context)
                    .load(product.imageUrl)
                    .into(itemBinding.ivProduct)
                itemBinding.tvProductTitle.text = product.title
                itemBinding.tvProductPrice.text = "¥${product.price}"
                itemBinding.root.setOnClickListener { callbacks.onRecommendProductClick(product.spuId) }
                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) marginStart = 12
                }
                itemBinding.root.layoutParams = layoutParams
                binding.llRecommend.addView(itemBinding.root)
            }
        }
    }
}