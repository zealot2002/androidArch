package com.joy.featuregoods.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.joy.common.extend.getCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.joy.appres.R as AppResR
import com.joy.common.extend.onClick
import com.joy.common.extend.onClick200
import com.joy.common.extend.onClick300
import com.joy.common.extend.onClick500
import com.joy.common.image.loadNetworkImage
import com.joy.common.image.loadNetworkImageCircle
import com.joy.common.utils.PaletteColorUtils
import com.joy.common.utils.SizeUtils
import com.joy.common.widgets.IconFontView
import com.joy.featuregoods.R
import com.joy.featuregoods.databinding.ItemBrowseHistoryProductBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailAfterSalesBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailDetailsTitleBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailDetailImageBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailListFooterBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailPriceMarketingBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailProductTitleBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailPurchaseQuantityBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailRecommendTitleBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailReviewBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailSectionDividerBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailServiceSalesBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailShopBinding
import com.joy.featuregoods.databinding.ItemGoodsDetailSpecSelectionBinding
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailProductSectionState
import com.joy.featuregoods.model.GoodsDetailReviewState
import com.joy.featuregoods.model.GoodsDetailReviewTag
import com.joy.featuregoods.model.GoodsDetailShopRating
import com.joy.featuregoods.model.GoodsDetailShopState

