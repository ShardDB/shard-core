package com.shard.db

import java.util.UUID

import akka.actor.Props
import com.shard.db.query.{Find, FindById, Insert}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import akka.pattern.ask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db
  */
class PerformanceSpec extends FlatSpec with Matchers {

  val db = Database
  import db.timeout

  val userTable = db.system.actorOf(Props(new UserShard("testThroughput")), "userShardThroughput")

  println("Sleeping for 5 seconds")
  Thread.sleep(5000L)
  // Small warmup --
  for(t <- 1 to 100000) {
    userTable ! "ping"
  }

  "A shard" should "have reasonable find throughput" in {
    val f = Future {

      val Henry = User(UUID.randomUUID(), 30, "Henry", "DeWalt")

      userTable ! Insert(Henry)

      val henries = (1 to 100000).map { t =>
        userTable ? Find(Henry)
      }

      val t = Await.result(Future.sequence(henries).mapTo[Seq[UUID]], 100.seconds)

      Thread.sleep(5000L)

      val timeForInserts = utils.timeInSeconds({
        val henries = (1 to 100000).map { t =>
          userTable ? Find(Henry)
        }

        val t = Await.result(Future.sequence(henries).mapTo[Seq[UUID]], 100.seconds)
      })

      println("Selects per second: " + (100000/timeForInserts/1000).toInt.toString + "K")
      assert(timeForInserts < 4.0)
    }

    Await.result(f, 120.seconds)
  }

  "A shard" should "have reasonable insert throughput" in {

    val f = Future {
      utils.timeInSeconds({
        val insertIds = (1 to 100000).map { t =>
          userTable ? Insert(User(UUID.randomUUID(), 30, "Henry", "DeWalt"))
        }
        val t = Await.result(Future.sequence(insertIds).mapTo[Seq[UUID]], 100.seconds)
      })

      Thread.sleep(5000L)

      val timeForInserts = utils.timeInSeconds({
          val insertIds = (1 to 100000).map { t =>
              userTable ? Insert(User(UUID.randomUUID(), 30, "Henry", "DeWalt"))
          }
          val t = Await.result(Future.sequence(insertIds).mapTo[Seq[UUID]], 100.seconds)
      })

      println("Requests per second: " + (100000/timeForInserts/1000).toInt.toString + "K")
      assert(timeForInserts < 4.0)
    }

    Await.result(f, 120.seconds)
  }
}
