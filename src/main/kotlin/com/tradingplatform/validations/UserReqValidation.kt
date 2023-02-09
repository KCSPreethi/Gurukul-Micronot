package com.tradingplatform.validations

import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.User
import io.micronaut.json.tree.JsonObject

class UserReqValidation {
    companion object {


        private val emailRegex =
            "([a-zA-Z0-9]+([+._-]?[a-zA-z0-9])*)[@]([a-zA-Z]+([-]?[a-zA-z0-9])+[.])+([a-zA-Z]+([-]?[a-zA-z0-9])+)"
        private val userNameRegex = "([a-zA-Z]+[(a-zA-z0-9)|_]*){3,}"
        private val nameRegex = "^[a-zA-z ]*\$"
        private val phoneNumberRegex = "^[+]+[0-9]{1,3}[0-9]{10}\$"


        private fun isEmailAsPerRegex(email: String): List<String> {
            if (!(email.isNotEmpty() && emailRegex.toRegex().matches(email))) {
                return listOf("Invalid email format")
            }
            return emptyList()
        }

        private fun isEmailInSpecifiedLength(email: String): List<String> {
            val errorList = arrayListOf<String>()
            val parts = email.split("@")
            val subDomains = parts[1].split(".")
            if (subDomains.size > 4 || parts[0].length > 64 || parts[1].length > 255 || subDomains[subDomains.size - 1].length < 2) {
                return listOf("Invalid email format")
            }
            for (subDomain in subDomains) {
                if (subDomain.length > 63) {
                    return listOf("Invalid email format")
                }
            }
            return errorList
        }

        fun isEmailValid(email: String): List<String> {
            val errorList = arrayListOf<String>()
            errorList.addAll(isEmailAsPerRegex(email))
            if (errorList.isNotEmpty()) return errorList
            errorList.addAll(isEmailInSpecifiedLength(email))
            if (errorList.isNotEmpty()) return errorList
            errorList.addAll(isEmailUnique(email))
            return errorList
        }

        private fun isEmailUnique(email: String): List<String> {
            val errorList = arrayListOf<String>()
            for (user in UserRepository.users) {
                if (user.value.email == email) {
                    errorList.add("email is already registered")
                }
            }
            return errorList
        }

        fun isPhoneValid(phoneNumber: String): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (!(phoneNumber.isNotEmpty() && phoneNumberRegex.toRegex().matches(phoneNumber))) {
                errorList.add("Invalid phoneNumber format")
            } else if (!isPhoneUnique(phoneNumber)) {
                errorList.add("phoneNumber already registered")
            }
            return errorList
        }

        private fun isPhoneUnique(phoneNumber: String): Boolean {
            for (user in UserRepository.users) {
                if (user.value.phoneNumber == phoneNumber) {
                    return false
                }
            }
            return true
        }


        fun isUserNameValid(userName: String): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (!(userName.isNotEmpty() && userNameRegex.toRegex().matches(userName))) {
                errorList.add("Invalid Username format")
            } else if (!isUnameUnique(userName)) {
                errorList.add("Username already registered")
            }
            return errorList
        }

        private fun isUnameUnique(userName: String): Boolean {
            for (user in UserRepository.users) {
                if (user.value.userName == userName) {
                    return false
                }
            }
            return true
        }


        fun isNameValid(name: String): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (!(name.isNotEmpty() && nameRegex.toRegex().matches(name))) {
                errorList.add("Invalid Name format")
            }
            return errorList
        }

        fun isFieldExists(fieldName: String, body: JsonObject): Boolean {
            return body[fieldName] == null
        }


        fun isUserExists(userName: String): MutableMap<String, List<String>>? {

            val response = mutableMapOf<String, List<String>>()

            if (UserRepository.getUser(userName) !is User) {
                response["error"] = listOf("User does not exists")
                return response
            }
            return null
        }


        fun isValidReq(req: JsonObject): MutableMap<String, List<String>>? {
            val errorList = arrayListOf<String>()
            val response = mutableMapOf<String, List<String>>()

            val validFeilds = ifValidFields(req)
            if (validFeilds != null)
                return validFeilds

            val feildDataValid = checkIfFeildDataIsValid(req)
            if (feildDataValid != null)
                return feildDataValid

            return null
        }


        fun ifValidFields(req: JsonObject): MutableMap<String, List<String>>? {
            val errorList = arrayListOf<String>()
            val response = mutableMapOf<String, List<String>>()

            val fields = arrayListOf("userName", "firstName", "lastName", "phoneNumber", "email")
            for (field in fields) {
                if (isFieldExists(field, req))
                    errorList.add("Enter the $field field")
                else if (req[field] == null || !req[field]!!.isString)
                    errorList.add("$field data type not in valid format")

            }

            if (errorList.isNotEmpty()) {
                response["error"] = errorList
                return response
            }

            return null
        }


        fun checkIfFeildDataIsValid(req: JsonObject): MutableMap<String, List<String>>? {
            val response = mutableMapOf<String, List<String>>()
            val errorList = arrayListOf<String>()
            errorList.addAll(isEmailValid(req["email"].stringValue))
            errorList.addAll(isPhoneValid(req["phoneNumber"].stringValue))
            errorList.addAll(isUserNameValid(req["userName"].stringValue))
            errorList.addAll(isNameValid(req["firstName"].stringValue))
            errorList.addAll(isNameValid(req["lastName"].stringValue))

            if (errorList.isNotEmpty()) {
                response["error"] = errorList
                return response
            }
            return null
        }

    }
}