package com.tradingplatform.service

import com.tradingplatform.data.UserRepo
import com.tradingplatform.exception.PlaceOrderException
import com.tradingplatform.model.*
import com.tradingplatform.validations.OrderValidation
import io.micronaut.http.HttpResponse
import jakarta.inject.Singleton

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