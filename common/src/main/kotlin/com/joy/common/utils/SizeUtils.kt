package com.joy.common.utils

import android.content.Context
import androidx.annotation.DimenRes

fun Context.dimenPx(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)

fun Context.dimenFloat(@DimenRes resId: Int): Float = resources.getDimension(resId)

object SizeUtils {
    fun getDimen(context: Context, @DimenRes resId: Int): Int = context.dimenPx(resId)

    fun getDimenFloat(context: Context, @DimenRes resId: Int): Float = context.dimenFloat(resId)
}
