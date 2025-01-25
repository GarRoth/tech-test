package com.apata.ccms.impl.db;

import com.apata.ccms.core.db.{CreditCardAccountDB, CreditCardDB}
import com.apata.ccms.impl.TestUtils
import org.h2.jdbcx.JdbcDataSource
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@RunWith(classOf[JUnitRunner])
class DaoImplTest extends AnyFlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {

  // In-memory H2 database for testing
  val dataSource = new JdbcDataSource()
  dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
  dataSource.setUser("test")
  dataSource.setPassword("")

  val db = JdbcBackend.Database.forConfig("db-config")
  val dao = new DaoImpl(db)

  val testAccount: CreditCardAccountDB = TestUtils.validTestDbAccount
  val testCard: CreditCardDB = TestUtils.validTestDbCard

  // Setting up the schema and inserting test data
  val setup = DBIO.seq(
    DBSchema.Accounts.schema.create,
    DBSchema.Cards.schema.create,
    DBSchema.Accounts += testAccount,
    DBSchema.Cards += testCard
  )

  override def beforeAll(): Unit = db.run(setup).futureValue

  /**
   * Note these tests are a bit flaky as each test impacts DB state. Look to cleanup after each test case
   */
  behavior of "DAOImpl"

  it should "retrieve all accounts and associated cards" in {
    val result: Future[Seq[(CreditCardAccountDB, CreditCardDB)]] = dao.findAll

    whenReady(result) { accounts =>
      accounts should have size 1
      accounts.head._1.cardholderName shouldEqual TestUtils.validTestDbAccount.cardholderName
      accounts.head._2.hashedCardNumber shouldEqual TestUtils.validTestDbCard.hashedCardNumber
    }
  }

  it should "successfully create an account and associated card" in {
    val account = CreditCardAccountDB(0L, "John Doe", 5000, "USD")
    val card = CreditCardDB("encrypted123", "hashed123", "12/25", 123, "VISA", 0L)

    val result: Future[Long] = dao.create(account, card)

    whenReady(result) { accountId =>
      //Dao strips the account number passed in, to allow slick to handle the account ID (Primary Key)
      accountId should be > 0L
    }
  }

  it should "retrieve an account by ID" in {
    val result: Future[Option[(CreditCardAccountDB, CreditCardDB)]] = dao.findById(testAccount.id)

    whenReady(result) { accountOption =>
      accountOption shouldBe defined
      accountOption.get._1.cardholderName shouldEqual testAccount.cardholderName
    }
  }

  it should "return None for a non-existent account ID" in {
    val result: Future[Option[(CreditCardAccountDB, CreditCardDB)]] = dao.findById(1337L)

    whenReady(result) { accountOption =>
      accountOption shouldBe empty
    }
  }

  it should "delete an account and associated card" in {
    val result: Future[Int] = dao.delete(testAccount.id)

    whenReady(result) { rowsDeleted =>
      rowsDeleted shouldEqual 1
    }
  }
}