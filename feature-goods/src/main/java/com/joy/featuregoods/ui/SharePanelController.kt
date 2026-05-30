package com.joy.featuregoods.ui

import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class SharePanelController(
    private val panelRoot: View,
    private val scrim: View,
    private val panel: View,
    cancelView: View,
) {

    private val interpolator = FastOutSlowInInterpolator()

    var onVisibilityChanged: ((Boolean) -> Unit)? = null

    private var visible = false

    init {
        val dismiss = { hide() }
        scrim.setOnClickListener { dismiss() }
        cancelView.setOnClickListener { dismiss() }
        panelRoot.visibility = View.GONE
        panelRoot.alpha = 1f
        panel.post { panel.translationY = panel.height.toFloat().coerceAtLeast(1f) }
    }

    fun isVisible(): Boolean = visible

    fun show() {
        if (visible) return
        visible = true
        onVisibilityChanged?.invoke(true)
        panelRoot.visibility = View.VISIBLE
        panelRoot.alpha = 0f
        val slideDistance = panel.height.toFloat().coerceAtLeast(1f)
        panel.translationY = slideDistance
        panelRoot.animate()
            .alpha(1f)
            .setDuration(SCRIM_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
        panel.animate()
            .translationY(0f)
            .setDuration(PANEL_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
    }

    fun hide() {
        if (!visible) return
        visible = false
        onVisibilityChanged?.invoke(false)
        val slideDistance = panel.height.toFloat().coerceAtLeast(1f)
        panel.animate()
            .translationY(slideDistance)
            .setDuration(PANEL_DURATION_MS)
            .setInterpolator(interpolator)
            .withEndAction {
                if (!visible) {
                    panelRoot.visibility = View.GONE
                }
            }
            .start()
        panelRoot.animate()
            .alpha(0f)
            .setDuration(SCRIM_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
    }

    companion object {
        private const val PANEL_DURATION_MS = 280L
        private const val SCRIM_DURATION_MS = 220L
    }
}
