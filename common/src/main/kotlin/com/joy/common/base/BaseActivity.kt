package com.joy.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        initObserver()
    }

    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initObserver()

    protected inline fun <reified T : ViewModel> getViewModel(noinline factory: (() -> T)? = null): T {
        return if (factory != null) {
            ViewModelProvider(this, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory() as T
                }
            })[T::class.java]
        } else {
            ViewModelProvider(this)[T::class.java]
        }
    }
}