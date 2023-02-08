package com.tradingplatform.model

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

const val LIMIT = 10_000_000L

@Introspected
data class CreateOrderRequestBody(
    @field:NotBlank(message = "quantity field is blank")
    @field:Min(value = 1, message = "quantity should be positive")
    @field:Max(value = LIMIT, message = "quantity should be less than $LIMIT")
    val quantity: String? = null,
    @field:NotBlank(message = "type field is blank")
    @field:Pattern(regexp = "^(BUY|SELL)$", message = "type field should be BUY or SELL")
    val type: String? = null,
    @field:NotBlank(message = "price field is blank")
    @field:Min(value = 1, message = "price should be positive")
    @field:Max(value = LIMIT, message = "price is more than $LIMIT")
    val price: String? = null,
    @field:Pattern(
        regexp = "^(PERFORMANCE|NORMAL)$",
        message = "ESOP type field should be PERFORMANCE or NON_PERFORMANCE"
    )
    val esopType: String? = "NORMAL"
)
