package com.tradingplatform.service

import com.tradingplatform.data.*
import com.tradingplatform.data.OrderRepository.Companion.BuyOrders
import com.tradingplatform.data.OrderRepository.Companion.CompletedOrders
import com.tradingplatform.data.OrderRepository.Companion.SellOrders
import com.tradingplatform.exception.PlaceOrderException
import com.tradingplatform.model.*
import io.micronaut.http.HttpResponse

class OrderService {
    fun placeOrder(createOrderRequestBody: CreateOrderRequestBody, user: User): Order {
        val quantity = createOrderRequestBody.quantity!!.toInt()
        val price = createOrderRequestBody.price!!.toInt()
        val type = createOrderRequestBody.type!!
        val esopType = createOrderRequestBody.esopType

        return when (type) {
            "BUY" -> placeBuyOrder(quantity, price, user)
            "SELL" -> placeSellOrder(esopType, quantity, price, user)

            else -> throw PlaceOrderException(listOf("Cannot place order"))
        }
    }

    private fun placeBuyOrder(
        quantity: Int,
        price: Int,
        user: User
    ) = Order("BUY", quantity, price, user, esopNormal)

    private fun placeSellOrder(
        esopType: String?,
        quantity: Int,
        price: Int,
        user: User
    ) = when (esopType) {
        "PERFORMANCE" -> Order("SELL", quantity, price, user, esopPerformance)
        "NORMAL" -> Order("SELL", quantity, price, user, esopNormal)
        else -> throw PlaceOrderException(listOf("Cannot place order"))
    }

    fun orderHandler(userName: String, type: String, quantity: Int, price: Int, esopType: String = "NORMAL"): Any {
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, Any>()
        var newOrder: Order? = null


        val user = UserRepo.getUser(userName)!!


        val totalAmount = quantity * price
        if (type == "BUY") {
            user.wallet.transferAmountFromFreeToLocked(totalAmount)
            user.inventory.addESOPToCredit(quantity)
            newOrder = Order("BUY", quantity, price, user, esopNormal)
            user.orders.add(newOrder.id)
        } else if (type == "SELL") {
            if (esopType == "PERFORMANCE") {
                user.inventory.addPerformanceESOPToLocked(quantity)
                user.inventory.removePerformanceESOPFromFree(quantity)
                user.wallet.credit += totalAmount
                newOrder = Order("SELL", quantity, price, user, esopPerformance)
                user.orders.add(newOrder.id)
            } else if (esopType == "NORMAL") {
                user.inventory.addNormalESOPToLocked(quantity)
                user.inventory.removeNormalESOPFromFree(quantity)
                user.wallet.credit += (totalAmount * 0.98).toInt()
                newOrder = Order("SELL", quantity, price, user, esopNormal)
                user.orders.add(newOrder.id)
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

    fun updateTransactionsOfOrder(allOrdersOfUser: MutableList<Order>) {
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


    fun getAllCompletedOrdersOfUser(userOrderIds: ArrayList<Pair<Int, Int>>): MutableList<Order> {

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


    fun getAllPendingOrdersOfUser(userName: String): MutableList<Order> {

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
}