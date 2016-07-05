package com.shard.db

import akka.actor.Props

/**
  * Author: Nicholas Connor
  * Date: 6/30/16
  * Package: com.shard.db
  */
object Messages {
  case class GetOrCreate(props: Props, name: String)
}
