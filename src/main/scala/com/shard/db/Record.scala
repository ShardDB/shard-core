package com.shard.db

import java.util.UUID

/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example
  */
trait Record {
  val _recordId: UUID = UUID.randomUUID()
}
