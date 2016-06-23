package com.shard.db

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db
  */

import java.util.UUID

import akka.actor.Props
import akka.pattern.ask
import com.shard.db.query.Ops.GreaterThan
import com.shard.db.query.{Find, InnerJoin, Insert, InsertMany}
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class BasicSpec extends FlatSpec with Matchers {

  val db = Database

  import db.timeout

  "A shard" should "perform basic operations" in {

    val userTable = db.system.actorOf(Props(new UserShard("testBasic")), "userShardBasic")

    val henry = User(UUID.randomUUID(), 30, "Henry", "DeWalt")

    val ins = Await.result((userTable ? Insert(henry)).mapTo[UUID], 2 seconds)
    val find = Await.result((userTable ? Find(henry)).mapTo[Option[User]].map {
      _.get
    }, 2 seconds)

    assert(ins == henry.id)
    assert(find == henry)

  }

  "Shards " should "join to other shards" in {
    val userTable = db.system.actorOf(Props(new UserShard("userShardJoin")), "userShardJoin")
    val orderTable = db.system.actorOf(Props(new OrderShard("orderShardJoin")), "orderShardJoin")

    /*
    import scala.util.Random


    val A = List(1, 2, 3, 4, 5, 6)
    A(Random.nextInt(A.size))
     */

    val users = Seq(
      User(UUID.randomUUID(), 35, "Minasde", "Komadfpf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
      , User(UUID.randomUUID(), 25, "Miasdne", "Komasdfpf")
      , User(UUID.randomUUID(), 15, "ASdfMiadne", "Kasdfompf")
      , User(UUID.randomUUID(), 5, "hhhMine", "Kompf")
      , User(UUID.randomUUID(), 395, "rrrMine", "Kompf")
    )

    // seed
    userTable ! InsertMany(users)

    import scala.util.Random
    orderTable ! InsertMany(
      Seq.fill(1000000)(
        Order(UUID.randomUUID(), users(Random.nextInt(users.size)).id, Random.nextInt(100).toFloat)
      )
    )

    import utils.timeInSeconds

    val f = Await.result((userTable ? InnerJoin("primaryKey", GreaterThan, orderTable, "user_id")).mapTo[Seq[(User, Order)]], 20 seconds)

    val time = timeInSeconds {
      val j = Await.result((userTable ? InnerJoin("primaryKey", GreaterThan, orderTable, "user_id")).mapTo[Seq[(User, Order)]], 20 seconds)
      println(j.size)
    }

    println("TIME ELAPSED: " + time.toString + " SECONDS")

    // join.foreach { j =>
    //   println(j._1.id.toString + "\t" + j._2.user_id.toString + "\t" + j._1.firstName + "\t" + j._2.price.toString)
    // }
    // println("Size of join is: " + join.size.toString)
  }

}
