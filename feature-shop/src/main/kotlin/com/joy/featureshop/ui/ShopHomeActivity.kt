package com.joy.featureshop.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.extend.onClick200
import com.joy.common.router.AppRouter
import com.joy.common.router.RouterConstants
import com.joy.common.utils.SizeUtils
import com.joy.common.widget.SharePanelController
import com.joy.featureshop.databinding.ActivityShopHomeBinding
import com.joy.appres.R as AppResR

@Route(path = RouterConstants.SHOP_HOME)
class ShopHomeActivity : BaseActivity() {

    private lateinit var binding: ActivityShopHomeBinding
    private lateinit var sharePanelController: SharePanelController
    private var shopId: String = DEFAULT_SHOP_ID

    private val sharePanelBackCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            sharePanelController.hide()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)

        shopId = intent.getStringExtra(RouterConstants.EXTRA_SHOP_ID).orEmpty().ifBlank { DEFAULT_SHOP_ID }

        setupSharePanel()
        binding.btnBack.onClick200 { finish() }
        binding.iconShare.onClick200 { sharePanelController.show() }
    }

    private fun setupSharePanel() {
        onBackPressedDispatcher.addCallback(this, sharePanelBackCallback)
        sharePanelController = SharePanelController(
            panelRoot = binding.sharePanelOverlay.root,
            scrim = binding.sharePanelOverlay.shareScrim,
            panel = binding.sharePanelOverlay.sharePanel,
            cancelView = binding.sharePanelOverlay.btnShareCancel,
        )
        sharePanelController.onVisibilityChanged = { visible ->
            sharePanelBackCallback.isEnabled = visible
        }
        binding.sharePanelOverlay.btnSharePoster.onClick200 {
            sharePanelController.hide()
            AppRouter.openShopBill(this, shopId)
        }
    }

    override fun onApplyWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        binding.toolbar.setPadding(left, top, right, 0)
        binding.sharePanelOverlay.sharePanel.setPadding(
            left,
            binding.sharePanelOverlay.sharePanel.paddingTop,
            right,
            bottom + SizeUtils.getDimen(this, AppResR.dimen.dp_12),
        )
    }

    companion object {
        private const val DEFAULT_SHOP_ID = "mock-shop"
    }
}
