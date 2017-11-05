package net.creasource.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.{JsString, JsValue}

import scala.collection.immutable.Seq

object APIRoutes {

  implicit val settings: RoutingSettings = RoutingSettings.apply(ConfigFactory.load())

  def routes: Route = {
    pathPrefix("api") {
      respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
        Route.seal(concat(
          get {
            pathPrefix("get") {
              complete(JsString("OK GET"))
            }
          },
          post {
            entity(as[JsValue]) { json =>
              complete(JsString("OK POST"))
            }
          },
          put {
            entity(as[JsValue]) { json =>
              complete(json)
            }
          },
          delete {
            complete(StatusCodes.OK)
          },
          options {
            val corsHeaders: Seq[HttpHeader] = Seq(
              RawHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"),
              RawHeader("Access-Control-Allow-Headers", "Content-Type")
            )
            respondWithHeaders(corsHeaders) {
              complete(StatusCodes.OK)
            }
          }
        ))
      }
    }
  }

}
