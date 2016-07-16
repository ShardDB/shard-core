package com.shard.db

/**
  * Author: Nicholas Connor
  * Date: 7/13/16
  * Package: com.shard.db
  */
import play.api.libs.json.Json

import scala.reflect.runtime.{universe => u}
import scala.reflect.runtime.universe._

object ReflectionHelper {

  val classLoader = Thread.currentThread().getContextClassLoader

  val mirror = u.runtimeMirror(classLoader)

  def getFieldType(className: String, fieldName: String): Option[Type] = {

    val classSymbol = mirror.staticClass(className)

    for {
      fieldSymbol <- classSymbol.selfType.members.collectFirst({
        case s: Symbol if s.isPublic && s.name.decodedName.toString() == fieldName => s
      })
    } yield {

      fieldSymbol.info.resultType
    }
  }

  def maybeUnwrapFieldType[A](fieldType: Type)(implicit tag: TypeTag[A]): Option[Type] = {
    if (fieldType.typeConstructor == tag.tpe.typeConstructor) {
      fieldType.typeArgs.headOption
    } else {
      Option(fieldType)
    }
  }

  def getFieldClass(className: String, fieldName: String): java.lang.Class[_] = {

    // case normal field return its class
    // case Option field return generic type of Option

    val result = for {
      fieldType <- getFieldType(className, fieldName)
      unwrappedFieldType <- maybeUnwrapFieldType[Option[_]](fieldType)
    } yield {
      mirror.runtimeClass(unwrappedFieldType)
    }

    // Consider changing return type to: Option[Class[_]]
    result.getOrElse(null)
  }
}