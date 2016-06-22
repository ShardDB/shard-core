name := """minimal-akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.7" % "test",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.7",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.4.1"
)

fork in run := true
cancelable in Global := true