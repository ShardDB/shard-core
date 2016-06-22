package com.shard.db


import java.util.UUID

import com.shard.db.structure.Shard
import com.shard.db.structure.schema.{Index, PrimaryIndex, Schema}


/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */
class UserShard(
                 override val persistenceId: String
               ) extends Shard[User] {
  val schema = Schema(
    primaryIndex = PrimaryIndex[User](getKey = (u: User) => u.id),
    storageEngine = "HashMap",
    secondaryIndexes = Seq.empty[Index[User]]
  )
}


case class User(id: UUID, age: Int, name: String)
