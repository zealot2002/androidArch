package com.joy.common.widget.popup

import android.app.Activity
import android.view.LayoutInflater
import android.widget.GridView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.joy.appres.R as AppResR
import com.joy.common.R
import com.joy.common.extend.onClick200
import com.joy.common.utils.StatusBarUtils
import com.joy.common.utils.ToastUtils
import com.joy.common.widgets.IconFontView

class QuickMenuPopup(
    private val activity: Activity,
    private val onShowingChanged: ((Boolean) -> Unit)? = null,
) {
    private val popup: TopSlidePopupWindow
    private var savedStatusBarColor: Int? = null
    private var savedLightStatusBars: Boolean? = null
    private var statusBarStyleCaptured = false

    init {
        val root = LayoutInflater.from(activity).inflate(R.layout.layout_quick_menu_popup, null, false)
        val statusBarTop = resolveStatusBarTopOffset(activity)
        root.findViewById<android.view.View>(R.id.llQuickPanel).updatePadding(top = statusBarTop)
        popup = TopSlidePopupWindow(activity, root, dimWindow = false)
        val dismiss = { popup.dismiss() }
        root.findViewById<android.view.View>(R.id.viewDismiss).onClick200 { dismiss() }
        root.findViewById<IconFontView>(R.id.iconClose).onClick200 { dismiss() }
        root.findViewById<GridView>(R.id.gvMenu).adapter =
            QuickMenuAdapter(activity, fixedMenuItems(activity, dismiss))
        popup.setOnDismissListener {
            restoreStatusBarStyle()
            onShowingChanged?.invoke(false)
        }
    }

    fun show() {
        applyStatusBarStyleForMenu()
        onShowingChanged?.invoke(true)
        popup.show(topOffsetPx = 0)
        activity.window.decorView.post { applyStatusBarStyleForMenu() }
    }

    @Suppress("DEPRECATION")
    private fun applyStatusBarStyleForMenu() {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (!statusBarStyleCaptured) {
            savedStatusBarColor = window.statusBarColor
            savedLightStatusBars = controller.isAppearanceLightStatusBars
            statusBarStyleCaptured = true
        }
        window.statusBarColor = ContextCompat.getColor(activity, AppResR.color.func_gray_bg_3)
        controller.isAppearanceLightStatusBars = true
    }

    @Suppress("DEPRECATION")
    private fun restoreStatusBarStyle() {
        if (!statusBarStyleCaptured) return
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        savedStatusBarColor?.let { window.statusBarColor = it }
        savedLightStatusBars?.let { controller.isAppearanceLightStatusBars = it }
        savedStatusBarColor = null
        savedLightStatusBars = null
        statusBarStyleCaptured = false
    }

    private companion object {
        private const val DEMO_MESSAGE_BADGE_COUNT = 33

        fun resolveStatusBarTopOffset(activity: Activity): Int {
            val decorView = activity.window.decorView
            val insetTop = ViewCompat.getRootWindowInsets(decorView)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())
                ?.top
            return insetTop?.takeIf { it > 0 } ?: StatusBarUtils.getStatusBarHeight(activity)
        }

        fun fixedMenuItems(activity: Activity, dismiss: () -> Unit): List<QuickMenuBean> = listOf(
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_message),
                title = activity.getString(AppResR.string.quick_menu_message),
                badgeCount = DEMO_MESSAGE_BADGE_COUNT,
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_message))
            },
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_home),
                title = activity.getString(AppResR.string.quick_menu_home),
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_home))
            },
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_profile),
                title = activity.getString(AppResR.string.quick_menu_mine),
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_mine))
            },
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_cart),
                title = activity.getString(AppResR.string.quick_menu_cart),
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_cart))
            },
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_feedback),
                title = activity.getString(AppResR.string.quick_menu_feedback),
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_feedback))
            },
            QuickMenuBean(
                iconText = activity.getString(AppResR.string.iconfont_search),
                title = activity.getString(AppResR.string.quick_menu_search),
            ) {
                dismiss()
                ToastUtils.show(activity, activity.getString(AppResR.string.quick_menu_search))
            },
        )
    }
}
