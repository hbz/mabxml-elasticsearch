name := """mabxml-elasticsearch"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "org.culturegraph" % "metafacture-core" % "2.0.1-HBZ-SNAPSHOT"
    exclude("com.fasterxml.jackson.core", "jackson-core"),
  "org.slf4j" % "slf4j-log4j12" % "1.7.6",
  "org.elasticsearch" % "elasticsearch" % "2.3.3"
    exclude("io.netty", "netty")
)

resolvers += Resolver.mavenLocal
