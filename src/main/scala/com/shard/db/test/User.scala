package com.shard.db.test


import com.shard.db.structure.Shard
import com.shard.db.Record
import com.shard.db.structure.schema.Schema


/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */

class UserShard(
                 override val persistenceId: String,
                 override val schema: Seq[Schema] = Seq.empty[Schema]
               ) extends Shard[User]

// class UserCluster(
//                    override val persistenceId: String,
//                    override val schema: Seq[Schema] = Seq.empty[Schema]
//                  ) extends Cluster[User] {
//   val props = Props(new UserShard(persistenceId))
//   val numberOfShards = 5
// }

case class User(age: Int, name: String) extends Record {

}