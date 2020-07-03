package it.ldsoftware.webfleet.commons.amqp

import com.rabbitmq.client._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

import scala.concurrent.ExecutionContext

class RabbitMqChannel(url: String, exchange: String) extends ConnectionFactory {

  setUri(url)
  private val connection = newConnection()

  val channel: Channel = connection.createChannel()

  channel.exchangeDeclare(exchange, "direct", true)

  def publish[T](destination: String, entityId: String, value: T)(
      implicit encoder: Encoder[AmqpEnvelope[T]]
  ): Unit = {
    val envelope = AmqpEnvelope(entityId, value)
    channel.basicPublish(exchange, destination, null, envelope.asJson.noSpaces.getBytes)
  }

  def createNamedQueueFor(destination: String, queueName: String): Unit = {
    channel.queueDeclare(queueName, true, false, false, null)
    channel.queueBind(queueName, exchange, destination)
  }

  def createQueueFor(keyword: String): String = {
    val queueName = channel.queueDeclare().getQueue
    channel.queueBind(queueName, exchange, keyword)
    queueName
  }

  def getConsumerFor[T](
      queue: String
  )(implicit decoder: Decoder[T], ec: ExecutionContext): AmqpConsumer[T] =
    new AmqpConsumer[T](queue, channel)
}
