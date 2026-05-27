package com.joy.featuregoods.ui

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joy.common.utils.DimensUtil

class RecommendGridSpacingDecoration(
    private val context: Context,
    private val detailAdapter: GoodsDetailAdapter,
) : RecyclerView.ItemDecoration() {

    private val cellSpacingPx = DimensUtil.dimen(context, 5)
    private val edgeInsetPx = DimensUtil.dimen(context, 14)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        if (detailAdapter.getItemViewType(position) != GoodsDetailAdapter.VIEW_TYPE_RECOMMEND_PRODUCT) {
            return
        }
        val lp = view.layoutParams as? GridLayoutManager.LayoutParams ?: return
        outRect.top = cellSpacingPx
        outRect.bottom = cellSpacingPx
        when (lp.spanIndex) {
            0 -> {
                outRect.left = edgeInsetPx
                outRect.right = cellSpacingPx
            }
            else -> {
                outRect.left = cellSpacingPx
                outRect.right = edgeInsetPx
            }
        }
    }
}
