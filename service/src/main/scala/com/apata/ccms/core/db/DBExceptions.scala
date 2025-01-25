package com.apata.ccms.core.db

sealed class DBException(message: String) extends Throwable(message)

object DBExceptions {
  case object UnexpectedDBException extends DBException(s"Unexpected DB Exception while processing request")
}
