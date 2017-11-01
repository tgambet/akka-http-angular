package net.creasource.api

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await

object Application {

  def apply() = new Application

}

class Application {

  val conf: Config = ConfigFactory.load()

  val system: ActorSystem = ActorSystem("MySystem", conf)

  system.log.info("Application starting.")

  def shutdown() {
    system.log.info("Shutting down Akka system.")
    import scala.concurrent.duration._
    Await.result(system.terminate(), 5.seconds)
  }

}