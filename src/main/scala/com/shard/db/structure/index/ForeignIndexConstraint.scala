package com.shard.db.structure.index

import akka.actor.ActorRef

/**
  * Author: Nicholas Connor
  * Date: 6/25/16
  * Package: com.shard.db.structure.schema
  */
case class ForeignIndexConstraint[T](foreignShard: ActorRef, foreignKey: String, getKey: (T) => Any)