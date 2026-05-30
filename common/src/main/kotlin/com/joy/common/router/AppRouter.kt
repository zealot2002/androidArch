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

    /** 商品详情海报 */
    fun openGoodsBill(context: Context?, goodsId: String) {
        openBill(context, goodsId, RouterConstants.BILL_CASE_GOODS)
    }

    /** 社交详情海报 */
    fun openSocialBill(context: Context?, postId: String) {
        openBill(context, postId, RouterConstants.BILL_CASE_SOCIAL)
    }

    /** 店铺首页海报 */
    fun openShopBill(context: Context?, shopId: String) {
        openBill(context, shopId, RouterConstants.BILL_CASE_SHOP)
    }

    fun openShopHome(context: Context?, shopId: String = "mock-shop") {
        val id = shopId.trim()
        if (id.isEmpty()) return
        ARouter.getInstance()
            .build(RouterConstants.SHOP_HOME)
            .withString(RouterConstants.EXTRA_SHOP_ID, id)
            .navigation(context)
    }

    private fun openBill(context: Context?, billId: String, billCase: Int) {
        val id = billId.trim()
        if (id.isEmpty()) return
        ARouter.getInstance()
            .build(RouterConstants.BILL_MAIN)
            .withInt(RouterConstants.EXTRA_BILL_CASE, billCase)
            .withString(RouterConstants.EXTRA_BILL_ID, id)
            .navigation(context)
    }

    fun openSocialDetail(context: Context?, postId: String = "mock-social-post") {
        val id = postId.trim()
        if (id.isEmpty()) return
        ARouter.getInstance()
            .build(RouterConstants.SOCIAL_DETAIL)
            .withString(RouterConstants.EXTRA_SOCIAL_POST_ID, id)
            .navigation(context)
    }
}
