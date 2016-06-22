package com.shard.db.structure.schema

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
case class PrimaryIndex[T](override val getKey: (T) => Any) extends UniqueIndex(name="primaryKey", getKey)