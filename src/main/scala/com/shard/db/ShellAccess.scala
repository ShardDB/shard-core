package com.shard.db

import ammonite.sshd.{SshServerConfig, SshdRepl}

/**
  * Author: Nicholas Connor
  * Date: 6/30/16
  * Package: com.shard.db
  */
trait ShellAccess {
  val replServer = new SshdRepl(
    SshServerConfig(
      address = "localhost", // or "0.0.0.0" for public-facing shells
      port = 22222, // Any available port
      username = "repl", // Arbitrary
      password = "" // or ""
    )
  )

  replServer.start()
}
