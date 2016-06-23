package com.shard.db

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.serialization.Snapshot
import akka.util.Timeout
import com.shard.db.query.{Insert, Size}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Database {

  implicit val timeout = Timeout(15 seconds)

  val system = ActorSystem("Database-1")

}

