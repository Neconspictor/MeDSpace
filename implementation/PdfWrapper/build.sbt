name := "PdfWrapper"
organization := "de.unipassau.medspace"

// project description
description := "PdfWrapper"

version := "0.1-PROTOTYPE"

lazy val pdf_wrapper = (project in file("."))
  .enablePlugins(PlayJava, LauncherJarPlugin) //, LauncherJarPlugin
  .aggregate(commons)
  .dependsOn(commons)
  .aggregate(commons_network)
  .dependsOn(commons_network)
  .aggregate(commons_play)
  .dependsOn(commons_play)

lazy val commons = RootProject(file("../commons"))
lazy val commons_network = RootProject(file("../commons_network"))
lazy val commons_play = RootProject(file("../commons_play"))

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

// https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.2.11"

// https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox
libraryDependencies += "org.apache.pdfbox" % "pdfbox" % "2.0.8"

//batScriptTemplateLocation := file("run.bat" )

javacOptions ++= Seq("-Xlint:unchecked")

PlayKeys.devSettings := Seq("play.server.http.port" -> "9400", "play.server.http.address" -> "localhost")