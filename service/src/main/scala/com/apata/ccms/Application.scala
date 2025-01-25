package com.apata.ccms

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import com.apata.ccms.encryption.EncryptionService
import com.apata.ccms.impl.account.AccountModule
import com.apata.ccms.impl.api.{HttpConfig, HttpModule}
import com.apata.ccms.impl.db.DbModule
import com.apata.ccms.impl.encryption.EncryptionServiceImpl
import com.apata.ccms.impl.transactions.TransactionModule
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object Application extends App {
  private val applicationConfig = ApplicationConfig()

  private val application = new Application(applicationConfig)
  private val whenFinished = application.run()

  Await.result(whenFinished, Duration.Inf)
}

class Application(applicationConfig: ApplicationConfig)
  extends HttpModule with DbModule with AccountModule with TransactionModule with StrictLogging {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()
  implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher

  lazy val httpConfig: HttpConfig = applicationConfig.http
  lazy val paymentGatewayConfig: PaymentGatewayConfig = applicationConfig.paymentGatewayConfig

  lazy val encryptionService: EncryptionService = new EncryptionServiceImpl(applicationConfig.keystoreConfig)

  def run(): Future[Done] = {
    logger.info(s"Starting Application ${applicationConfig.service.name}")

    httpService.start() //Coordinate this better

    //TODO Make sure to add coord shutdown
    CoordinatedShutdown(actorSystem).addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, "shutdown") { () =>
      logger.info(s"Terminating ${applicationConfig.service.name}")
      Future.successful(Done)
    }

    actorSystem.whenTerminated.map(_ => Done)
  }

}
