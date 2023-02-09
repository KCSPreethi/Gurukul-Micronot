package com.tradingplatform.service

import com.tradingplatform.exception.PlaceOrderException
import com.tradingplatform.model.*
import jakarta.inject.Singleton

@Singleton
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
}