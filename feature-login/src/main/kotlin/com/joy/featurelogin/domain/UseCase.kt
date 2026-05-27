package com.joy.featurelogin.domain

import com.joy.common.data.Result
import com.joy.featurelogin.domain.User
import com.joy.featurelogin.domain.UserRepository

class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(phone: String, password: String): Result<User> {
        return repository.login(phone, password)
    }
}