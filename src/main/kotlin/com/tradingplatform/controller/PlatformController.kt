package com.tradingplatform.controller

import com.tradingplatform.model.PlatformData
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.math.BigInteger

@Controller("/platform")
class PlatformController {
    @Get("/fees")
    fun getTotalCollectedPlatformFees(): HttpResponse<*> {
        val responseMap: MutableMap<String, BigInteger> = mutableMapOf()
        responseMap["collectedFees"] = PlatformData.feesEarned
        return HttpResponse.ok(responseMap)
    }
}