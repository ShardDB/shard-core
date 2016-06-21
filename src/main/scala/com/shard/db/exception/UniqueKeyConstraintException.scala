package com.shard.db.exception

/**
  * Author: Nicholas Connor
  * Date: 6/20/16
  * Package: com.example.exception
  */
class UniqueKeyConstraintException(
                                    message: String = null, cause: Throwable = null
                                  ) extends RuntimeException(message, cause)