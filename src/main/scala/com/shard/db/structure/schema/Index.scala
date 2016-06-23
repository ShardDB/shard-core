package com.shard.db.structure.schema

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/19/16
  */
trait Index[T] {
  val _data: mutable.Map[Any, scala.collection.mutable.MutableList[Any]] = mutable.Map.empty[Any, mutable.MutableList[Any]]
  val getKey: (T) => Any
  def get(key: Any): Seq[Any]
  val name: String
  def add(item: T)(implicit schema: Schema[T]): Unit
}