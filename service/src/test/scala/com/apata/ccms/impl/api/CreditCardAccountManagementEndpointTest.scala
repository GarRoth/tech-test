package com.apata.ccms.impl.api;

import akka.Done
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.apata.ccms.core.CreditCardAccount
import com.apata.ccms.core.account.AccountService
import com.apata.ccms.core.requests.{AccountCreationRequest, ChargeRequest, CreditRequest, UpdateCreditLimitRequest}
import com.apata.ccms.core.transactions.TransactionService
import org.junit.runner.RunWith
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Future;

@RunWith(classOf[JUnitRunner])
class CreditCardAccountManagementEndpointTest extends Matchers with AnyFlatSpecLike with ScalatestRouteTest {

  behavior of "CreditCardAccountManagementEndpoint"

  it should "return a list of accounts for a GET request to /credit-cards" in new WorkingFixture { f =>
    Get("/credit-cards") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      CreditCardAccountHelper.creditCardAccountFieldNames.foreach { field =>
        withClue(s"Response [$responseData] did not contain expected account field [$field]") {
          responseData.contains(field) shouldEqual true
        }
      }
      println(responseData)
    }
  }

  it should "create a new account for a POST request to /credit-cards" in new WorkingFixture { f =>
    val requestPayload =
      """
        {
          "cardholderName": "Old McDonald",
          "creditLimit": 7000,
          "currency": "USD",
          "creditCard": {
            "cardNumber": "4111111111111111",
            "expiry": "12/25",
            "cvv": 123,
            "cardType": "VISA"
          }
        }
      """
    Post("/credit-cards", HttpEntity(ContentTypes.`application/json`, requestPayload)) ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.Created
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected success message") {
        responseData.contains("New account has been created") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "return a specific account for a GET request to /credit-cards/{id}" in new WorkingFixture { f =>
    val accountId = 123L
    Get(s"/credit-cards/$accountId") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not match the requested account ID [$accountId]") {
        responseData.contains(accountId.toString) shouldEqual true
        println(responseData)
      }
    }
  }

  it should "update the credit limit for a PUT request to /credit-cards/{id}" in new WorkingFixture { f =>
    val accountId = 123L
    val newCreditLimit = 15000
    Put(s"/credit-cards/$accountId?newCreditLimit=$newCreditLimit") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected success message") {
        responseData.contains("Credit limit successfully updated") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "delete an account for a DELETE request to /credit-cards/{id}" in new WorkingFixture { f =>
    val accountId = 123L
    Delete(s"/credit-cards/$accountId") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected success message") {
        responseData.contains("Credit Card Account successfully deleted") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "charge a card for a POST request to /credit-cards/charge" in new WorkingFixture { f =>
    val requestPayload =
      """
        {
          "cardNumber": "4111111111111111",
          "amount": 100
        }
      """
    Post("/credit-cards/charge", HttpEntity(ContentTypes.`application/json`, requestPayload)) ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected success message") {
        responseData.contains("Card successfully charged") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "credit a card for a POST request to /credit-cards/credit" in new WorkingFixture { f =>
    val requestPayload =
      """
        {
          "cardNumber": "4111111111111111",
          "amount": 50
        }
      """
    Post("/credit-cards/credit", HttpEntity(ContentTypes.`application/json`, requestPayload)) ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.OK
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected success message") {
        responseData.contains("Card successfully credited") shouldEqual true
        println(responseData)
      }
    }
  }

  // Unhappy paths
  it should "return InternalServerError for GET /credit-cards when AccountService fails" in new FailingFixture { f =>
    Get("/credit-cards") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.InternalServerError
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected error message") {
        responseData.contains("Encountered unknown error during request processing.") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "return BadRequest for POST /credit-cards with invalid payload" in new FailingFixture { f =>
    val invalidPayload =
      """
        {
          "cardholderName": "Pat McCormack"
        }
      """
    Post("/credit-cards", HttpEntity(ContentTypes.`application/json`, invalidPayload)) ~> Route.seal(endpoint.route) ~> check {
      status shouldEqual StatusCodes.BadRequest
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected validation error") {
        responseData.contains("Missing Required Field") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "return InternalServerError for DELETE /credit-cards/{id} when AccountService fails" in new FailingFixture { f =>
    val accountId = 123L
    Delete(s"/credit-cards/$accountId") ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.InternalServerError
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected error message") {
        responseData.contains("An unexpected error occurred. Please try again later.") shouldEqual true
        println(responseData)
      }
    }
  }

  it should "return InternalServerError for POST /credit-cards/charge when TransactionService fails" in new FailingFixture { f =>
    val requestPayload =
      """
        {
          "cardNumber": "4111111111111111",
          "amount": 100
        }
      """
    Post("/credit-cards/charge", HttpEntity(ContentTypes.`application/json`, requestPayload)) ~> Route.apply(endpoint.route) ~> check {
      status shouldEqual StatusCodes.InternalServerError
      val responseData = entityAs[String]
      withClue(s"Response [$responseData] did not contain expected error message") {
        responseData.contains("An unexpected error occurred. Please try again later.") shouldEqual true
        println(responseData)
      }
    }
  }


  // WorkingFixture helper for tests
  class WorkingFixture {
    val fakeWorkingAccountService: AccountService = new AccountService {
      override def createCreditCardAccount(accountCreationRequest: AccountCreationRequest): Future[Long] = {
        Future.successful(1L)
      }
      override def getAllAccounts: Future[Seq[CreditCardAccount]] = {
        Future.successful(List.fill(3)(CreditCardAccountHelper.createTestCreditCardAccount))
      }
      override def getAccountById(accountId: Long): Future[Option[CreditCardAccount]] = {
        Future.successful(Some(CreditCardAccountHelper.createTestCreditCardAccount.copy(accountId = accountId)))
      }
      override def getAccountByCard(cardNumber: String): Future[Option[CreditCardAccount]] = {
        val account = CreditCardAccountHelper.createTestCreditCardAccount
        Future.successful(Some(account.copy(creditCard = account.creditCard.copy(cardNumber = cardNumber))))
      }
      override def updateAccountCreditLimit(updateCreditLimitRequest: UpdateCreditLimitRequest): Future[Done] = {
        Future.successful(Done)
      }
      override def deleteAccount(accountId: Long): Future[Done] = Future.successful(Done)
    }

    val fakeWorkingTransactionService: TransactionService = new TransactionService {
      override def chargeToCard(chargeRequest: ChargeRequest): Future[Done] = Future.successful(Done)
      override def creditToCard(creditRequest: CreditRequest): Future[Done] =         Future.successful(Done)
    }

    val endpoint = new CreditCardAccountManagementEndpoint(fakeWorkingAccountService, fakeWorkingTransactionService)
  }

  class FailingFixture {
    val fakeFailingAccountService: AccountService = new AccountService {
      override def createCreditCardAccount(accountCreationRequest: AccountCreationRequest): Future[Long] = {
        Future.failed(new Exception("Account creation failed"))
      }
      override def getAllAccounts: Future[Seq[CreditCardAccount]] = {
        Future.failed(new Exception("Failed to fetch accounts"))
      }
      override def getAccountById(accountId: Long): Future[Option[CreditCardAccount]] = {
        Future.failed(new Exception("Failed to fetch account by ID"))
      }
      override def getAccountByCard(cardNumber: String): Future[Option[CreditCardAccount]] = {
        Future.failed(new Exception("Failed to fetch account by card number"))
      }
      override def updateAccountCreditLimit(updateCreditLimitRequest: UpdateCreditLimitRequest): Future[Done] = {
        Future.failed(new Exception("Failed to update credit limit"))
      }
      override def deleteAccount(accountId: Long): Future[Done] = {
        Future.failed(new Exception("Failed to delete account"))
      }
    }

    val fakeFailingTransactionService: TransactionService = new TransactionService {
      override def chargeToCard(chargeRequest: ChargeRequest): Future[Done] = {
        Future.failed(new Exception("Transaction charge failed"))
      }
      override def creditToCard(creditRequest: CreditRequest): Future[Done] = {
        Future.failed(new Exception("Transaction credit failed"))
      }
    }

    val endpoint = new CreditCardAccountManagementEndpoint(fakeFailingAccountService, fakeFailingTransactionService)
  }
}
