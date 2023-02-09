package com.tradingplatform


import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WalletTest {

    @BeforeEach
    fun `Remove all the Users and Orders`() {
        UserRepository.users.clear()
    }

    @Test
    fun `check whether amount added to wallet`() {

        val user1 = User("", "", "", "tt@gmail.com", "atul_1")


        user1.wallet.addAmountToFree(1000)



        Assertions.assertEquals(1000, user1.wallet.getFreeAmount())
    }

}