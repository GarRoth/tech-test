package com.apata.ccms.core.transactions.gateway

import akka.Done
import com.apata.ccms.core.CreditCard

import scala.concurrent.Future

/**
 *  Service for interacting with a PaymentGateway for the purposes of
 *   - Charging a given card
 *   - Crediting a given card
 *  The responses for both actions should result in either
 *   - Future.Success: Action was completed successfully
 *   - Future.Failure: There was a problem while processing this request,
 *                     in this case an informative custom exception should be returned.
 */
trait PaymentGatewayClient {
  def chargeToCard(creditCard: CreditCard, amount: Int): Future[Done]
  def creditToCard(creditCard: CreditCard, amount: Int): Future[Done]
}
