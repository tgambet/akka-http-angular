package net.creasource.http

import java.io.File

import akka.Done
import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, Stash, Status, SupervisorStrategy, Terminated}
import akka.event.Logging
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, OverflowStrategy, SharedKillSwitch}

import scala.concurrent.duration._
import scala.util.Try
import net.creasource.api._

object SocketActor {
  def props()(implicit materializer: ActorMaterializer): Props = Props[SocketActor]
}

class SocketActor()(implicit materializer: ActorMaterializer) extends Actor with Stash {
  private val logger = Logging(context.system, this)

  override def receive: Receive = {
    case sourceActor: ActorRef ⇒
      val user = context.watch(context.actorOf(UserActor.props(), "user"))
      unstashAll()
      context.become {
        case TextMessage.Strict(data)        => user ! data
        case BinaryMessage.Strict(_)         => // ignore
        case TextMessage.Streamed(stream)    => stream.runWith(Sink.ignore)
        case BinaryMessage.Streamed(stream)  => stream.runWith(Sink.ignore)
        case msg: String if sender() == user => sourceActor ! TextMessage(msg)
        case Terminated(`user`) =>
          logger.info("UserActor terminated. Terminating.")
          sourceActor ! Status.Success(())
          context.stop(self)
        case s @ Status.Success(_) =>
          logger.info("Socket closed. Terminating.")
          sourceActor ! s
          context.stop(self)
        case f @ Failure(cause) =>
          logger.error(cause, "Socket failed. Terminating.")
          sourceActor ! f
          context.stop(self)
      }
    case _ => stash()
  }

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1.minute, loggingEnabled = true) {
      case _: Exception => SupervisorStrategy.Stop
    }
}


/**
  * Created by Thomas on 16/02/2017.
  */
class WebServer(implicit val app: Application) extends HttpApp {

  implicit lazy val system: ActorSystem = app.system
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()

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

  override def postServerShutdown(attempt: Try[Done], system: ActorSystem): Unit = {
    killSwitch.shutdown()
    super.postServerShutdown(attempt, system)
  }

}



