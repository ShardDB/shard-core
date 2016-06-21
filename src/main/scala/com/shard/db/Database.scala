package com.shard.db

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.shard.db.query.Insert
import com.shard.db.test.{User, UserCluster, UserShard}

import scala.concurrent.Await
import scala.concurrent.duration._

object Database extends App {

  def time[R](block: => R, msg: String): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + ((t1 - t0) / 1000000000.0) + "s ---" + msg)
    result
  }

  implicit val timeout = Timeout(5 seconds) // needed for `?` below

  val system = ActorSystem("Database-1")

  val userTable = system.actorOf(Props[UserShard], "userTable")
  val shardedTable = system.actorOf(Props[UserCluster], "shardedUserTable")

  val warmUp = Seq.fill[Int](3)(3)
  val times = Seq.fill[Int](50)(3)

  time(
    warmUp.foreach {
      t =>
        Await.result(userTable ? Insert(User(30, "Henry")), 5.seconds)
    }, "Regular table"
  )

  time(
    times.foreach {
      t =>
        Await.result(userTable ? Insert(User(30, "Henry")), 5.seconds)
    }, "Regular table"
  )

  time(
    warmUp.foreach {
      t =>
        Await.result(shardedTable ? Insert(User(30, "Henry")), 5.seconds)
    }, "Sharded Table Warmup"
  )

  time(
    times.foreach {
      t =>
        Await.result(shardedTable ? Insert(User(30, "Henry")), 5.seconds)
    }, "Sharded Table"
  )



  //val entireTable = (userTable ? All).mapTo[Seq[User]]

//println(
//  Await.result(entireTable, 20 seconds).size.toString + "<----- All"
//)

//userTable ! Find(User(30, "Henry"))
//userTable ! Snapshot

  system.awaitTermination()
}