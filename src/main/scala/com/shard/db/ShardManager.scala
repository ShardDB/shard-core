package com.shard.db

import akka.actor._
import com.shard.db.Messages.GetOrCreate

import scala.collection.mutable

/**
  * http://stackoverflow.com/questions/10766187/on-demand-actor-get-or-else-create
  * @author nietaki
  */
class ShardManager extends Actor {
  val keyActorRefMap: scala.collection.mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()
  val actorRefKeyMap: scala.collection.mutable.Map[ActorRef, String] = mutable.Map[ActorRef, String]()

  def getOrCreateActor(props: => Props, name: String): ActorRef = {
    println(s"Creating actor of $name")
    keyActorRefMap get name match {
      case Some(ar) => ar
      case None =>
        val newRef: ActorRef = context.actorOf(props, name)
        //newRef shouldn't be present in the map already (if the key is different)
        actorRefKeyMap get newRef match{
          case Some(x) => throw new Exception{}
          case None =>
        }
        keyActorRefMap += Tuple2(name, newRef)
        actorRefKeyMap += Tuple2(newRef, name)
        newRef
    }
  }

  def receive: Receive = {
    case Terminated(ref) => {
      //removing both key and actor ref from both maps
      val pr: Option[Tuple2[String, ActorRef]] = for{
        key <- actorRefKeyMap.get(ref)
        reref <- keyActorRefMap.get(key)
      } yield (key, reref)

      pr match {
        case None => //error
        case Some((key, reref)) => {
          actorRefKeyMap -= ref
          keyActorRefMap -= key
        }
      }
    }
    case gc: GetOrCreate => sender() ! getOrCreateActor(gc.props, gc.name)
    case "ping" => println("pong")
  }
}