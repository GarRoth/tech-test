package com.apata.ccms.impl

import akka.Done
import com.apata.ccms.core.{CreditCard, EUR, Visa}
import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB, Dao}
import com.apata.ccms.core.requests.AccountCreationRequest
import com.apata.ccms.core.transactions.gateway.PaymentGatewayClient
import com.apata.ccms.encryption.EncryptionService

/**
 * A test utility containing test implementations of commonly used objects shared across multiple tests
 * The naming of each object should reflect it's validity.
 */
import scala.concurrent.Future

object TestUtils {

  val validTestCardNumber = "4111111111111111"

  val validTestDbAccount: CreditCardAccountDB = CreditCardAccountDB(1L, "Paddy Smith", 5000, "EUR")

  val validTestDbCard: CreditCardDB = CreditCardDB(
    encryptedCardNumber = s"ENCRYPTED_$validTestCardNumber",
    hashedCardNumber = s"HASHED_$validTestCardNumber",
    expiry = "12/25",
    cvv = 123,
    cardType = "VISA",
    accountId = 1L
  )

  val validCreateAccountRequest: AccountCreationRequest = AccountCreationRequest(
    cardholderName = "Paddy Smith",
    creditLimit = 5000,
    currency = EUR,
    creditCard = CreditCard(
      cardNumber = "4111111111111111",
      expiry = "12/25",
      cvv = 123,
      cardType = Visa
    )
  )

  val workingTestDao: Dao =  new Dao {

    override def create(account: CreditCardAccountDB, creditCardDB: CreditCardDB): Future[Long] =
      Future.successful(1L)

    override def findAll: Future[Seq[(CreditCardAccountDB, CreditCardDB)]] =
      Future.successful(Seq((validTestDbAccount, validTestDbCard)))

    override def findById(id: Long): Future[Option[(CreditCardAccountDB, CreditCardDB)]] =
      if (id == validTestDbAccount.id) Future.successful(Some((validTestDbAccount, validTestDbCard)))
      else Future.successful(None)

    override def findByCardNumber(cardNumber: String): Future[Option[(CreditCardAccountDB, CreditCardDB)]] =
      if (cardNumber == validTestCardNumber) Future.successful(Some((validTestDbAccount, validTestDbCard)))
      else Future.successful(None)

    override def updateAccountCreditLimit(accountId: Long, newLimit: Long): Future[Int] =
      if (accountId == validTestDbAccount.id) Future.successful(1)
      else Future.successful(0)

    override def delete(id: Long): Future[Int] =
      if (id == validTestDbAccount.id) Future.successful(1)
      else Future.successful(0)
  }

  val workingPaymentGatewayClient: PaymentGatewayClient = new PaymentGatewayClient {
    override def chargeToCard(creditCard: CreditCard, amount: Int): Future[Done] = Future.successful(Done)
    override def creditToCard(creditCard: CreditCard, amount: Int): Future[Done] = Future.successful(Done)
  }

  val fakeWorkingEncryptionService: EncryptionService = new EncryptionService {
    override def encrypt(data: String): String = s"ENCRYPTED_$data"
    override def decrypt(data: String): String = data.replace("ENCRYPTED_", "")
  }
}
