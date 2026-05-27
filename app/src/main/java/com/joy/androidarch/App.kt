package com.joy.androidarch

import android.app.Application
import android.content.pm.ApplicationInfo
import com.alibaba.android.arouter.launcher.ARouter
import com.joy.common.widgets.IconFontView

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initARouter()
        IconFontView.registerApp(this)
    }

    private fun initARouter() {
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }
}
