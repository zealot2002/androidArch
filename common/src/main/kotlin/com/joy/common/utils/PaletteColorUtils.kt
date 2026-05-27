package com.joy.common.utils

import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object PaletteColorUtils {
    const val MUTED_BACKGROUND_ALPHA = 30

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
        lifecycleScope: CoroutineScope,
        bitmap: Bitmap,
        endColor: Int,
        fallbackStartColor: Int,
        alpha: Int = MUTED_BACKGROUND_ALPHA,
        onResult: (GradientDrawable) -> Unit,
    ) {
        lifecycleScope.launch {
            val startColor = withContext(Dispatchers.IO) {
                mutedBackgroundColor(bitmap, fallbackStartColor, alpha)
            }
            val drawable = topToBottomGradient(startColor, endColor)
            onResult(drawable)
        }
    }
}