class GoodsDetailAdapter(
    private val context: Context,
    private val callbacks: Callbacks,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Callbacks {
        fun onCouponClick()
        fun onAfterSalesClick()
        fun onWeightSpecSelected(index: Int)
        fun onFlavorSpecSelected(index: Int)
        fun onQuantityMinus()
        fun onQuantityPlus()
        fun onRowReviewClick()
        fun onEnterShopClick()
        fun onRecommendProductClick(spuId: String)
    }

    enum class AnchorTab {
        PRODUCT,
        REVIEW,
        DETAIL,
        RECOMMEND,
    }

    companion object {
        const val VIEW_TYPE_PRICE_MARKETING = 1
        const val VIEW_TYPE_PRODUCT_TITLE = 2
        const val VIEW_TYPE_SERVICE_SALES = 3
        const val VIEW_TYPE_AFTER_SALES = 4
        const val VIEW_TYPE_SPEC_SELECTION = 5
        const val VIEW_TYPE_PURCHASE_QUANTITY = 6
        const val VIEW_TYPE_REVIEW = 7
        const val VIEW_TYPE_SHOP = 8
        const val VIEW_TYPE_DETAILS_TITLE = 9
        const val VIEW_TYPE_DETAIL_IMAGE = 10
        const val VIEW_TYPE_RECOMMEND_TITLE = 11
        const val VIEW_TYPE_RECOMMEND_PRODUCT = 12
        const val VIEW_TYPE_LIST_FOOTER = 13
        const val VIEW_TYPE_SECTION_DIVIDER = 14
    }

    private val items = mutableListOf<GoodsDetailListItem>()
    private var detailImageUrls: List<String> = emptyList()

    fun submit(
        productSection: GoodsDetailProductSectionState,
        detail: GoodsDetail,
        detailImageUrls: List<String>,
        recommendProducts: List<BrowseProduct>,
        showListEndFooter: Boolean,
    ) {
        this.detailImageUrls = detailImageUrls
        items.clear()
        items.addAll(
            GoodsDetailListAssembler.build(
                productSection = productSection,
                detail = detail,
                detailImageUrls = detailImageUrls,
                recommendProducts = recommendProducts,
                showListEndFooter = showListEndFooter,
            ),
        )
        notifyDataSetChanged()
    }

    fun anchorPosition(tab: AnchorTab): Int {
        val index = items.indexOfFirst { item ->
            when (tab) {
                AnchorTab.PRODUCT -> item is GoodsDetailListItem.PriceMarketing
                AnchorTab.REVIEW -> item is GoodsDetailListItem.Review
                AnchorTab.DETAIL -> item is GoodsDetailListItem.DetailsTitle
                AnchorTab.RECOMMEND -> item is GoodsDetailListItem.RecommendTitle
            }
        }
        return index.coerceAtLeast(0)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GoodsDetailListItem.PriceMarketing -> VIEW_TYPE_PRICE_MARKETING
            is GoodsDetailListItem.ProductTitle -> VIEW_TYPE_PRODUCT_TITLE
            is GoodsDetailListItem.ServiceSales -> VIEW_TYPE_SERVICE_SALES
            is GoodsDetailListItem.AfterSales -> VIEW_TYPE_AFTER_SALES
            is GoodsDetailListItem.SpecSelection -> VIEW_TYPE_SPEC_SELECTION
            is GoodsDetailListItem.PurchaseQuantity -> VIEW_TYPE_PURCHASE_QUANTITY
            is GoodsDetailListItem.SectionDivider -> VIEW_TYPE_SECTION_DIVIDER
            is GoodsDetailListItem.Review -> VIEW_TYPE_REVIEW
            is GoodsDetailListItem.Shop -> VIEW_TYPE_SHOP
            is GoodsDetailListItem.DetailsTitle -> VIEW_TYPE_DETAILS_TITLE
            is GoodsDetailListItem.DetailImage -> VIEW_TYPE_DETAIL_IMAGE
            is GoodsDetailListItem.RecommendTitle -> VIEW_TYPE_RECOMMEND_TITLE
            is GoodsDetailListItem.RecommendProduct -> VIEW_TYPE_RECOMMEND_PRODUCT
            is GoodsDetailListItem.ListFooter -> VIEW_TYPE_LIST_FOOTER
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PRICE_MARKETING -> PriceMarketingVH(
                ItemGoodsDetailPriceMarketingBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_PRODUCT_TITLE -> ProductTitleVH(
                ItemGoodsDetailProductTitleBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_SERVICE_SALES -> ServiceSalesVH(
                ItemGoodsDetailServiceSalesBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_AFTER_SALES -> AfterSalesVH(
                ItemGoodsDetailAfterSalesBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_SPEC_SELECTION -> SpecSelectionVH(
                ItemGoodsDetailSpecSelectionBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_PURCHASE_QUANTITY -> PurchaseQuantityVH(
                ItemGoodsDetailPurchaseQuantityBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_SECTION_DIVIDER -> SectionDividerVH(
                ItemGoodsDetailSectionDividerBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_REVIEW -> ReviewVH(ItemGoodsDetailReviewBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SHOP -> ShopVH(ItemGoodsDetailShopBinding.inflate(inflater, parent, false))
            VIEW_TYPE_DETAILS_TITLE -> DetailsTitleVH(
                ItemGoodsDetailDetailsTitleBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_DETAIL_IMAGE -> DetailImageVH(
                ItemGoodsDetailDetailImageBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_RECOMMEND_TITLE -> RecommendTitleVH(
                ItemGoodsDetailRecommendTitleBinding.inflate(inflater, parent, false),
            )
            VIEW_TYPE_RECOMMEND_PRODUCT -> RecommendProductVH(
                ItemBrowseHistoryProductBinding.inflate(inflater, parent, false),
            )
            else -> ListFooterVH(ItemGoodsDetailListFooterBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GoodsDetailListItem.PriceMarketing -> (holder as PriceMarketingVH).bind(item.state)
            is GoodsDetailListItem.ProductTitle -> (holder as ProductTitleVH).bind(item.state)
            is GoodsDetailListItem.ServiceSales -> (holder as ServiceSalesVH).bind(item.state)
            is GoodsDetailListItem.AfterSales -> (holder as AfterSalesVH).bind(item.state)
            is GoodsDetailListItem.SpecSelection -> (holder as SpecSelectionVH).bind(item.state)
            is GoodsDetailListItem.PurchaseQuantity -> (holder as PurchaseQuantityVH).bind(item.state)
            is GoodsDetailListItem.SectionDivider -> Unit
            is GoodsDetailListItem.Review -> (holder as ReviewVH).bind(item.state)
            is GoodsDetailListItem.Shop -> (holder as ShopVH).bind(item.state)
            is GoodsDetailListItem.DetailsTitle -> Unit
            is GoodsDetailListItem.DetailImage -> {
                val imageIndex = detailImageUrls.indexOf(item.imageUrl)
                (holder as DetailImageVH).bind(item.imageUrl, imageIndex)
            }
            is GoodsDetailListItem.RecommendProduct -> (holder as RecommendProductVH).bind(item.product)
            is GoodsDetailListItem.RecommendTitle,
            is GoodsDetailListItem.ListFooter,
            -> Unit
        }
    }

    private inner class PriceMarketingVH(
        private val binding: ItemGoodsDetailPriceMarketingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            binding.tvPrice.text = state.priceYuan
            binding.tvOriginalPrice.text = state.originalPriceYuan
            binding.tvOriginalPrice.paint.flags =
                binding.tvOriginalPrice.paint.flags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvStatusTag.text = state.statusTag
            binding.tvStatusTag.isVisible = state.statusTag.isNotBlank()
            binding.tvCouponText.text = state.couponText
            binding.layoutCoupon.isVisible = state.couponText.isNotBlank()
            binding.layoutCoupon.onClick200 { callbacks.onCouponClick() }
        }
    }

    private inner class ProductTitleVH(
        private val binding: ItemGoodsDetailProductTitleBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            binding.tvProductTitle.text = state.title
        }
    }

    private inner class ServiceSalesVH(
        private val binding: ItemGoodsDetailServiceSalesBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            binding.tvRecommendLabel.text = state.recommendLabel
            binding.tvMonthlySales.text = state.monthlySales
            binding.tvShipFrom.text = state.shipFrom
        }
    }

    private inner class AfterSalesVH(
        private val binding: ItemGoodsDetailAfterSalesBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            bindGuaranteeItems(binding.llGuarantees, state.guarantees)
            binding.rowAfterSales.onClick200 { callbacks.onAfterSalesClick() }
        }
    }

    private inner class SpecSelectionVH(
        private val binding: ItemGoodsDetailSpecSelectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            bindSpecChips(
                container = binding.llWeightSpecs,
                specs = state.weightSpecs,
                selectedIndex = state.selectedWeightIndex,
                onSelect = callbacks::onWeightSpecSelected,
            )
            bindSpecChips(
                container = binding.llFlavorSpecs,
                specs = state.flavorSpecs,
                selectedIndex = state.selectedFlavorIndex,
                onSelect = callbacks::onFlavorSpecSelected,
            )
        }
    }

    private inner class PurchaseQuantityVH(
        private val binding: ItemGoodsDetailPurchaseQuantityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailProductSectionState) {
            binding.tvQuantity.text = state.quantity.toString()
            binding.btnMinus.onClick(0) { callbacks.onQuantityMinus() }
            binding.btnPlus.onClick(0) { callbacks.onQuantityPlus() }
        }
    }

    private inner class SectionDividerVH(
        binding: ItemGoodsDetailSectionDividerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            val context = binding.root.context
            val heightPx = SizeUtils.getDimen(context, AppResR.dimen.dp_8)
            itemView.layoutParams = (itemView.layoutParams ?: RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                heightPx,
            )).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = heightPx
            }
        }
    }

    private inner class ReviewVH(
        private val binding: ItemGoodsDetailReviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(state: GoodsDetailReviewState) {
            val context = binding.root.context
            binding.tvReviewTitle.text = context.getString(
                R.string.goods_review_section_title_format,
                state.totalCount,
            )
            binding.tvPositiveRate.text = context.getString(
                R.string.goods_review_positive_rate_format,
                state.positiveRate,
            )
            bindReviewTags(binding.flReviewTags, state.tags)
            val preview = state.preview
            binding.tvReviewUser.text = preview.userName
            if (preview.avatarUrl.isBlank()) {
                binding.ivReviewAvatar.setImageDrawable(null)
            } else {
                binding.ivReviewAvatar.loadNetworkImageCircle(preview.avatarUrl)
            }
            bindReviewStars(binding.llReviewStars, preview.starCount)
            binding.tvReviewContent.text = preview.content
            bindReviewImages(binding.llReviewImages, preview.imageUrls)
            val openReviewList = { callbacks.onRowReviewClick() }
            binding.rowReviewHeader.onClick200 { openReviewList() }
            binding.llViewAllReviews.onClick200 { openReviewList() }
        }
    }

    private inner class ShopVH(
        private val binding: ItemGoodsDetailShopBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundLogoUrl: String? = null

        fun bind(state: GoodsDetailShopState) {
            boundLogoUrl = state.logoUrl
            val context = binding.root.context
            val fallbackStartColor = ContextCompat.getColor(context, AppResR.color.func_gray_bg_2)
            val endColor = ContextCompat.getColor(context, AppResR.color.t_white_1)
            val fallbackBackground = PaletteColorUtils.topToBottomGradient(fallbackStartColor, endColor)
            binding.root.background = fallbackBackground
            binding.tvShopName.text = state.shopName
            val selfTag = state.selfOperatedTag
            binding.tvSelfOperatedTag.isVisible = !selfTag.isNullOrBlank()
            binding.tvSelfOperatedTag.text = selfTag
            binding.tvFollowers.text = context.getString(
                R.string.goods_shop_followers_format,
                state.followerCount,
            )
            binding.tvItemCount.text = context.getString(
                R.string.goods_shop_items_format,
                state.itemCount,
            )
            binding.btnEnterShop.text = state.enterShopText
            if (state.logoUrl.isBlank()) {
                binding.ivShopLogo.setImageDrawable(null)
            } else {
                val logoUrl = state.logoUrl
                binding.ivShopLogo.loadNetworkImage(
                    logoUrl,
                    onError = {
                        if (boundLogoUrl == logoUrl) {
                            binding.root.background = fallbackBackground
                        }
                    },
                    onSuccess = { drawable ->
                        if (boundLogoUrl != logoUrl) return@loadNetworkImage
                        val bitmap = when (drawable) {
                            is BitmapDrawable -> drawable.bitmap
                            else -> drawable.toBitmap()
                        }
                        PaletteColorUtils.computeMutedBackgroundGradient(
                            context.getCoroutineScope(),
                            bitmap = bitmap,
                            endColor = endColor,
                            fallbackStartColor = fallbackStartColor,
                        ) { gradient ->
                            if (boundLogoUrl == logoUrl) {
                                binding.root.background = gradient
                            }
                        }
                    },
                )
            }
            bindShopRating(
                labelView = binding.tvRatingDescLabel,
                scoreView = binding.tvRatingDescScore,
                levelView = binding.tvRatingDescLevel,
                rating = state.ratings.getOrNull(0),
            )
            bindShopRating(
                labelView = binding.tvRatingServiceLabel,
                scoreView = binding.tvRatingServiceScore,
                levelView = binding.tvRatingServiceLevel,
                rating = state.ratings.getOrNull(1),
            )
            bindShopRating(
                labelView = binding.tvRatingLogisticsLabel,
                scoreView = binding.tvRatingLogisticsScore,
                levelView = binding.tvRatingLogisticsLevel,
                rating = state.ratings.getOrNull(2),
            )
            binding.btnEnterShop.onClick300 { callbacks.onEnterShopClick() }
        }

        private fun bindShopRating(
            labelView: TextView,
            scoreView: TextView,
            levelView: TextView,
            rating: GoodsDetailShopRating?,
        ) {
            if (rating == null) {
                labelView.text = ""
                scoreView.text = ""
                levelView.isVisible = false
                return
            }
            labelView.text = rating.label
            scoreView.text = rating.score
            levelView.isVisible = rating.levelLabel.isNotBlank()
            levelView.text = rating.levelLabel
        }
    }

    private inner class DetailsTitleVH(
        binding: ItemGoodsDetailDetailsTitleBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    private inner class DetailImageVH(
        private val binding: ItemGoodsDetailDetailImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String, imageIndex: Int) {
            binding.ivDetailImage.loadNetworkImage(imageUrl)
            binding.ivDetailImage.onClick500 {
                // 详情大图预览待接入
            }
        }
    }

    private inner class RecommendTitleVH(
        binding: ItemGoodsDetailRecommendTitleBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    private inner class RecommendProductVH(
        private val binding: ItemBrowseHistoryProductBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: BrowseProduct) {
            binding.ivProduct.loadNetworkImage(product.imageUrl)
            binding.tvTitle.text = product.title
            val badge = product.badge
            binding.tvTag.isVisible = !badge.isNullOrBlank()
            binding.tvTag.text = badge
            val num = product.price.trim().removePrefix("¥").removePrefix("￥").trim()
            binding.tvPrice.text = if (num.isEmpty()) "—" else num
            binding.tvOriginalPrice.text = product.originalPrice
            binding.tvOriginalPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvSales.text = "销量 ${product.sales}"
            binding.root.onClick500 { callbacks.onRecommendProductClick(product.spuId) }
        }
    }

    private inner class ListFooterVH(
        binding: ItemGoodsDetailListFooterBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    private fun bindGuaranteeItems(container: LinearLayout, guarantees: List<String>) {
        container.removeAllViews()
        val context = container.context
        val iconSizePx = SizeUtils.getDimenFloat(context, AppResR.dimen.dp_12)
        guarantees.forEachIndexed { index, label ->
            if (index > 0) {
                container.addView(
                    TextView(context).apply {
                        text = "  "
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    },
                )
            }
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            row.addView(
                IconFontView(context).apply {
                    text = context.getString(AppResR.string.iconfont_check_outline)
                    setTextColor(ContextCompat.getColor(context, AppResR.color.func_orange_text_1))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, iconSizePx)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                },
            )
            row.addView(
                TextView(context).apply {
                    text = label
                    setTextAppearance(AppResR.style.tv_black_3_size_12)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        marginStart = SizeUtils.getDimen(context, AppResR.dimen.dp_4)
                    }
                },
            )
            container.addView(row)
        }
    }

    private fun bindSpecChips(
        container: LinearLayout,
        specs: List<String>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit,
    ) {
        container.removeAllViews()
        val context = container.context
        val gapPx = SizeUtils.getDimen(context, AppResR.dimen.dp_8)
        val padHPx = SizeUtils.getDimen(context, AppResR.dimen.dp_12)
        val padVPx = SizeUtils.getDimen(context, AppResR.dimen.dp_6)
        specs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val chip = TextView(context).apply {
                text = label
                setPadding(padHPx, padVPx, padHPx, padVPx)
                background = ContextCompat.getDrawable(
                    context,
                    if (selected) {
                        AppResR.drawable.bg_orange_stroke_interact_1
                    } else {
                        AppResR.drawable.bg_gray_fill_interact_1
                    },
                )
                setTextAppearance(
                    if (selected) AppResR.style.tv_orange_1_size_12 else AppResR.style.tv_black_2_size_12,
                )
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    if (index > 0) marginStart = gapPx
                }
            }
            chip.onClick200 { onSelect(index) }
            container.addView(chip)
        }
    }

    private fun bindReviewTags(container: FlexboxLayout, tags: List<GoodsDetailReviewTag>) {
        container.removeAllViews()
        val context = container.context
        val padHPx = SizeUtils.getDimen(context, AppResR.dimen.dp_10)
        val padVPx = SizeUtils.getDimen(context, AppResR.dimen.dp_4)
        val marginEndPx = SizeUtils.getDimen(context, AppResR.dimen.dp_8)
        val marginBottomPx = SizeUtils.getDimen(context, AppResR.dimen.dp_8)
        tags.forEach { tag ->
            val chip = TextView(context).apply {
                text = context.getString(R.string.goods_review_tag_format, tag.label, tag.count)
                setPadding(padHPx, padVPx, padHPx, padVPx)
                background = ContextCompat.getDrawable(context, AppResR.drawable.bg_orange_fill_interact_3)
                setTextAppearance(AppResR.style.tv_orange_1_size_12)
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    setMargins(0, 0, marginEndPx, marginBottomPx)
                }
            }
            container.addView(chip)
        }
    }

    private fun bindReviewStars(container: LinearLayout, count: Int) {
        container.removeAllViews()
        val context = container.context
        val starColor = ContextCompat.getColor(context, AppResR.color.func_orange_text_1)
        val starSizePx = SizeUtils.getDimenFloat(context, AppResR.dimen.dp_12)
        repeat(count.coerceIn(0, 5)) {
            val star = IconFontView(context).apply {
                text = context.getString(AppResR.string.iconfont_star_fill)
                setTextColor(starColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, starSizePx)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
            container.addView(star)
        }
    }

    private fun bindReviewImages(container: LinearLayout, imageUrls: List<String>) {
        container.removeAllViews()
        val urls = imageUrls.filter { it.isNotBlank() }.take(3)
        container.isVisible = urls.isNotEmpty()
        if (urls.isEmpty()) return
        val context = container.context
        val sizePx = SizeUtils.getDimen(context, AppResR.dimen.dp_96)
        val gapPx = SizeUtils.getDimen(context, AppResR.dimen.dp_8)
        val cornerPx = SizeUtils.getDimenFloat(context, AppResR.dimen.dp_8)
        urls.forEachIndexed { index, url ->
            val card = CardView(context).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    if (index > 0) marginStart = gapPx
                }
                radius = cornerPx
                cardElevation = 0f
                setCardBackgroundColor(ContextCompat.getColor(context, AppResR.color.func_gray_bg_1))
            }
            val image = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            }
            image.loadNetworkImage(url)
            card.addView(image)
            container.addView(card)
        }
    }
}
