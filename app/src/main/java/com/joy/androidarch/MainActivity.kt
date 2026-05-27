package com.joy.androidarch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joy.common.router.AppRouter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRouter.openGoodsDetail(this, spuId = "mock-salmon")
        finish()
    }
}
