package com.tradingplatform.model
import java.math.BigInteger
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


const val esopNormal = 0
const val esopPerformance = 1

data class PriceQtyPair(val price: Int, var quantity: Int) //Utility class to make the response json pretty

data class Order constructor(val type : String, val qty: Int, val price : Int, val createdBy : String, val esopType: Int= esopNormal) {
     var status = "unfilled"
    var filled = ArrayList<PriceQtyPair>()
    val id:Pair<Int,Int> = Pair(BuyOrders.size + SellOrders.size + CompletedOrders.size*2,esopType)
    val timestamp = System.currentTimeMillis()
    var filledQty = 0

    // The match orders function has to be called here
    init {

        if (type == "BUY") {
            placeBuyOrder()
        } else if (type == "SELL") {
            placeSellOrder()
        }
    }

    private fun placeBuyOrder(){

        while(SellOrders.isNotEmpty()){
            val potentialSellOrder = SellOrders.poll()
            if(potentialSellOrder.price > price || filledQty == qty){
                SellOrders.add(potentialSellOrder)
                break
            }
            else {
                val potentialSellOrderQty =
                    min(qty - filledQty, potentialSellOrder.qty - potentialSellOrder.filledQty)


                filled.add(PriceQtyPair(potentialSellOrder.price, potentialSellOrderQty))
                filledQty += potentialSellOrderQty


                Users[createdBy]!!.wallet.removeAmountFromLocked(potentialSellOrderQty * price)
                Users[createdBy]!!.wallet.addAmountToFree(potentialSellOrderQty * (price - potentialSellOrder.price))
                Users[createdBy]!!.inventory.esopNormal.free += potentialSellOrderQty
                Users[createdBy]!!.inventory.credit -= potentialSellOrderQty


                potentialSellOrder.filled.add(PriceQtyPair(potentialSellOrder.price, potentialSellOrderQty))
                potentialSellOrder.filledQty += potentialSellOrderQty

                if (potentialSellOrder.id.second == 1) {
                    Users[potentialSellOrder.createdBy]!!.wallet.addAmountToFree( potentialSellOrderQty * potentialSellOrder.price)
                    Users[potentialSellOrder.createdBy]!!.inventory.esopPerformance.locked -= potentialSellOrderQty
                } else {

                    val taxAmount : Int = ceil(potentialSellOrderQty * potentialSellOrder.price*0.02).toInt()

                    Users[potentialSellOrder.createdBy]!!.wallet.addAmountToFree(potentialSellOrderQty*potentialSellOrder.price-taxAmount)
                    PlatformData.feesEarned += BigInteger(taxAmount.toString())
                    Users[potentialSellOrder.createdBy]!!.inventory.esopNormal.locked -= potentialSellOrderQty
                }




                if(potentialSellOrder.filledQty < potentialSellOrder.qty && potentialSellOrder.filledQty > 0) potentialSellOrder.status = "partially filled"
                SellOrders.add(potentialSellOrder)
                if(potentialSellOrder.filledQty == potentialSellOrder.qty) {
                    potentialSellOrder.status = "filled"
                    SellOrders.remove(potentialSellOrder)


                    CompletedOrders[potentialSellOrder.id] = potentialSellOrder
                }
            }
        }
        if(filledQty == qty) {
            status = "filled"
            CompletedOrders[id] = this
        }
        else{
            if(filledQty in 1 until qty) status = "partially filled"
            BuyOrders.add(this)
        }
    }

    private fun placeSellOrder(){
        while(BuyOrders.isNotEmpty()){
            val potentialBuyOrder = BuyOrders.poll()
            if(potentialBuyOrder.price < price || filledQty == qty){
                BuyOrders.add(potentialBuyOrder)
                break
            }
            else {
                val potentialBuyOrderQty = min(qty - filledQty, potentialBuyOrder.qty - potentialBuyOrder.filledQty)

                filled.add(PriceQtyPair(price, potentialBuyOrderQty))
                filledQty += potentialBuyOrderQty


                if (id.second == 1){
                    Users[createdBy]!!.inventory.esopPerformance.locked -= potentialBuyOrderQty
                    Users[createdBy]!!.wallet.addAmountToFree( potentialBuyOrderQty * price)
                    Users[createdBy]!!.wallet.removeAmountFromCredit( potentialBuyOrderQty * price)
                }
                else {

                    val taxAmount : Int = ceil(potentialBuyOrderQty * price*0.02).toInt()

                    Users[createdBy]!!.wallet.addAmountToFree (potentialBuyOrderQty * price - taxAmount)
                    Users[createdBy]!!.wallet.removeAmountFromCredit (potentialBuyOrderQty * price - taxAmount)
                    PlatformData.feesEarned += BigInteger(taxAmount.toString())
                    Users[createdBy]!!.inventory.esopNormal.locked -= potentialBuyOrderQty

                }


                potentialBuyOrder.filled.add(PriceQtyPair(price,potentialBuyOrderQty))
                potentialBuyOrder.filledQty += potentialBuyOrderQty
                Users[potentialBuyOrder.createdBy]!!.wallet.removeAmountFromLocked( potentialBuyOrderQty *potentialBuyOrder.price)

                Users[potentialBuyOrder.createdBy]!!.wallet.addAmountToFree( potentialBuyOrderQty * (potentialBuyOrder.price - price))
                Users[potentialBuyOrder.createdBy]!!.inventory.esopNormal.free += potentialBuyOrderQty
                if(potentialBuyOrder.filledQty < potentialBuyOrder.qty && potentialBuyOrder.filledQty > 0) potentialBuyOrder.status = "partially filled"
                BuyOrders.add(potentialBuyOrder)
                if(potentialBuyOrder.filledQty == potentialBuyOrder.qty) {
                    potentialBuyOrder.status = "filled"
                    BuyOrders.remove(potentialBuyOrder)
                    CompletedOrders[potentialBuyOrder.id] = potentialBuyOrder
                }
            }
        }
        if(filledQty == qty) {
            status = "filled"
            CompletedOrders[id] = this
        }
        else{
            if(filledQty in 1 until qty) status = "partially filled"
            SellOrders.add(this)
        }
    }
}





val BuyOrders = PriorityQueue { order1 : Order, order2 : Order ->
    when{
        order1.price > order2.price -> -1
        order1.price < order2.price -> 1
        else -> {(order1.timestamp - order2.timestamp).toInt()}
    }
}

val SellOrders = PriorityQueue { order1 : Order, order2 : Order ->
    when{
        order1.id.second > order2.id.second -> -1
        order1.id.second < order2.id.second -> 1
        order1.price > order2.price -> 1
        order1.price < order2.price -> -1
        else -> {(order1.timestamp - order2.timestamp).toInt()}
        }
    }

val CompletedOrders = HashMap<Pair<Int,Int>, Order>()


