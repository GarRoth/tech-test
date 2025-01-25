package com.apata.ccms.core.account

import akka.Done
import com.apata.ccms.core.CreditCardAccount
import com.apata.ccms.core.requests.{AccountCreationRequest, UpdateCreditLimitRequest}

import scala.concurrent.Future

/**
 *  Service responsible for the management of Credit Card accounts
 */
trait AccountService {
  /**
   * Attempt to create a new account for the given details
   * @return A failed future indicating the reason for failure OR the id of the successfully created new account
   */
  def createCreditCardAccount(accountCreationRequest: AccountCreationRequest): Future[Long]

  /**
   * Request to retrieve all accounts
   * @return Either a failed future, or a seq of all accounts
   */
  def getAllAccounts: Future[Seq[CreditCardAccount]]

  /**
   * Retrieve a specific account
   * @param accountId The id for the specific account being requested
   * @return Either a failed Future, or:
   *         - None: Representing no account present under that ID
   *         - Some(account): The requested account
   *         Note, important to differentiate between a failure case (Failed future),
   *         and a successful action, resulting in no found account (None).
   */
  def getAccountById(accountId: Long): Future[Option[CreditCardAccount]]

  /**
   * Retrieve an Account by card number
 *
   * @param cardNumber The card number associated with the account being requested
   * @return Either a failed Future, or:
   *         - None: Representing no account present associated with that card number
   *         - Some(account): The requested account
   *         Note, important to differentiate between a failure case (Failed future),
   *         and a successful action, resulting in no found account (None).
   */
  def getAccountByCard(cardNumber: String): Future[Option[CreditCardAccount]]

  /**
   * Update the credit limit of a credit card account
   * @param updateCreditLimitRequest The new limit and account ID to apply it to
   * @return Either a failed future with reason for failure, or a successful future indicating the account was updated
   */
  def updateAccountCreditLimit(updateCreditLimitRequest: UpdateCreditLimitRequest): Future[Done]

  /**
   * Delete a credit card account
   * @param accountId ID of account to be deleted
   * @return Either a failed future with reason for failure, or a successful future indicating the account was deleted
   */
  def deleteAccount(accountId: Long): Future[Done]
}
