package com.shard.db.structure.schema

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Author: Nicholas Connor
  * Date: 6/19/16
  */
trait Index[T, V] {
  val _data: TrieMap[Any, V] = TrieMap.empty[Any, V]
  val getKey: (T) => Any
  def get(key: Any): Future[Option[V]]
  val name: String
  def add(item: T)(implicit schema: Schema[T]): Future[Any]
  def all = Future(_data.values)
}