package com.joy.featurebill.bill

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

object BillBitmapUtils {

    fun viewToBitmap(view: View): Bitmap {
        val width = view.width
        val height = view.height
        require(width > 0 && height > 0) { "Bill view size is zero, wait for layout before capture." }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            Canvas(bitmap).apply { view.draw(this) }
        }
    }
}
