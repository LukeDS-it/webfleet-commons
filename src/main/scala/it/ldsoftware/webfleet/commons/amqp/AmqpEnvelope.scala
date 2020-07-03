package it.ldsoftware.webfleet.commons.amqp

case class AmqpEnvelope[T](entityId: String, content: T)
