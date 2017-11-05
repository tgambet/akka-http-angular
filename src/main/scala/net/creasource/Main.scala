package net.creasource

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn
import net.creasource.core.Application
import net.creasource.api._
import net.creasource.http.{SPAWebServer, SocketWebServer}

import scala.concurrent.duration._

object Main extends App with SPAWebServer with SocketWebServer {

  implicit val app: Application = Application()

  override implicit val system: ActorSystem = app.system

  override val userActorProps: Props = UserActor.props(APIRoutes.routes)

  private val host = app.conf.getString("http.host")
  private val port = app.conf.getInt("http.port")
  private val stopOnReturn = app.conf.getBoolean("http.stop-on-return")
  private val keepAliveInSec = app.conf.getInt("http.webSocket.keep-alive")

  override val keepAliveTimeout: FiniteDuration = keepAliveInSec.seconds

  override def routes: Route = APIRoutes.routes ~ super.routes

  start(host, port) foreach { _ =>
    if (stopOnReturn) {
      system.log.info(s"Press RETURN to stop...")
      StdIn.readLine()
      stop().onComplete(_ => app.shutdown())
    }
  }

}
