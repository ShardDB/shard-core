package com.shard.db.structure

import java.util.UUID

import akka.actor.Props
import akka.routing.{ActorRefRoutee, Broadcast, ConsistentHashingRoutingLogic, Router}
import com.shard.db.Record

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  */
// abstract class Cluster[T <: Record] extends Shard[T] {
//
//   val numberOfShards: Int
//   val props: Props
//
//   private lazy val router = {
//     val routees = Vector.fill(numberOfShards) {
//       val r = context.actorOf(props, name=UUID.randomUUID().toString)
//       context watch r
//       ActorRefRoutee(r)
//     }
//
//     Router(
//       ConsistentHashingRoutingLogic(
//         context.system,
//         numberOfShards * 5,
//         hashMapping = shardByMessage),
//       routees
//     )
//   }
//
//   private def getShardId(record: T): Int = {
//     record._recordId.hashCode() % numberOfShards
//   }
//
//   private lazy val shardByMessage = {
//       case Find(record: T) => getShardId(record)
//       case Insert(record: T) => getShardId(record)
//   }
//
//   override def receive = {
//     case All => router.route(Broadcast(All), sender())
//     case Insert(record: T) => router.route(Insert(record), sender())
//     case Find(record: T) => router.route(Find(record), sender())
//     case Snapshot => snapshot()
//   }
//
//   def send(message: Any) = {
//
//   }
//
//   def ask(message: Any) = {
//     //val shard = shardByMessagemessage
//   }
// }
