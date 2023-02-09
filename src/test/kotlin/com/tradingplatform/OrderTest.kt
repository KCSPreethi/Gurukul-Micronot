package com.tradingplatform

import com.tradingplatform.data.OrderRepository.Companion.BuyOrders
import com.tradingplatform.data.OrderRepository.Companion.CompletedOrders
import com.tradingplatform.data.OrderRepository.Companion.SellOrders
import com.tradingplatform.data.UserRepository
import org.junit.jupiter.api.BeforeEach

class OrderTest {


    @BeforeEach
    fun `Remove all the Users and Orders`() {
        CompletedOrders.clear()
        BuyOrders.clear()
        SellOrders.clear()
        UserRepository.users.clear()
    }


}

