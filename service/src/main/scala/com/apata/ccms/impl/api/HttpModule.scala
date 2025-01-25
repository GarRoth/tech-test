package com.apata.ccms.impl.api

import akka.actor.ActorSystem
import com.apata.ccms.core.account.AccountService
import com.apata.ccms.core.api.HttpService
import com.apata.ccms.core.transactions.TransactionService

trait HttpModule {

  implicit def actorSystem: ActorSystem

  def httpConfig: HttpConfig
  def accountService: AccountService
  def transactionService: TransactionService

  def creditCardEndpoint: CreditCardAccountManagementEndpoint = new CreditCardAccountManagementEndpoint(accountService, transactionService)

  lazy val httpService: HttpService = {
    new HttpServiceImpl(Seq(creditCardEndpoint), httpConfig)
  }

}
