package com.apata.ccms.core.requests

import com.apata.ccms.core.{CreditCard, Currency}

final case class AccountCreationRequest(cardholderName: String, creditLimit: Long, currency: Currency, creditCard: CreditCard)
