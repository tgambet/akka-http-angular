package net.creasource.http

import akka.actor.{ActorRef, ActorSystem, Status}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy, SharedKillSwitch}
import net.creasource.core._

import scala.concurrent.ExecutionContext

trait WebServer {

  implicit val app: Application

  implicit lazy val system: ActorSystem = app.system
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()

  implicit lazy val dispatcher: ExecutionContext = system.dispatcher

  val killSwitch: SharedKillSwitch = KillSwitches.shared("sockets")

  def socketFlow: Flow[Message, Message, Any] = {

    val socketActor: ActorRef = system.actorOf(SocketActor.props())

    val flow: Flow[Message, Message, ActorRef] =
      Flow.fromSinkAndSourceMat(
        Sink.actorRef(socketActor, Status.Success(())),
        Source.actorRef(1000, OverflowStrategy.fail)
      )(Keep.right)

    flow.mapMaterializedValue(sourceActor => socketActor ! sourceActor).via(killSwitch.flow)
  }

  def routes: Route =
    path("socket") {
      handleWebSocketMessages(socketFlow)
    } ~
    extractUnmatchedPath { path =>
      encodeResponse {
        headerValueByName("Accept") { accept =>
          val serveIndexIfNotFound: RejectionHandler =
            RejectionHandler.newBuilder()
              .handleNotFound {
                if (accept.contains("text/html")) {
                  getFromResource("dist/index.html")
                } else {
                  complete(StatusCodes.NotFound, "The requested resource could not be found.")
                }
              }
              .result()
          handleRejections(serveIndexIfNotFound) {
            getFromResourceDirectory("dist")
          }
        }
      }
  }

}



