package com.shard.db.test


import akka.actor.Props
import com.shard.db.structure.{Cluster, Shard}
import com.shard.db.Record


/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */

class UserShard extends Shard[User]

class UserCluster extends Cluster[User] {
  val props = Props(new UserShard())
  val numberOfShards = 5
}

case class User(age: Int, name: String) extends Record {

}