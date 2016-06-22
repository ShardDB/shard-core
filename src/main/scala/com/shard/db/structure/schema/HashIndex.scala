package com.shard.db.structure.schema

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
class HashIndex[T](override val name: String, override val getKey: (T) => Any) extends Index[T] {

  val _data: mutable.Map[Any, scala.collection.mutable.MutableList[Any]] = mutable.Map.empty[Any, mutable.MutableList[Any]]

  def exists(item: T): Boolean = _data.contains(getKey(item))

  def get(key: Any): Seq[Any] = {
    _data.get(key) match {
      case Some(ids) => ids.toSeq
      case None => Seq.empty[Any]
    }
  }

  override def add(item: T)(implicit schema: Schema[T]): Unit = {
    if (exists(item)) {
      _data(getKey(item)) += schema.primaryIndex.getKey(item)
    } else {
      _data(getKey(item)) = mutable.MutableList(schema.primaryIndex.getKey(item))
    }
  }
}
