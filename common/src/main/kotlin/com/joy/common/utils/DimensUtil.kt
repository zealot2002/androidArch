package com.joy.common.utils

import android.content.Context
import com.joy.appres.R
import kotlin.math.roundToInt

object DimensUtil {
    private var dp1 = 0f
    private val map = mutableMapOf<Float, Float>()

    fun dimen(context: Context, i: Float): Int = dimenF(context, i).roundToInt()

    fun dimen(context: Context, i: Int): Int = dimen(context, i.toFloat())

    fun dimenF(context: Context, i: Float): Float {
        if (!map.containsKey(i)) {
            map[i] = getDimenFloat(context, i)
        }
        return map[i]!!
    }

    private fun getDimenFloat(context: Context, i: Float): Float {
        if (dp1 == 0f) {
            dp1 = context.dimenFloat(R.dimen.dp_1)
        }
        return i * dp1
    }
}
