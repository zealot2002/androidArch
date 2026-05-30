package com.joy.featurebill.bill

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * 海报渲染模版：子类只实现 [onRenderView]，在布局绘制完成且异步资源就绪后回调 [BillRender.Listener.screenReady]。
 */
abstract class BaseBillRender<T, B : ViewBinding>(private val context: Context) : BillRender {

    private lateinit var binding: B

    abstract fun onRenderView(
        data: T,
        binding: B,
        listener: BillRender.Listener,
    )

    init {
        val superclass = javaClass.genericSuperclass as ParameterizedType
        @Suppress("UNCHECKED_CAST")
        val bindingClass = superclass.actualTypeArguments[1] as Class<B>
        val inflate = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = inflate.invoke(null, (context as Activity).layoutInflater) as B
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindView(data: Any, listener: BillRender.Listener) {
        onRenderView(data as T, binding, listener)
    }

    override fun getBillView(): View = binding.root
}
