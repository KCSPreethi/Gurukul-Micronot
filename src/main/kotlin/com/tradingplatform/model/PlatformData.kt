package com.tradingplatform.model

import java.math.BigInteger

class PlatformData {
    companion object {
        var feesEarned: BigInteger = BigInteger("0")
        const val MAX_INVENTORY_LIMIT = 10000000
        const val MAX_WALLET_LIMIT = 10_000_000

    }
}