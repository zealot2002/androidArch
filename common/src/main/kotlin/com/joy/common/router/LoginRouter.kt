package com.joy.common.router

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.joy.common.livedata.LoginStateLiveData

/**
 * 封装了pending事件，登录成功之后，自动跳转到目的地
 * 使用方法：
 *
loginRouter = LoginRouter(this)

loginRouter.runBLock {
ARouter.getInstance().build(RouterConstants.SHOP_CART).navigation(context)
}
...
 *
 * */
class LoginRouter(private var context: Context) {
    private var pendingBlock: (() -> Unit)? = null

    /**************************************************************************************************/
    init {
        if (context is LifecycleOwner) {
            LoginStateLiveData.observe(context as LifecycleOwner) {
                if (it) {
                    pendingBlock?.invoke()
                    pendingBlock = null
                }
            }
            (context as LifecycleOwner).lifecycle.addObserver(
                LifecycleEventObserver { _: LifecycleOwner?, event: Lifecycle.Event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME, //从登录页面(未登录)回到当前页面时
                        Lifecycle.Event.ON_DESTROY //在登录页面停留很久(当前页被回收)
                        -> {
                            pendingBlock = null
                        }

                        else -> {
                        }
                    }
                }
            )
        }
    }

    /*
    * 登录后再继续block.invoke()的操作
    * block:登录成功后，继续的操作（比如跳其他路由页，或请求接口等操作）
    * */
    fun runBlock(block: () -> Unit) {
        if (context is LifecycleOwner) {
            val isLogin = LoginStateLiveData.value
            if (isLogin == true) {
                block.invoke()
            } else {
                pendingBlock = block
                AppRouter.openLogin(context)
            }
        } else {
            block.invoke()
        }
    }
}