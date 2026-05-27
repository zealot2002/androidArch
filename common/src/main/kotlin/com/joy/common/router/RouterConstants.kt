package com.joy.common.router

/**
 * ARouter path 与 Intent 参数名。业务跳转请使用 [AppRouter]。
 */
object RouterConstants {
    const val GOODS_DETAIL = "/goods/detail"
    const val LOGIN_MAIN = "/login/main"

    /** 商品 SPU（详情页查询主键） */
    const val EXTRA_GOODS_SPU_ID = "goods_spu_id"
}
