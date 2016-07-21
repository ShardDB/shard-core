package com.shard.db.structure


import akka.actor.{ActorLogging, ActorRef, ActorSelection, ActorSystem, OneForOneStrategy}
import akka.persistence.serialization.Snapshot
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.shard.db.query._
import com.shard.db.query.Ops.EqualTo
import com.shard.db.structure.index.{HashIndex, PrimaryIndex, Schema}
import Ops.{Op, StringOperators}
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.pattern.pipe

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success, Try}


/**
  * Author: Nicholas Connor
  * Date: 6/21/16
  * Package: com.shard.db.structure
  *
  * @tparam T
  */
trait Shard[T] {
  import scala.reflect.ClassTag
  implicit val timeout = Timeout(30.seconds)
  val databaseName: String
  val shardName: String
  val system: ActorSystem

  val path = s"akka.tcp://$databaseName@localhost:2556/user/shards/$shardName"
  /**
    *
    */
  val actorRef: ActorSelection = system.actorSelection(path)

  /**
    * @param item
    * @return
    */
  def insert(item: T) = (actorRef ? Insert(item)).mapTo[Future[T]]

  /**
    * @param items
    * @return
    */
  def insert(items: Seq[T]): Future[Seq[Any]] = (actorRef ? InsertMany(items)).mapTo[Seq[T]]

  def delete(item: T) = (actorRef ? Delete(item)).mapTo[Boolean]

  def upsert(item: T)(implicit c: ClassTag[T]) = (actorRef ? Upsert(item)).mapTo[T]

  def update(item: T) = (actorRef ? Update(item)).mapTo[Option[T]]

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

  def innerJoin[R](leftKey: String, op: Op, rightTable: ActorSelection, rightKey: String): Future[Seq[(T, R)]] = {
    (actorRef ? InnerJoin(leftKey, op, rightTable, rightKey)).mapTo[Seq[(T, R)]]
  }

  case class PartialJoin[R](rightTable: ActorSelection) {
    def on(leftKey: String, op: Op, rightKey: String) = (actorRef ? InnerJoin(leftKey, op, rightTable, rightKey)).mapTo[Seq[(T, R)]]
  }

  def innerJoin[R](rightTable: ActorSelection) = new PartialJoin[R](rightTable)

  def where(keyName: String, op: Op, value: Any) = (actorRef ? Where(FilterExpression(keyName, op, value))).mapTo[Seq[T]]
}


/**
  * @param schema Storage meta
  * @tparam T Storage Unit
  */

abstract class ShardActor[T](
                       implicit val schema: Schema[T]
                       ) extends PersistentActor with ActorLogging {

  override protected def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    for(x <- 1 to 50){
      println("RECOVERY FAILURE")
    }
  }

  println(self.path.name)

  val persistenceId = schema.name

  var state: PrimaryIndex[T] = schema.primaryIndex

  implicit val timeout = Timeout(30.seconds)

  def handleSingleCommand[A](event: A, persist: Boolean) (func: (A) => Unit) = {
    if (persist) {
      persistAsync(event)(func)
    } else {
      Try {
        func(event)
      } match {
        case Success(x) =>
        case Failure(ex) => println(ex.getMessage)
      }
    }
  }

  def handleCommands(persist: Boolean): Receive = {
    case All =>
      handleSingleCommand(All, persist) { x => all pipeTo sender() }
    /// without indexes we could do (L, R) => Boolean
    /// lets keep to the assumption that we
    /// only use HashIndexes for a sec
    case lj: InnerJoin => handleSingleCommand(lj, persist) { evt =>
      innerJoin(evt) pipeTo sender()
    }

    case i: Insert[T] => handleSingleCommand(i, persist) { evt =>
      insert(evt.record) pipeTo sender()
    }
    case i: InsertMany[T] => handleSingleCommand(i, persist) { evt => insertMany(evt.record) pipeTo sender() }

    case u: Upsert[T] => handleSingleCommand(u, persist) { evt => upsert(evt.record) pipeTo sender()}
    case u: Update[T] => handleSingleCommand(u, persist) { evt => update(evt.record) pipeTo sender()}
    case d: Delete[T] => handleSingleCommand(d, persist) { evt => delete(evt.record) pipeTo sender()}

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
      s.foreach{ case (k,v) => state._data.put(k, v)}
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
  protected def where(expr: FilterExpression): Future[Seq[T]] = {
    val ids = _indexes.getOrElse(expr.keyName, throw new RuntimeException("Index " + expr.keyName + " does not exist")).get(expr.value)

    ids.map {
      case Some(realIds) =>
        Future.sequence {
          realIds.map { theId =>
            state.get(theId)
          }
        }.map(_.flatten)
      case None =>
        Future(Seq.empty[T])
    }.flatMap(identity)
  }

  protected def update(item: T): Future[Option[T]] = {
    Future(state._data.replace(schema.primaryIndex.getKey(item), item))
  }

  protected def delete(item: T): Future[Boolean] = Future {
    state._data.remove(schema.primaryIndex.getKey(item), item)
  }

  protected def upsert(item: T): Future[T] = {
    Future {
      state._data.update(schema.primaryIndex.getKey(item), item)
      item
    }
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
  protected def insert(item: T): Future[T] = {
    val sec = Future.sequence(_indexes.map { case (name, ui) => ui.add(item) })
    val indexing =
      for {
        secondaries <- sec
        primary <- state.add(item)
      } yield (item, primary, secondaries)

    indexing.map{_._1}
  }
}
