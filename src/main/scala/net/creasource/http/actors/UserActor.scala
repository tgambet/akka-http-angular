package net.creasource.http.actors

import akka.actor.{Actor, Props, Stash}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object UserActor {
  def props(): Props = Props[UserActor]
}

case class JsonMessage(method: String, message: String)

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[JsonMessage] = jsonFormat2(JsonMessage)
}

class UserActor extends Actor with Stash with JsonSupport {

  override def receive: Receive = {
    case message: String => {
      println(message)
      sender() ! JsonMessage("pong", message).toJson.toString()
    }
  }

}
