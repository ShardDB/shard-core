package com.shard.db

import akka.actor.Actor
import ammonite.sshd.{SshServerConfig, SshdRepl}

/**
  * Author: Nicholas Connor
  * Date: 6/30/16
  * Package: com.shard.db
  */
class Shell extends Actor {

  val replServer = new SshdRepl(
    SshServerConfig(
      address = "localhost", // or "0.0.0.0" for public-facing shells
      port = 22223, // Any available port
      username = "repl", // Arbitrary
      password = "" // or ""
    )
  )

  replServer.start()

  override def receive: Receive = {
    case "stop" => replServer.stop()
  }
}
