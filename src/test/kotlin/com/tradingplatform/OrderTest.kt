package com.tradingplatform

import com.tradingplatform.data.UserRepo
import com.tradingplatform.model.BuyOrders
import com.tradingplatform.model.CompletedOrders
import com.tradingplatform.model.SellOrders
import org.junit.jupiter.api.BeforeEach

class OrderTest {


    @BeforeEach
    fun `Remove all the Users and Orders`() {
        CompletedOrders.clear()
        BuyOrders.clear()
        SellOrders.clear()
        UserRepo.users.clear()
    }


}

