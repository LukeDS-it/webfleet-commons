package it.ldsoftware.webfleet.commons.flows

import akka.persistence.query.Offset

import scala.concurrent.Future

trait OffsetManager {

  def writeOffset(consumer: String, offset: Offset): Future[Int]

  def getLastOffset(consumer: String): Future[Long]

}
