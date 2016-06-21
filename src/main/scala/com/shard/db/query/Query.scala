package com.shard.db.query

import com.shard.db.Record
import com.shard.db.query.QueryTypeAliases.SimpleFilter

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */

object QueryTypeAliases {
  type SimpleFilter = (Record) => Boolean
}

trait Query {
  val cache = false
}



case object All
case class Where(exec: SimpleFilter, override val cache: Boolean = false) extends Query
case class Find[T <: Record](record: T) extends Query
case class Insert[T <: Record](record: T) extends Query
case class Update(record: Record) extends Query
case object Size extends Query
case object Snapshot extends Query

class Test {

  val f = Where(exec = (t: Record) => toString == "")

  f match {
    case Where(exec: SimpleFilter, false) => handleWhere(exec)
  }

  def handleWhere(expr: (Record) => Boolean)
}