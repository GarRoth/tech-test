package com.apata.ccms

import com.apata.ccms.impl.api.HttpConfig
import pureconfig.ConfigSource

case class ApplicationConfig(
  service: ServiceInfo,
  http: HttpConfig,
  keystoreConfig: KeystoreConfig,
  dbConfig: DbConfig,
  paymentGatewayConfig: PaymentGatewayConfig
)

object ApplicationConfig {
  def apply(): ApplicationConfig = config

  private lazy val config: ApplicationConfig = {
    import pureconfig.generic.auto._
    ConfigSource
      .default
      .load[ApplicationConfig]
      .fold(failures => sys.error(failures.toString()), identity)
  }
}

// ------- Application Config case classes -------

case class ServiceInfo(name: String)

case class KeystoreConfig(secretKeyAlias: String, password: String)

case class DbConfig(driver: String, url: String, user: String, password: String)

case class PaymentGatewayConfig(scheme: String, host: String, port: Int)


