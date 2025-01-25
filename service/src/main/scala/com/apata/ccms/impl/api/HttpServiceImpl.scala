package com.apata.ccms.impl.api

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.apata.ccms.core.api.{HttpEndpoint, HttpService}
import com.typesafe.scalalogging.StrictLogging

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpServiceImpl(httpEndpoints: Seq[HttpEndpoint], httpConfig: HttpConfig) (implicit actorSystem: ActorSystem) extends HttpService {
  private val routes: Route = concat(httpEndpoints.map(_.route) : _*)

  private lazy val serverBinding: Future[Http.ServerBinding] = HttpService.bindRoutes(routes, httpConfig)

  def start(): Future[Http.ServerBinding] = serverBinding
}

object HttpService extends StrictLogging {

  def bindRoutes(routes: Route, config: HttpConfig)(implicit actorSystem: ActorSystem): Future[Http.ServerBinding] = {
    //Using http dispatcher for server configuration, so it is separate from other threads in the application
    implicit val executionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.dispatchers.http-dispatcher")

    val futureServer: Future[Http.ServerBinding] = Http()
      .bindAndHandle(routes, config.interface, config.port)

    futureServer.andThen {
      case Failure(exception) =>
        logger.error(s"Encountered exception [${exception.getMessage}] when attempting to bind the server: interface=${config.interface}, port=${config.port}")
      case Success(serverBinding) =>
        logger.info(s"Successfully bound server socket: interface=${config.interface}, port=${config.port}")

        val shutdown = CoordinatedShutdown(actorSystem)

        shutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, "http-terminate") { () =>
          logger.info(s"Shutting down server. Will unbind socket: address=${serverBinding.localAddress}")
          serverBinding.terminate(4.seconds).map(_ => Done) //TODO: Ensure coordinatedShutdown timeout is larger than http termination timeout for graceful shutdown
        }
    }
  }

  //TODO: Remove if not using https
  def createHttpsContext(keyStore: KeyStore, password: Array[Char]): HttpsConnectionContext = {
    val kmf = KeyManagerFactory.getInstance("SunX509")
    kmf.init(keyStore, password)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.getKeyManagers, null, new SecureRandom())

    ConnectionContext.https(sslContext)
  }
}



final case class HttpConfig (
  interface: String,
  port: Int
)
