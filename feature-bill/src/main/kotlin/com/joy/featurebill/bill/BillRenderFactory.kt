package com.joy.featurebill.bill

import android.content.Context
import com.joy.common.router.RouterConstants
import com.joy.featurebill.bill.render.GoodsBillRender
import com.joy.featurebill.bill.render.SocialBillRender

object BillRenderFactory {

    fun make(context: Context, case: Int): BillRender {
        return when (case) {
            RouterConstants.BILL_CASE_SOCIAL -> SocialBillRender(context)
            else -> GoodsBillRender(context)
        }
    }
}
