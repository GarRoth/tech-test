package com.apata.ccms.core.api

import akka.http.scaladsl.Http

import scala.concurrent.Future

/**
 *  The HttpService is responsible for taking the defined API routes for the server,
 *  and bootstraps a server with those endpoints.
 */
trait HttpService {

  /**
   *  Start the HttpService by creating the server using the provided API routes
   * @return
   */
  def start(): Future[Http.ServerBinding]
}

