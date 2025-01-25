package com.apata.ccms.impl.util

import akka.Done
import com.apata.ccms.core.{CardType, CreditCard}
import com.apata.ccms.core.account.AccountExceptions
import com.apata.ccms.core.requests.{AccountCreationRequest, ChargeRequest, CreditRequest}
import com.apata.ccms.core.transactions.TransactionExceptions

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

/**
 * A utility object for the validation of incoming requests.
 * This will return either:
 *    - Failed Future: Informative exception message indicating why a validation failure has occurred.
 *    - Successful Future: Indicating no validation errors have occurred, and processing can continue
 */
object ValidationUtils {

  private val NAME_CANNOT_BE_EMPTY = "Cardholder name can not be empty."
  private val INVALID_CREDIT_LIMIT = "Invalid credit limit. Credit limit can not be negative or 0."

  def validateCreationRequestAccountDetails(accountCreationRequest: AccountCreationRequest): Future[Done] = {
    accountCreationRequest match {
      case AccountCreationRequest(name, _, _, _) if name.isBlank || name.isEmpty =>
        Future.failed(AccountExceptions.InvalidCreationRequest(NAME_CANNOT_BE_EMPTY))

      case AccountCreationRequest(_, creditLimit, _, _) if !isValidCreditLimit(creditLimit) =>
        Future.failed(AccountExceptions.InvalidCreationRequest(INVALID_CREDIT_LIMIT))

      case AccountCreationRequest(_, _, _, _) =>
        Future.successful(Done)
    }
  }

  def validateCreditLimit(limit: Long): Future[Done] = {
    if (isValidCreditLimit(limit)) Future.successful(Done)
    else Future.failed(AccountExceptions.InvalidUpdateRequest(INVALID_CREDIT_LIMIT))
  }

  private def isValidCreditLimit(limit: Long): Boolean = limit > 0


  private val INVALID_CARD_NUMBER = "Credit card number must contain digits only."
  private val INVALID_CARD_LENGTH = "Credit card number length invalid for card type."
  private val OUT_OF_DATE_CARD = "Credit card number length invalid for card type."
  def validateCreditCard(creditCard: CreditCard): Future[Done] = {
    creditCard match {
      case CreditCard(cardNumber, _, _, _) if !isValidCardNumber(cardNumber) =>
        Future.failed(AccountExceptions.InvalidCreationRequest(INVALID_CARD_NUMBER))
      case CreditCard(cardNumber, _, _, cardType) if !isValidCardLength(cardType, cardNumber) =>
        Future.failed(AccountExceptions.InvalidCreationRequest(INVALID_CARD_LENGTH))
      case CreditCard(_, expiry, _, _) if !isValidExpiry(expiry) =>
        Future.failed(AccountExceptions.InvalidCreationRequest(OUT_OF_DATE_CARD))
      case CreditCard(_, _, cvv, _) if !isValidCvv(cvv) =>
        Future.failed(AccountExceptions.InvalidCreationRequest(OUT_OF_DATE_CARD))
      case CreditCard(_, _, _, _) =>
        Future.successful(Done)
    }
  }

  private def isValidCardNumber(cardNumber: String): Boolean = cardNumber.matches("\\d+")

  private def isValidCardLength(cardType: CardType, cardNumber: String): Boolean = {
    cardType.cardNumberLengths.contains(cardNumber.length)
  }

  private def isValidExpiry(expiry: String): Boolean = {
    val formatter = DateTimeFormatter.ofPattern("MM/yy")
    val expiryDate = YearMonth.parse(expiry, formatter)
    val now = YearMonth.now()

    expiryDate.isAfter(now)
  }

  private def isValidCvv(cvv: Int): Boolean = cvv >= 100 && cvv <= 999

  private val CHARGE_AMOUNT_CANNOT_BE_NEGATIVE = "Cannot process a Charge for a negative amount."
  private val CREDIT_AMOUNT_CANNOT_BE_NEGATIVE = "Cannot process a Credit for a negative amount."

  def validateChargeRequest(chargeRequest: ChargeRequest): Future[Done] = {
    chargeRequest match {
      //Allowing charges of 0 for card verifications etc?
      case ChargeRequest(amount, _) if amount < 0 =>
        Future.failed(TransactionExceptions.InvalidTransactionRequest(CHARGE_AMOUNT_CANNOT_BE_NEGATIVE))
      case ChargeRequest(_, cardNumber) if !isValidCardNumber(cardNumber) =>
        Future.failed(TransactionExceptions.InvalidTransactionRequest("Invalid card number for charge request."))
      case ChargeRequest(_, _) => Future.successful(Done)
    }
  }

  def validateCreditRequest(creditRequest: CreditRequest): Future[Done] = {
    creditRequest match {
      case CreditRequest(amount, _) if amount < 0 =>
        Future.failed(TransactionExceptions.InvalidTransactionRequest(CHARGE_AMOUNT_CANNOT_BE_NEGATIVE))
      case CreditRequest(_, cardNumber) if !isValidCardNumber(cardNumber) =>
        Future.failed(TransactionExceptions.InvalidTransactionRequest("Invalid card number for credit request."))
      case CreditRequest(_, _) => Future.successful(Done)
    }
  }

}
