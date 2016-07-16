package com.shard.db

import akka.actor.Props

/**
  * Author: Nicholas Connor
  * Date: 6/30/16
  * Package: com.shard.db
  */
object Messages {
  case class RunShard(props: Props, name: String)
}
