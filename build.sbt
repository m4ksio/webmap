name := "webmap"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "org.scala-graph" %% "graph-core" % "1.11.5"
libraryDependencies += "org.scala-graph" %% "graph-dot" % "1.11.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.3"
libraryDependencies +=  "com.typesafe.akka" %% "akka-http" % "10.0.9"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2"
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test