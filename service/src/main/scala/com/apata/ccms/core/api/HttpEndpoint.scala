package com.apata.ccms.core.api

import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging

/**
 *  A HttpEndpoint definition
 */
trait HttpEndpoint extends StrictLogging {

  /**
   * The definition of the given endpoint's Route
   */
  def route: Route

}
