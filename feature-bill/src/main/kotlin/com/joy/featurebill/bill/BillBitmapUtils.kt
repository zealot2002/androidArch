package com.joy.featurebill.bill

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

object BillBitmapUtils {

    /** 海报设计宽度，与 layout 中 cardRoot / flBillContainer 一致 */
    private const val BILL_WIDTH_DP = 300

    /**
     * 将 View 转为 Bitmap。
     *
     * 不能直接用 [View.width] + [View.draw]：海报 View 挂在 invisible 容器里时测量可能不稳定，
     * 导致同 sp 文字在不同海报上像素占比不同。参照 kfz [ConvertUtils.view2Bitmap]，
     * 先按固定宽度 remeasure / layout，再绘制，并写入 [Bitmap.density]。
     */
    fun viewToBitmap(view: View): Bitmap {
        val densityDpi = view.resources.displayMetrics.densityDpi
        val widthPx = (BILL_WIDTH_DP * view.resources.displayMetrics.density + 0.5f).toInt()

        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)

        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        require(measuredWidth > 0 && measuredHeight > 0) {
            "Bill view size is zero, wait for layout before capture."
        }
        view.layout(0, 0, measuredWidth, measuredHeight)

        return Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).also { bitmap ->
            bitmap.density = densityDpi
            Canvas(bitmap).apply { view.draw(this) }
        }
    }
}
