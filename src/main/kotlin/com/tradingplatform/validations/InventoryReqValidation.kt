package com.tradingplatform.validations

import com.tradingplatform.model.PlatformData
import com.tradingplatform.model.User
import io.micronaut.json.tree.JsonNode
import kotlin.math.ceil
import kotlin.math.roundToInt

class InventoryReqValidation {

    companion object {


        fun isQuantityNull(quantity: JsonNode?): String? {
            if (quantity == null)
                return "Quantity is missing"
            return null
        }

        fun isQuantityValid(quantity: JsonNode?): String? {
            if (!quantity!!.isNumber || ceil(quantity.doubleValue).roundToInt() != quantity.intValue)
                return "Quantity data type is invalid"
            return null
        }

        fun isAmountWithinLimit(quantity: Int): String? {
            if (quantity <= 0 || quantity > maxLimitForInventory)
                return "Quantity is not valid. Range between 1 and $maxLimitForInventory"
            return null
        }

        fun willQuantityExceedLimit(user: User, quantity: Int): String? {
            if (!user.inventory.isInventoryWithinLimit(quantity))
                return "Cannot place the order. Wallet amount will exceed ${PlatformData.MAX_INVENTORY_LIMIT}"
            return null
        }

        fun isEsopTypeValid(type: JsonNode?): String? {
            if (type != null && (!type.isString || type.stringValue != "PERFORMANCE")) {
                return "ESOP type is invalid ( Allowed value : PERFORMANCE and NON-PERFORMANCE)"
            }


            return null
        }

    }
}
