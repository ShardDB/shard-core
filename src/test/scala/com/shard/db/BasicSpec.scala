package com.shard.db

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db
  */
import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.persistence.serialization.Snapshot
import akka.util.Timeout
import akka.pattern.ask
import com.shard.db.query.{Find, Insert, Size}
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class BasicSpec extends FlatSpec with Matchers {

  val db = Database
  import db.timeout

  "A shard" should "perform basic operations" in {

    val userTable = db.system.actorOf(Props(new UserShard("testBasic")), "userShardBasic")

    val henry = User(UUID.randomUUID(), 30, "Henry")

    val ins = Await.result((userTable ? Insert(henry)).mapTo[UUID], 2 seconds)
    val find = Await.result((userTable ? Find(henry)).mapTo[Option[User]].map{_.get}, 2 seconds)

    assert(ins == henry.id)
    assert(find == henry)

  }

}
