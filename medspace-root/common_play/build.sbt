import sbt.Keys.libraryDependencies
// Project name (artifact name in Maven)
name := "common_play"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

// project description
description := "MeDSpace Common play"

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
javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

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


lazy val common_play = (project in file("."))
	.dependsOn(common)
	.dependsOn(common_network)

lazy val common = RootProject(file("../common"))
lazy val common_network = RootProject(file("../common_network"))

// library dependencies. (orginization name) % (project name) % (version)

// https://mvnrepository.com/artifact/com.google.inject/guice
libraryDependencies += "com.google.inject" % "guice" % "4.0"

libraryDependencies ++= Seq(

	// https://mvnrepository.com/artifact/com.typesafe.play/play
	"com.typesafe.play" %% "play" % "2.6.6",

	// https://mvnrepository.com/artifact/com.typesafe.play/play-guice
	"com.typesafe.play" %% "play-guice" % "2.6.6"

)