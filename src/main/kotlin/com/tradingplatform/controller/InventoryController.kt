package com.tradingplatform.controller

import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.User
import com.tradingplatform.validations.InventoryReqValidation
import com.tradingplatform.validations.UserReqValidation
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonObject


@Controller(value = "user/{userName}")
class InventoryController {
    @Post(value = "/inventory")
    fun addInventory(
        @Body body: JsonObject,
        @PathVariable(name = "userName") userName: String
    ): MutableHttpResponse<out Any>? {

        val response = mutableMapOf<String, MutableList<String>>()
        val msg = mutableListOf<String>()
        val errorList = ArrayList<String>()

        val errorResponse = UserReqValidation.isUserExists(userName)
        if (errorResponse != null)
            return HttpResponse.badRequest(errorResponse)

        val user = UserRepository.getUser(userName)!!

        var responseTemp = InventoryReqValidation.isQuantityNull(body["quantity"])
        if (responseTemp != null) {
            errorList.add(responseTemp)
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }

        val quantity = body["quantity"]
        responseTemp = InventoryReqValidation.isQuantityValid(quantity)
        if (responseTemp != null) {
            errorList.add(responseTemp)
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }

        responseTemp = InventoryReqValidation.isAmountWithinLimit(quantity.intValue)
        if (responseTemp != null) {
            errorList.add(responseTemp)
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }
        responseTemp = InventoryReqValidation.willQuantityExceedLimit(user, quantity.intValue)
        if (responseTemp != null) {
            errorList.add(responseTemp)
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }


        val type = body["type"]

        responseTemp = InventoryReqValidation.isEsopTypeValid(type)
        if (responseTemp != null) {
            errorList.add(responseTemp)
            response["error"] = errorList
            return HttpResponse.badRequest(response)
        }


        if (type != null)
            msg.add(addESOPStoUserInventory(user, "PERFORMANCE", quantity.intValue))
        else
            msg.add(addESOPStoUserInventory(user, "NORMAL", quantity.intValue))

        response["message"] = msg
        return HttpResponse.ok(response)
    }

    fun addESOPStoUserInventory(user: User, type: String, esopQuantity: Int): String {

        if (type == "PERFORMANCE") {
            user.inventory.addPerformanceESOPToFree(esopQuantity)
            return ("$esopQuantity $type ESOPs added to account")
        }

        user.inventory.addNormalESOPToFree(esopQuantity)
        return ("$esopQuantity ESOPs added to account")

    }
}