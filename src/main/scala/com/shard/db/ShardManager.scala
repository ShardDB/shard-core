package com.shard.db

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume}
import akka.actor._
import com.shard.db.Messages.RunShard

class ShardManager extends Actor {

  override val supervisorStrategy = OneForOneStrategy () {

    case _: ActorKilledException =>
      for(x <- 1 to 10) {

        println("ACTOR KILLED")
      }
      Escalate
    case _: ActorInitializationException =>
      for(x <- 1 to 10) {

        println("ACTOR INIT FAILED")
      }
      Escalate
    case _: RuntimeException =>
      for(x <- 1 to 10) {
        println("RUN TIME EXCEPTION")
      }
      Resume
    case _ =>
      println("SOMETHING WENT WRONG")
      Restart // keep restarting faulty actor
  }

  def receive: Receive = {
    case Terminated(child) =>
      for(x <- 1 to 10) {
        println("SOMETHING WAS TERMINATED")
      }
    case RunShard(props, name) =>
      val act = context.actorOf(props, name)
      context.watch(act)
   // case gc: GetOrCreate => sender() ! getOrCreateActor(gc.props, gc.name)
    case "ping" => println("pong")
  }
}