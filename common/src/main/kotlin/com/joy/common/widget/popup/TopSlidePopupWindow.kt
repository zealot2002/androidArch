package com.joy.common.widget.popup

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

class TopSlidePopupWindow(
    private val activity: Activity,
    contentView: View,
    private val dimWindow: Boolean = true,
) {
    private var onDismissListener: PopupWindow.OnDismissListener? = null

    private val popup = PopupWindow(
        contentView,
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
        true,
    ).apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isClippingEnabled = false
        setOnDismissListener {
            restoreWindowAlpha()
            onDismissListener?.onDismiss()
        }
    }

    val isShowing: Boolean
        get() = popup.isShowing

    fun show(topOffsetPx: Int = 0) {
        showAtOffset(topOffsetPx)
    }

    fun dismiss() {
        if (popup.isShowing) {
            popup.dismiss()
        }
    }

    fun setOnDismissListener(listener: PopupWindow.OnDismissListener) {
        onDismissListener = listener
    }

    private fun showAtOffset(offsetY: Int) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (dimWindow) {
            setWindowAlpha(WINDOW_DIM_ALPHA)
        }
        popup.showAtLocation(activity.window.decorView, Gravity.TOP, 0, offsetY)
    }

    private fun restoreWindowAlpha() {
        if (dimWindow) {
            setWindowAlpha(1f)
        }
    }

    private fun setWindowAlpha(alpha: Float) {
        val window = activity.window ?: return
        val params = window.attributes
        params.alpha = alpha
        window.attributes = params
    }

    private companion object {
        const val WINDOW_DIM_ALPHA = 0.6f
    }
}
