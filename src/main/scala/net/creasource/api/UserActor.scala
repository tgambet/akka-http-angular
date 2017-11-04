package net.creasource.api

import akka.actor.{Actor, Props, Stash}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.Try

object UserActor {
  def props(): Props = Props[UserActor]
}

case class JsonMessage(method: String, message: JsValue)

object JsonMessage extends JsonSupport {
  def unapply(arg: JsValue): Option[(String, JsValue)] = {
    //try {
      Try(arg.convertTo[JsonMessage]).toOption.map(m => (m.method, m.message))
    //}
  }
}

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[JsonMessage] = jsonFormat2(JsonMessage.apply)
}

class UserActor extends Actor with Stash with JsonSupport {

  context.parent ! JsonMessage("ping", JsString("Hello World!")).toJson.compactPrint

  override def receive: Receive = {
    case message: String => {
      println(message)
      JsonParser(message) match {
        case JsonMessage(_, _) => println("OK")
        case _ =>
      }

      sender() ! JsonMessage("pong", JsonParser(message)).toJson.compactPrint
    }
  }

}
