package com.shard.db

import java.io.File
import java.net.{URL, URLClassLoader}
import java.util.jar.{JarEntry, JarFile}

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

  def loadJar(pathToJar: String) = {
    val jarFile = new JarFile(pathToJar)
    val e = jarFile.entries()

    val urls = Array{ new URL("jar:file:" + pathToJar+"!/") }
    val cl = URLClassLoader.newInstance(urls)

    while (e.hasMoreElements) {
      val je: JarEntry = e.nextElement()
      if(je.isDirectory || !je.getName.endsWith(".class")){

      } else {
        // -6 because of .class
        var className = je.getName.substring(0,je.getName.length()-6)
        className = className.replace('/', '.')
        //println(className)
        val c = cl.loadClass(className)
        //println(c.getConstructors.mkString(","))
      }
    }
  }
}
