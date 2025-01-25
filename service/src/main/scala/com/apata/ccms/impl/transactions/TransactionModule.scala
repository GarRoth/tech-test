package com.apata.ccms.impl.transactions

import com.apata.ccms.PaymentGatewayConfig
import com.apata.ccms.core.db.Dao
import com.apata.ccms.core.transactions.TransactionService
import com.apata.ccms.core.transactions.gateway.PaymentGatewayClient
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.transactions.gateway.PaymentGatewayClientImpl

import scala.concurrent.ExecutionContext

trait TransactionModule {

  implicit def executionContext: ExecutionContext

  def dao: Dao
  def paymentGatewayConfig: PaymentGatewayConfig
  def encryptionService: EncryptionService

  val paymentGatewayClient: PaymentGatewayClient = new PaymentGatewayClientImpl(paymentGatewayConfig)
  val transactionService: TransactionService = new TransactionServiceImpl(dao, paymentGatewayClient, encryptionService)

}
