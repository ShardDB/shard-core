package com.shard.db.structure

import akka.actor.ActorLogging
import akka.persistence.serialization.Snapshot
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.query._
import com.shard.db.query.Ops.EqualTo
import com.shard.db.structure.schema.{Index, Schema}
import Ops.Operators
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.pipe

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  */
abstract class Shard[T] extends PersistentActor with ActorLogging {

  implicit val schema: Schema[T]
  var state: scala.collection.mutable.Map[Any, T] = mutable.Map.empty[Any, T]

  implicit val timeout = Timeout(10.seconds)

  def receiveCommand = {

    case "ping" => persistAsync("ping") { evt => sender() ! "pong" }

    case All => persistAsync(All) { evt => sender() ! all }

    /// without indexes we could do (L, R) => Boolean
    /// lets keep to the assumption that we
    /// only use HashIndexes for a sec
    case lj: InnerJoin => persistAsync(lj) { evt =>
      val leftIndex = if(evt.leftKey == "primaryKey") schema.primaryIndex._data else _indexes(evt.leftKey)._data
      // Hrm this seems a little complex
      Future.sequence {
        // for every index in the Left,
        leftIndex.map { case (k1, ids1) =>
          // Query the right for IDS
          val rightFuture = (evt.rightTable ? Where(evt.rightKey === k1)).mapTo[Seq[Any]]
          // on every Right Record
          // if non-empty, join to left record
          rightFuture.map { rightSeq =>
            if(rightSeq.isEmpty) {
              None
            } else {
              Some(rightSeq.map { rightItem =>
                (state(k1), rightItem)
              })
            }
          }
        }
        // Flatten the shit out of it
      }.map {_.flatten.toSeq.flatten} pipeTo sender()
    }

    case i: Insert[T] => persistAsync(i) { evt => sender() ! insert(evt.record) }
    case i: InsertMany[T] => persistAsync(i) { evt => sender() ! insertMany(evt.record) }

    case f: Find[T] => persistAsync(f) { evt => sender() ! find(evt.record) }

    case w: Where[T] => persistAsync(w) { evt =>
      sender() ! where(evt.expr)
    }

    case Size => persistAsync(Size) { evt => sender() ! state.size }
    case Snapshot => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) => println(metadata.toString)
    case SaveSnapshotFailure(metadata, reason) => println(reason.getMessage)
  }

  def receiveRecover: Receive = {
    case SnapshotOffer(_, s: scala.collection.mutable.Map[Any, T]) => state = s
  }

  private lazy val _indexes: Map[String, Index[T]] = schema.secondaryIndexes.map(i => i.name -> i).toMap

  private def indexData() = {
    _indexes.foreach { case (indexName, index) =>
      state.foreach { case (key, item) =>
        try {
          index.add(item)
        } catch {
          case e: Exception => println(e.toString)
        }
      }
    }
  }

  protected def where(expr: (T) => Boolean): Seq[T] = all.filter(expr)

  private def filterExpressionToSimple(expr: FilterExpression): (T) => Boolean = {
    expr.op match {
      case EqualTo => (i: T) => false
    }
  }

  protected def where(expr: FilterExpression): Seq[T] = {

    val indexedIds = expr.op match {
      case EqualTo =>
        val possibleIndex = _indexes.get(expr.keyName)
        possibleIndex.map { i => i.get(expr.value) }
    }

    indexedIds match {
      case Some(ids) => ids.flatMap(state.get(_))
      case None => where(filterExpressionToSimple(expr))
    }
  }

  protected def find(record: T) = state.get(schema.primaryIndex.getKey(record))

  protected def all: Seq[T] = state.values.toSeq

  protected def insertMany(items: Seq[T]) = items.map(insert)

  protected def insert(item: T): Any = {
    schema.primaryIndex.add(item)
    _indexes.foreach { case (name, ui) => ui.add(item) }
    val pk = schema.primaryIndex.getKey(item)
    state(pk) = item
    pk
  }
}
