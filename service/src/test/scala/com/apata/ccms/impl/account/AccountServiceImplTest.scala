package com.apata.ccms.impl.account;

import akka.Done
import com.apata.ccms.core.{CreditCard, EUR, Visa}
import com.apata.ccms.core.account.AccountExceptions
import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB, Dao}
import com.apata.ccms.core.requests.AccountCreationRequest
import com.apata.ccms.encryption.EncryptionService
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class AccountServiceImplTest extends AnyFlatSpec with Matchers with ScalaFutures {

  behavior of "AccountService"

  it should "successfully create a credit card account" in new WorkingFixture {
    val request = AccountCreationRequest(
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

    val result = accountService.createCreditCardAccount(request)

    whenReady(result) { accountId =>
      accountId shouldEqual 1L
    }
  }

  it should "fail to create a credit card account due to validation error" in new WorkingFixture {
    val request = AccountCreationRequest(
      cardholderName = "",
      creditLimit = 5000,
      currency = EUR,
      creditCard = CreditCard(
        cardNumber = "4111111111111111",
        expiry = "12/25",
        cvv = 123,
        cardType = Visa
      )
    )

    val result = accountService.createCreditCardAccount(request)

    whenReady(result.failed) { ex =>
      ex shouldBe a[AccountExceptions.InvalidCreationRequest]
      ex.getMessage shouldBe "Invalid create request: Cardholder name can not be empty."
    }
  }

  it should "successfully retrieve all accounts" in new WorkingFixture {
    val result = accountService.getAllAccounts

    whenReady(result) { accounts =>
      accounts should have size 1
      accounts.head.cardHolderName shouldEqual "Paddy Smith"
    }
  }

  it should "successfully retrieve account by ID" in new WorkingFixture {
    val result = accountService.getAccountById(1L)

    whenReady(result) { accountOption =>
      accountOption shouldBe defined
      accountOption.get.cardHolderName shouldEqual "Paddy Smith"
    }
  }

  it should "fail to retrieve account by ID if it does not exist" in new WorkingFixture {
    val result = accountService.getAccountById(999L)

    whenReady(result) { accountOption =>
      accountOption shouldBe empty
    }
  }

  it should "successfully delete an account" in new WorkingFixture {
    val result = accountService.deleteAccount(1L)

    whenReady(result) { res =>
      res shouldEqual Done
    }
  }

  it should "fail to delete an account if it does not exist" in new WorkingFixture {
    val result = accountService.deleteAccount(999L)

    whenReady(result.failed) { ex =>
      ex shouldBe a[AccountExceptions.InvalidDeletionRequest]
      ex.getMessage shouldBe "Invalid delete request: No account matching the requested account ID."
    }
  }

  class WorkingFixture {
    val testCardNumber = "4111111111111111"
    val testAccount = CreditCardAccountDB(1L, "Paddy Smith", 5000, "EUR")
    val testCard = CreditCardDB(
      encryptedCardNumber = s"ENCRYPTED_$testCardNumber",
      hashedCardNumber = s"HASHED_$testCardNumber",
      expiry = "12/25",
      cvv = 123,
      cardType = "VISA",
      accountId = 1L
    )

    val fakeDao: Dao = new Dao {
      override def create(account: CreditCardAccountDB, creditCardDB: CreditCardDB): Future[Long] =
        Future.successful(1L)

      override def findAll: Future[Seq[(CreditCardAccountDB, CreditCardDB)]] =
        Future.successful(Seq((testAccount, testCard)))

      override def findById(id: Long): Future[Option[(CreditCardAccountDB, CreditCardDB)]] =
        if (id == testAccount.id) Future.successful(Some((testAccount, testCard)))
        else Future.successful(None)

      override def findByCardNumber(cardNumber: String): Future[Option[(CreditCardAccountDB, CreditCardDB)]] =
        if (cardNumber == testCardNumber) Future.successful(Some((testAccount, testCard)))
        else Future.successful(None)

      override def updateAccountCreditLimit(accountId: Long, newLimit: Long): Future[Int] =
        if (accountId == testAccount.id) Future.successful(1)
        else Future.successful(0)

      override def delete(id: Long): Future[Int] =
        if (id == testAccount.id) Future.successful(1)
        else Future.successful(0)
    }

    val fakeEncryptionService: EncryptionService = new EncryptionService {
      override def encrypt(data: String): String = s"ENCRYPTED_$data"
      override def decrypt(data: String): String = data.replace("ENCRYPTED_", "")
    }

    val accountService = new AccountServiceImpl(fakeDao, fakeEncryptionService)
  }
}