package com.shard.db.structure


import akka.actor.ActorLogging
import akka.persistence.serialization.Snapshot
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.query._
import com.shard.db.query.Ops.EqualTo
import com.shard.db.structure.schema.{HashIndex, Index, PrimaryIndex, Schema}
import Ops.Operators
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.pipe

import scala.collection.concurrent.TrieMap

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  */
abstract class Shard[T](
                       implicit val schema: Schema[T]
                       ) extends PersistentActor with ActorLogging {


  var state: PrimaryIndex[T] = schema.primaryIndex

  implicit val timeout = Timeout(10.seconds)

  def receiveCommand = {

    case "ping" => persistAsync("ping") { evt => sender() ! "pong" }

    case All => persistAsync(All) { evt => sender() ! all }

    /// without indexes we could do (L, R) => Boolean
    /// lets keep to the assumption that we
    /// only use HashIndexes for a sec
    case lj: InnerJoin => persistAsync(lj) { evt => innerJoin(evt) pipeTo sender()}

    case i: Insert[T] => persistAsync(i) { evt => insert(evt.record) pipeTo sender() }
    case i: InsertMany[T] => persistAsync(i) { evt => insertMany(evt.record) pipeTo sender() }

    case f: Find[T] => find(f.record) pipeTo sender()
    case fById: FindById => persistAsync(fById) { evt => state(fById.id) pipeTo sender() }

    case w: Where[T] => persistAsync(w) { evt =>
      where(evt.expr) pipeTo sender()
    }

    case Size => persistAsync(Size) { evt => state.size pipeTo sender() }
    case Snapshot => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) => println(metadata.toString)
    case SaveSnapshotFailure(metadata, reason) => println(reason.getMessage)
  }

  protected def innerJoin(evt: InnerJoin) = {
    val leftIndex = if(evt.leftKey == "primaryIndex") schema.primaryIndex._data else _indexes(evt.leftKey)._data
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

  def receiveRecover: Receive = {
    case SnapshotOffer(_, s: PrimaryIndex[T]) => state = s
  }

  private lazy val _indexes: Map[String, HashIndex[T]] = schema.secondaryIndexes.map(i => i.name -> i).toMap

  private def indexData() = {
    _indexes.foreach { case (indexName, index) =>
      state._data.foreach { case (key, item) =>
        try {
          index.add(item)
        } catch {
          case e: Exception => println(e.toString)
        }
      }
    }
  }

  protected def where(expr: (T) => Boolean): Future[Seq[T]] = all.map(_.filter(expr))

  private def filterExpressionToSimple(expr: FilterExpression): (T) => Boolean = {
    expr.op match {
      case EqualTo => (i: T) => false
    }
  }

  protected def where(expr: FilterExpression): Future[Option[Seq[T]]] = {
    val index = _indexes.getOrElse(expr.keyName, throw new RuntimeException("Wheres must use an index"))

    // current problem is a Unique Index would return 1 id, Hash Index many
    val ids = index.get(expr.value)

    ids.map {
      case Some(realIds) =>
        val t = Future.sequence(
          realIds.map(state.get(_))
        ).map(_.flatten).map(Some(_))
        t
      case None =>
        Future(None)
    }.flatMap(identity)
  }

  protected def find(record: T) = state.get(schema.primaryIndex.getKey(record))

  protected def all: Future[Seq[T]] = state.all.map(_.toSeq)

  protected def insertMany(items: Seq[T]): Future[Seq[Any]] = Future.sequence(items.map(insert))

  protected def insert(item: T): Future[Any] = {
    _indexes.foreach { case (name, ui) => ui.add(item) }
    state.add(item)
  }
}
