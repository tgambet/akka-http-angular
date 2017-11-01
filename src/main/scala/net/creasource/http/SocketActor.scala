package net.creasource.http

import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, Stash, Status, SupervisorStrategy, Terminated}
import akka.event.Logging
import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.stream.scaladsl.Sink
import akka.stream.ActorMaterializer

import scala.concurrent.duration._

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
      case f @ Status.Failure(cause) =>
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