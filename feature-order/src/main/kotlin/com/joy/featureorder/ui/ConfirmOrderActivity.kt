package com.joy.featureorder.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.router.RouterConstants
import com.joy.featureorder.databinding.ActivityConfirmOrderBinding

@Route(path = RouterConstants.ORDER_CONFIRM)
class ConfirmOrderActivity : BaseActivity() {

    private lateinit var binding: ActivityConfirmOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)
    }
}
