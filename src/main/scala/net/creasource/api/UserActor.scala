package net.creasource.api

import akka.actor.{Actor, Props, Stash}
import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try

object UserActor {
  def props()(implicit materializer: ActorMaterializer): Props = Props(new UserActor())
}

case class JsonMessage(method: String, body: JsValue)

object JsonMessage extends JsonSupport {
  def unapply(arg: JsValue): Option[(String, JsValue)] = {
    Try(arg.convertTo[JsonMessage]).toOption.map(m => (m.method, m.body))
  }
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonMessageFormat: RootJsonFormat[JsonMessage] = jsonFormat2(JsonMessage.apply)
  implicit val httpResponseFormat: RootJsonWriter[HttpResponse] = new RootJsonWriter[HttpResponse] {
    override def write(obj: HttpResponse): JsValue =
      JsObject(
        "status" -> JsNumber(obj.status.intValue),
        "entity" -> JsString(obj.entity match {
          case HttpEntity.Strict(ct @ ContentTypes.`application/json`, body)  => body.decodeString(ct.charset.value)
          case HttpEntity.Strict(ct @ ContentTypes.`text/plain(UTF-8)`, body) => body.decodeString(ct.charset.value)
          case _ => throw new UnsupportedOperationException("Only strict application/json and text/plain endpoints are supported.")
        })
      )
  }
  implicit val httpRequestFormat: RootJsonReader[HttpRequest] = new RootJsonReader[HttpRequest] {
    override def read(json: JsValue): HttpRequest = {
      val (method, uri, headers, entity) = json match {
        case js: JsObject =>
          val method = js.fields.get("method") match {
            case Some(JsString("GET"))     => HttpMethods.GET
            case Some(JsString("POST"))    => HttpMethods.POST
            case Some(JsString("PUT"))     => HttpMethods.PUT
            case Some(JsString("DELETE"))  => HttpMethods.DELETE
            case Some(JsString("OPTIONS")) => HttpMethods.OPTIONS
            case Some(JsString("HEAD"))    => HttpMethods.HEAD
            case Some(m)                   => throw new UnsupportedOperationException(s"Method $m is not supported.")
            case _                         => throw new UnsupportedOperationException(s"No Method header found.")
          }
          val uri = js.fields.get("url") match {
            case Some(JsString(url)) => Uri(url)
            case _ => throw new UnsupportedOperationException(s"No url or malformed url parameter found.")
          }
          (method, uri, Nil, HttpEntity.Empty)
        case _ => throw new UnsupportedOperationException("The body of an HttpRequest message must be a JsObject.")
      }
      HttpRequest(method = method, uri = uri, headers = headers, entity = entity)
    }
  }
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
