package com.apata.ccms.impl.account

import akka.Done
import com.apata.ccms.core.CreditCardAccount
import com.apata.ccms.core.account.{AccountExceptions, AccountService}
import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB, DBException, Dao}
import com.apata.ccms.core.requests.{AccountCreationRequest, UpdateCreditLimitRequest}
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.util.{CreditCardAccountUtils, HashUtil, ValidationUtils}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AccountServiceImpl(dao: Dao, encryptionService: EncryptionService)
                        (implicit executionContext: ExecutionContext) extends AccountService with StrictLogging {

  override def createCreditCardAccount(accountCreationRequest: AccountCreationRequest): Future[Long] = {
    ValidationUtils.validateCreationRequestAccountDetails(accountCreationRequest)
      .flatMap { _ =>
        ValidationUtils.validateCreditCard(accountCreationRequest.creditCard).map { _ =>
          logger.info("Creating new Account.")

          val account = CreditCardAccountDB(
            0L,
            accountCreationRequest.cardholderName,
            accountCreationRequest.creditLimit,
            accountCreationRequest.currency.toString
          )

          val creditCard = CreditCardDB(
            encryptionService.encrypt(accountCreationRequest.creditCard.cardNumber),
            HashUtil.hash(accountCreationRequest.creditCard.cardNumber),
            accountCreationRequest.creditCard.expiry,
            accountCreationRequest.creditCard.cvv,
            accountCreationRequest.creditCard.cardType.toString,
            0L
          )

          dao.create(account, creditCard)
        }
      }.flatten
      .recoverWith {
        case exception: AccountExceptions.InvalidCreationRequest =>
          logger.error(s"Validation failed for account creation: ${exception.getMessage}")
          throw exception
        case exception: DBException =>
          logger.error(s"Account creation failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not process request due to a DB error.")
        case exception =>
          logger.error(s"Account creation failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not process request due to an unexpected error.")
      }
      .andThen {
        case Success(value) =>
          logger.info(s"Successfully created new account. ID: [$value]")
      }
  }

  override def getAllAccounts: Future[Seq[CreditCardAccount]] = {
    dao.findAll
      .map { seq =>
        seq.map(result => CreditCardAccountUtils.transformDbAccountAndCard(result._1, result._2, encryptionService))
      }
      .recoverWith {
        case exception: DBException =>
          logger.error(s"Get all accounts failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve accounts due to a DB error.")
        case exception =>
          logger.error(s"Get all accounts failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve accounts due to an unexpected error.")
      }
      .andThen {
        case Success(_) =>
          logger.info("Successfully retrieved all accounts.")
      }
  }

  override def getAccountById(accountId: Long): Future[Option[CreditCardAccount]] = {
    dao.findById(accountId)
      .map(option =>
        option.map(result => CreditCardAccountUtils.transformDbAccountAndCard(result._1, result._2, encryptionService))
      )
      .recoverWith {
        case exception: DBException =>
          logger.error(s"Get account by ID failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve account due to a DB error.")
        case exception =>
          logger.error(s"Get account by ID failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve account due to an unexpected error.")
      }
      .andThen {
        case Success(_) =>
          logger.info(s"Successfully retrieved account by ID: [$accountId]")
      }
  }

  override def getAccountByCard(cardNumber: String): Future[Option[CreditCardAccount]] = {
    dao.findByCardNumber(cardNumber)
      .map(option =>
        option.map(result => CreditCardAccountUtils.transformDbAccountAndCard(result._1, result._2, encryptionService))
      )
      .recoverWith {
        case exception: DBException =>
          logger.error(s"Get account by card number failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve account due to a DB error.")
        case exception =>
          logger.error(s"Get account by card number failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not retrieve account due to an unexpected error.")
      }
      .andThen {
        case Success(_) =>
          logger.info("Successfully retrieved account by card number.")
      }
  }

  override def updateAccountCreditLimit(updateCreditLimitRequest: UpdateCreditLimitRequest): Future[Done] = {
    ValidationUtils.validateCreditLimit(updateCreditLimitRequest.newLimit)
      .flatMap { _ =>
        dao.updateAccountCreditLimit(updateCreditLimitRequest.accountID, updateCreditLimitRequest.newLimit).flatMap {
          case 0 =>
            Future.failed(AccountExceptions.InvalidUpdateRequest("No account matching the requested account ID."))
          case _ =>
            Future.successful(Done)
        }
      }
      .recoverWith {
        case exception: AccountExceptions.InvalidUpdateRequest =>
          logger.error(s"Validation failed for credit limit update: ${exception.getMessage}")
          throw exception
        case exception: DBException =>
          logger.error(s"Update credit limit failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not update credit limit due to a DB error.")
        case exception =>
          logger.error(s"Update credit limit failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not update credit limit due to an unexpected error.")
      }
      .andThen {
        case Success(_) =>
          logger.info(s"Successfully updated credit limit to [${updateCreditLimitRequest.newLimit}] for account [${updateCreditLimitRequest.accountID}].")
      }
  }

  override def deleteAccount(accountId: Long): Future[Done] = {
    dao.delete(accountId)
      .flatMap {
        case 0 =>
          Future.failed(AccountExceptions.InvalidDeletionRequest("No account matching the requested account ID."))
        case _ =>
          Future.successful(Done)
      }
      .recoverWith {
        case exception: AccountExceptions.InvalidDeletionRequest =>
          logger.error(s"Validation failed for account deletion: ${exception.getMessage}")
          throw exception
        case exception: DBException =>
          logger.error(s"Delete account failed due to DB exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not delete account due to a DB error.")
        case exception =>
          logger.error(s"Delete account failed due to unexpected exception: ${exception.getMessage}")
          throw AccountExceptions.AccountRequestFailed("Could not delete account due to an unexpected error.")
      }
      .andThen {
        case Success(_) =>
          logger.info(s"Successfully deleted account: [$accountId]")
      }
  }
}