package com.joy.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.joy.appres.R
import com.joy.common.utils.dimenPx

class BadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        includeFontPadding = false
        gravity = Gravity.CENTER
        setTextAppearance(R.style.tv_white_1_size_10)
        isVisible = false
    }

    fun setCount(count: Int?) {
        when {
            count != null && count <= 0 -> isVisible = false
            count == null -> showDot()
            else -> showNumber(count)
        }
    }

    private fun showDot() {
        text = ""
        setPadding(0, 0, 0, 0)
        setBackgroundResource(R.drawable.bg_red_shape_circle_1)
        val sizePx = context.dimenPx(R.dimen.dp_6)
        minWidth = sizePx
        minHeight = sizePx
        isVisible = true
    }

    private fun showNumber(count: Int) {
        text = if (count > MAX_COUNT) "$MAX_COUNT+" else count.toString()
        val heightPx = context.dimenPx(R.dimen.dp_14)
        minHeight = heightPx
        if (count > 9) {
            setBackgroundResource(R.drawable.bg_red_shape_rect_1)
            val padHPx = context.dimenPx(R.dimen.dp_4)
            setPadding(padHPx, 0, padHPx, 0)
            minWidth = 0
        } else {
            setBackgroundResource(R.drawable.bg_red_shape_circle_1)
            setPadding(0, 0, 0, 0)
            minWidth = heightPx
        }
        isVisible = true
    }

    private companion object {
        const val MAX_COUNT = 99
    }
}
