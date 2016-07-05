package com.shard.db


import java.util.UUID
import com.shard.db.structure.Shard
import com.shard.db.structure.index.{HashIndex, PrimaryIndex, Schema}

/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */

object UserShard extends {
  val database = MyDatabase
  val schema = Schema(
    name = "Users",
    primaryIndex = new PrimaryIndex[User](getKey = (u: User) => u.id),
    storageEngine = "HashMap",
    secondaryIndexes = Seq(
      new HashIndex[User]("lastName", (u: User) => u.lastName)
    )
  )
} with Shard[User]

object OrderShard extends {
  val database = MyDatabase
  val schema = Schema(
    name = "Orders",
    primaryIndex = new PrimaryIndex[Order](getKey = (u: Order) => u.id),
    storageEngine = "HashMap",
    secondaryIndexes = Seq(
      new HashIndex[Order]("user_id", (o: Order) => o.user_id)
    )
  )
} with Shard[Order]

case class Order(id: UUID, user_id: UUID, price: Double)

case class User(id: UUID, age: Int, firstName: String, lastName: String)