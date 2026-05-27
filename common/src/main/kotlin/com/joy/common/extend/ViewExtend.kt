package com.joy.common.extend

import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap

fun View.onClick(intervalTime: Int = 0, block: (View) -> Unit) {
    setOnClickListener { v ->
        val clickInterval = clickMap[v]
        val currentClickTime = SystemClock.elapsedRealtime()
        if (clickInterval != null && currentClickTime - clickInterval.lastTime <= clickInterval.intervalTime) {
            return@setOnClickListener
        }
        if (intervalTime > 0) {
            clickMap[v] = ClickInterval(intervalTime, currentClickTime)
        }
        block(v)
    }
}

fun View.onClick200(block: (View) -> Unit) = onClick(200, block)

fun View.onClick300(block: (View) -> Unit) = onClick(300, block)

fun View.onClick500(block: (View) -> Unit) = onClick(500, block)

private data class ClickInterval(
    val intervalTime: Int,
    val lastTime: Long,
)

private val clickMap = WeakHashMap<View, ClickInterval>()
