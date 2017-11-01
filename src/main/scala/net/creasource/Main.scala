package net.creasource

import akka.http.scaladsl.Http

import scala.concurrent.Future
import scala.io.StdIn

import net.creasource.core.Application
import net.creasource.http.WebServer

object Main extends App with WebServer {

  implicit val app: Application = Application()

  private val host = app.conf.getString("http.host")
  private val port = app.conf.getInt("http.port")
  private val stopOnReturn  = app.conf.getBoolean("http.stopOnReturn")

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)

  bindingFuture.foreach { _ =>
    app.system.log.info("Server online at http://{}:{}/", host, port)
  }

  bindingFuture.failed.foreach { ex =>
    app.system.log.error(ex, "Failed to bind to {}:{}!", host, port)
  }

  if (stopOnReturn) {
    println(s"Press RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete { _ =>
        killSwitch.shutdown()
        app.shutdown()
      }
  }

}
