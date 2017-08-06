name := "SqlWrapper"
organization := "de.unipassau.medspace"

// project description
description := "SqlWrapper"

version := "1.0-SNAPSHOT"

lazy val sql_wrapper = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin)
  .aggregate(d2rmap)
  .dependsOn(d2rmap)

lazy val d2rmap = RootProject(file("../D2Rmap"))

scalaVersion := "2.12.2"

libraryDependencies += guice

javacOptions ++= Seq("-Xlint:unchecked")