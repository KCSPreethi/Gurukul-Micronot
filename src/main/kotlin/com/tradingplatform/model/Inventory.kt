package com.tradingplatform.model

class Inventory(var esopNormal: ESOPType, var esopPerformance: ESOPType, var credit: Int) {
    fun getNormalFreeQuantity(): Int {
        return esopNormal.free
    }

    fun getPerformanceFreeQuantity(): Int {
        return esopPerformance.free
    }


    fun addNormalESOPToFree(quantity: Int) {
        esopNormal.free += quantity
    }

    fun addPerformanceESOPToFree(quantity: Int) {
        esopPerformance.free += quantity
    }

    fun removeNormalESOPFromFree(quantity: Int) {
        esopNormal.free -= quantity
    }

    fun removePerformanceESOPFromFree(quantity: Int) {
        esopPerformance.free -= quantity
    }

    fun addPerformanceESOPToLocked(quantity: Int) {
        esopPerformance.locked += quantity
    }

    fun addNormalESOPToLocked(quantity: Int) {
        esopNormal.locked += quantity
    }

    fun removeNormalESOPFromLocked(quantity: Int) {
        esopNormal.locked -= quantity
    }

    fun removePerformanceESOPFromLocked(quantity: Int) {
        esopPerformance.locked -= quantity
    }

    fun addESOPToCredit(quantity: Int) {
        credit += quantity
    }

    fun removeESOPFromCredit(quantity: Int) {
        credit -= quantity
    }

    fun isInventoryWithinLimit(quantity: Int): Boolean {
        return esopNormal.free + esopNormal.locked + esopPerformance.free + esopPerformance.locked + credit + quantity <= PlatformData.MAX_INVENTORY_LIMIT
    }


}
