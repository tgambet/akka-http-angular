package net.creasource

import akka.http.scaladsl.settings.ServerSettings
import net.creasource.api.Application
import net.creasource.http.WebServer

/**
  * Created by Thomas on 16/02/2017.
  */
object Main extends App {

  implicit val app: Application = Application()

  private val address = app.conf.getString("http.binding.address")
  private val port = app.conf.getInt("http.binding.port")

  new WebServer().startServer(address, port, ServerSettings(app.conf), app.system)

  app.shutdown()

}
