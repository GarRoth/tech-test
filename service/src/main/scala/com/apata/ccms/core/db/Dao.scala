package com.apata.ccms.core.db

import scala.concurrent.Future

/**
 * Data Access Object for standard operations with the DB layer
 */
trait Dao {

  def create(account: CreditCardAccountDB, creditCardDB: CreditCardDB): Future[Long]

  def findAll: Future[Seq[(CreditCardAccountDB, CreditCardDB)]]

  def findById(id: Long): Future[Option[(CreditCardAccountDB, CreditCardDB)]]

  def findByCardNumber(cardNumber: String): Future[Option[(CreditCardAccountDB, CreditCardDB)]]

  def updateAccountCreditLimit(accountId: Long, newLimit: Long): Future[Int]

  def delete(id: Long): Future[Int]
}
