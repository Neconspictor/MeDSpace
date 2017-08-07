name := "SqlWrapper"
organization := "de.unipassau.medspace"

// project description
description := "SqlWrapper"

version := "0.1-PROTOTYPE"

lazy val sql_wrapper = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin)
  .aggregate(d2rmap)
  .dependsOn(d2rmap)

lazy val d2rmap = RootProject(file("../D2Rmap"))

scalaVersion := "2.12.2"

libraryDependencies += guice

libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.6"

// https://mvnrepository.com/artifact/dnsjava/dnsjava
libraryDependencies += "dnsjava" % "dnsjava" % "2.1.8"


javacOptions ++= Seq("-Xlint:unchecked")