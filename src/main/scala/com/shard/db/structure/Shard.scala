package com.shard.db.structure

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.exception.UniqueKeyConstraintException
import com.shard.db.query._
import com.shard.db.{Record, utils}
import com.shard.db.structure.schema.{Index, Schema, UniqueIndex}

import scala.collection.mutable

/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  */

trait ShardRouter {
  def getShardId(message: Any): Int
  val shards: ActorRef
  def route()
  // Future.sequence(workers map (_ ? msg)) pipeTo sender
}

abstract class Shard[T <: Record] extends PersistentActor with ActorLogging {

  val schema: Seq[Schema]

  var state: scala.collection.mutable.Map[UUID, T] = mutable.Map.empty[UUID, T]

  def receiveCommand = {
    case All => persist(All) { evt => sender() ! all }
    case Insert(item: T) => persist(Insert(item: T)) { evt => sender() ! insert(evt.record) }
    case Find(record: T) => persist(Find(record: T)) { evt => sender() ! find(evt.record) }
    case Where(expr, false) => where(expr)

    case Snapshot => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata)         => println(metadata.toString)
    case SaveSnapshotFailure(metadata, reason) => println(reason.getMessage)
  }

  def receiveRecover: Receive = {
    case SnapshotOffer(_, s: scala.collection.mutable.Map[UUID, T]) =>
      state = s
  }

  def pickleTo(item: T, filename: String): Unit

  def unpickleFrom(filename: String): T

  private lazy val _indexes: Seq[Index[T]] = {
    try {
      schema.flatMap {
        case i: Index[T] => Some(i)
        case _ => None
      }
    } catch {
      case e: Exception =>
        println(e.toString)
        println("Localized message:" + e.getLocalizedMessage)
        println(e.getStackTrace.mkString("\n"))
        Seq.empty[Index[T]]
    }
  }

  private lazy val _uniqueIndexes: Seq[UniqueIndex[T]] = _indexes.flatMap {
    case u: UniqueIndex[T] => Some(u)
    case _ => None
  }

  protected def fromSnapshot(filename: String): Unit = utils.getListOfFiles(filename).foreach { f =>
    insert(unpickleFrom(f.getCanonicalPath))
  }

  protected def snapshot() = {
    state.foreach { case (uuid, record) =>
      pickleTo(record, "/var/lib/reactive-db-test/"+uuid.toString)
    }
  }

  private def indexData() = {
    try {
      schema.par.foreach {
        case u: UniqueIndex[T] =>
          state.par.foreach { case(uuid, record) =>
            _uniqueIndexes.par.foreach { index =>
              checkUnique(record)
              index._data(index.fromModel(record)) += uuid
            }
          }
      }
    } catch {
      case e: Exception => println(e.toString)
    }
  }

  private def checkUnique(item: T) = {
    _uniqueIndexes.foreach { i: UniqueIndex[T] =>
      val f = i.fromModel(item)
      if (i.exists(f)) {
        throw new UniqueKeyConstraintException("Unique constraint violation for index!")
      }
    }
  }

  protected def where(expr: (T) => Boolean): Seq[T] = all.filter(expr)

  protected def find(record: T) = state.get(record._recordId)

  protected def all: Seq[T] = state.values.toSeq

  protected def insert(item: T): UUID = {
    // Check indexes
    checkUnique(item)
    println(self.path.name)
    _uniqueIndexes.foreach( ui => ui.add(item))
    state(item._recordId) = item
    item._recordId
  }
}
