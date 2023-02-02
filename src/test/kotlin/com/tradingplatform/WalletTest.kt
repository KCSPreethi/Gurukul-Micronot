package com.tradingplatform

import com.tradingplatform.controller.UserController
import com.tradingplatform.controller.WalletController
import com.tradingplatform.model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WalletTest {

    @BeforeEach
    fun `Remove all the Users and Orders`() {
        Users.clear()
    }

    @Test
    fun `check whether amount added to wallet`() {

        val user1 = User("", "", "", "tt@gmail.com", "atul_1")
        val objectOfUserController = UserController()
        objectOfUserController.addUser(user1)
        val objectOfWalletController = WalletController()


        objectOfWalletController.addAmountToWallet(user1.userName, 1000)


        Assertions.assertEquals(1000, user1.walletFree)
    }

}