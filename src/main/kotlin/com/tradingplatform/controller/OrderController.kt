package com.tradingplatform.controller

import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.CreateOrderRequestBody
import com.tradingplatform.model.Order
import com.tradingplatform.model.User
import com.tradingplatform.service.OrderService
import com.tradingplatform.service.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller(value = "/user")
class OrderController {

    val userService = UserService()
    val orderService = OrderService()


    @Get(value = "/{userName}/order")
    fun orderHistory(@QueryValue userName: String): Any? {
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, MutableList<String>>()
        val allOrdersOfUser: MutableList<Order> = mutableListOf()
        val user = UserRepository.getUser(userName)
        if (user !is User) {
            errorList.add("User does not exists")
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }

        val userOrderIds = user.orders

        allOrdersOfUser.addAll(orderService.getAllCompletedOrdersOfUser(userOrderIds))
        allOrdersOfUser.addAll(orderService.getAllPendingOrdersOfUser(userName))

        orderService.updateTransactionsOfOrder(allOrdersOfUser)
        return HttpResponse.ok(allOrdersOfUser)

    }


    @Post(value = "/{userName}/order")
    fun createOrder(@Body @Valid createOrderRequestBody: CreateOrderRequestBody, @QueryValue userName: String): Any {
        val user = userService.getUser(userName)
        userService.canPlaceOrder(user, createOrderRequestBody)
        return orderService.placeOrder(createOrderRequestBody, user)
    }
}