package com.joy.common.extend

import android.app.Activity

//可以进行ui操作
fun Activity?.isAvailableForUi(): Boolean {
    return this != null && !isFinishing && !isDestroyed && window != null && window.decorView.windowToken != null
}
