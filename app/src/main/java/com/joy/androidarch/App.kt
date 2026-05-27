package com.joy.androidarch

import android.app.Application
import com.joy.common.widgets.IconFontView

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        IconFontView.registerApp(this)
    }
}
