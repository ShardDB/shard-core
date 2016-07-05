package com.shard.db.structure.index

import com.shard.db.exception.UniqueKeyConstraintException

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
class PrimaryIndex[T](override val getKey: (T) => Any) extends HashIndexTr[T, T] {
  _data = TrieMap.empty[Any, T]

  override val name = "primaryIndex"
  def add(item: T)(implicit schema: Schema[T]): Future[Any] = {
    exists(item).mapTo[Boolean].map {
      case true =>
        println(
          _data(getKey(item))
        )
        throw new UniqueKeyConstraintException("Unique index: key already exists!")
      case false =>
        val key = getKey(item)
        _data(key) = item
        key
    }
  }
}