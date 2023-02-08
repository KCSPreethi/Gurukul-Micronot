package com.tradingplatform.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.hateoas.JsonError

@Controller
class NotFoundController {

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun notFound(): Any {
        val response = mutableMapOf<String, MutableList<String>>()
        response["error"] = mutableListOf("Page not found")


        return HttpResponse.notFound<JsonError>()
            .body(response)
    }


}