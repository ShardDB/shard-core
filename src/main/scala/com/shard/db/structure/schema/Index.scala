package com.shard.db.structure.schema

import java.util.UUID

import com.shard.db.Record

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/19/16
  * Package: com.example
  */

trait Schema

//object IndexTest {
//  def by[T](f: T => Index): String = {
//    f
//    ""
//  }
//}

trait Index[T <: Record] extends Schema {
  val name: String
  val fromModel: (T) => Any
  val _data: mutable.Map[Any, scala.collection.mutable.MutableList[UUID]] = mutable.Map.empty[Any, mutable.MutableList[UUID]]
  def exists(key: Any): Boolean = _data.contains(key)
  def add(item: T) = {
    if (exists(fromModel(item))) {
      _data(fromModel(item)) += item._recordId
    } else {
      _data(fromModel(item)) = mutable.MutableList(item._recordId)
    }
  }
}

case class UniqueIndex[M <: Record](name: String, fromModel: (M) => Any) extends Index[M]