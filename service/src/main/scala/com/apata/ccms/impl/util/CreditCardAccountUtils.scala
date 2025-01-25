package com.apata.ccms.impl.util

import com.apata.ccms.core.{CardType, CreditCard, CreditCardAccount, Currency}
import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB}
import com.apata.ccms.encryption.EncryptionService

object CreditCardAccountUtils {

  def transformDbAccountAndCard(accountDB: CreditCardAccountDB, creditCardDB: CreditCardDB, encryptionService: EncryptionService): CreditCardAccount = {
    CreditCardAccount(
      accountDB.id,
      accountDB.cardholderName,
      accountDB.creditLimit,
      Currency(accountDB.currency),
      CreditCard(
        encryptionService.decrypt(creditCardDB.encryptedCardNumber),
        creditCardDB.expiry,
        creditCardDB.cvv,
        CardType(creditCardDB.cardType)
      )
    )
  }

}
