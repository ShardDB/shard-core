package com.shard.db.structure.schema

import com.shard.db.exception.UniqueKeyConstraintException
import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
class UniqueIndex[T](override val name: String, override val getKey: (T) => Any) extends HashIndex(name, getKey) {

  override def add(item: T)(implicit schema: Schema[T]): Unit = {
    if (exists(item)) {
      throw new UniqueKeyConstraintException("Unique index: key already exists!")
    } else {
      _data(getKey(item)) = mutable.MutableList(schema.primaryIndex.getKey(item))
    }
  }

}