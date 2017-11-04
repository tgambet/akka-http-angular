package net.creasource

import akka.actor.{ActorSystem, Props}

import scala.io.StdIn
import net.creasource.core.Application
import net.creasource.api.UserActor
import net.creasource.http.{SPAWebServer, SocketWebServer}

object Main extends App with SPAWebServer with SocketWebServer {

  implicit val app: Application = Application()

  override implicit val system: ActorSystem = app.system

  override val userActorProps: Props = UserActor.props()

  private val host = app.conf.getString("http.host")
  private val port = app.conf.getInt("http.port")
  private val stopOnReturn = app.conf.getBoolean("http.stopOnReturn")

  start(host, port)

  //override def routes: Route = complete(StatusCodes.OK, "OK") ~ super.routes

  if (stopOnReturn) {
    system.log.info(s"Press RETURN to stop...")
    StdIn.readLine()
    import system.dispatcher
    stop().onComplete(_ => app.shutdown())
  }

}
