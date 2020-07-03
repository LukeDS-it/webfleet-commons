package it.ldsoftware.webfleet.commons.flows

import akka.NotUsed
import akka.persistence.query.{Offset, Sequence}
import akka.persistence.query.scaladsl.EventsByTagQuery
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import akka.stream.{Materializer, SharedKillSwitch}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class EventFlow[T](
    tag: String,
    readJournal: EventsByTagQuery,
    offsetManager: OffsetManager,
    val consumer: EventConsumer[T]
)(
    implicit ec: ExecutionContext,
    mat: Materializer
) {
  val consumerName: String = consumer.getClass.getSimpleName

  def run(killSwitch: SharedKillSwitch): Unit =
    RestartSource
      .withBackoff(500.millis, maxBackoff = 20.seconds, randomFactor = 0.1) { () =>
        Source.futureSource {
          offsetManager.getLastOffset(consumerName).map { offset =>
            processEvents(offset)
              .mapAsync(1)(offsetManager.writeOffset(consumerName, _))
          }
        }
      }
      .via(killSwitch.flow)
      .runWith(Sink.ignore)

  def processEvents(offset: Long): Source[Offset, NotUsed] =
    readJournal.eventsByTag(tag, Sequence(offset)).mapAsync(1) { envelope =>
      envelope.event match {
        case e: T =>
          consumer
            .consume(envelope.persistenceId, e)
            .recover(th => println(th))
            .map(_ => envelope.offset)
        case unknown => Future.failed(new IllegalArgumentException(s"Cannot process $unknown"))
      }
    }
}
