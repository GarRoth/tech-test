package com.apata.ccms.impl.transactions.gateway

import akka.Done
import com.apata.ccms.PaymentGatewayConfig
import com.apata.ccms.core.CreditCard
import com.apata.ccms.core.transactions.gateway.PaymentGatewayClient

import scala.concurrent.Future
import scala.util.Random

/**
 * Currently a mocked payment gateway client.
 * Config is set up so that it can be extended to call an actual gateway in the future,
 * for now it defaults to an empty config
 *
 * There is a 1 in 10 chance a call will fail
 */
class PaymentGatewayClientImpl(paymentGatewayConfig: PaymentGatewayConfig = PaymentGatewayConfig("", "", 0)) extends PaymentGatewayClient {

  override def chargeToCard(creditCard: CreditCard, amount: Int): Future[Done] = {
    val random = Random.nextInt(11)
    if(random == 7) Future.failed(new RuntimeException("Payment gateway processing failed"))
    else Future.successful(Done)
  }

  override def creditToCard(creditCard: CreditCard, amount: Int): Future[Done] = {
    val random = Random.nextInt(11)
    if(random == 7) Future.failed(new RuntimeException("Payment gateway processing failed"))
    else Future.successful(Done)
  }
}
