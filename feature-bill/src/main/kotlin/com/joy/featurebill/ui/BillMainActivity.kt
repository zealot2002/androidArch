package com.joy.featurebill.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.router.RouterConstants
import com.joy.featurebill.databinding.ActivityBillMainBinding

@Route(path = RouterConstants.BILL_MAIN)
class BillMainActivity : BaseActivity() {

    private lateinit var binding: ActivityBillMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)
    }
}
