name := """mabxml-elasticsearch"""

version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.7"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "org.culturegraph" % "metafacture-core" % "4.0.0-HBZ-SNAPSHOT"
    exclude("com.fasterxml.jackson.core", "jackson-core"),
  "org.slf4j" % "slf4j-log4j12" % "1.7.6",
  "org.elasticsearch" % "elasticsearch" % "5.6.3",
  "org.elasticsearch.client" % "transport" % "5.6.3",
  "io.netty" % "netty" % "3.9.3.Final" force()
)

resolvers += Resolver.mavenLocal
