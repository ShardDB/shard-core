package com.shard.db.query

import com.shard.db.Record
import com.shard.db.query.Ops.Op

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */

object Ops {
  sealed trait Op
  case object GreaterThan extends Op
  case object GreaterThanOrEqualTo extends Op
  case object LessThan extends Op
  case object LessThanOrEqualTo extends Op
  case object EqualTo extends Op

  implicit class Operators(s: String) {
    def >(value: Any) = FilterExpression(s, Ops.GreaterThan, value)
    def <(value: Any) = FilterExpression(s, Ops.LessThan, value)
    def >=(value: Any) = FilterExpression(s, Ops.GreaterThanOrEqualTo, value)
    def <=(value: Any) = FilterExpression(s, Ops.LessThanOrEqualTo, value)
    def ==(value: Any) = FilterExpression(s, Ops.EqualTo, value)
  }
}

case class FilterExpression(keyName: String, op: Op, value: Any)

trait Query {
  val cache = false
}

case object All
case class Where[T](expr: Either[(T) => Boolean, FilterExpression], override val cache: Boolean = false) extends Query
case class Find[T](record: T) extends Query
case class Insert[T](record: T) extends Query
case class Update(record: Record) extends Query
case object Size extends Query
