package com.joy.featurelogin.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.data.Result
import com.joy.featurelogin.domain.MockUserRepository
import com.joy.common.router.AppRouter
import com.joy.common.router.RouterConstants
import com.joy.common.utils.LoadingUtils
import com.joy.common.utils.ToastUtils
import com.joy.featurelogin.databinding.ActivityLoginBinding
import com.joy.featurelogin.domain.LoginUseCase
import com.joy.featurelogin.domain.LoginViewModel

@Route(path = RouterConstants.LOGIN_MAIN)
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        initObserver()
    }
    fun initView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun initData() {
        val userRepository = MockUserRepository()
        val loginUseCase = LoginUseCase(userRepository)
        loginViewModel = LoginViewModel(loginUseCase)
    }

    fun initObserver() {
        loginViewModel.loginState.observe(this) { result ->
            when (result) {
                is Result.Loading -> LoadingUtils.show(this)
                is Result.Success -> {
                    LoadingUtils.dismiss()
                    ToastUtils.showSuccess(this, "登录成功")
                    goToProductDetail()
                }
                is Result.Failure -> {
                    LoadingUtils.dismiss()
                    ToastUtils.showError(this, result.exception.message)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (phone.isEmpty()) {
                ToastUtils.show(this, "请输入手机号")
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                ToastUtils.show(this, "请输入密码")
                return@setOnClickListener
            }
            
            loginViewModel.login(phone, password)
        }

        binding.tvRegister.setOnClickListener {
            ToastUtils.show(this, "注册功能开发中")
        }
    }

    private fun goToProductDetail() {
        AppRouter.openGoodsDetail(this, spuId = "mock-salmon")
        finish()
    }
}