package com.shard.db.structure.schema

import java.util.UUID

import com.shard.db.Record
import com.shard.db.exception.UniqueKeyConstraintException

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/19/16
  * Package: com.example
  */

trait Schema

trait Index[T <: Record] extends Schema {
  val _data: mutable.Iterable
  val getKey: (T) => Any
  def get(key: Any): Seq[UUID]
  val name: String
  def add(item: T): Unit
}

case class HashIndex[T <: Record](name: String, getKey: (T) => Any) extends Index {

  val _data: mutable.Map[Any, scala.collection.mutable.MutableList[UUID]] = mutable.Map.empty[Any, mutable.MutableList[UUID]]

  def exists(item: T): Boolean = _data.contains(getKey(item))

  def get(key: Any): Seq[UUID] = {
    _data.get(key) match {
      case Some(ids) => ids.toSeq
      case None => Seq.empty[UUID]
    }
  }

  def add(item: T): Unit = {
    if (exists(item)) {
      _data(getKey(item)) += item._recordId
    } else {
      _data(getKey(item)) = mutable.MutableList(item._recordId)
    }
  }
}

case class UniqueIndex[T <: Record](override val name: String, override val getKey: (T) => Any) extends HashIndex[T](name, getKey) {

  override val _data: mutable.Map[Any, UUID] = mutable.Map.empty[Any, UUID]

  override def get(key: Any): Seq[UUID] = {
    _data.get(key) match {
      case Some(id) => Seq(id)
      case None => Seq.empty[UUID]
    }
  }

  override def add(item: T): Unit = {
    if (exists(item)) {
      throw new UniqueKeyConstraintException("Unique index: key already exists!")
    } else {
      _data(getKey(item)) = item._recordId
    }
  }

}