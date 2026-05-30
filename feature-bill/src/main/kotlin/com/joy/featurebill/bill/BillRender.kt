package com.joy.featurebill.bill

import android.graphics.Bitmap
import android.view.View

interface BillRender {
    fun onBindView(data: Any, listener: Listener)
    fun getBillView(): View

    interface Listener {
        fun screenReady(bgBitmap: Bitmap? = null)
    }
}
