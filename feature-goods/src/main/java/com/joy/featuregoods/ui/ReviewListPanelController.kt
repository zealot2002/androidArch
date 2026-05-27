package com.joy.featuregoods.ui

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.joy.featuregoods.model.GoodsDetailReviewState

class ReviewListPanelController(
    private val activity: FragmentActivity,
    private val panelRoot: View,
    private val scrim: View,
    private val panel: View,
    private val fragmentContainerId: Int,
) {

    private val panelWidthPx: Int =
        (activity.resources.displayMetrics.widthPixels * PANEL_WIDTH_RATIO).toInt()

    private val interpolator = FastOutSlowInInterpolator()

    var onVisibilityChanged: ((Boolean) -> Unit)? = null

    private var visible = false

    init {
        (panel.layoutParams as? FrameLayout.LayoutParams)?.apply {
            width = panelWidthPx
            gravity = Gravity.END
            panel.layoutParams = this
        }
        panel.translationX = panelWidthPx.toFloat()
        scrim.setOnClickListener { hide() }
        panelRoot.visibility = View.GONE
        panelRoot.alpha = 1f
    }

    fun isVisible(): Boolean = visible

    fun show(reviewState: GoodsDetailReviewState) {
        if (visible) {
            findFragment()?.render(reviewState)
            return
        }
        ensureFragment(reviewState)
        applyPanelInsets()
        visible = true
        onVisibilityChanged?.invoke(true)
        panelRoot.visibility = View.VISIBLE
        panelRoot.alpha = 0f
        panel.translationX = panelWidthPx.toFloat()
        panelRoot.animate()
            .alpha(1f)
            .setDuration(SCRIM_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
        panel.animate()
            .translationX(0f)
            .setDuration(PANEL_DURATION_MS)
            .setInterpolator(interpolator)
            .start()
    }

    fun hide() {
        if (!visible) return
        visible = false
        onVisibilityChanged?.invoke(false)
        panel.animate()
            .translationX(panelWidthPx.toFloat())
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

    private fun ensureFragment(reviewState: GoodsDetailReviewState) {
        val manager = activity.supportFragmentManager
        val existing = manager.findFragmentById(fragmentContainerId) as? GoodsReviewListFragment
        if (existing != null) {
            existing.onCloseClick = { hide() }
            existing.render(reviewState)
            return
        }
        val fragment = GoodsReviewListFragment.newInstance().apply {
            onCloseClick = { hide() }
        }
        manager.beginTransaction()
            .replace(fragmentContainerId, fragment)
            .commitNow()
        fragment.render(reviewState)
    }

    private fun findFragment(): GoodsReviewListFragment? {
        return activity.supportFragmentManager.findFragmentById(fragmentContainerId) as? GoodsReviewListFragment
    }

    private fun applyPanelInsets() {
        val insets = ViewCompat.getRootWindowInsets(panelRoot) ?: return
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        panel.updatePadding(top = top, bottom = bottom)
    }

    companion object {
        private const val PANEL_WIDTH_RATIO = 0.88f
        private const val PANEL_DURATION_MS = 280L
        private const val SCRIM_DURATION_MS = 220L
    }
}
