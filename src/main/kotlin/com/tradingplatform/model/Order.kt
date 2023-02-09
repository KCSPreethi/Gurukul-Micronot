package com.tradingplatform.model

import com.tradingplatform.data.OrderRepository.Companion.BuyOrders
import com.tradingplatform.data.OrderRepository.Companion.CompletedOrders
import com.tradingplatform.data.OrderRepository.Companion.SellOrders
import java.math.BigInteger
import kotlin.math.ceil
import kotlin.math.min

const val esopNormal = 0
const val esopPerformance = 1

data class PriceQtyPair(val price: Int, var quantity: Int) //Utility class to make the response json pretty
data class Order(val type: String, val qty: Int, val price: Int, val user: User, val esopType: Int = esopNormal) {
    private var status = "unfilled"
    var filled = ArrayList<PriceQtyPair>()
    val id: Pair<Int, Int> = Pair(BuyOrders.size + SellOrders.size + CompletedOrders.size * 2, esopType)
    val timestamp = System.currentTimeMillis()
    private var filledQty = 0

    // The match orders function has to be called here
    init {

        if (type == "BUY") {
            placeBuyOrder()
        } else if (type == "SELL") {
            placeSellOrder()
        }
    }

    private fun placeBuyOrder() {

        while (SellOrders.isNotEmpty()) {
            val potentialSellOrder = SellOrders.poll()
            if (potentialSellOrder.price > price || filledQty == qty) {
                SellOrders.add(potentialSellOrder)
                break
            } else {
                val potentialSellOrderQty =
                    min(qty - filledQty, potentialSellOrder.qty - potentialSellOrder.filledQty)


                filled.add(PriceQtyPair(potentialSellOrder.price, potentialSellOrderQty))
                filledQty += potentialSellOrderQty


                user.wallet.removeAmountFromLocked(potentialSellOrderQty * price)
                user.wallet.addAmountToFree(potentialSellOrderQty * (price - potentialSellOrder.price))
                user.inventory.addNormalESOPToFree(potentialSellOrderQty)

                user.inventory.removeESOPFromCredit(potentialSellOrderQty)


                potentialSellOrder.filled.add(PriceQtyPair(potentialSellOrder.price, potentialSellOrderQty))
                potentialSellOrder.filledQty += potentialSellOrderQty

                if (potentialSellOrder.id.second == 1) {
                    potentialSellOrder.user.wallet.addAmountToFree(potentialSellOrderQty * potentialSellOrder.price)
                    potentialSellOrder.user.inventory.removePerformanceESOPFromLocked(potentialSellOrderQty)
                } else {

                    val taxAmount: Int = ceil(potentialSellOrderQty * potentialSellOrder.price * 0.02).toInt()

                    potentialSellOrder.user.wallet.addAmountToFree(potentialSellOrderQty * potentialSellOrder.price - taxAmount)
                    PlatformData.feesEarned += BigInteger(taxAmount.toString())
                    potentialSellOrder.user.inventory.removeNormalESOPFromLocked(potentialSellOrderQty)
                }




                if (potentialSellOrder.filledQty < potentialSellOrder.qty && potentialSellOrder.filledQty > 0) potentialSellOrder.status =
                    "partially filled"
                SellOrders.add(potentialSellOrder)
                if (potentialSellOrder.filledQty == potentialSellOrder.qty) {
                    potentialSellOrder.status = "filled"
                    SellOrders.remove(potentialSellOrder)


                    CompletedOrders[potentialSellOrder.id] = potentialSellOrder
                }
            }
        }
        if (filledQty == qty) {
            status = "filled"
            CompletedOrders[id] = this
        } else {
            if (filledQty in 1 until qty) status = "partially filled"
            BuyOrders.add(this)
        }
    }

    private fun placeSellOrder() {
        while (BuyOrders.isNotEmpty()) {
            val potentialBuyOrder = BuyOrders.poll()
            if (potentialBuyOrder.price < price || filledQty == qty) {
                BuyOrders.add(potentialBuyOrder)
                break
            } else {
                val potentialBuyOrderQty = min(qty - filledQty, potentialBuyOrder.qty - potentialBuyOrder.filledQty)

                filled.add(PriceQtyPair(price, potentialBuyOrderQty))
                filledQty += potentialBuyOrderQty


                if (id.second == 1) {
                    user.inventory.removePerformanceESOPFromLocked(potentialBuyOrderQty)
                    user.wallet.addAmountToFree(potentialBuyOrderQty * price)
                    user.wallet.removeAmountFromCredit(potentialBuyOrderQty * price)
                } else {

                    val taxAmount: Int = ceil(potentialBuyOrderQty * price * 0.02).toInt()

                    user.wallet.addAmountToFree(potentialBuyOrderQty * price - taxAmount)
                    user.wallet.removeAmountFromCredit(potentialBuyOrderQty * price - taxAmount)
                    PlatformData.feesEarned += BigInteger(taxAmount.toString())
                    user.inventory.removeNormalESOPFromLocked(potentialBuyOrderQty)

                }


                potentialBuyOrder.filled.add(PriceQtyPair(price, potentialBuyOrderQty))
                potentialBuyOrder.filledQty += potentialBuyOrderQty
                potentialBuyOrder.user.wallet.removeAmountFromLocked(potentialBuyOrderQty * potentialBuyOrder.price)

                potentialBuyOrder.user.wallet.addAmountToFree(potentialBuyOrderQty * (potentialBuyOrder.price - price))
                potentialBuyOrder.user.inventory.addNormalESOPToFree(potentialBuyOrderQty)
                if (potentialBuyOrder.filledQty < potentialBuyOrder.qty && potentialBuyOrder.filledQty > 0) potentialBuyOrder.status =
                    "partially filled"
                BuyOrders.add(potentialBuyOrder)
                if (potentialBuyOrder.filledQty == potentialBuyOrder.qty) {
                    potentialBuyOrder.status = "filled"
                    BuyOrders.remove(potentialBuyOrder)
                    CompletedOrders[potentialBuyOrder.id] = potentialBuyOrder
                }
            }
        }
        if (filledQty == qty) {
            status = "filled"
            CompletedOrders[id] = this
        } else {
            if (filledQty in 1 until qty) status = "partially filled"
            SellOrders.add(this)
        }
    }
}


