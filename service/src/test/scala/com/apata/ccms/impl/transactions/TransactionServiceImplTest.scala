package com.apata.ccms.impl.transactions

import akka.Done
import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB, Dao}
import com.apata.ccms.core.requests.{ChargeRequest, CreditRequest}
import com.apata.ccms.core.transactions.TransactionExceptions
import com.apata.ccms.core.transactions.gateway.PaymentGatewayClient
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.TestUtils
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class TransactionServiceImplTest extends AnyFlatSpec with Matchers with ScalaFutures {

  behavior of "TransactionServiceImpl"

  it should "successfully charge a credit card" in new Fixture {
    val chargeRequest: ChargeRequest = ChargeRequest(amount = 100, cardNumber = "4111111111111111")

    val result: Future[Done] = transactionServiceImpl.chargeToCard(chargeRequest)

    whenReady(result) { res =>
      res shouldEqual Done
    }
  }

  it should "fail to charge a credit card due to insufficient credit limit" in new Fixture {
    val chargeRequest: ChargeRequest = ChargeRequest(amount = 1000000, cardNumber = "4111111111111111")

    val result: Future[Done] = transactionServiceImpl.chargeToCard(chargeRequest)

    whenReady(result.failed) { ex =>
      ex shouldBe a[TransactionExceptions.InvalidTransactionRequest]
      ex.getMessage shouldBe "Insufficient credit limit to process charge request."
    }
  }

  it should "successfully credit a credit card" in new Fixture {
    val creditRequest: CreditRequest = CreditRequest(amount = 200, cardNumber = TestUtils.validTestCardNumber)

    val result: Future[Done] = transactionServiceImpl.creditToCard(creditRequest)

    whenReady(result) { res =>
      res shouldEqual Done
    }
  }

  it should "fail to credit a non-existent credit card" in new Fixture {
    val creditRequest: CreditRequest = CreditRequest(amount = 200, cardNumber = "0000000000000000")

    val result: Future[Done] = transactionServiceImpl.creditToCard(creditRequest)

    whenReady(result.failed) { ex =>
      ex shouldBe a[TransactionExceptions.InvalidTransactionRequest]
      ex.getMessage shouldBe "No account known for the provided card."
    }
  }


  class Fixture {
    val fakeWorkingDao: Dao = TestUtils.workingTestDao
    val fakeWorkingPaymentGatewayClient: PaymentGatewayClient = TestUtils.workingPaymentGatewayClient
    val fakeWorkingEncryptionService: EncryptionService = TestUtils.fakeWorkingEncryptionService
    val transactionServiceImpl = new TransactionServiceImpl(
      fakeWorkingDao,
      fakeWorkingPaymentGatewayClient,
      fakeWorkingEncryptionService
    )
  }

}