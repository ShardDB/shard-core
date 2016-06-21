package com.shard.db

import java.io.File
import java.security.MessageDigest


/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */
package object utils {
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
