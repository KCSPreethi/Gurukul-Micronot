package com.tradingplatform.data

import com.tradingplatform.model.Order
import java.util.*

class OrderRepository {
    companion object {
        val BuyOrders = PriorityQueue { order1: Order, order2: Order ->
            when {
                order1.price > order2.price -> -1
                order1.price < order2.price -> 1
                else -> {
                    (order1.timestamp - order2.timestamp).toInt()
                }
            }
        }

        val SellOrders = PriorityQueue { order1: Order, order2: Order ->
            when {
                order1.id.second > order2.id.second -> -1
                order1.id.second < order2.id.second -> 1
                order1.price > order2.price -> 1
                order1.price < order2.price -> -1
                else -> {
                    (order1.timestamp - order2.timestamp).toInt()
                }
            }
        }

        val CompletedOrders = HashMap<Pair<Int, Int>, Order>()
    }
}