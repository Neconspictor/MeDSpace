name := "ImageWrapper"
organization := "de.unipassau.medspace"

// project description
description := "ImageWrapper"

version := "1.0"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

//fix the scala version (used by play and akka)
scalaVersion := "2.12.2"

// we use Java 8 for the source code
javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked")

// we don't want to use the strict mode of javadocs in Java 8
javacOptions in (Compile, doc) ++= Seq("-Xdoclint:none")

//disable link warnings
scalacOptions in (Compile, doc) ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)

// Force SBT to create javadocs and not scaladocs!
sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".java"))

// Eclipse integration
EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true


lazy val image_wrapper = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin) //, LauncherJarPlugin
  .aggregate(commons)
  .dependsOn(commons)
  .aggregate(commons_network)
  .dependsOn(commons_network)
  .aggregate(common_play)
  .dependsOn(common_play)

lazy val commons = RootProject(file("../commons"))
lazy val commons_network = RootProject(file("../commons_network"))
lazy val common_play = RootProject(file("../commons_play"))


libraryDependencies += guice

libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.6"

// https://mvnrepository.com/artifact/dnsjava/dnsjava
libraryDependencies += "dnsjava" % "dnsjava" % "2.1.8"

libraryDependencies += ws

// https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-runtime
libraryDependencies += "org.eclipse.rdf4j" % "rdf4j-runtime" % "2.2.2"

// https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-queryresultio-binary
libraryDependencies += "org.eclipse.rdf4j" % "rdf4j-queryresultio-binary" % "2.2.2" % "runtime"

// https://mvnrepository.com/artifact/org.eclipse.rdf4j/rdf4j-core
libraryDependencies += "org.eclipse.rdf4j" % "rdf4j-core" % "2.2.2" pomOnly()

// https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.2.11"


//batScriptTemplateLocation := file("run.bat" )

javacOptions ++= Seq("-Xlint:unchecked")

PlayKeys.devSettings := Seq("play.server.http.port" -> "9300", "play.server.http.address" -> "localhost")