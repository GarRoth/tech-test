package com.apata.ccms.impl.db

import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB, DBExceptions, Dao}
import com.apata.ccms.impl.db.DBSchema.{Accounts, Cards}
import com.apata.ccms.impl.util.HashUtil
import com.typesafe.scalalogging.StrictLogging
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class DaoImpl(db: Database)(implicit executionContext: ExecutionContext) extends Dao with StrictLogging {

  override def create(account: CreditCardAccountDB, creditCardDB: CreditCardDB): Future[Long] = {
    val checkCardExistsAction = Cards.filter(_.hashedCardNumber === creditCardDB.hashedCardNumber).result.headOption
    val insertAccountAction = Accounts returning Accounts.map(_.id) into ((account: CreditCardAccountDB, id: Long) => account.copy(id = id)) += account

    val insertAction = for {
      existingCard <- checkCardExistsAction
      result <- existingCard match {
        case Some(_) =>

          DBIO.failed(DBExceptions.CardAlreadyExistsException)
        case None =>
          for {
            insertedAccount <- insertAccountAction
            _ <- Cards += creditCardDB.copy(accountId = insertedAccount.id)
          } yield insertedAccount.id
      }
    } yield result

    db.run(insertAction.transactionally)
      .recoverWith {
        case DBExceptions.CardAlreadyExistsException =>
          logger.error(s"DB insert operation failed as Credit Card already exists")
          Future.failed(DBExceptions.CardAlreadyExistsException)
        case exception =>
          logger.error(s"DB insert operation Failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }

  override def findAll: Future[Seq[(CreditCardAccountDB, CreditCardDB)]] = {
    val query = for {
      (account, card) <- Accounts join Cards on (_.id === _.accountId)
    } yield (account, card)

    db.run(query.result)
      .recoverWith {
        case exception =>
          logger.info(s"DB findAll query Failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }

  override def findById(id: Long): Future[Option[(CreditCardAccountDB, CreditCardDB)]] = {
    val query = for {
      (account, card) <- Accounts join Cards on (_.id === _.accountId)
      if account.id === id
    } yield (account, card)

    db.run(query.result.headOption)
      .recoverWith {
        case exception =>
          logger.info(s"DB find query Failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }

  override def findByCardNumber(cardNumber: String): Future[Option[(CreditCardAccountDB, CreditCardDB)]] = {
    val query = findByCardNumberAction(cardNumber)

    db.run(query.result.headOption)
      .recoverWith {
        case exception =>
          logger.info(s"DB find query failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }

  private def findByCardNumberAction(cardNumber: String): Query[(DBSchema.CreditCardAccounts, DBSchema.CreditCards), (CreditCardAccountDB, CreditCardDB), Seq] = {
    for {
      (account, card) <- Accounts join Cards on (_.id === _.accountId)
      if card.hashedCardNumber === HashUtil.hash(cardNumber)
    } yield (account, card)
  }

  override def updateAccountCreditLimit(accountId: Long, newCreditLimit: Long): Future[Int] = {
    val action = Accounts
      .filter(_.id === accountId)
      .map(_.creditLimit)
      .update(newCreditLimit)

    db.run(action)
      .recoverWith {
        case exception =>
          logger.info(s"DB Update Credit Limit Operation Failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }

  override def delete(id: Long): Future[Int] = {
    val action = for {
      _ <- Cards.filter(_.accountId === id).delete
      rowsDeleted <- Accounts.filter(_.id === id).delete
    } yield rowsDeleted

    db.run(action.transactionally) // Ideally we would turn on cascading deletes. Being explicit here for now
      .recoverWith {
        case exception =>
          logger.info(s"DB Delete Operation Failed: ${exception.getMessage}")
          Future.failed(DBExceptions.UnexpectedDBException)
      }
  }
}
