package com.joy.featurelogin.domain

import com.joy.common.data.Result

interface UserRepository {
    fun getCurrentUserId(): String
    suspend fun login(phone: String, password: String): Result<User>
    suspend fun register(phone: String, password: String): Result<User>
}

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val avatar: String,
    val token: String
)

class MockUserRepository : UserRepository {
    override fun getCurrentUserId(): String {
        return "user_001"
    }

    override suspend fun login(phone: String, password: String): Result<User> {
        kotlinx.coroutines.delay(800)
        return Result.Success(
            User(
                id = "user_001",
                name = "张三",
                phone = phone,
                avatar = "",
                token = "mock_token_123456"
            )
        )
    }

    override suspend fun register(phone: String, password: String): Result<User> {
        kotlinx.coroutines.delay(800)
        return Result.Success(
            User(
                id = "user_${System.currentTimeMillis()}",
                name = "新用户",
                phone = phone,
                avatar = "",
                token = "mock_token_${System.currentTimeMillis()}"
            )
        )
    }
}
