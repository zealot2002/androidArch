package com.joy.common.extend

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 从Context安全获取CoroutineScope
 * @return 绑定了对应生命周期的协程作用域
 * @throws IllegalArgumentException 如果Context无法转换为LifecycleOwner且不是Application
 */
fun Context.getCoroutineScope(): CoroutineScope {
    // 1. 递归解包ContextWrapper，找到真正的LifecycleOwner
    var context = this
    while (context is ContextWrapper) {
        if (context is LifecycleOwner) {
            return context.lifecycleScope
        }
        context = context.baseContext
    }

    // 2. 如果是Application上下文，返回全局安全Scope
    if (context.applicationContext === context) {
        return GlobalAppScope
    }

    // 3. 其他情况抛出异常（避免创建无生命周期绑定的匿名Scope）
    throw IllegalArgumentException(
        "Context $context is not a LifecycleOwner or Application. " +
                "Cannot get a safe CoroutineScope."
    )
}

/**
 * 全局应用级协程作用域（替代GlobalScope）
 * 用于不需要绑定页面生命周期的全局操作
 */
val GlobalAppScope: CoroutineScope = CoroutineScope(
    SupervisorJob() + Dispatchers.Main.immediate
)