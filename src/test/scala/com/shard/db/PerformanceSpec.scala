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

  val Henry = User(UUID.randomUUID(), 30, "Henry", "DeWalt")

  "A shard" should "have reasonable find throughput" in {
    val f = Future {
      UserShard.insert(Henry)

      val henries = (1 to 100000).map { t =>
        UserShard.find(Henry)
      }

      val t = Await.result(Future.sequence(henries).mapTo[Seq[UUID]], 100.seconds)

      Thread.sleep(5000L)

      val timeForInserts = utils.timeInSeconds({
        val henries = (1 to 100000).map { t =>
          UserShard.find(Henry)
        }

        val t = Await.result(Future.sequence(henries).mapTo[Seq[UUID]], 360.seconds)
      })

      println("Selects per second: " + (100000/timeForInserts/1000).toInt.toString + "K")
      //assert(timeForInserts < 4.0)
    }

    Await.result(f, 360.seconds)
  }

  "A shard" should "have reasonable insert throughput" in {

    val f = Future {
      val timeForInserts = utils.timeInSeconds({
          val insertIds = (1 to 1000000).map { t =>
              UserShard.insert(User(UUID.randomUUID(), 30, "Henry", "DeWalt"))
          }
          val t = Await.result(Future.sequence(insertIds).mapTo[Seq[UUID]], 480.seconds)
      })

      println("Time for 2 million inserts " + timeForInserts.toString + " Seconds")
      println("Requests per second: " + (1000000/timeForInserts/1000).toInt.toString + "K")
     // assert(timeForInserts < 4.0)
    }

    Await.result(f, 480.seconds)

    val timeForFindAtEnd = utils.timeInSeconds {
      val h = Await.result(UserShard.findById(Henry.id), 10.seconds)

      h match {
        case Some(u) => println(u.firstName)
        case None => println("nadda found")
      }
    }

    println("Time for finds " + timeForFindAtEnd.toString + " Seconds")
    UserShard.takeSnapshot
    Thread.sleep(5000L)
  }
}
