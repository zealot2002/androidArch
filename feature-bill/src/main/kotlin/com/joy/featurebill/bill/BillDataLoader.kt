package com.joy.featurebill.bill

import com.joy.common.router.RouterConstants
import com.joy.featurebill.bill.model.GoodsBillData
import com.joy.featurebill.bill.model.SocialBillData

object BillDataLoader {

    fun load(case: Int, id: String): Any {
        return when (case) {
            RouterConstants.BILL_CASE_SOCIAL -> SocialBillData(
                postId = id,
                nickname = "书友小明",
                content = "今天读到了一本很好的架构书，推荐给大家。",
                imageUrl = "https://picsum.photos/seed/social-$id/600/400",
            )
            else -> GoodsBillData(
                spuId = id,
                title = "挪威三文鱼刺身 200g",
                price = "¥68.00",
                imageUrl = "https://picsum.photos/seed/goods-$id/600/400",
            )
        }
    }
}
