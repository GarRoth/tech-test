package com.apata.ccms.core

// Standard Credit Card Account
final case class CreditCardAccount(accountId: Long, cardHolderName: String, creditLimit: Long, currency: Currency, creditCard: CreditCard)

// Standard Credit Card
final case class CreditCard(cardNumber: String, expiry: String, cvv: Int, cardType: CardType) {
  override def toString: String = {
    val maskedCardNumber = cardNumber.replaceAll("\\d(?=\\d{4})", "*") // Mask all but last 4 digits
    s"CreditCard(cardNumber=$maskedCardNumber, expiry=$expiry, cvv=***, cardType=$cardType)"
  }
}

// Currently supported card types
sealed trait CardType {
  val cardNumberLengths: Seq[Int]
}
case object Mastercard extends CardType {
  override val cardNumberLengths: Seq[Int] = List(16)
  override def toString: String = "MASTERCARD"
}
case object Visa extends CardType {
  override val cardNumberLengths: Seq[Int] = List(13, 16)
  override def toString: String = "VISA"
}

case object UnknownCardType extends CardType {
  override val cardNumberLengths: Seq[Int] = List().empty
  override def toString: String = "Unknown Card Type"
}

object CardType {
  def apply(cardType: String): CardType = {
    cardType match {
      case "MASTERCARD" => Mastercard
      case "VISA" => Visa
      case _ => UnknownCardType
    }
  }
}

// TODO: Ideally this would be not be used, and a more robust and exhaustive currency representation would be used
trait Currency
case object USD extends Currency { override def toString: String = "USD" }
case object EUR extends Currency { override def toString: String = "EUR" }
case object GBP extends Currency { override def toString: String = "GBP" }
case object NA extends Currency  { override def toString: String = "Invalid Currency" }

object Currency {
  def apply(currency: String): Currency = {
    currency match {
      case "USD" => USD
      case "EUR" => EUR
      case "GBP" => GBP
      case _     => NA
    }
  }
}