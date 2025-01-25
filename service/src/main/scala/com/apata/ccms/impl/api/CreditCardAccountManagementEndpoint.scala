package com.apata.ccms.impl.api

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, MalformedRequestContentRejection, RejectionHandler, Route}
import com.apata.ccms.core.account.{AccountException, AccountExceptions, AccountService}
import com.apata.ccms.core.api.HttpEndpoint
import com.apata.ccms.core.requests.{AccountCreationRequest, ChargeRequest, CreditRequest, UpdateCreditLimitRequest}
import com.apata.ccms.core.transactions.{TransactionExceptions, TransactionService}
import com.apata.ccms.common.CirceSupport._
import com.apata.ccms.core.db.DBException
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.{Failure, Success, Try}


/**
 *  Endpoint for the management of Credit Card Accounts
 */
class CreditCardAccountManagementEndpoint(accountService: AccountService, transactionService: TransactionService) extends HttpEndpoint {

  private val rejectionHandler: RejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case MalformedRequestContentRejection(message, _) =>
        complete(StatusCodes.BadRequest, s"Missing Required Field(s): [$message]")
      case rejection =>
        complete(StatusCodes.BadRequest, s"There was a problem handling this request. [$rejection]")
    }.result()

  override def route: Route = handleRejections(rejectionHandler)(simpleRoute)

  private def simpleRoute: Route = pathPrefix("credit-cards") {
      concat (
        baseCreditCardRoute,
        chargeRoute,
        creditRoute,
        individualCreditCardRoute
      )
    }

  private val baseCreditCardRoute = pathEndOrSingleSlash {
    // Get all credit card accounts
    concat (
      get {
        onComplete(accountService.getAllAccounts) {
          case Failure(exception) => exception match {
            case ex: AccountExceptions.AccountRequestFailed =>
              logger.error(s"Account request failed during accounts retrieval: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError, ex.getMessage)
            case ex =>
              logger.error(s"Unknown Exception encountered during account creation request: $ex")
              complete(StatusCodes.InternalServerError, "Encountered unknown error during request processing.")
          }

          case Success(accounts) => complete(accounts.asJson)
        }
      },

      //Create new credit card account
      post {
        entity(as[AccountCreationRequest]) { accountCreationRequest =>
          onComplete(accountService.createCreditCardAccount(accountCreationRequest)) {

            case Failure(exception) => exception match {
              case AccountExceptions.InvalidCreationRequest(message) =>
                complete(StatusCodes.BadRequest, s"Failure to create account due to: $message")
              case ex =>
                logger.error(s"Unknown Exception encountered during account creation request: $ex")
                complete(StatusCodes.InternalServerError, "Encountered unknown error during request processing.")
            }
            case Success(accountId) =>
              complete(StatusCodes.Created, s"New account has been created. New account ID: $accountId")
          }
        }
      }
    )
  }

  private val chargeRoute = path("charge") {
    // Charge credit card
    pathEnd {
      post {
        entity(as[ChargeRequest]) { chargeRequest =>
          onComplete(transactionService.chargeToCard(chargeRequest)) {
            marshallResponseForAction(
              action = "charge request",
              successMessage = "Card successfully charged."
            )
          }
        }
      }
    }
  }

    private val creditRoute = path("credit") {
      pathEnd {
        // Credit to credit card
        post {
          entity(as[CreditRequest]) { creditRequest =>
            onComplete(transactionService.creditToCard(creditRequest)) {
              marshallResponseForAction(
                action = "credit request",
                successMessage = "Card successfully credited."
              )
            }
          }
        }
      }
    }

    private val individualCreditCardRoute = path(LongNumber) { id =>
      pathEnd {
        concat(
          // Get credit card account by ID
          get {
            onComplete(accountService.getAccountById(id)) {
              case Failure(exception) => exception match {
                case ex: AccountExceptions.AccountRequestFailed =>
                  logger.error(s"Account request failed during account retrieval: ${ex.getMessage}")
                  complete(StatusCodes.InternalServerError, ex.getMessage)
                case ex =>
                  logger.error(s"Unknown Exception encountered during account creation request: $ex")
                  complete(StatusCodes.InternalServerError, "Encountered unknown error during request processing.")
              }
              case Success(account) =>
                complete(StatusCodes.OK, account.asJson)
            }
          },

          // Change credit limit for account by ID
          put {
            parameter("newCreditLimit".as[Int]) { newCreditLimit =>
              onComplete(accountService.updateAccountCreditLimit(UpdateCreditLimitRequest(newCreditLimit, id))) {
                marshallResponseForAction(
                  action = "credit limit update",
                  successMessage = "Credit limit successfully updated"
                )
              }
            }
          },

          // Delete credit card by ID
          delete {
            onComplete(accountService.deleteAccount(id)) {
              marshallResponseForAction(
                action = "account deletion",
                successMessage = "Credit Card Account successfully deleted"
              )
            }
          }
        )
      }
    }

  // Use to simplify marshalling of exceptions. Currently only use for endpoints not returning an entity that needs encoding. Expand for these cases later
  private def marshallResponseForAction[T](action: String, successMessage: String, successStatus: StatusCode = StatusCodes.OK): PartialFunction[Try[T], Route] = {
    case Failure(exception) =>
      exception match {
        case ex: AccountExceptions.InvalidCreationRequest =>
          logger.error(s"Validation error during $action: ${ex.getMessage}")
          complete(StatusCodes.BadRequest, ex.getMessage)
        case ex: AccountExceptions.AccountRequestFailed =>
          logger.error(s"Account request failed during $action: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError, ex.getMessage)
        case ex: AccountExceptions.InvalidUpdateRequest =>
          logger.error(s"Validation error during $action: ${ex.getMessage}")
          complete(StatusCodes.BadRequest, ex.getMessage)
        case ex: AccountExceptions.InvalidDeletionRequest =>
          logger.error(s"Validation error during $action: ${ex.getMessage}")
          complete(StatusCodes.BadRequest, ex.getMessage)
        case ex: TransactionExceptions.InvalidTransactionRequest =>
          logger.error(s"Validation error during $action: ${ex.getMessage}")
          complete(StatusCodes.BadRequest, ex.getMessage)
        case ex: TransactionExceptions.TransactionFailed =>
          logger.error(s"Transaction failed during $action: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError, ex.getMessage)
        case ex: DBException =>
          //Shouldn't propagate to this level, just a safeguard
          logger.error(s"Database error during $action: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError, "A database error occurred. Please try again later.")
        case ex =>
          logger.error(s"Unknown exception encountered during $action: ${ex.getMessage}")
          complete(StatusCodes.InternalServerError, "An unexpected error occurred. Please try again later.")
      }

    case Success(_) =>
      complete(successStatus, successMessage)
  }
}