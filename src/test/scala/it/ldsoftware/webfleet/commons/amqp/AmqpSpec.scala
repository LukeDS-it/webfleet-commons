package it.ldsoftware.webfleet.commons.amqp

import akka.Done
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.testcontainers.containers.Network

import scala.concurrent.{ExecutionContext, Future}

class AmqpSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with FailFastCirceSupport
    with Eventually
    with ForAllTestContainer {

  val network: Network = Network.newNetwork()

  implicit val ec: ExecutionContext = ExecutionContext.global

  override val container: GenericContainer = GenericContainer("rabbitmq:3.8.5", Seq(5672))

  "The RabbitMQChannel" should {
    "publish to an exchange and read from a queue" in {
      val destination = "topic"
      val subject =
        new RabbitMqChannel(s"amqp://localhost:${container.mappedPort(5672)}", "exchange")

      val queue = subject.createQueueFor(destination)
      val consumer = subject.getConsumerFor[String](queue)

      var result: String = ""
      consumer.consume {
        case Left(value) =>
          Future {
            result = value.getMessage
            Done
          }
        case Right(value) =>
          Future {
            result = value.content
            Done
          }
      }

      subject.publish(destination, "id", "Published!")

      eventually {
        result shouldBe "Published!"
      }
    }
  }
}
