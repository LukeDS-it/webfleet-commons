package it.ldsoftware.webfleet.commons.flows

import akka.Done

import scala.concurrent.Future

trait EventConsumer[T] {
  def consume(actorId: String, event: T): Future[Done]
}
