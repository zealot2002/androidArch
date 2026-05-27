package com.joy.androidarch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joy.featuregoods.ui.GoodsDetailActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 直接跳转到商品详情页
        val intent = Intent(this, GoodsDetailActivity::class.java)
        intent.putExtra("spuId", "mock-salmon")
        startActivity(intent)
        finish()
    }
}