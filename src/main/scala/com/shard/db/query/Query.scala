package com.shard.db.query

import akka.actor.{ActorRef, ActorSelection}
import com.shard.db.query.Ops.Op

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.query
  */
sealed trait Query {
  val cache = false
}

trait Statement[R, T] extends Query {
  val tableRef: ActorRef
  val transformer: (R) => T
}

//case class InsertStatement[R, T](tableRef: ActorRef, transformer: (R) => T) extends Statement[R, T]
//case class UpdateStatement[R, T](tableRef: ActorRef, transformer: (R) => T, implicit val oldRec: T) extends Statement[R, T]


//case class ConjugateQuery[Q, R](
//                           statement: Statement,
//                           wheres: Option[Seq[Where]] = None,
//                           sorts: Option[Seq[Sort]] = None,
//                           limit: Option[Limit] = None,
//                           skip: Option[Skip] = None,
//                           leftJoins: Option[Seq[LeftJoin]] = None,
//                           innerJoins: Option[Seq[InnerJoin]] = None,
//                           rightJoins: Option[Seq[RightJoin]] = None
//                         ) extends Query


case object All
case class Where[T](expr: FilterExpression, override val cache: Boolean = false) extends Query
case class WhereIndex(name: String) extends Query

case class Find[T](record: T) extends Query
// What if I want
//     Insert(Seq())
//     Insert(T)
//     Insert()
// for example ConjugateQuery might just use Insert()
case class Insert[T](record: T) extends Query
case class InsertMany[T](record: Seq[T]) extends Query
case class Sort[T, B](expr: (T) => B) extends Query

case class Upsert[T](record: T) extends Query
case class Update[T](record: T) extends Query
case class Delete[T](record: T) extends Query

// InnerJoin(on "id" === customers "user_id")

trait JoinQuery {
  val leftKey: String
  val op: Op
  val rightTable: ActorSelection
  val rightKey: String
}

case class LeftJoin(
                        leftKey: String,
                        op: Op,
                        rightTable: ActorSelection,
                        rightKey: String
                      ) extends JoinQuery

case class InnerJoin(
                         leftKey: String,
                         op: Op,
                         rightTable: ActorSelection,
                         rightKey: String
                       ) extends JoinQuery

case class RightJoin(
                      leftKey: String,
                      op: Op,
                      rightTable: ActorSelection,
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
  // userTable ?   Insert ( userTable, (o: Order, u:user) => User("", o.price))
  //            // Where ( userTable ("id") > 5 )
  //            // InnerJoin (userTable("order_id") == orderTable("id")
}