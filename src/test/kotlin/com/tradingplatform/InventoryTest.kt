package com.tradingplatform

import com.tradingplatform.controller.InventoryController
import com.tradingplatform.data.UserRepo
import com.tradingplatform.model.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InventoryTest {

    @Test
    fun `check whether Performance esops added to inventory`() {
        val user = User("", "", "", "tt@gmail.com", "atul_1")
        UserRepo.addUser(user)
        val objectOfInventoryController = InventoryController()


        objectOfInventoryController.addESOPStoUserInventory(user, "PERFORMANCE", 100)


        Assertions.assertEquals(100, user.inventory.esopPerformance.free)
    }


    @Test
    fun `check whether Normal esops added to inventory`() {

        val user = User("", "", "", "tt@gmail.com", "atul_1")
        UserRepo.addUser(user)
        val objectOfInventoryController = InventoryController()


        objectOfInventoryController.addESOPStoUserInventory(user, "NORMAL", 100)


        Assertions.assertEquals(100, user.inventory.esopNormal.free)
    }


}