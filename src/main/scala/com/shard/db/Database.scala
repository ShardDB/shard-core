package com.shard.db

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.serialization.Snapshot
import akka.util.Timeout
import com.shard.db.query.{All, Insert, Size}
import com.shard.db.test.{User, UserShard}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Database extends App {

  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("Database-1")

  val userTable = system.actorOf(Props(new UserShard("test")), "userShard")


  val times = Seq.fill[Int](100000)(3)
  val timesWar = Seq.fill[Int](50000)(3)

  Future {
    println("Small warmup")
    timesWar.foreach { t =>
      userTable ! "ping"
    }

    Thread.sleep(5000L)
    println("Slept for 10 seconds")

    utils.time(
      {
        val f = times.map {
          t =>
            userTable ? Insert(User(30, "Henry"))
        }
        val t = Await.result(Future.sequence(f).mapTo[Seq[UUID]], 100.seconds)
      }, "Regular table"
    )

    val entireTable = (userTable ? Size).mapTo[Int]

    println(
      Await.result(entireTable, 20 seconds).toString + "<----- All"
    )

    userTable ! Snapshot
  }


  //userTable ! Find(User(30, "Henry"))
  //userTable ! Snapshot

  system.awaitTermination()
}

/*
package com.shard.db

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.persistence.serialization.Snapshot
import akka.util.Timeout
import com.shard.db.query.{All, Insert, Size}
import com.shard.db.test.{User, UserShard}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Database extends App {

  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("Database-1")

  val userTable = system.actorOf(Props(new UserShard("test")), "userShard")


  val times = Seq.fill[Int](100000)(3)
  val timesWar = Seq.fill[Int](20000)(3)

  Future {
    println("Small warmup")
    timesWar.foreach { t =>
      userTable ! "ping"
    }

    Thread.sleep(5000L)
    println("Slept for 10 seconds")

    utils.time(
      {
        val f = times.map {
          t =>
            userTable ? Insert(User(30, "Henry"))
        }
        val t = Await.result(Future.sequence(f).mapTo[Seq[UUID]], 100.seconds)
      }, "Regular table"
    )

    val entireTable = (userTable ? Size).mapTo[Int]

    println(
      Await.result(entireTable, 20 seconds).toString + "<----- All"
    )

    userTable ! Snapshot
  }


//userTable ! Find(User(30, "Henry"))
//userTable ! Snapshot

  system.awaitTermination()
}
 */