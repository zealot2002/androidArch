package com.joy.featurebill.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.router.RouterConstants
import com.joy.common.utils.ToastUtils
import com.joy.featurebill.R
import com.joy.featurebill.bill.BillBitmapUtils
import com.joy.featurebill.bill.BillDataLoader
import com.joy.featurebill.bill.BillImageSaver
import com.joy.featurebill.bill.BillRender
import com.joy.featurebill.bill.BillRenderFactory
import com.joy.featurebill.databinding.ActivityBillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 海报页：固定截屏工作流（模版方法在 [BillRender] / [BaseBillRender]），
 * 保存图片、分享入口等交互在 Activity 层处理，Render 只负责内容渲染与 screenReady。
 */
@Route(path = RouterConstants.BILL_MAIN)
class BillActivity : BaseActivity() {

    private lateinit var binding: ActivityBillBinding
    private lateinit var billRender: BillRender
    private var billBitmap: Bitmap? = null
    private var savedToAlbum = false
    private var billCase = RouterConstants.BILL_CASE_GOODS

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityBillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)

        billCase = intent.getIntExtra(RouterConstants.EXTRA_BILL_CASE, RouterConstants.BILL_CASE_GOODS)
        val billId = intent.getStringExtra(RouterConstants.EXTRA_BILL_ID).orEmpty().ifBlank { "mock" }

        initShareTips()
        initClickListeners()

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
            override fun screenReady(bgBitmap: Bitmap?) {
                captureBillSnapshot()
            }
        })
    }

    private fun initShareTips() {
        val tipsRes = when (billCase) {
            RouterConstants.BILL_CASE_SOCIAL -> R.string.bill_share_tips_social
            RouterConstants.BILL_CASE_SHOP -> R.string.bill_share_tips_shop
            else -> R.string.bill_share_tips_goods
        }
        binding.tvShareTips.setText(tipsRes)
    }

    private fun initClickListeners() {
        binding.vScrim.setOnClickListener { finish() }
        binding.tvCancel.setOnClickListener { finish() }
        binding.llSave.setOnClickListener { saveFile() }
        binding.llWx.setOnClickListener {
            ToastUtils.show(this, getString(R.string.bill_wechat))
        }
        binding.llCircle.setOnClickListener {
            ToastUtils.show(this, getString(R.string.bill_moments))
        }
    }

    private fun captureBillSnapshot() {
        val billView = billRender.getBillView()
        billView.post {
            // measure / draw 必须在主线程，与 kfz BillActivity 一致
            val bitmap = BillBitmapUtils.viewToBitmap(billView)
            billBitmap = bitmap
            binding.ivBill.setImageBitmap(bitmap)
            updatePreviewImageHeight(bitmap)
            binding.llLoading.visibility = View.GONE
            binding.flBillContainer.visibility = View.GONE
            binding.scrollPreview.visibility = View.VISIBLE
            binding.llBottom.visibility = View.VISIBLE
        }
    }

    /** 预览区固定 300dp 宽，按 bitmap 比例算高度，避免 adjustViewBounds 二次缩放导致字号视觉不一致 */
    private fun updatePreviewImageHeight(bitmap: Bitmap) {
        val previewWidth = binding.scrollPreview.width.takeIf { it > 0 }
            ?: (300 * resources.displayMetrics.density + 0.5f).toInt()
        binding.ivBill.layoutParams = binding.ivBill.layoutParams.apply {
            height = bitmap.height * previewWidth / bitmap.width
        }
    }

    private fun saveFile() {
        if (savedToAlbum) {
            ToastUtils.show(this, getString(R.string.bill_saved))
            return
        }
        val bitmap = billBitmap ?: return
        lifecycleScope.launch {
            val path = withContext(Dispatchers.IO) {
                BillImageSaver.saveToAlbum(this@BillActivity, bitmap)
            }
            if (path != null) {
                savedToAlbum = true
                ToastUtils.show(this@BillActivity, getString(R.string.bill_saved))
            } else {
                ToastUtils.show(this@BillActivity, getString(R.string.bill_save_failed))
            }
        }
    }
}
