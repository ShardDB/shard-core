package com.shard.db.structure.index

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

class HashIndex[T](override val name: String, override val getKey: (T) => Any) extends HashIndexTr[T, List[Any]] {
 // var _data = mutable.Map.empty[Any, List[Any]]
  var timesAdded = 0

  override def add(item: T)(implicit schema: Schema[T]): Future[Any] = {
    Future(_data.synchronized {
      _data.contains(getKey(item)) match {
        case true =>
          val key = getKey(item)
          _data(key) = schema.primaryIndex.getKey(item) :: _data(key)
          key
        case false =>
          val key = getKey(item)
          //println(_data.get(key))
          _data(key) = List(schema.primaryIndex.getKey(item))
          key
      }
    })
  }
}
