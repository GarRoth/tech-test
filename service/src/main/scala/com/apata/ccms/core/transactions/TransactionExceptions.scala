package com.apata.ccms.core.transactions

sealed class TransactionException(message: String) extends Throwable(message)

object TransactionExceptions {
  final case class InvalidTransactionRequest(reason: String) extends TransactionException(reason)
  final case class TransactionFailed(reason: String) extends TransactionException(reason)
}
