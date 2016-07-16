package com.shard.db

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.shard.db.Messages.RunShard
import com.shard.db.structure.ShardActor
import com.shard.db.structure.index.Schema

import scala.concurrent.duration._

/**
  * Author: Nicholas Connor
  * Date: 6/25/16
  * Package: com.shard.db.structure
  */
trait Database {

  val system: ActorSystem

  val shardManager = system.actorOf(Props[ShardManager], "shards")

  /**
  This loads all the schema
    */
  //utils.loadJar(args.head)

  //import org.clapper.classutil.ClassFinder

  //val finder = ClassFinder()
  //val classes = finder.getClasses // classes is an Iterator[ClassInfo]
  //classes.filter{ x => x.toString().contains("schema") }.foreach(println)

  def startShell = {
    system.actorOf(Props(new Shell))
  }

  def addShard[T](schema: Schema[T]) = {
    class Actor() extends ShardActor[T]()(implicitly(schema))
    shardManager ! RunShard(Props(new Actor()), schema.name)
  }
}
