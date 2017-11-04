package net.creasource.api

import akka.actor.{Actor, Props, Stash}
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._

import scala.collection.immutable.Seq
import scala.concurrent.Future

object UserActor {
  def props()(implicit materializer: ActorMaterializer): Props = Props(new UserActor())
}

class UserActor()(implicit materializer: ActorMaterializer) extends Actor with Stash with JsonSupport {

  import context.dispatcher

  private val logger = Logging(context.system, this)

  private val client = context.parent

  override def receive: Receive = {

    case value: JsValue =>
      handleMessages.applyOrElse(value, (v: JsValue) => logger.warning("Unhandled Json message:\n{}", v.prettyPrint))

    case value =>
      logger.error("UserActor should only receive Json Messages: {}", value.toString)

  }

  def handleMessages: PartialFunction[JsValue, Unit] = {

    case JsonMessage("HttpRequest", body) => toResponse(body.convertTo[HttpRequest]) foreach (client ! _.toJson)

    case a @ JsonMessage(_, _) => client ! a

  }

  def routes2Response: Route => HttpRequest => Future[HttpResponse] = (route) => (request) =>
    route2HandlerFlow(route).runWith(Source[HttpRequest](Seq(request)), Sink.head)._2

  val toResponse: HttpRequest => Future[HttpResponse] = routes2Response(APIRoutes.routes)

}
