package com.apata.ccms.core.transactions

import akka.Done
import com.apata.ccms.core.requests.{ChargeRequest, CreditRequest}

import scala.concurrent.Future

/**
 * Transaction service is responsible for the actions of charging or crediting a card, in relation to this service.
 * It acts as a delegate, and abstracts business/domain logic away from the core functionality.
 * This ensures we avoid domain bleed into the other services.
 * This also keeps the Endpoint clear of delegation responsibilities
 */
trait TransactionService {

  def chargeToCard(chargeRequest: ChargeRequest): Future[Done]

  def creditToCard(creditRequest: CreditRequest): Future[Done]
}
