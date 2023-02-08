package com.tradingplatform.exception

class UserNotFoundException(val errorList: List<String>) : Throwable() {}
