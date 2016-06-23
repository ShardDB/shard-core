package com.shard.db.structure.schema

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.structure.schema
  */
trait HashIndexTr[T, V] extends Index[T, V] {
  def exists(item: T): Future[Boolean] = Future(_data.contains(getKey(item)))
  def get(key: Any): Future[Option[V]] = Future(_data.get(key))
  def size() = Future(_data.size)
  def apply(key: Any) = get(key)
}

class HashIndex[T](override val name: String, override val getKey: (T) => Any) extends HashIndexTr[T, mutable.MutableList[Any]] {

 override val _data: TrieMap[Any, mutable.MutableList[Any]] = TrieMap.empty[Any, mutable.MutableList[Any]]

  override def add(item: T)(implicit schema: Schema[T]): Future[Any] = {
      exists(item).mapTo[Boolean].map {
        case true =>
          val key = getKey(item)
          _data(key) += schema.primaryIndex.getKey(item)
          key
        case false =>
          val key = getKey(item)
          _data(key) = mutable.MutableList(schema.primaryIndex.getKey(item))
          key
      }
  }
}
