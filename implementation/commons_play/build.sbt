import sbt.Keys.libraryDependencies
// Project name (artifact name in Maven)
name := "medspace_common_play"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

version := "0.1-PROTOTYPE"

// project description
description := "MeDSpace Common play"

lazy val common_play = (project in file("."))
	.aggregate(commons)
	.dependsOn(commons)
	.aggregate(commons_network)
	.dependsOn(commons_network)

lazy val commons = RootProject(file("../commons"))
lazy val commons_network = RootProject(file("../commons_network"))


scalaVersion := "2.12.2"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
//crossPaths := false

// This forbids including Scala related libraries into the dependency
//autoScalaLibrary := false

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(

	// https://mvnrepository.com/artifact/com.typesafe.play/play
	"com.typesafe.play" %% "play" % "2.6.6"

)