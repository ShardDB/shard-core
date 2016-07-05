package com.shard.db.structure.index

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */

case class Schema[T](
                      name: String,
                      primaryIndex: PrimaryIndex[T],
                      storageEngine: String,
                      secondaryIndexes: Seq[HashIndex[T]] = Seq.empty[HashIndex[T]],
                      foreignConstraints: Option[Seq[ForeignIndexConstraint[T]]] = None
                    )
