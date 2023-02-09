package com.tradingplatform.service

import com.tradingplatform.data.UserRepo
import com.tradingplatform.exception.PlaceOrderException
import com.tradingplatform.exception.UserNotFoundException
import com.tradingplatform.model.CreateOrderRequestBody
import com.tradingplatform.model.PlatformData
import com.tradingplatform.model.User
import jakarta.inject.Singleton

@Singleton
class UserService {
    fun getUser(userName: String): User {
        return UserRepo.getUser(userName) ?: throw UserNotFoundException(listOf("user does not exists"))
    }

    fun canPlaceOrder(user: User, createOrderRequestBody: CreateOrderRequestBody) {
        val quantity = createOrderRequestBody.quantity!!.toInt()
        val price = createOrderRequestBody.price!!.toInt()
        val type = createOrderRequestBody.type!!
        val esopType = createOrderRequestBody.esopType
        val totalAmount = quantity * price
        when (type) {
            "BUY" -> canPlaceBuyOrder(totalAmount, user, quantity)
            "SELL" -> canPlaceSellOrder(esopType, quantity, user, totalAmount)
        }
    }

    private fun canPlaceSellOrder(
        esopType: String?,
        quantity: Int,
        user: User,
        totalAmount: Int
    ) {
        when (esopType) {
            "PERFORMANCE" -> canPlacePerformanceSellOrder(quantity, user, totalAmount)
            "NORMAL" -> canPlaceNonPerformanceSellOrder(quantity, user, totalAmount)
        }
    }

    private fun canPlacePerformanceSellOrder(quantity: Int, user: User, totalAmount: Int) {
        if (quantity > user.inventory.getPerformanceFreeQuantity()) {
            throw PlaceOrderException(listOf("Insufficient Performance ESOPs in inventory"))
        }
        if (!user.wallet.isWalletAmountWithinLimit(totalAmount)) {
            throw PlaceOrderException(listOf("Cannot place the order. Wallet amount will exceed ${PlatformData.MAX_WALLET_LIMIT}"))
        }
    }

    private fun canPlaceBuyOrder(totalAmount: Int, user: User, quantity: Int) {
        if (totalAmount > user.wallet.getFreeAmount()) {
            throw PlaceOrderException(listOf("Insufficient funds in wallet"))
        }
        if (!user.inventory.isInventoryWithinLimit(quantity)) {
            throw PlaceOrderException(listOf("Cannot place the order. Inventory will exceed ${PlatformData.MAX_INVENTORY_LIMIT}"))
        }
    }

    private fun canPlaceNonPerformanceSellOrder(quantity: Int, user: User, totalAmount: Int) {
        if (quantity > user.inventory.getNormalFreeQuantity()) {
            throw PlaceOrderException(listOf("Insufficient Normal ESOPs in inventory"))
        }
        if (!user.wallet.isWalletAmountWithinLimit(totalAmount)) {
            throw PlaceOrderException(listOf("Cannot place the order. Wallet amount will exceed ${PlatformData.MAX_WALLET_LIMIT}"))
        }
    }
}

