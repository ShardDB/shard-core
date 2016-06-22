package com.shard.db.query

import com.shard.db.Record
import com.shard.db.query.Ops.Op

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */




sealed trait Query {
  val cache = false
}

case object All
case class Where[T](expr: Either[(T) => Boolean, FilterExpression], override val cache: Boolean = false) extends Query
case class Find[T](record: T) extends Query
case class Insert[T](record: T) extends Query
case class Update(record: Record) extends Query
case object Size extends Query
