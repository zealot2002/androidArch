package com.joy.common.router

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter

object AppRouter {

    fun openLogin(context: Context?) {
        ARouter.getInstance()
            .build(RouterConstants.LOGIN_MAIN)
            .navigation(context)
    }

    fun openGoodsDetail(context: Context?, spuId: String) {
        val id = spuId.trim()
        if (id.isEmpty()) return
        ARouter.getInstance()
            .build(RouterConstants.GOODS_DETAIL)
            .withString(RouterConstants.EXTRA_GOODS_SPU_ID, id)
            .navigation(context)
    }
}
