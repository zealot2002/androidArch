package com.joy.common.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.app.Application
import androidx.appcompat.widget.AppCompatTextView

class IconFontView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatTextView(context, attributeSet) {

    init {
        if (typeface.isBold) {
            typeface = Typeface.DEFAULT
        }
        this.typeface = globalIconFont
        includeFontPadding = false
    }

    companion object {
        private var globalApp: Application? = null

        fun registerApp(app: Application) {
            globalApp = app
        }

        val globalIconFont: Typeface by lazy {
            val app = globalApp ?: return@lazy Typeface.DEFAULT
            runCatching {
                Typeface.createFromAsset(app.assets, "iconfont.ttf")
            }.getOrElse { Typeface.DEFAULT }
        }
    }
}
