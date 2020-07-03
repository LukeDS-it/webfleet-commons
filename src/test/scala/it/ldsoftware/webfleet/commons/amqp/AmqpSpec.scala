package it.ldsoftware.webfleet.commons.amqp

import akka.Done
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import it.ldsoftware.webfleet.commons.amqp.AmqpSpec.ExpectedObject
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
      val consumer = subject.getConsumerFor[ExpectedObject](queue)
      val expected = ExpectedObject("Published!")

      var result: ExpectedObject = null
      consumer.consume {
        case Left(value) =>
          Future {
            result = ExpectedObject(value.getMessage)
            Done
          }
        case Right(value) =>
          Future {
            result = value.content
            Done
          }
      }

      subject.publish(destination, "id", expected)

      eventually {
        result shouldBe expected
      }
    }

    "return an error if there is a failure to parse the message" in {
      val destination = "failure"
      val subject =
        new RabbitMqChannel(s"amqp://localhost:${container.mappedPort(5672)}", "exchange")

      val queue = subject.createQueueFor(destination)
      val consumer = subject.getConsumerFor[ExpectedObject](queue)

      var result: String = ""
      consumer.consume {
        case Left(value) =>
          Future {
            result = value.getMessage
            Done
          }
        case Right(value) =>
          Future {
            result = value.content.toString
            Done
          }
      }

      subject.publish(destination, "id", "Published!")

      eventually {
        result shouldBe """Could not parse {"entityId":"id","content":"Published!"}: DecodingFailure(Attempt to decode value on failed cursor, List(DownField(message), DownField(content)))"""
      }
    }

    "create a named queue" in {
      val destination = "topic"
      val queue = "my-custom-queue"
      val subject =
        new RabbitMqChannel(s"amqp://localhost:${container.mappedPort(5672)}", "exchange")

      subject.createNamedQueueFor(destination, queue)
      val consumer = subject.getConsumerFor[ExpectedObject](queue)
      val expected = ExpectedObject("Published!")

      var result: ExpectedObject = null
      consumer.consume {
        case Left(value) =>
          Future {
            result = ExpectedObject(value.getMessage)
            Done
          }
        case Right(value) =>
          Future {
            result = value.content
            Done
          }
      }

      subject.publish(destination, "id", expected)

      eventually {
        result shouldBe expected
      }
    }
  }
}

object AmqpSpec {
  case class ExpectedObject(message: String)
}
