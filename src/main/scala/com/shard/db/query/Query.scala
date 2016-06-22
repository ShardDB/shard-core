package com.shard.db.query

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */
sealed trait Query {
  val cache = false

  val t = Seq.empty[String]

  def apply(q: Query): ConjugateQuery = {
    q match {
      case w: Where => ConjugateQuery(wheres = Seq(w))
    }
  }
}

trait Modifier extends Query
case object SelectStmt extends Modifier
case object InsertStmt extends Modifier
case object UpdateStmt extends Modifier
case object DeleteStmt extends Modifier

case class ConjugateQuery(
                           statement: Modifier = SelectStmt,
                           wheres: Seq[Where] = Seq.empty[Where],
                           sorts: Seq[Sort] = Seq.empty[Sort],
                           limit: Option[Limit] = None,
                           skip: Option[Skip] = None,
                           leftJoins: Seq[LeftJoin] = Seq.empty[LeftJoin],
                           innerJoins: Seq[InnerJoin] = Seq.empty[InnerJoin],
                           rightJoins: Seq[RightJoin] = Seq.empty[RightJoin]
                         ) extends Query


case object All
case class Where[T](expr: Either[(T) => Boolean, FilterExpression], override val cache: Boolean = false) extends Query
case class Find[T](record: T) extends Query
// What if I want
//     Insert(Seq())
//     Insert(T)
//     Insert()
// for example ConjugateQuery might just use Insert()
case class Insert[T](record: T) extends Query
case class InsertMany[T](record: Seq[T]) extends Query

case class Update[T](record: T) extends Query
case class Sort[T, B](expr: (T) => B) extends Query
case class LeftJoin[T]() extends Query
case class InnerJoin[T]() extends Query
case class RightJoin[T]() extends Query
case class Limit(num: Int) extends Query
case class Skip(num: Int) extends Query

class Test {
  val t = LeftJoin()
  val f = LeftJoin()(RightJoin())
}

case object Size extends Query
