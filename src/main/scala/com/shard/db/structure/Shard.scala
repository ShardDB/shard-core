package com.shard.db.structure


import akka.actor.{ActorLogging, ActorRef, ActorSelection, ActorSystem}
import akka.persistence.serialization.Snapshot
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.query._
import com.shard.db.query.Ops.EqualTo
import com.shard.db.structure.index.{HashIndex, PrimaryIndex, Schema}
import Ops.{Op, StringOperators}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.pipe

import scala.collection.concurrent.TrieMap


/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  *
  * @tparam T
  */
trait Shard[T] {
  implicit val timeout = Timeout(30.seconds)
  val databaseName: String
  val shardName: String
  val system: ActorSystem

  val path = s"akka.tcp://$databaseName@localhost:2556/user/$shardName"
  println(path)
  /**
    *
    */
  val actorRef: ActorSelection = system.actorSelection(path)

  /**
    * @param item
    * @return
    */
  def insert(item: T) = actorRef ? Insert(item)

  /**
    * @param items
    * @return
    */
  def insert(items: Seq[T]): Future[Seq[Any]] = (actorRef ? InsertMany(items)).mapTo[Seq[Any]]

  /**
    * @param item
    * @return
    */
  def find(item: T): Future[Option[T]] = (actorRef ? Find(item)).mapTo[Option[T]]

  /**
    * @param id
    * @return
    */
  def findById(id: Any): Future[Option[T]] = (actorRef ? FindById(id)).mapTo[Option[T]]

  /**
    * @return
    */
  def all: Future[Seq[T]] = (actorRef ? All).mapTo[Seq[T]]

  def size: Future[Int] = (actorRef ? Size).mapTo[Int]

  def takeSnapshot = actorRef ! Snapshot

  def innerJoin[R](leftKey: String, op: Op, rightTable: ActorRef, rightKey: String): Future[Seq[(T, R)]] = {
    (actorRef ? InnerJoin(leftKey, op, rightTable, rightKey)).mapTo[Seq[(T, R)]]
  }

  def where(keyName: String, op: Op, value: Any) = (actorRef ? Where(FilterExpression(keyName, op, value))).mapTo[Option[Seq[T]]]
}


/**
  * @param schema Storage meta
  * @tparam T Storage Unit
  */

