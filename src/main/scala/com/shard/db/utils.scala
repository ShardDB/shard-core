package com.shard.db

import java.io.File

/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */
package object utils {

  def time[R](block: => R, msg: String): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + ((t1 - t0) / 1000000000.0) + "s ---" + msg)
    result
  }

  def timeInSeconds[R](block: => R): Double = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    (t1 - t0) / 1000000000.0
  }

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def md5(s: String) = {
     val m = java.security.MessageDigest.getInstance("MD5")
     val b = s.getBytes("UTF-8")
     m.update(b, 0, b.length)
     new java.math.BigInteger(1, m.digest()).toString(16)
  }
}
