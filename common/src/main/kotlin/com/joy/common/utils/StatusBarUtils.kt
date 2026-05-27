package com.joy.common.utils

import android.content.Context
import android.content.res.Resources

object StatusBarUtils {
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            (24 * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
