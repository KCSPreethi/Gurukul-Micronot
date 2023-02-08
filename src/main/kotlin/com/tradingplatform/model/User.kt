package com.tradingplatform.model

data class User constructor(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val userName: String
) {
    var wallet = Wallet(0, 0, 0)
    var inventory: Inventory = Inventory(ESOPType(0, 0), ESOPType(0, 0), 0)
    val orders = arrayListOf<Pair<Int, Int>>()
}

