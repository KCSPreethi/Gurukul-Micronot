package com.tradingplatform.controller

import com.tradingplatform.data.UserRepository
import com.tradingplatform.validations.UserReqValidation
import com.tradingplatform.validations.WalletReqValidation
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.json.tree.JsonNode
import io.micronaut.json.tree.JsonObject

@Controller(value = "/user")
class WalletController {
    @Post(value = "/{userName}/wallet")
    fun addWallet(@Body body: JsonObject, @PathVariable userName: String): MutableHttpResponse<out Any>? {
        val responseMap = HashMap<String, String>()
        val errorList = arrayListOf<String>()
        val response = mutableMapOf<String, MutableList<String>>()


        val errorResponse = UserReqValidation.isUserExists(userName)
        if (errorResponse != null)
            return HttpResponse.badRequest(errorResponse)
        val user = UserRepository.getUser(userName)!!

        val amount: JsonNode? = body["amount"]
        errorList.addAll(WalletReqValidation.checkWalletValidations(amount, user))
        response["error"] = errorList
        if (errorList.isNotEmpty()) return HttpResponse.badRequest(response)

        user.wallet.addAmountToFree(amount!!.intValue)
        responseMap["message"] = "${amount.intValue} added to account"
        return HttpResponse.ok(responseMap)
    }

}