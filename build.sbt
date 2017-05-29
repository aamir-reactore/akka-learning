name := "akka-learning"

version := "1.0"

scalaVersion := "2.11.8"
lazy val akkaVersion = "2.4.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-http_2.11" % "10.0.0",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.5.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.8" % "test",
  "org.specs2" %% "specs2" % "2.4.6" % "test"

)
libraryDependencies += "org.scalafx" % "scalafx_2.11" % "8.0.102-R11"