package com.tradingplatform.controller

import com.tradingplatform.data.UserRepo
import com.tradingplatform.model.*
import com.tradingplatform.service.OrderService
import com.tradingplatform.service.UserService
import com.tradingplatform.validations.OrderValidation
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import javax.validation.Valid

@Validated
@Controller(value = "/user")
class OrderController {

    @Inject
    lateinit var userService: UserService
    @Inject
    lateinit var orderService: OrderService


    @Get(value = "/{userName}/order")
    fun orderHistory(@QueryValue userName: String): Any? {
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, MutableList<String>>()
        val allOrdersOfUser: MutableList<Order> = mutableListOf()
        val user = UserRepo.getUser(userName)
        if (user !is User) {
            errorList.add("User does not exists")
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }

        val userOrderIds = user.orders

        allOrdersOfUser.addAll(getAllCompletedOrdersOfUser(userOrderIds))
        allOrdersOfUser.addAll(getAllPendingOrdersOfUser(userName))

        updateTransactionsOfOrder(allOrdersOfUser)
        return HttpResponse.ok(allOrdersOfUser)

    }

    private fun updateTransactionsOfOrder(allOrdersOfUser: MutableList<Order>) {
        for (individualOrder in allOrdersOfUser) {

            val transOfIndividualOrder = individualOrder.filled

            val transAtSamePrice: ArrayList<PriceQtyPair> = arrayListOf()
            val transIndexAtPrice: MutableMap<Int, Int> = mutableMapOf()

            for (transPriceAndQty in transOfIndividualOrder) {
                if (transIndexAtPrice.contains(transPriceAndQty.price)) {
                    transAtSamePrice[transIndexAtPrice[transPriceAndQty.price]!!].quantity += transPriceAndQty.quantity
                } else {
                    transAtSamePrice.add(transPriceAndQty)
                    transIndexAtPrice[transPriceAndQty.price] = transAtSamePrice.size - 1
                }
            }
            individualOrder.filled = transAtSamePrice
        }
    }


    private fun getAllCompletedOrdersOfUser(userOrderIds: ArrayList<Pair<Int, Int>>): MutableList<Order> {

        val completedOrdersOfUser: MutableList<Order> = mutableListOf()
        for (orderId in userOrderIds) {
            if (CompletedOrders.containsKey(orderId)) {
                val currOrder = CompletedOrders[orderId]
                if (currOrder != null) {
                    completedOrdersOfUser.add(currOrder)
                }
            }
        }
        return completedOrdersOfUser
    }


    private fun getAllPendingOrdersOfUser(userName: String): MutableList<Order> {

        val pendingOrdersOfUser: MutableList<Order> = mutableListOf()
        for (order in SellOrders) {
            if (userName == order.user.userName) {
                pendingOrdersOfUser.add(order)
            }
        }
        for (order in BuyOrders) {
            if (userName == order.user.userName) {
                pendingOrdersOfUser.add(order)
            }
        }
        return pendingOrdersOfUser
    }


    @Post(value = "/{userName}/order")
    fun createOrder(@Body @Valid createOrderRequestBody: CreateOrderRequestBody, @QueryValue userName: String): Any {
        val user = userService.getUser(userName)
        userService.canPlaceOrder(user, createOrderRequestBody)
        val order: Order = orderService.placeOrder(createOrderRequestBody, user)
        println(order)
        userService.updateInventoryAndWallet(user,order)


        //Match order os

        //Update wi

        val quantity = createOrderRequestBody.quantity!!.toInt()
        val type = createOrderRequestBody.type!!
        val price = createOrderRequestBody.price!!.toInt()
        val esopType = createOrderRequestBody.esopType!!

//        return orderHandler(userName, type, quantity, price, esopType)
        val response = mutableMapOf<String, Any>()

        response["orderId"] = order!!.id.first
        response["quantity"] = quantity
        response["type"] = type
        response["price"] = price

        return HttpResponse.ok(response)
    }

    fun orderHandler(userName: String, type: String, quantity: Int, price: Int, esopType: String = "NORMAL"): Any {
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, Any>()
        var newOrder: Order? = null


        val user = UserRepo.getUser(userName)!!


        val totalAmount = quantity * price
        if (type == "BUY") {
            if (totalAmount > user.wallet.getFreeAmount()) {
                errorList.add("Insufficient funds in wallet")
            } else if (!user.inventory.isInventoryWithinLimit(quantity)) {
                errorList.add("Cannot place the order. Wallet amount will exceed ${PlatformData.MAX_INVENTORY_LIMIT}")
            } else {

                user.wallet.transferAmountFromFreeToLocked(totalAmount)
                user.inventory.addESOPToCredit(quantity)
                newOrder = Order("BUY", quantity, price, user, esopNormal)
                user.orders.add(newOrder.id)

            }
        } else if (type == "SELL") {
            if (esopType == "PERFORMANCE") {
                if (quantity > user.inventory.getPerformanceFreeQuantity()) {
                    errorList.add("Insufficient Performance ESOPs in inventory")
                } else if (!OrderValidation().isWalletAmountWithinLimit(
                        errorList, user, price * quantity
                    )
                )
                else {
                    user.inventory.addPerformanceESOPToLocked(quantity)
                    user.inventory.removePerformanceESOPFromFree(quantity)
                    user.wallet.credit += totalAmount

                    newOrder = Order("SELL", quantity, price, user, esopPerformance)
                    user.orders.add(newOrder.id)

                }
            } else if (esopType == "NORMAL") {
                if (quantity > user.inventory.getNormalFreeQuantity()) {
                    errorList.add("Insufficient Normal ESOPs in inventory")
                } else if (!OrderValidation().isWalletAmountWithinLimit(
                        errorList, user, (price * quantity * 0.98).toInt()
                    )
                )
                else {
                    user.inventory.addNormalESOPToLocked(quantity)
                    user.inventory.removeNormalESOPFromFree(quantity)
                    user.wallet.credit += (totalAmount * 0.98).toInt()

                    newOrder = Order("SELL", quantity, price, user, esopNormal)
                    user.orders.add(newOrder.id)
                }
            }
        }

        response["error"] = errorList
        if (errorList.isNotEmpty()) {
            return HttpResponse.badRequest(response)
        }

        response["orderId"] = newOrder!!.id.first
        response["quantity"] = quantity
        response["type"] = type
        response["price"] = price

        return HttpResponse.ok(response)
    }


}