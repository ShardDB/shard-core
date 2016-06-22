package com.shard.db.query

import com.shard.db.query.Ops.Op

/**
  * Author: Nicholas Connor
  * Date: 6/22/16
  * Package: com.shard.db.query
  */
case class FilterExpression(keyName: String, op: Op, value: Any)
