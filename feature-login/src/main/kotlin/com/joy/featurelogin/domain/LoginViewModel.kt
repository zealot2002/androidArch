package com.joy.featurelogin.domain

import com.joy.common.base.BaseViewModel
import com.joy.common.data.Result
import com.joy.common.domain.User
import com.joy.common.domain.UserRepository

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {
    private val _loginState = androidx.lifecycle.MutableLiveData<Result<User>>()
    val loginState: androidx.lifecycle.LiveData<Result<User>> = _loginState

    fun login(phone: String, password: String) {
        _loginState.value = Result.Loading
        launchOnIO {
            val result = loginUseCase(phone, password)
            launchOnMain {
                _loginState.value = result
            }
        }
    }
}