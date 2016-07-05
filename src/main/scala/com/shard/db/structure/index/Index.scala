package com.shard.db.structure.index

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Author: Nicholas Connor
  * Date: 6/19/16
  */
trait Index[T, V] {
  var _data: scala.collection.mutable.Map[Any, V] = mutable.Map.empty[Any, V]
  val getKey: (T) => Any
  def get(key: Any): Future[Option[V]]
  val name: String
  def add(item: T)(implicit schema: Schema[T]): Future[Any]
  def all = Future(_data.values)
}