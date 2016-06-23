package com.shard.db.structure.schema

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */

case class Schema[T](
                      primaryIndex: PrimaryIndex[T],
                      storageEngine: String,
                      secondaryIndexes: Seq[HashIndex[T]] = Seq.empty[HashIndex[T]]
                    )
