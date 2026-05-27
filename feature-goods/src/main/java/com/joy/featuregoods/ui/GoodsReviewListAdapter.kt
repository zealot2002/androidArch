package com.joy.featuregoods.ui

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.joy.appres.R as AppResR
import com.joy.common.image.loadNetworkImage
import com.joy.common.image.loadNetworkImageCircle
import com.joy.common.utils.SizeUtils
import com.joy.common.widgets.IconFontView
import com.joy.featuregoods.R
import com.joy.featuregoods.databinding.ItemGoodsReviewListRowBinding
import com.joy.featuregoods.model.GoodsDetailReviewListItem
import com.joy.featuregoods.model.GoodsDetailReviewTag

class GoodsReviewListAdapter : RecyclerView.Adapter<GoodsReviewListAdapter.ReviewRowVH>() {

    private val items = mutableListOf<GoodsDetailReviewListItem>()

    fun submitList(data: List<GoodsDetailReviewListItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewRowVH {
        val binding = ItemGoodsReviewListRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ReviewRowVH(binding)
    }

    override fun onBindViewHolder(holder: ReviewRowVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ReviewRowVH(
        private val binding: ItemGoodsReviewListRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GoodsDetailReviewListItem) {
            binding.tvReviewUser.text = item.userName
            if (item.avatarUrl.isBlank()) {
                binding.ivReviewAvatar.setImageDrawable(null)
            } else {
                binding.ivReviewAvatar.loadNetworkImageCircle(item.avatarUrl)
            }
            bindReviewStars(binding.llReviewStars, item.starCount)
            binding.tvReviewContent.text = item.content
            bindReviewImages(binding.llReviewImages, item.imageUrls)
            val meta = buildMetaLabel(item.specLabel, item.timeLabel)
            binding.tvReviewMeta.isVisible = meta.isNotBlank()
            binding.tvReviewMeta.text = meta
        }

        private fun buildMetaLabel(specLabel: String, timeLabel: String): String {
            return when {
                specLabel.isNotBlank() && timeLabel.isNotBlank() -> "$specLabel · $timeLabel"
                specLabel.isNotBlank() -> specLabel
                timeLabel.isNotBlank() -> timeLabel
                else -> ""
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

    companion object {
        fun bindReviewTags(container: FlexboxLayout, tags: List<GoodsDetailReviewTag>) {
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
    }
}
