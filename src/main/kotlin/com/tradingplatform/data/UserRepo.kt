package com.tradingplatform.data

import com.tradingplatform.model.User

class UserRepo {
    companion object {
        var users = HashMap<String, User>()
        fun getUser(userName: String): User? {
            return users[userName]
        }

        fun addUser(user: User) {
            users[user.userName] = user
        }
    }
}