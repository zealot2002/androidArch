package com.joy.featurelogin.ui

import android.content.Intent
import android.os.Bundle
import com.joy.common.base.BaseActivity
import com.joy.common.data.Result
import com.joy.common.domain.MockUserRepository
import com.joy.common.utils.LoadingUtils
import com.joy.common.utils.ToastUtils
import com.joy.featurelogin.databinding.ActivityLoginBinding
import com.joy.featurelogin.domain.LoginUseCase
import com.joy.featurelogin.domain.LoginViewModel

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun initView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initData() {
        val userRepository = MockUserRepository()
        val loginUseCase = LoginUseCase(userRepository)
        loginViewModel = LoginViewModel(loginUseCase)
    }

    override fun initObserver() {
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
        // 使用反射避免循环依赖
        try {
            val clazz = Class.forName("com.joy.featuregoods.ui.ProductDetailActivity")
            val intent = Intent(this, clazz)
            intent.putExtra("productId", "p001")
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            ToastUtils.show(this, "商品详情页未找到")
        }
    }
}