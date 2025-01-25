package com.apata.ccms.core.transactions.gateway

sealed class PaymentGatewayException(message: String) extends Throwable(message)

//Create possible exceptions stemming from failed gateway requests (client layer)
object PaymentGatewayExceptions
