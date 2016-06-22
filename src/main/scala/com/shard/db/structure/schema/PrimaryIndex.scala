package com.shard.db.structure.schema

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
class PrimaryIndex[T](override val getKey: (T) => Any) extends UniqueIndex[T](name="primaryKey", getKey)