abstract class ShardActor[T](
                       implicit val schema: Schema[T]
                       ) extends PersistentActor with ActorLogging {

  println(self.path.name)

  val persistenceId = schema.name

  var state: PrimaryIndex[T] = schema.primaryIndex

  implicit val timeout = Timeout(30.seconds)

  def handleSingleCommand[A](event: A, persist: Boolean) (func: (A) => Unit) = {
    if (persist) {
      persistAsync(event)(func)
    } else {
      func(event)
    }
  }

  def handleCommands(persist: Boolean): Receive = {
    case All =>
      handleSingleCommand(All, persist) { x => sender() ! x }
    /// without indexes we could do (L, R) => Boolean
    /// lets keep to the assumption that we
    /// only use HashIndexes for a sec
    case lj: InnerJoin => handleSingleCommand(lj, persist) { evt =>
      innerJoin(evt) pipeTo sender()
    }

    case i: Insert[T] => handleSingleCommand(i, persist) { evt =>
      println("Inserting record....")
      insert(evt.record) pipeTo sender()
    }
    case i: InsertMany[T] => handleSingleCommand(i, persist) { evt => insertMany(evt.record) pipeTo sender() }

    case f: Find[T] => find(f.record) pipeTo sender()

    case fById: FindById => handleSingleCommand(fById, persist) { evt =>
      state.get(fById.id).mapTo[Option[T]] pipeTo sender()
    }

    case w: Where[T] => handleSingleCommand(w, persist) { evt =>
      where(evt.expr) pipeTo sender()
    }

    case Size => handleSingleCommand(Size, persist) { evt => state.size pipeTo sender() }

    case Snapshot => saveSnapshot(state._data)
    case SaveSnapshotSuccess(metadata) => println(metadata.toString)
    case SaveSnapshotFailure(metadata, reason) => println(reason.getMessage)
  }

  /**
    * @return
    */
  def receiveRecover: Receive = handleCommands(false) orElse {
    case SnapshotOffer(_, s: TrieMap[Any, T]) =>
      state._data = s
  }

  /**
    * @return
    */
  def receiveCommand: Receive = handleCommands(true)

  /**
    * @param evt
    * @return
    */
  protected def innerJoin(evt: InnerJoin) = {
    val ans = evt.leftKey match {
      case "primaryIndex" =>
        val leftData = schema.primaryIndex._data
        // Hrm this seems a little complex
        val ret = Future.sequence {
          // for every index in the Left,
          leftData.map { case (k1, v1) =>
            // Query the right for IDS
            val rightFuture = (evt.rightTable ? Where(evt.rightKey === k1)).mapTo[Option[Seq[Any]]]
            // on every Right Recordrd
            rightFuture.map {
              // if non-empty, join to left reco
              case None =>
                None
              case Some(rightSeq) =>
                Some(rightSeq.map { rightItem =>
                  (v1, rightItem)
                })
            }
          }
        }.map(_.flatten.toSeq.flatten )
        ret

      case _ =>
        val leftIndex = _indexes(evt.leftKey)._data
        // Hrm this seems a little complex
        Future.sequence {
          // for every index in the Left,
          leftIndex.map { case (k1, ids1) =>
            // Query the right for IDS
            val rightFuture = (evt.rightTable ? Where(evt.rightKey === k1)).mapTo[Option[Seq[Any]]]
            // on every Right Recordrd
            rightFuture.map {
              // if non-empty, join to left reco
              case None => None
              case Some(rightSeq) =>
                Some(ids1.flatMap { id1 =>
                  rightSeq.map { rightItem =>
                    (state._data(k1), rightItem)
                  }
                })
            }
          }
        }.map(_.flatten.toSeq.flatten )
    }

    ans
  }

  private lazy val _indexes: Map[String, HashIndex[T]] = schema.secondaryIndexes.map(i => i.name -> i).toMap

  /**
    *
    */
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

  /**
    * @param expr
    * @return
    */
  protected def where(expr: (T) => Boolean): Future[Seq[T]] = all.map(_.filter(expr))

  /**
    * @param expr
    * @return
    */
  private def filterExpressionToSimple(expr: FilterExpression): (T) => Boolean = {
    expr.op match {
      case EqualTo => (i: T) => false
    }
  }

  /**
    * @param expr
    * @return
    */
  protected def where(expr: FilterExpression): Future[Option[Seq[T]]] = {
    val ids = _indexes.getOrElse(expr.keyName, throw new RuntimeException("Index " + expr.keyName + " does not exist")).get(expr.value)

    ids.map {
      case Some(realIds) =>
        Future.sequence {
          realIds.map { theId =>
            state.get(theId)
          }
        }.map(_.flatten).map(Some(_))
      case None =>
        Future(None)
    }.flatMap(identity)
  }

  /**
    * @param record
    * @return
    */
  protected def find(record: T) = state.get(schema.primaryIndex.getKey(record))

  /**
    * @return
    */
  protected def all: Future[Seq[T]] = state.all.map(_.toSeq)

  /**
    * @param items
    * @return
    */
  protected def insertMany(items: Seq[T]): Future[Seq[Any]] = Future.sequence(items.map(insert))

  /**
    * @param item
    * @return
    */
  protected def insert(item: T): Future[Option[T]] = {
    val sec = Future.sequence(_indexes.map { case (name, ui) => ui.add(item) })
    val indexing =
      for {
        secondaries <- sec
        primary <- state.add(item).map(state.get(_))
      } yield (primary, secondaries)

    indexing.map{_._1}.flatMap(identity)
  }
}
