package com.apata.ccms.common

import com.apata.ccms.core.{CardType, CreditCard, CreditCardAccount, Currency, EUR, GBP, Mastercard, USD, Visa}
import com.apata.ccms.core.requests.AccountCreationRequest
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.deriveEncoder

// Refined Circe Support
object CirceSupport {

  // Decoders
  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.emap {
    case "USD" => Right(USD)
    case "EUR" => Right(EUR)
    case "GBP" => Right(GBP)
    case _     => Left("Invalid currency")
  }

  implicit val cardTypeDecoder: Decoder[CardType] = Decoder.decodeString.emap {
    case "MASTERCARD" => Right(Mastercard)
    case "VISA"       => Right(Visa)
    case _            => Left("Unknown card type")
  }

  implicit val creditCardDecoder: Decoder[CreditCard] = Decoder.forProduct4("cardNumber", "expiry", "cvv", "cardType")(CreditCard.apply)

  implicit val accountCreationRequestDecoder: Decoder[AccountCreationRequest] =
    Decoder.forProduct4("cardholderName", "creditLimit", "currency", "creditCard")(AccountCreationRequest.apply)

  // Encoders
  implicit val currencyEncoder: Encoder[Currency] = Encoder.encodeString.contramap(_.toString)

  implicit val cardTypeEncoder: Encoder[CardType] = Encoder.encodeString.contramap(_.toString)

  implicit val creditCardEncoder: Encoder[CreditCard] = Encoder.instance { card =>
    Json.obj(
      "cardNumber" -> Json.fromString(card.cardNumber.replaceAll("\\d(?=\\d{4})", "*")), // Mask all but the last 4 digits
      "expiry" -> Json.fromString(card.expiry),
      "cvv" -> Json.fromString("***"), // Mask CVV completely
      "cardType" -> Json.fromString(card.cardType.toString)
    )
  }

  implicit val creditCardAccountEncoder: Encoder[CreditCardAccount] = deriveEncoder[CreditCardAccount]
}

