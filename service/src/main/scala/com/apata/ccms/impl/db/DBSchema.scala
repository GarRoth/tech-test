package com.apata.ccms.impl.db

import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB}
import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape, Tag}

object DBSchema {

  /**
   * Schema definition for the Accounts table
   */
  class CreditCardAccounts(tag: Tag) extends Table[CreditCardAccountDB](tag, "CREDIT_CARD_ACCOUNTS") {
    def id: Rep[Long] = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def cardholderName: Rep[String] = column[String]("CARDHOLDER_NAME")
    def creditLimit: Rep[Long] = column[Long]("CREDIT_LIMIT")
    def currency: Rep[String] = column[String]("CURRENCY")

    override def * : ProvenShape[CreditCardAccountDB] =
      (id, cardholderName, creditLimit, currency).mapTo[CreditCardAccountDB]
  }

  val Accounts: TableQuery[CreditCardAccounts] = TableQuery[CreditCardAccounts]

  /**
   * Schema definition for the Cards Table
   */
  class CreditCards(tag: Tag) extends Table[CreditCardDB](tag, "CREDIT_CARDS") {
    def encryptedCardNumber: Rep[String] = column[String]("CARD_NUMBER") // MUST BE ENCRYPTED
    def hashedCardNumber: Rep[String] = column[String]("HASHED_CARD_NUMBER") // One-way hash
    def expiry: Rep[String] = column[String]("EXPIRY")
    def cvv: Rep[Int] = column[Int]("CVV")
    def cardType: Rep[String] = column[String]("CARD_TYPE")
    def accountId: Rep[Long] = column[Long]("ACCOUNT_ID") // Foreign key to CreditCardAccounts

    // Index for hashedCardNumber
    def idxHashedCardNumber = index("IDX_HASHED_CARD_NUMBER", hashedCardNumber, unique = true)

    // Foreign key relationship
    def account: ForeignKeyQuery[CreditCardAccounts, CreditCardAccountDB] =
      foreignKey("FK_ACCOUNT", accountId, Accounts)(_.id)

    override def * : ProvenShape[CreditCardDB] =
      (encryptedCardNumber, hashedCardNumber, expiry, cvv, cardType, accountId).mapTo[CreditCardDB]
  }

  val Cards: TableQuery[CreditCards] = TableQuery[CreditCards]
}