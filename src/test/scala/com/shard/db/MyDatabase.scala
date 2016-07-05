package com.shard.db


import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.serialization.Snapshot
import akka.util.Timeout
import com.shard.db.query.{Insert, Size}
import com.shard.db.structure.ShardActor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object MyDatabase extends Database