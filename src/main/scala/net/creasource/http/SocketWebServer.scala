package net.creasource.http

import akka.actor.{ActorRef, Props, Status}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{KillSwitches, OverflowStrategy, SharedKillSwitch}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

import net.creasource.http.actors.{SocketActor, SocketActorSupervisor}

trait SocketWebServer extends WebServer { self: WebServer =>

  protected val userActorProps: Props

  protected val keepAliveMessage: Option[TextMessage] = Some(TextMessage("""{"method":"keepAlive"}"""))
  protected val keepAliveTimeout: FiniteDuration = 1.minute

  private val socketsKillSwitch: SharedKillSwitch = KillSwitches.shared("sockets")

  private lazy val supervisor = system.actorOf(SocketActorSupervisor.props(), "sockets")

  def socketFlow: Future[Flow[Message, Message, Unit]] = {
    import akka.pattern.ask
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val timeout: Timeout = 1.second
    (supervisor ? SocketActor.props(userActorProps)).mapTo[ActorRef].map { socketActor =>

      val flow: Flow[Message, Message, ActorRef] =
        Flow.fromSinkAndSourceMat(
          Sink.actorRef(socketActor, Status.Success(())),
          Source.actorRef(1000, OverflowStrategy.fail)
        )(Keep.right)

      val flow2: Flow[Message, Message, Unit] = flow.mapMaterializedValue(sourceActor => socketActor ! sourceActor)

      keepAliveMessage match {
        case Some(message) => flow2.keepAlive(keepAliveTimeout, () => message)
        case None          => flow2
      }
    }
  }

  override def stop(): Future[Unit] = {
    system.log.info("Killing open sockets.")
    socketsKillSwitch.shutdown()
    super.stop()
  }

  override def routes: Route =
    path("socket") {
      extractUpgradeToWebSocket { _ =>
        onSuccess(socketFlow) { socketFlow =>
          handleWebSocketMessages(socketFlow.via(socketsKillSwitch.flow))
        }
      }
    } ~ super.routes

}
