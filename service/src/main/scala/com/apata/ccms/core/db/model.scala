package com.apata.ccms.core.db

import com.apata.ccms.core.CardType

// Standard Credit Card Account
case class CreditCardAccountDB(id: Long, cardholderName: String, creditLimit: Long, currency: String)

// Standard Credit Card
final case class CreditCardDB(encryptedCardNumber: String, hashedCardNumber: String, expiry: String, cvv: Int, cardType: String, accountId: Long)