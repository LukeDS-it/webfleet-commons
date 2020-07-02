package it.ldsoftware.webfleet.commons.http

import scala.concurrent.Future

trait PermissionProvider {
  def getPermissions(domain: String, user: String): Future[Set[String]]
}
