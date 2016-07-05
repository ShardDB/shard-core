package com.shard.db
import scala.reflect.runtime.universe

/**
  * Author: Nicholas Connor
  * Date: 7/1/16
  * Package: com.shard.db
  */
object Runner extends App with ShellAccess {
  //utils.loadJar(args.head)
  val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
  val module = runtimeMirror.staticModule(args.head)
  /*
    Run the database
   */
  println("Reflecting..")
  val obj = runtimeMirror.reflectModule(module)
  println(obj.instance)
  println("And done..")
}
