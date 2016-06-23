package com.shard.db.query

import akka.actor.ActorRef
import com.shard.db.query.Ops.Op
import com.shard.db.structure.Shard

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */
sealed trait Query {
  val cache = false
}

trait Statement extends Query
case object SelectStmt extends Statement
case object InsertStmt extends Statement
case object UpdateStmt extends Statement
case object DeleteStmt extends Statement

//case class ConjugateQuery[Q, R](
//                           statement: Statement = SelectStmt,
//                           wheres: Seq[Where] = Seq.empty[Where],
//                           sorts: Seq[Sort] = Seq.empty[Sort],
//                           limit: Option[Limit] = None,
//                           skip: Option[Skip] = None,
//                           leftJoins: Seq[LeftJoin] = Seq.empty[LeftJoin],
//                           innerJoins: Seq[InnerJoin] = Seq.empty[InnerJoin],
//                           rightJoins: Seq[RightJoin] = Seq.empty[RightJoin]
//                         ) extends Query


case object All
case class Where[T](expr: FilterExpression, override val cache: Boolean = false) extends Query
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


// InnerJoin(on "id" === customers "user_id")

trait JoinQuery {
  val leftKey: String
  val op: Op
  val rightTable: ActorRef
  val rightKey: String
}

case class LeftJoin(
                        leftKey: String,
                        op: Op,
                        rightTable: ActorRef,
                        rightKey: String
                      ) extends JoinQuery

case class InnerJoin(
                         leftKey: String,
                         op: Op,
                         rightTable: ActorRef,
                         rightKey: String
                       ) extends JoinQuery

case class RightJoin(
                         leftKey: String,
                         op: Op,
                         rightTable: ActorRef,
                         rightKey: String
                       ) extends JoinQuery

case class Limit(num: Int) extends Query
case class Skip(num: Int) extends Query
case class Reduce[T](op: (T, T) => T) extends Query
case class FindById(id: Any) extends Query
case class GetIndex(name: String)

case object Size extends Query

class Test {

  Reduce[(String, Int)]( (a: (String, Int), b: (String, Int)) => (a._1 + b._1, a._2+b._2) )

}