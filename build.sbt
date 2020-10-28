name := """mabxml-elasticsearch"""

version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "org.metafacture" % "metafacture-framework" % "5.1.0",
  "org.metafacture" % "metafacture-json" % "5.1.0",
  "org.metafacture" % "metafacture-monitoring" % "5.1.0",
  "org.metafacture" % "metafacture-files" % "5.1.0",
  "org.metafacture" % "metafacture-io" % "5.1.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.6",
  "org.elasticsearch" % "elasticsearch" % "5.6.3",
  "org.elasticsearch.client" % "transport" % "5.6.3",
  "io.netty" % "netty" % "3.9.3.Final" force()
)

resolvers += Resolver.mavenLocal
