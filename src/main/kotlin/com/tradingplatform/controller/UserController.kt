package com.tradingplatform.controller

import com.tradingplatform.data.UserRepo
import com.tradingplatform.model.*
import com.tradingplatform.validations.UserValidation
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.json.tree.JsonObject


@Controller("/user")
class UserController {
    @Post(value = "/register", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun register(@Body body: JsonObject): MutableHttpResponse<*>? {

        var errorList = arrayListOf<String>()
        val errorResponse = mutableMapOf<String, MutableList<String>>()
        val fieldLists = arrayListOf("userName", "firstName", "lastName", "phoneNumber", "email")

        //Check for empty fields
        for (field in fieldLists) {
            if (UserValidation().isFieldExists(field, body)) {
                errorList.add("Enter the $field field")
                errorResponse["error"] = errorList
            } else if (body[field] == null || !body[field]!!.isString) {
                errorList.add("$field data type not in valid format")
                errorResponse["error"] = errorList
            }
        }

        if (errorList.isNotEmpty()) {
            return HttpResponse.badRequest(errorResponse)
        }

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

        errorList = checkIfInputDataIsValid(userData)

        if (errorList.isNotEmpty()) {
            errorResponse["error"] = errorList
            return HttpResponse.badRequest(errorResponse)
        }

        UserRepo.addUser(userData)

        val okResponse = HashMap<String, String>()
        okResponse["message"] = "User registered successfully"

        return HttpResponse.ok(okResponse)
    }

    fun checkIfInputDataIsValid(user: User): ArrayList<String> {
        val errorList = arrayListOf<String>()
        errorList.addAll(UserValidation().isEmailValid(user.email))
        UserValidation().isPhoneValid(errorList, user.phoneNumber)
        UserValidation().isUserNameValid(errorList, user.userName)
        UserValidation().isNameValid(errorList, user.firstName)
        UserValidation().isNameValid(errorList, user.lastName)
        return errorList
    }


    @Get(value = "/{userName}/accountInformation")
    fun getAccountInformation(@PathVariable(name = "userName") userName: String): MutableHttpResponse<out Any?>? {

        val response = mutableMapOf<String, Any>()
        val errorList = arrayListOf<String>()
        val user = UserRepo.getUser(userName)
//        UserValidation().isUserExists(errorList, userName)
        if (user !is User) {
            response["error"] = errorList
            errorList.add("User does not exists")
            return HttpResponse.badRequest(response)
        }


        val wallet = mutableMapOf<String, Int>()
        wallet["free"] = user.wallet.getFreeAmount()
        wallet["locked"] = user.wallet.getLockedAmount()

        val inventory = mutableListOf<InventoryOutput>()

        val normalInventory = InventoryOutput(user.inventory.esopNormal.free, user.inventory.esopNormal.locked, "NON_PERFORMANCE")
        val performanceInventory = InventoryOutput(user.inventory.esopPerformance.free, user.inventory.esopPerformance.locked, "PERFORMANCE")

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