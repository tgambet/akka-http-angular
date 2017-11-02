package net.creasource.http

import akka.actor.{ActorRef, ActorSystem, Status}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy, SharedKillSwitch}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import net.creasource.http.actors.SocketActor

import scala.concurrent.Future

trait SocketWebServer extends WebServer { self: WebServer =>

  protected val socketsKillSwitch: SharedKillSwitch = KillSwitches.shared("sockets")

  def socketFlow: Flow[Message, Message, Any] = {

    val socketActor: ActorRef = system.actorOf(SocketActor.props())

    val flow: Flow[Message, Message, ActorRef] =
      Flow.fromSinkAndSourceMat(
        Sink.actorRef(socketActor, Status.Success(())),
        Source.actorRef(1000, OverflowStrategy.fail)
      )(Keep.right)

    flow.mapMaterializedValue(sourceActor => socketActor ! sourceActor)
  }

  override def stop(): Future[Unit] = {
    system.log.info("Killing open sockets.")
    socketsKillSwitch.shutdown()
    super.stop()
  }

  override def routes: Route =
    path("socket") {
      extractUpgradeToWebSocket { _ =>
        handleWebSocketMessages(socketFlow.via(socketsKillSwitch.flow))
      }
    } ~ super.routes

}
