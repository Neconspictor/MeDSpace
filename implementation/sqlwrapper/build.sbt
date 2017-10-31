name := "SqlWrapper"
organization := "de.unipassau.medspace"

// project description
description := "SqlWrapper"

version := "0.1-PROTOTYPE"

lazy val sql_wrapper = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin) //, LauncherJarPlugin
  .aggregate(commons)
  .dependsOn(commons)
  .aggregate(commons_network)
  .dependsOn(commons_network)
  .aggregate(d2rmap)
  .dependsOn(d2rmap)

lazy val commons = RootProject(file("../commons"))
lazy val commons_network = RootProject(file("../commons_network"))
lazy val d2rmap = RootProject(file("../D2Rmap"))

scalaVersion := "2.12.2"

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

//batScriptTemplateLocation := file("run.bat" )

javacOptions ++= Seq("-Xlint:unchecked")