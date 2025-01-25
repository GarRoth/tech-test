package com.apata.ccms.core.account

sealed class AccountException(message: String) extends Throwable(message)

object AccountExceptions {
  final case class InvalidCreationRequest(reason: String) extends AccountException(s"Invalid create request: $reason")
  final case class InvalidUpdateRequest(reason: String) extends AccountException(s"Invalid Update request: $reason")
  final case class InvalidDeletionRequest(reason: String) extends AccountException(s"Invalid delete request: $reason")

  final case class AccountRequestFailed(reason: String) extends AccountException(s"Account request failed: $reason")
}
