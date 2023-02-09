package com.tradingplatform

import com.tradingplatform.data.OrderRepository.Companion.BuyOrders
import com.tradingplatform.data.OrderRepository.Companion.CompletedOrders
import com.tradingplatform.data.OrderRepository.Companion.SellOrders
import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.CreateOrderRequestBody
import com.tradingplatform.model.User
import com.tradingplatform.service.OrderService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderMatchingTest {

    @BeforeEach
    fun `Remove all the Users and Orders`() {
        CompletedOrders.clear()
        BuyOrders.clear()
        SellOrders.clear()
        UserRepository.users.clear()
    }


    @Test
    fun `Check a single buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val orderService = OrderService()

        //Act
        OrderService().placeOrder(CreateOrderRequestBody("1", "BUY", "20", "NORMAL"), user1)

        //Assert
        Assertions.assertEquals(80, user1.wallet.getFreeAmount())
    }

    @Test
    fun `Check buy order satisfied partially by sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)

        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10

        //Act
        val orderService = OrderService()
        orderService.placeOrder(CreateOrderRequestBody("5", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)

        //Assert
        Assertions.assertEquals(0, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(60, user1.wallet.getLockedAmount())

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


    @Test
    fun `Check buy order satisfied partially by performance esops of sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)

        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopPerformance.free = 10

        //Act
        val orderService = OrderService()
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20", "PERFORMANCE"), user2)
        orderService.placeOrder(CreateOrderRequestBody("5", "BUY", "20"), user1)


        //Assert
        Assertions.assertEquals(0, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(60, user1.wallet.getLockedAmount())

        Assertions.assertEquals(40, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopPerformance.free)
        Assertions.assertEquals(0, user2.inventory.esopPerformance.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


    @Test
    fun `Check sell order satisfied partially by buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.inventory.esopNormal.free = 11

        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.wallet.addAmountToFree(100)


        //Act
        val orderService = OrderService()
        orderService.placeOrder(CreateOrderRequestBody("10", "SELL", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("5", "BUY", "20"), user2)

        //Assert
        Assertions.assertEquals(98, user1.wallet.getFreeAmount())
        Assertions.assertEquals(1, user1.inventory.esopNormal.free)
        Assertions.assertEquals(5, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())

        Assertions.assertEquals(0, user2.wallet.getFreeAmount())
        Assertions.assertEquals(5, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }

    @Test
    fun `Check sell order of performance esops satisfied partially by buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)

        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopPerformance.free = 10

        //Act
        val orderService = OrderService()
        orderService.placeOrder(CreateOrderRequestBody("5", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20","PERFORMANCE"), user2)

        //Assert
        Assertions.assertEquals(0, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(60, user1.wallet.getLockedAmount())

        Assertions.assertEquals(40, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopPerformance.free)
        Assertions.assertEquals(0, user2.inventory.esopPerformance.locked)
        Assertions.assertEquals(0, user2
            .wallet.getLockedAmount())
    }


    @Test
    fun `Check a single sell order of performance esops`() {
        //Arrange
        val user1 = User("", "", "", "", "kcsp")
        UserRepository.users[user1.userName] = user1
        user1.inventory.esopNormal.free = 40
        user1.inventory.esopPerformance.free = 40
        user1.wallet.addAmountToFree(100)

        //Act
        OrderService().placeOrder(CreateOrderRequestBody("10", "SELL", "100", "PERFORMANCE"), user1)

        //Assert
        Assertions.assertEquals(40, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(100, user1.wallet.getFreeAmount())
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())
        Assertions.assertEquals(30, user1.inventory.esopPerformance.free)
        Assertions.assertEquals(10, user1.inventory.esopPerformance.locked)
    }


    @Test
    fun `Check buy order after a sell order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10
        val orderService = OrderService()
        //Act
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)

        //Assert
        Assertions.assertEquals(60, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }

    @Test
    fun `Check sell order after a buy order`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10
        val orderService = OrderService()

        //Act
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)

        //Assert
        Assertions.assertEquals(60, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


    @Test
    fun `Check match of sell order with 2 buy order , where high price buy order is placed after low price buy`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10
        val orderService = OrderService()

        //Act

        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "30"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)

        //Assert
        Assertions.assertEquals(40, user1.wallet.getLockedAmount())
        Assertions.assertEquals(20, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }

    @Test
    fun `Check match of sell order with 2 buy order , where low price buy order is placed after high price buy`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10
        val orderService = OrderService()
        //Act

        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "30"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)

        //Assert
        Assertions.assertEquals(40, user1.wallet.getLockedAmount())
        Assertions.assertEquals(20, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


    @Test
    fun `Check match of sell order with 2 buy order , both buy at same price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.wallet.addAmountToFree(100)
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.inventory.esopNormal.free = 10
        val orderService = OrderService()

        //Act
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user2)

        //Assert
        Assertions.assertEquals(40, user1.wallet.getLockedAmount())
        Assertions.assertEquals(20, user1.wallet.getFreeAmount())
        Assertions.assertEquals(2, user1.inventory.esopNormal.free)
        Assertions.assertEquals(0, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(39, user2.wallet.getFreeAmount())
        Assertions.assertEquals(8, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }

    @Test
    fun `Check match of buy order with 2 sell order , first sell at low price than second sell price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.inventory.esopNormal.free = 10
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.wallet.addAmountToFree(100)
        val orderService = OrderService()
        //Act
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "30"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user2)

        //Assert
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())
        Assertions.assertEquals(39, user1.wallet.getFreeAmount())
        Assertions.assertEquals(6, user1.inventory.esopNormal.free)
        Assertions.assertEquals(2, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(60, user2.wallet.getFreeAmount())
        Assertions.assertEquals(2, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


    @Test
    fun `Check match of buy order with 2 sell order , first sell at higher price than second sell price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.inventory.esopNormal.free = 10
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.wallet.addAmountToFree(100)
        val orderService = OrderService()
        //Act

        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "30"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user2)

        //Assert
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())
        Assertions.assertEquals(39, user1.wallet.getFreeAmount())
        Assertions.assertEquals(6, user1.inventory.esopNormal.free)
        Assertions.assertEquals(2, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(60, user2.wallet.getFreeAmount())
        Assertions.assertEquals(2, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }

    @Test
    fun `Check match of buy order with 2 sell order , both sell order at same price`() {
        //Arrange
        val user1 = User("", "", "", "", "atul_1")
        UserRepository.users[user1.userName] = user1
        user1.inventory.esopNormal.free = 10
        val user2 = User("", "", "", "", "atul_2")
        UserRepository.users[user2.userName] = user2
        user2.wallet.addAmountToFree(100)
        val orderService = OrderService()

        //Act
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "SELL", "20"), user1)
        orderService.placeOrder(CreateOrderRequestBody("2", "BUY", "20"), user2)

        //Assert
        Assertions.assertEquals(0, user1.wallet.getLockedAmount())
        Assertions.assertEquals(39, user1.wallet.getFreeAmount())
        Assertions.assertEquals(6, user1.inventory.esopNormal.free)
        Assertions.assertEquals(2, user1.inventory.esopNormal.locked)

        Assertions.assertEquals(60, user2.wallet.getFreeAmount())
        Assertions.assertEquals(2, user2.inventory.esopNormal.free)
        Assertions.assertEquals(0, user2.inventory.esopNormal.locked)
        Assertions.assertEquals(0, user2.wallet.getLockedAmount())
    }


}

