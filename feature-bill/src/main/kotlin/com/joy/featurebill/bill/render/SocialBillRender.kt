package com.joy.featurebill.bill.render

import android.content.Context
import android.view.ViewTreeObserver
import com.joy.common.image.loadNetworkImage
import com.joy.common.image.loadNetworkImageCircle
import com.joy.featurebill.bill.BaseBillRender
import com.joy.featurebill.bill.BillRender
import com.joy.featurebill.bill.model.SocialBillData
import com.joy.featurebill.databinding.LayoutBillSocialBinding

class SocialBillRender(context: Context) :
    BaseBillRender<SocialBillData, LayoutBillSocialBinding>(context) {

    private var readyCount = 0
    private var listener: BillRender.Listener? = null

    override fun onRenderView(
        data: SocialBillData,
        binding: LayoutBillSocialBinding,
        listener: BillRender.Listener,
    ) {
        this.listener = listener
        readyCount = 0
        binding.tvNickname.text = data.nickname
        binding.tvContent.text = data.content
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
        binding.ivQrCode.loadNetworkImageCircle(
            data = data.miniProgramCodeUrl,
            onSuccess = { markReady() },
            onError = { markReady() },
        )
    }

    private fun markReady() {
        if (++readyCount < READY_TOTAL) return
        getBillView().postDelayed({ listener?.screenReady() }, SCREEN_READY_DELAY_MS)
    }

    companion object {
        private const val READY_TOTAL = 3
        private const val SCREEN_READY_DELAY_MS = 100L
    }
}
