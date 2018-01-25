name := "Register"
organization := "de.unipassau.medspace"

// project description
description := "MeDSpace Register Module"

version := "0.0-PreAlpha"

lazy val register = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin)
  .aggregate(commons)
  .dependsOn(commons)
  .aggregate(commons_network)
  .dependsOn(commons_network)
  
lazy val commons = RootProject(file("../commons"))  

lazy val commons_network = RootProject(file("../commons_network"))  

scalaVersion := "2.12.2"

libraryDependencies += guice

libraryDependencies += ws

libraryDependencies += "org.webjars.bower" % "bootstrap-sass" % "3.3.6"

// https://mvnrepository.com/artifact/dnsjava/dnsjava
libraryDependencies += "dnsjava" % "dnsjava" % "2.1.8"

javacOptions ++= Seq("-Xlint:unchecked")

unmanagedResourceDirectories in Test += baseDirectory.value / "app/resources"
unmanagedResourceDirectories in Compile += baseDirectory.value / "app/resources"