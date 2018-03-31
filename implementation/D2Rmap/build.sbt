// Project name (artifact name in Maven)
name := "d2rmap"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

version := "1.0"

// project description
description := "D2R Map"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

//fix the scala version (used by play and akka)
scalaVersion := "2.12.2"

//disable link warnings
scalacOptions in (Compile, doc) ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)

// we use Java 8 for the source code
javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked")

// we don't want to use the strict mode of javadocs in Java 8
javacOptions in (Compile, doc) ++= Seq("-Xdoclint:none")

// Force SBT to create javadocs and not scaladocs!
sources in (Compile, doc) ~= (_ filter (_.getName endsWith ".java"))

// Eclipse integration
EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true


lazy val d2rmap = (project in file("."))
  .aggregate(commons)
  .dependsOn(commons)

lazy val commons = RootProject(file("../commons"))