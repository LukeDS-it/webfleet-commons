package it.ldsoftware.webfleet.commons.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.stream.KillSwitches
import it.ldsoftware.webfleet.commons.flows.EventFlow

import scala.concurrent.duration._

object EventProcessor {

  def apply(flow: EventFlow[_]): Behavior[Nothing] = Behaviors.setup[Nothing] { _ =>
    val killSwitch = KillSwitches.shared("eventProcessorSwitch")
    flow.run(killSwitch)
    Behaviors.receiveSignal[Nothing] {
      case (_, PostStop) =>
        killSwitch.shutdown()
        Behaviors.same
    }
  }

  def init(
      system: ActorSystem[_],
      flow: EventFlow[_]
  ): Unit = {
    val settings = ShardedDaemonProcessSettings(system)
      .withKeepAliveInterval(1.second)
      .withShardingSettings(ClusterShardingSettings(system))

    ShardedDaemonProcess(system).init[Nothing](
      flow.consumer.getClass.getSimpleName,
      1,
      _ => EventProcessor(flow),
      settings,
      None
    )
  }

}
