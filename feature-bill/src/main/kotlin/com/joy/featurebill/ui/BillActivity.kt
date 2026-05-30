package com.joy.featurebill.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.router.RouterConstants
import com.joy.featurebill.bill.BillBitmapUtils
import com.joy.featurebill.bill.BillDataLoader
import com.joy.featurebill.bill.BillRender
import com.joy.featurebill.bill.BillRenderFactory
import com.joy.featurebill.databinding.ActivityBillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 海报页：固定截屏工作流（模版方法在 [BillRender] / [com.joy.featurebill.bill.BaseBillRender]），
 * 具体业务由 Goods / Social 等 Render 子类填充内容并在就绪后回调 [BillRender.Listener.screenReady]。
 */
@Route(path = RouterConstants.BILL_MAIN)
class BillActivity : BaseActivity() {

    private lateinit var binding: ActivityBillBinding
    private lateinit var billRender: BillRender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)

        val billCase = intent.getIntExtra(RouterConstants.EXTRA_BILL_CASE, RouterConstants.BILL_CASE_GOODS)
        val billId = intent.getStringExtra(RouterConstants.EXTRA_BILL_ID).orEmpty().ifBlank { "mock" }

        billRender = BillRenderFactory.make(this, billCase)
        binding.flBillContainer.addView(
            billRender.getBillView(),
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )

        val billData = BillDataLoader.load(billCase, billId)
        billRender.onBindView(billData, object : BillRender.Listener {
            override fun screenReady(bgBitmap: android.graphics.Bitmap?) {
                captureBillSnapshot()
            }
        })
    }

    private fun captureBillSnapshot() {
        val billView = billRender.getBillView()
        billView.post {
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    BillBitmapUtils.viewToBitmap(billView)
                }
                binding.ivPreview.setImageBitmap(bitmap)
                binding.ivPreview.visibility = View.VISIBLE
                binding.progressLoading.visibility = View.GONE
                binding.tvLoadingTips.visibility = View.GONE
                binding.flBillContainer.visibility = View.GONE
            }
        }
    }
}
