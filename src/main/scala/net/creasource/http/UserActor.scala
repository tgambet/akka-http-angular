package net.creasource.http

import akka.actor.{Actor, Props, Stash}

object UserActor {
  def props(): Props = Props[UserActor]
}

class UserActor extends Actor with Stash {

  override def receive: Receive = {
    case message => sender() ! message
  }

}
