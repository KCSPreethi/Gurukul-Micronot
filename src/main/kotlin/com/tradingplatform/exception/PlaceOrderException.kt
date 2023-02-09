package com.tradingplatform.exception

class PlaceOrderException(val errorList: List<String>) : Throwable() {}
