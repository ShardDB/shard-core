package com.shard.db.query

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.query
  */

object Ops {
  sealed trait Op
  case object GreaterThan extends Op
  case object GreaterThanOrEqualTo extends Op
  case object LessThan extends Op
  case object LessThanOrEqualTo extends Op
  case object EqualTo extends Op

  implicit class OpOperators(o: Op) {
    def >(value: Any) = FilterExpression("primaryIndex", o, value)
    def ===(value: Any): FilterExpression = FilterExpression("primaryIndex", o, value)
  }

  implicit class StringOperators(s: String) {
    def >(value: Any) = FilterExpression(s, Ops.GreaterThan, value)
    def <(value: Any) = FilterExpression(s, Ops.LessThan, value)
    def >=(value: Any) = FilterExpression(s, Ops.GreaterThanOrEqualTo, value)
    def <=(value: Any) = FilterExpression(s, Ops.LessThanOrEqualTo, value)
    def ===(value: Any): FilterExpression = FilterExpression(s, Ops.EqualTo, value)
  }
}

