package com.tradingplatform.controller

import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.*
import com.tradingplatform.validations.UserReqValidation
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.json.tree.JsonObject


@Controller("/user")
class UserController {
    @Post(value = "/register", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun register(@Body body: JsonObject): MutableHttpResponse<*>? {

        val response = UserReqValidation.isValidReq(body)
        if (response != null)
            return HttpResponse.badRequest(response)

        val registerSuccessfullyResponse = registerUser(body)

        return HttpResponse.ok(registerSuccessfullyResponse)
    }

    private fun registerUser(body: JsonObject): HashMap<String, String> {
        val okResponse = HashMap<String, String>()

        val userName = body["userName"]!!.stringValue
        val phoneNumber = body["phoneNumber"]!!.stringValue
        val firstName = body["firstName"]!!.stringValue
        val lastName = body["lastName"]!!.stringValue
        val email = body["email"]!!.stringValue
        val userData = User(
            firstName = firstName,
            lastName = lastName,
            userName = userName,
            email = email.lowercase(),
            phoneNumber = phoneNumber
        )

        UserRepository.addUser(userData)

        okResponse["message"] = "User registered successfully"
        return okResponse
    }


    @Get(value = "/{userName}/accountInformation")
    fun getAccountInformation(@PathVariable(name = "userName") userName: String): MutableHttpResponse<out Any?>? {

        val response = mutableMapOf<String, Any>()
        val errorResponse = UserReqValidation.isUserExists(userName)

        if (errorResponse != null)
            return HttpResponse.badRequest(errorResponse)

        val user = UserRepository.getUser(userName)!!

        val wallet = mutableMapOf<String, Int>()
        wallet["free"] = user.wallet.getFreeAmount()
        wallet["locked"] = user.wallet.getLockedAmount()

        val inventory = mutableListOf<InventoryOutput>()

        val normalInventory =
            InventoryOutput(user.inventory.esopNormal.free, user.inventory.esopNormal.locked, "NON_PERFORMANCE")
        val performanceInventory =
            InventoryOutput(user.inventory.esopPerformance.free, user.inventory.esopPerformance.locked, "PERFORMANCE")

        inventory.add(normalInventory)
        inventory.add(performanceInventory)
        response["firstName"] = user.firstName
        response["lastName"] = user.lastName
        response["phoneNumber"] = user.phoneNumber
        response["email"] = user.email
        response["wallet"] = wallet
        response["inventory"] = inventory

        return HttpResponse.ok(response)
    }
}