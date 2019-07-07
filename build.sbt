name := "sparrow-account"
version := "0.1"
scalaVersion := "2.12.8"

scalacOptions ++= Seq("-deprecation", "-feature")
logLevel := Level.Error
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.twitter" %% "twitter-server" % "19.1.0",
  "com.github.finagle" %% "finch-core" % "0.28.0",
  "com.github.finagle" %% "finch-circe" % "0.28.0",
  "io.circe" %% "circe-generic" % "0.11.1",
  "io.circe" %% "circe-core" % "0.11.1",
  "io.circe" %% "circe-parser" % "0.11.1",
  "com.typesafe" % "config" % "1.3.4",
  "org.scala-stm" %% "scala-stm" % "0.9.1",
  "org.scalatest" %% "scalatest" % "3.0.7" % Test,
)

val meta = """META.INF(.)*""".r

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case meta(_)  => MergeStrategy.discard
  case other => MergeStrategy.defaultMergeStrategy(other)
}

(sourceDirectories in Test) := Seq(new File("src/test/scala/sparrow/account/unit/"))
