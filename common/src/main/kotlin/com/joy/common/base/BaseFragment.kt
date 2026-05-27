package com.joy.common.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initData()
        initObserver()
    }

    protected abstract fun initView(view: View)
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