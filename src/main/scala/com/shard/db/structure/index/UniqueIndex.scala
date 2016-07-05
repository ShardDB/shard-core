package com.shard.db.structure.index

import com.shard.db.exception.UniqueKeyConstraintException

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
class UniqueIndex[T](override val name: String, override val getKey: (T) => Any) extends HashIndex[T](name, getKey) {
  override def add(item: T)(implicit schema: Schema[T]): Future[Any] = {
    exists(item).mapTo[Boolean].map {
      case true =>
        throw new UniqueKeyConstraintException("Unique index: key already exists!")
      case false =>
        _data(getKey(item)) = List(schema.primaryIndex.getKey(item))
    }
  }
}