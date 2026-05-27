package com.joy.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import java.util.concurrent.Executors

object PaletteColorUtils {
    const val MUTED_BACKGROUND_ALPHA = 30

    private val paletteExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun topToBottomGradient(startColor: Int, endColor: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(startColor, endColor),
        )
    }

    fun mutedBackgroundColor(
        bitmap: Bitmap?,
        fallbackColor: Int,
        alpha: Int = MUTED_BACKGROUND_ALPHA,
    ): Int {
        if (bitmap == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            return fallbackColor
        }
        val swatch = Palette.from(bitmap).generate().let { palette ->
            palette.mutedSwatch ?: palette.lightMutedSwatch ?: palette.dominantSwatch
        } ?: return fallbackColor
        return ColorUtils.setAlphaComponent(swatch.rgb, alpha)
    }

    fun computeMutedBackgroundGradient(
        bitmap: Bitmap,
        endColor: Int,
        fallbackStartColor: Int,
        alpha: Int = MUTED_BACKGROUND_ALPHA,
        onResult: (GradientDrawable) -> Unit,
    ) {
        paletteExecutor.execute {
            val startColor = mutedBackgroundColor(bitmap, fallbackStartColor, alpha)
            val drawable = topToBottomGradient(startColor, endColor)
            mainHandler.post { onResult(drawable) }
        }
    }
}
