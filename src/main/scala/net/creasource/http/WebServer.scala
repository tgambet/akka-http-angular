package net.creasource.http

import java.io.File

import akka.actor.{ActorRef, ActorSystem, Status}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy, SharedKillSwitch}

import net.creasource.api._

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
    // Websocket
    path("socket") {
      handleWebSocketMessages(socketFlow)
    } ~
    // 1. check if file exists
    // 2. if so serve the file
    // 3. if not check Accept header
    // 4. if text/html serve index.html
    // 5. if not reject
    extractUnmatchedPath { path =>
      encodeResponse {
        headerValueByName("Accept") { accept =>
          val webFolder = "web/dist"
          val requestedFile = new File(s"$webFolder$path")
          if (requestedFile.isFile)
            getFromFile(requestedFile)
          else if (accept.contains("text/html"))
            getFromFile(s"$webFolder/index.html")
          else
            reject()
        }
      }
  }

}



