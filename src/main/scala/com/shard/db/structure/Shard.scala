package com.shard.db.structure

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef}
import akka.persistence.serialization.Snapshot
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.query._
import com.shard.db.Record
import com.shard.db.query.Ops.EqualTo
import com.shard.db.structure.schema.{Index, Schema}

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  */

abstract class Shard[T <: Record] extends PersistentActor with ActorLogging {

  val schema: Seq[Schema]
  var state: scala.collection.mutable.Map[UUID, T] = mutable.Map.empty[UUID, T]

  def receiveCommand = {

    case "ping" => persistAsync("ping") { evt => sender() ! "pong" }

    case All => persistAsync(All) { evt => sender() ! all }
    case i: Insert[T] => persistAsync(i) { evt => sender() ! insert(evt.record) }
    case f: Find[T] => persistAsync(f) { evt => sender() ! find(evt.record) }
    case w: Where => persistAsync(w) { evt =>
      evt.expr match {
        case Left(simp) => sender() ! where(simp)
        case Right(expr) => sender() ! where(expr)
      }
    }
    case Size => persistAsync(Size) { evt => sender() ! state.size }
    case Snapshot => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) => println(metadata.toString)
    case SaveSnapshotFailure(metadata, reason) => println(reason.getMessage)
  }

  def receiveRecover: Receive = {
    case SnapshotOffer(_, s: scala.collection.mutable.Map[UUID, T]) => state = s
  }

  private lazy val _indexes: Map[String, Index[T]] = {
    try {
      schema.flatMap {
        case i: Index[T] =>
          Some(i.name -> i)
        case _ => None
      }.toMap
    } catch {
      case e: Exception =>
        println(e.toString)
        println("Localized message:" + e.getLocalizedMessage)
        println(e.getStackTrace.mkString("\n"))
        Map.empty[String, Index[T]]
    }
  }

  //private lazy val _uniqueIndexes: Seq[UniqueIndex[T]] = _indexes.flatMap {
  //  case u: UniqueIndex[T] => Some(u)
  //  case _ => None
  //}

  private def indexData() = {
    _indexes.foreach { case(indexName, index) =>
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
        possibleIndex.map { i => i.get(expr.value)}
    }

    indexedIds match {
      case Some(ids) => ids.flatMap(state.get(_))
      case None => where(filterExpressionToSimple(expr))
    }
  }

  protected def find(record: T) = state.get(record._recordId)

  protected def all: Seq[T] = state.values.toSeq

  protected def insert(item: T): UUID = {
    _indexes.foreach { case(name, ui) => ui.add(item) }
    state(item._recordId) = item
    item._recordId
  }
}
