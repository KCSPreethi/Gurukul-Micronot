package com.tradingplatform.validations

import com.tradingplatform.model.User

const val maxLimitForWallet = 10000000
const val maxLimitForInventory = 100000000


class OrderValidation {
    fun isValidAmount(list: ArrayList<String>, amount: Int): Boolean {
        if (amount <= 0) {
            list.add("Enter a positive amount")
            return false
        } else if (amount > maxLimitForWallet) {

            list.add("Enter amount between 0 to $maxLimitForWallet")
            return false
        }
        return true

    }

    fun isWalletAmountWithinLimit(list: ArrayList<String>, user: User, amount: Int): Boolean {
        if (!user.wallet.isWalletAmountWithinLimit(amount)) {
            list.add("Cannot place the order. Wallet amount will exceed $maxLimitForWallet")
            return false
        }
        return true
    }


}


