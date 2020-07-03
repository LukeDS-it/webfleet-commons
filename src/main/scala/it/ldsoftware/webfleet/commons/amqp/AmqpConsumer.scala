package it.ldsoftware.webfleet.commons.amqp

import akka.Done
import com.rabbitmq.client._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.{ExecutionContext, Future}

class AmqpConsumer[T](queueName: String, channel: Channel)(
    implicit decoder: Decoder[T],
    ec: ExecutionContext
) {

  private val autoAckOff = false

  private def wrappedConsumer(
      worker: Either[Error, AmqpEnvelope[T]] => Future[Done]
  ): DefaultConsumer =
    new DefaultConsumer(channel) {
      override def handleDelivery(
          tag: String,
          env: Envelope,
          props: AMQP.BasicProperties,
          body: Array[Byte]
      ): Unit = {
        val bodyString = new String(body)

        val x = decode[AmqpEnvelope[T]](bodyString) match {
          case Left(value)  => worker(Left(new Error(s"Could not parse $bodyString: $value")))
          case Right(value) => worker(Right(value))
        }

        x.map(_ => channel.basicAck(env.getDeliveryTag, false))
      }
    }

  def consume(worker: Either[Error, AmqpEnvelope[T]] => Future[Done]): Unit = {
    channel.basicConsume(queueName, autoAckOff, "consumer-tag", wrappedConsumer(worker))
  }

}
