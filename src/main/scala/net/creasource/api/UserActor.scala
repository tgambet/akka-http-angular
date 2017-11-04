package net.creasource.api

import akka.actor.{Actor, Props, Stash}
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.Try

object UserActor {
  def props(): Props = Props[UserActor]
}

case class JsonMessage(method: String, body: JsValue)

object JsonMessage extends JsonSupport {
  def unapply(arg: JsValue): Option[(String, JsValue)] = {
    Try(arg.convertTo[JsonMessage]).toOption.map(m => (m.method, m.body))
  }
}

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[JsonMessage] = jsonFormat2(JsonMessage.apply)
}

class UserActor extends Actor with Stash with JsonSupport {
  private val logger = Logging(context.system, this)

  context.parent ! JsonMessage("ping", JsString("Hello World!")).toJson

  override def receive: Receive = {

    case value: JsValue =>
      handleMessages.applyOrElse(value, (v: JsValue) => logger.warning("Unhandled Json Message {}", v.prettyPrint))

    case value =>
      logger.error("UserActor should only receive Json Messages {}", value.toString)

  }

  def handleMessages: PartialFunction[JsValue, Unit] = {

    case JsonMessage("HttpRequest", body) => sender() ! body

    case a @ JsonMessage(_, _) => sender() ! a

  }

}
