package com.joy.featurebill.bill.render

import android.content.Context
import android.view.ViewTreeObserver
import com.joy.common.image.loadNetworkImage
import com.joy.featurebill.bill.BaseBillRender
import com.joy.featurebill.bill.BillRender
import com.joy.featurebill.bill.model.GoodsBillData
import com.joy.featurebill.databinding.LayoutBillGoodsBinding

class GoodsBillRender(context: Context) :
    BaseBillRender<GoodsBillData, LayoutBillGoodsBinding>(context) {

    private var readyCount = 0
    private var listener: BillRender.Listener? = null

    override fun onRenderView(
        data: GoodsBillData,
        binding: LayoutBillGoodsBinding,
        listener: BillRender.Listener,
    ) {
        this.listener = listener
        readyCount = 0
        binding.tvTitle.text = data.title
        binding.tvPrice.text = data.price
        binding.cardRoot.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.cardRoot.viewTreeObserver.removeOnPreDrawListener(this)
                markReady()
                return true
            }
        })
        binding.ivCover.loadNetworkImage(
            data = data.imageUrl,
            onSuccess = { markReady() },
            onError = { markReady() },
        )
    }

    private fun markReady() {
        if (++readyCount < READY_TOTAL) return
        getBillView().postDelayed({ listener?.screenReady() }, SCREEN_READY_DELAY_MS)
    }

    companion object {
        private const val READY_TOTAL = 2
        private const val SCREEN_READY_DELAY_MS = 100L
    }
}
