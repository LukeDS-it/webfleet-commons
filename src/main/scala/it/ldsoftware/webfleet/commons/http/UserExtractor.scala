package it.ldsoftware.webfleet.commons.http

import it.ldsoftware.webfleet.commons.security.User

import scala.concurrent.Future

trait UserExtractor {
  def extractUser(jwt: String, domain: Option[String]): Future[Option[User]]
}
