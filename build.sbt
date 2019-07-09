name := "sparrow-account"
organization := "io.github.edersoncorbari"
version := "0.1"
scalaVersion := "2.12.8"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

coverageMinimum := 50
coverageFailOnMinimum := false
coverageHighlighting := true

scalastyleConfig := baseDirectory.value / "project/scalastyle-config.xml"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
logLevel := Level.Error

libraryDependencies ++= Seq(
  "com.twitter" %% "twitter-server" % "19.1.0",
  "com.github.finagle" %% "finch-core" % "0.28.0",
  "com.github.finagle" %% "finch-circe" % "0.28.0",
  "io.circe" %% "circe-generic" % "0.11.1",
  "io.circe" %% "circe-core" % "0.11.1",
  "io.circe" %% "circe-parser" % "0.11.1",
  "com.typesafe" % "config" % "1.3.4",
  "org.scala-stm" %% "scala-stm" % "0.9.1",
  "org.slf4j" % "slf4j-jdk14" % "1.7.21",
  "org.wvlet.airframe" %% "airframe-log" % "19.7.2",
  "org.scalatest" %% "scalatest" % "3.0.7" % Test,
)

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case meta(_)  => MergeStrategy.discard
  case other => MergeStrategy.defaultMergeStrategy(other)
}
