package com.joy.common.router

/**
 * ARouter path 与 Intent 参数名。业务跳转请使用 [AppRouter]。
 */
object RouterConstants {
    const val GOODS_DETAIL = "/goods/detail"
    const val LOGIN_MAIN = "/login/main"
    const val HOME_MAIN = "/home/main"
    const val ORDER_CONFIRM = "/order/confirm"
    /** 海报页（bill = poster） */
    const val BILL_MAIN = "/bill/main"
    const val SOCIAL_DETAIL = "/social/detail"
    const val SHOP_HOME = "/shop/home"

    /** 商品 SPU（详情页查询主键） */
    const val EXTRA_GOODS_SPU_ID = "goods_spu_id"
    const val EXTRA_SOCIAL_POST_ID = "social_post_id"
    const val EXTRA_SHOP_ID = "shop_id"

    /** 海报业务类型（bill = poster） */
    const val EXTRA_BILL_CASE = "bill_case"
    const val EXTRA_BILL_ID = "bill_id"
    const val BILL_CASE_GOODS = 1
    const val BILL_CASE_SOCIAL = 2
    const val BILL_CASE_SHOP = 3
}
