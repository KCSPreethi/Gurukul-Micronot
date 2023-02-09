package com.tradingplatform.controller

import com.tradingplatform.data.UserRepository
import com.tradingplatform.model.User
import com.tradingplatform.validations.maxLimitForInventory
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

@MicronautTest
class InventoryControllerTest {

    @Test
    fun `return success on valid quantity and esop type`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": 5,
                    "type": "PERFORMANCE"
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(200)
            .body("message", Matchers.contains("5 PERFORMANCE ESOPs added to account"))
    }

    @Test
    fun `return proper error message on missing quantity field`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "type": "PERFORMANCE"
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(400)
            .body("error", Matchers.contains("Quantity is missing"))
    }

    @Test
    fun `return success for valid quantity`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": 10
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(200)
            .body("message", Matchers.contains("10 ESOPs added to account"))
    }

    @Test
    fun `return proper error message on missing quantity and type field`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body("{}")
            .post("/user/${user.userName}/inventory")
            .then()
            .body("error", Matchers.contains("Quantity is missing"))
    }

    @Test
    fun `return proper error message when quantity entered exceeds maxLimit`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": 100000002
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(400).and()
            .body("error", Matchers.contains("Quantity is not valid. Range between 1 and $maxLimitForInventory"))

    }

    @Test
    fun `return proper error message when quantity entered is negative`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": -5
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(400).and()
            .body("error", Matchers.contains("Quantity is not valid. Range between 1 and $maxLimitForInventory"))

    }

    @Test
    fun `return proper error message when type entered is invalid`(spec: RequestSpecification) {
        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": 10,
                    "type": "abs"
                }
            """.trimIndent()
            )
            .post("/user/${user.userName}/inventory")
            .then()
            .statusCode(400).and()
            .body("error", Matchers.contains("ESOP type is invalid ( Allowed value : PERFORMANCE and NON-PERFORMANCE)"))

    }

    @Test
    fun `Proper error message when user not registered`(spec: RequestSpecification) {
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": 5,
                    "type": "PERFORMANCE"
                }
            """.trimIndent()
            )
            .post("/user/pcs/inventory")
            .then()
            .statusCode(400).and()
            .body("error", Matchers.contains("User does not exists"))
    }

    @Test
    fun `proper error quantity entered is a string`(spec: RequestSpecification) {

        val user = User("", "", "", "tat@gmail.com", "pkcs")
        UserRepository.addUser(user)
        spec.`when`()
            .header("Content-Type", "application/json")
            .body(
                """
                {
                    "quantity": "abc"
                }
            """.trimIndent()
            )
            .post("/user/pkcs/inventory")
            .then()
            .statusCode(400).and()
            .body("error", Matchers.contains("Quantity data type is invalid"))

    }
}