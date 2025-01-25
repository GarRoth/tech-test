package com.apata.ccms.impl.transactions

import akka.Done
import com.apata.ccms.core.{CardType, CreditCard}
import com.apata.ccms.core.db.{DBException, Dao}
import com.apata.ccms.core.requests.{ChargeRequest, CreditRequest}
import com.apata.ccms.core.transactions.{TransactionExceptions, TransactionService}
import com.apata.ccms.core.transactions.gateway.{PaymentGatewayClient, PaymentGatewayException}
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.util.ValidationUtils
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class TransactionServiceImpl(dao: Dao, paymentGatewayClient: PaymentGatewayClient, encryptionService: EncryptionService)
                            (implicit executionContext: ExecutionContext) extends TransactionService with StrictLogging {

  //TODO: Implementation is exposed to race-conditions
  // perhaps rethink DAO to return actions instead, so multiple actions can be created and run transactionally
  override def chargeToCard(chargeRequest: ChargeRequest): Future[Done] = {
    ValidationUtils.validateChargeRequest(chargeRequest).map { _ =>
      dao
        .findByCardNumber(chargeRequest.cardNumber)
        .flatMap {
          case Some(accountAndCard) if (accountAndCard._1.creditLimit < chargeRequest.amount) =>
            Future.failed(TransactionExceptions.InvalidTransactionRequest("Insufficient credit limit to process charge request."))

          case Some(accountAndCard) =>
            val dbCard = accountAndCard._2
            val creditCard = CreditCard(chargeRequest.cardNumber,  dbCard.expiry, dbCard.cvv, CardType(dbCard.cardType))
            paymentGatewayClient
              .chargeToCard(creditCard, chargeRequest.amount)
              .map{ _ =>
                //Only update entry if charge was successful
                  dao.updateAccountCreditLimit(accountAndCard._1.id, (accountAndCard._1.creditLimit - chargeRequest.amount))
              }
          case None =>
            Future.failed(TransactionExceptions.InvalidTransactionRequest("No account known for the provided card."))

        }.flatten
    }.flatten
      .map(_ => Done)
      .recoverWith {
        // Validation exceptions
        case exception: TransactionExceptions.InvalidTransactionRequest =>
          logger.error(s"Transaction validation failed: ${exception.getMessage}")
          Future.failed(exception)

        // DB exceptions
        case exception: DBException =>
          logger.error(s"Charge to card failed due to DB exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process charge due to a DB error."))

        // Payment gateway exceptions
        case exception: PaymentGatewayException =>
          logger.error(s"Charge to card failed due to payment gateway exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process charge due to a payment gateway error."))

        // Unexpected exceptions
        case exception =>
          logger.error(s"Charge to card failed due to unexpected exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process charge due to an unexpected error."))
      }
      .andThen {
        case Success(_) =>
          logger.info(s"Successfully credited [${chargeRequest.amount}] to card [${chargeRequest.cardNumber}].")
      }
  }

  override def creditToCard(creditRequest: CreditRequest): Future[Done] = {
    ValidationUtils.validateCreditRequest(creditRequest)
      .flatMap { _ =>
        dao.findByCardNumber(creditRequest.cardNumber)
          .flatMap {
            case Some(accountAndCard) =>
              val dbCard = accountAndCard._2
              val creditCard = CreditCard(creditRequest.cardNumber, dbCard.expiry, dbCard.cvv, CardType(dbCard.cardType))

              paymentGatewayClient
                .creditToCard(creditCard, creditRequest.amount)
                .flatMap { _ =>
                  // Update the credit limit after a successful credit
                  dao.updateAccountCreditLimit(accountAndCard._1.id, accountAndCard._1.creditLimit + creditRequest.amount)
                }

            case None =>
              Future.failed(TransactionExceptions.InvalidTransactionRequest("No account known for the provided card."))
          }
      }
      .map(_ => Done)
      .recoverWith {
        // Validation exceptions
        case exception: TransactionExceptions.InvalidTransactionRequest =>
          logger.error(s"Transaction validation failed: ${exception.getMessage}")
          Future.failed(exception)

        // DB exceptions
        case exception: DBException =>
          logger.error(s"Credit to card failed due to DB exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process credit due to a DB error."))

        // Payment gateway exceptions
        case exception: PaymentGatewayException =>
          logger.error(s"Credit to card failed due to payment gateway exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process credit due to a payment gateway error."))

        // Unexpected exceptions
        case exception =>
          logger.error(s"Credit to card failed due to unexpected exception: ${exception.getMessage}")
          Future.failed(TransactionExceptions.TransactionFailed("Could not process credit due to an unexpected error."))
      }
      .andThen {
        case Success(_) =>
          logger.info(s"Successfully credited [${creditRequest.amount}] to card [${creditRequest.cardNumber}].")
      }
  }
}
