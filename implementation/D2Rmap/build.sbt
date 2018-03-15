// Project name (artifact name in Maven)
name := "d2rmap"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

version := "0.1-PROTOTYPE"

// project description
description := "D2R Map"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
//crossPaths := false

// This forbids including Scala related libraries into the dependency
//autoScalaLibrary := false

scalaVersion := "2.12.2"


lazy val d2rmap = (project in file("."))
  .aggregate(commons)
  .dependsOn(commons)

lazy val commons = RootProject(file("../commons"))

mainClass in (Compile,run) := Some("de.unipassau.medspace.TestProcessor")