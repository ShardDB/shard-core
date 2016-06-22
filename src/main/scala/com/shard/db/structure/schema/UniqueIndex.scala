package com.shard.db.structure.schema

import com.shard.db.exception.UniqueKeyConstraintException
import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
case class UniqueIndex[T](override val name: String, override val getKey: (T) => Any) extends HashIndex(name, getKey) {

  override val _data: mutable.Map[Any, Any] = mutable.Map.empty[Any, Any]

  override def get(key: Any): Seq[Any] = {
    _data.get(key) match {
      case Some(id) => Seq(id)
      case None => Seq.empty[Any]
    }
  }

  override def add(item: T)(implicit schema: Schema[T]): Unit = {
    if (exists(item)) {
      throw new UniqueKeyConstraintException("Unique index: key already exists!")
    } else {
      _data(getKey(item)) = schema.primaryIndex.getKey(item)
    }
  }

}