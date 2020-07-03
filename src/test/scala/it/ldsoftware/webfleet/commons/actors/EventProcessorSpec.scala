package it.ldsoftware.webfleet.commons.actors

import java.time.ZonedDateTime

import akka.Done
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.persistence.query.scaladsl.EventsByTagQuery
import akka.persistence.query.{EventEnvelope, Sequence}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import it.ldsoftware.webfleet.commons.flows.{EventConsumer, EventFlow, OffsetManager}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class EventProcessorSpec
    extends ScalaTestWithActorTestKit(
      EventSourcedBehaviorTestKit.config.withFallback(ConfigFactory.load("application"))
    )
    with AnyWordSpecLike
    with BeforeAndAfterEach
    with MockitoSugar {

  implicit val ec: ExecutionContext = testKit.system.executionContext

  val tag = "tag"

  "The event processor" should {
    "process events saved from Content" in {
      val readJournal = mock[EventsByTagQuery]
      val db = mock[OffsetManager]
      val envelope = makeEnvelope

      val probe = testKit.createTestProbe[String]("waiting")

      val flow = new EventFlow(tag, readJournal, db, new ProbeEventConsumer(probe))

      when(db.getLastOffset("ProbeEventConsumer")).thenReturn(Future.successful(0L))
      when(readJournal.eventsByTag(tag, Sequence(0))).thenReturn(Source(Seq(envelope)))

      EventProcessor.init(system, flow)

      probe.expectMessage("id: String event")
    }
  }

  def makeEnvelope: EventEnvelope = EventEnvelope(
    akka.persistence.query.Sequence(1),
    "id",
    1L,
    "String event",
    ZonedDateTime.now.toInstant.getEpochSecond
  )

  class ProbeEventConsumer(probe: TestProbe[String]) extends EventConsumer[String] {
    override def consume(str: String, evt: String): Future[Done] =
      Future(probe.ref ! s"$str: $evt")
        .map(_ => akka.Done)
  }
}
