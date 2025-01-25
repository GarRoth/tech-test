package com.apata.ccms.impl.account

import com.apata.ccms.core.db.Dao
import com.apata.ccms.encryption.EncryptionService

import scala.concurrent.ExecutionContext

trait AccountModule {

  implicit def executionContext: ExecutionContext

  def dao: Dao
  def encryptionService: EncryptionService

  val accountService = new AccountServiceImpl(dao, encryptionService)

}
