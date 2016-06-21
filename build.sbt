name := "graphql-demo"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.sangria-graphql" %% "sangria" % "0.7.0"
libraryDependencies += "org.sangria-graphql" %% "sangria-marshalling-api" % "0.2.1"
libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.3.9.v20160517"
libraryDependencies += "commons-io" % "commons-io" % "2.4"

val circeVersion = "0.4.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
)