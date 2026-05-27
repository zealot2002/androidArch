package com.joy.common.widgets.recyclerview

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joy.common.utils.DimensUtil

class GridSpacingDecoration(
    private val context: Context,
    private val cellSpacingDp: Int = 5,
    private val edgeInsetDp: Int = 14,
    private val targetViewType: Int? = null,
) : RecyclerView.ItemDecoration() {

    private val cellSpacingPx by lazy { DimensUtil.dimen(context, cellSpacingDp) }
    private val edgeInsetPx by lazy { DimensUtil.dimen(context, edgeInsetDp) }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        
        if (targetViewType != null) {
            val adapter = parent.adapter ?: return
            if (adapter.getItemViewType(position) != targetViewType) {
                return
            }
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
