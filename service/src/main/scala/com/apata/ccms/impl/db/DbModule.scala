package com.apata.ccms.impl.db

import com.apata.ccms.core.db.Dao
import com.apata.ccms.impl.db.DBSchema.{Accounts, Cards}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext

trait DbModule {

  implicit def executionContext: ExecutionContext

  private val schema = Accounts.schema ++ Cards.schema

  // Create tables programmatically
  private val setupAction = DBIO.seq(
    schema.create // This will create both tables and their constraints
  )

  private val db: JdbcBackend.DatabaseDef = JdbcBackend.Database.forConfig("db-config")

  db.run(setupAction)

  def dao: Dao = new DaoImpl(db)

}
