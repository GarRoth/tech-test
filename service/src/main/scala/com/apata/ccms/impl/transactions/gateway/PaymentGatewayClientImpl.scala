package com.apata.ccms.impl.transactions.gateway

import akka.Done
import com.apata.ccms.PaymentGatewayConfig
import com.apata.ccms.core.CreditCard
import com.apata.ccms.core.transactions.gateway.PaymentGatewayClient

import scala.concurrent.Future

class PaymentGatewayClientImpl(paymentGatewayConfig: PaymentGatewayConfig) extends PaymentGatewayClient {

  override def chargeToCard(creditCard: CreditCard, amount: Int): Future[Done] = {
    Future.successful(Done)
  }

  override def creditToCard(creditCard: CreditCard, amount: Int): Future[Done] = {
    Future.successful(Done)
  }
}
