package com.tradingplatform.controller

import com.fasterxml.jackson.core.JsonParseException
import com.tradingplatform.exception.PlaceOrderException
import com.tradingplatform.exception.UserNotFoundException
import com.tradingplatform.model.ErrorResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import javax.validation.ConstraintViolationException

@Controller
class GlobalExceptionController {
    @Error(global = true)
    fun invalidJsonError(error: JsonParseException): HttpResponse<ErrorResponse> =
        HttpResponse.badRequest(ErrorResponse(listOf("invalid JSON")))

    @Error(global = true)
    fun constraintViolationError(error: ConstraintViolationException): HttpResponse<ErrorResponse> =
        HttpResponse.badRequest(ErrorResponse(error.constraintViolations.toList().map { it.message }))

    @Error(global = true)
    fun userNotFoundError(error: UserNotFoundException): HttpResponse<ErrorResponse> =
        HttpResponse.notFound(ErrorResponse(error.errorList))

    @Error(global = true)
    fun placerOrderError(error: PlaceOrderException): HttpResponse<ErrorResponse> =
        HttpResponse.badRequest(ErrorResponse(error.errorList))


    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun notFoundError(): HttpResponse<ErrorResponse> =
        HttpResponse.notFound(ErrorResponse(listOf("invalid endpoint")))

    @Error(global = true)
    fun emptyJsonError(request: HttpRequest<*>, e: Throwable): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse(listOf(e.message ?: "")))
    }

}