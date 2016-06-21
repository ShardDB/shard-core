package com.shard.db.test

import java.io.FileOutputStream

import akka.actor.Props
import com.shard.db.structure.{Cluster, Shard}
import com.shard.db.Record

import scala.pickling.Defaults._
import scala.pickling.binary.{BinaryPickle, _}

/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */

class UserShard extends Shard[User] {

  val schema = Seq(
    //UniqueIndex("uk_age", (u: User) => u.age)
  )

  // fromSnapshot("/var/lib/reactive-db-test")

  def pickleTo(item: User, filename: String) = {
    // Manually generate a pickler using macro
    val itemPickle = item.pickle
    val itemPickleByteArray = itemPickle.value
    val fos = new FileOutputStream(filename)
    fos.write(itemPickleByteArray)
    fos.close()
  }

  def unpickleFrom(filename: String): User = {
    val itemRawFromFile = scala.io.Source.fromFile(filename).map(_.toByte).toArray
    val itemUnpickleValue = BinaryPickle(itemRawFromFile)
    itemUnpickleValue.unpickle[User]
  }
}

class UserCluster extends Cluster[User] {

  val props = Props(new UserShard())

  val numberOfShards = 5

  val schema = Seq(
    //UniqueIndex("uk_age", (u: User) => u.age)
  )

  // fromSnapshot("/var/lib/reactive-db-test")

  def pickleTo(item: User, filename: String) = {
    // Manually generate a pickler using macro
    val itemPickle = item.pickle
    val itemPickleByteArray = itemPickle.value
    val fos = new FileOutputStream(filename)
    fos.write(itemPickleByteArray)
    fos.close()
  }

  def unpickleFrom(filename: String): User = {
    val itemRawFromFile = scala.io.Source.fromFile(filename).map(_.toByte).toArray
    val itemUnpickleValue = BinaryPickle(itemRawFromFile)
    itemUnpickleValue.unpickle[User]
  }
}

case class User(age: Int, name: String) extends Record {

}