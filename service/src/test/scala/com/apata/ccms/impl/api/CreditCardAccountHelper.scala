package com.apata.ccms.impl.api

import com.apata.ccms.core._

import scala.util.Random
import scala.reflect.runtime.universe._

object CreditCardAccountHelper {

  def createTestCreditCardAccount: CreditCardAccount = {
    val randomCardType = Random.shuffle(Seq(Mastercard, Visa)).head
    val cardNumber = generateCardNumber(randomCardType)
    val expiry = generateExpiry
    val cvv = Random.nextInt(900) + 100 // Generate a 3-digit CVV
    val card = CreditCard(cardNumber, expiry, cvv, randomCardType)

    val accountId = Random.nextLong().abs // Generate a positive random account ID
    val cardHolderName = generateCardHolderName
    val creditLimit = Random.nextLong(50000) + 1000 // Generate credit limit between 1000 and 50000
    val currency = Random.shuffle(Seq(USD, EUR, GBP)).head

    CreditCardAccount(accountId, cardHolderName, creditLimit, currency, card)
  }

  private def generateCardNumber(cardType: CardType): String = {
    val length = Random.shuffle(cardType.cardNumberLengths).head
    val remainingDigits = (1 to length).map(_ => Random.nextInt(10).toString).mkString
    remainingDigits
  }

  private def generateExpiry: String = {
    val month = Random.nextInt(12) + 1 // Generate a month between 1 and 12
    val year = Random.nextInt(5) + 25 // Generate a year between 2025 and 2030
    f"$month%02d/$year"
  }

  private def generateCardHolderName: String = {
    val firstNames = Seq("John", "David", "Paddy", "Michael", "James")
    val lastNames = Seq("Smith", "O'Reilly", "McDonald", "Doe", "McCormack")
    s"${Random.shuffle(firstNames).head} ${Random.shuffle(lastNames).head}"
  }

  private def getCaseClassValNames[T: TypeTag]: Seq[String] = {
    val tpe = typeOf[T]
    tpe.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.toString
    }.toSeq
  }

  val creditCardAccountFieldNames: Seq[String] = getCaseClassValNames[CreditCardAccount]

}
