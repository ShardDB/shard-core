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
import com.shard.db.query._
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class BasicSpec extends FlatSpec with Matchers {

  "A shard" should "perform basic operations" in {

    val henry = User(UUID.randomUUID(), 30, "Henry", "DeWalt")

    val ins = Await.result(UserShard.insert(henry).mapTo[UUID], 20 seconds)
    val find = Await.result(UserShard.find(henry).map {
      _.get
    }, 20 seconds)

    assert(ins == henry.id)
    assert(find == henry)

  }

  "Shards" should "inner join to other shards" in {

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
    Await.result(UserShard.insert(users), 1 second)

    import scala.util.Random
    Await.result(OrderShard.insert(
      Seq.fill(50000)(
        Order(UUID.randomUUID(), users(Random.nextInt(users.size)).id, Random.nextInt(100).toFloat)
      )
    ), 3 seconds)

    val orderSize = Await.result(OrderShard.size, 3 seconds)
    val userSize = Await.result(UserShard.size, 3 seconds)

    println("NUMBER OF ORDERS ----- " + orderSize.toString)
    println("NUMBER OF USERS ----- " + userSize.toString)

    assert(orderSize == 50000)

    val join = utils.timeInSeconds {
      Await.result(UserShard.innerJoin("primaryIndex", GreaterThan, OrderShard.actorRef, "user_id"), 20 seconds)
    }

    println("JOIN SIZE ----- " + join.toString)


  }

}