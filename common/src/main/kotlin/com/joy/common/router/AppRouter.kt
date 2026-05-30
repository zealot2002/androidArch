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

    fun openHome(context: Context?) {
        ARouter.getInstance()
            .build(RouterConstants.HOME_MAIN)
            .navigation(context)
    }

    fun openConfirmOrder(context: Context?) {
        ARouter.getInstance()
            .build(RouterConstants.ORDER_CONFIRM)
            .navigation(context)
    }

    /** 打开海报页（bill = poster） */
    fun openBill(context: Context?, spuId: String) {
        val id = spuId.trim()
        if (id.isEmpty()) return
        ARouter.getInstance()
            .build(RouterConstants.BILL_MAIN)
            .withString(RouterConstants.EXTRA_GOODS_SPU_ID, id)
            .navigation(context)
    }
}
