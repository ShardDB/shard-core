name := """shard-core"""

version := "0.0.61"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.7"

libraryDependencies ++= Seq(
  "org.clapper" %% "classutil" % "1.0.11",
  //"com.mohiva" %% "play-silhouette" % "4.0.0-RC1",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  // Level DB
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  // Serializers
  //"com.github.romix.akka" %% "akka-kryo-serialization" % "0.4.1",
  // Scala Test
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  // Ammo SSH
  "com.twitter" %% "chill-akka" % "0.8.0",

  "com.lihaoyi" % "ammonite-sshd" % "0.6.2" cross CrossVersion.full
)

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

fork in run := true
fork in Test := true
cancelable in Global := true