package net.creasource.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

trait SPAWebServer extends WebServer { self: WebServer =>

  override def routes: Route =
    extractUnmatchedPath { path =>
      encodeResponse {
        headerValueByName("Accept") { accept =>
          val serveIndexIfNotFound: RejectionHandler =
            RejectionHandler.newBuilder()
              .handleNotFound {
                if (accept.contains("text/html")) {
                  getFromResource("dist/index.html")
                } else {
                  complete(StatusCodes.NotFound, "The requested resource could not be found.")
                }
              }
              .result()
          handleRejections(serveIndexIfNotFound) {
            getFromResourceDirectory("dist")
          }
        }
      }
    } ~ super.routes

}
