name := """minimal-akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.7"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.7" % "test",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.7",
  // Level DB
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  // Serializers
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.4.1",
  // Scala Test
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

fork in run := true
fork in Test := true
cancelable in Global := true