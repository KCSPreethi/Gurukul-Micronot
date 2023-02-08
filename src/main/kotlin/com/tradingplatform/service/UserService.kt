package com.tradingplatform.service

import com.tradingplatform.data.UserRepo
import com.tradingplatform.exception.UserNotFoundException
import com.tradingplatform.model.User
import jakarta.inject.Singleton

@Singleton
class UserService {
    fun getUser(userName: String): User {
        return UserRepo.getUser(userName) ?: throw UserNotFoundException(listOf("user does not exists"))
    }
}