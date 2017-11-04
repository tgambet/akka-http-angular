package net.creasource.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.scaladsl.{Sink, Source}
import spray.json.{DefaultJsonProtocol, JsValue}

import scala.collection.immutable.Seq
import scala.concurrent.Future

object PersonJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

}

object APIRoutes {

  import SprayJsonSupport._

  def routes: Route = {
    pathPrefix("api") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        get {
          complete(StatusCodes.OK, """{"message":"OK"}""")
        } ~
        post {
          entity(as[JsValue]) { json =>
            complete(s"${json.prettyPrint}")
          }
        } ~
        options {
          val corsHeaders: Seq[HttpHeader] = Seq(
            RawHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"),
            RawHeader("Access-Control-Allow-Headers", "Content-Type")
          )
          respondWithHeaders(corsHeaders) {
            complete(StatusCodes.OK, "")
          }
        }
      }
    }
  }

//  def routes2Response: Route => HttpRequest => Future[HttpResponse] = (route) => (request) =>
//    route2HandlerFlow(route).runWith(Source[HttpRequest](List(request)), Sink.head)._2
//
//  val a: HttpRequest => Future[HttpResponse] = routes2Response(routes)

}
