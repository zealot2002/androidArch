package com.joy.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.joy.common.databinding.ViewScrollToTopFloatBinding
import com.joy.common.extend.onClick200

class ScrollToTopFloatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewScrollToTopFloatBinding.inflate(
        LayoutInflater.from(context),
        this,
        true,
    )

    private var onTopClickListener: (() -> Unit)? = null

    init {
        visibility = INVISIBLE
        binding.iconTop.onClick200 {
            onTopClickListener?.invoke()
        }
    }

    fun setOnTopClickListener(listener: () -> Unit) {
        onTopClickListener = listener
    }

    fun show() {
        if (visibility != VISIBLE) {
            visibility = VISIBLE
        }
    }

    fun hide() {
        if (visibility != INVISIBLE) {
            visibility = INVISIBLE
        }
    }
}
