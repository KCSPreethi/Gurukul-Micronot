package com.tradingplatform.model

data class OrderResponse(
    val orderId: Int,
    val quantity: Int,
    val price: Int,
    val type: String,
    val esopType: String
)
