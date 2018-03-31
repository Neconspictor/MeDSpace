name := "MeDSpace"
organization := "de.unipassau.medspace"

// project description
description := "Medical Dataspace"

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


// additional resource directory ins app/resource
unmanagedResourceDirectories in Test += baseDirectory.value / "app/resources"
unmanagedResourceDirectories in Compile += baseDirectory.value / "app/resources"

// in sbt development mode we don't want to type always "run PORT -Dhttp.port=PORT"
// So we specify it here
PlayKeys.devSettings := Seq("play.server.http.port" -> "9500", "play.server.http.address" -> "localhost")


lazy val medspace = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin)
  .aggregate(commons)
  .dependsOn(commons)
  .aggregate(commons_network)
  .dependsOn(commons_network)
  .aggregate(commons_play)
  .dependsOn(commons_play)
  
lazy val commons = RootProject(file("../commons"))
lazy val commons_network = RootProject(file("../commons_network"))
lazy val commons_play = RootProject(file("../commons_play"))


// library dependencies

libraryDependencies += guice

libraryDependencies += ws

libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.6"

// https://mvnrepository.com/artifact/org.ehcache/ehcache
libraryDependencies += "org.ehcache" % "ehcache" % "3.5.0"


// https://mvnrepository.com/artifact/dnsjava/dnsjava
libraryDependencies += "dnsjava" % "dnsjava" % "2.1.8"