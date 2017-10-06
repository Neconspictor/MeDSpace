// Project name (artifact name in Maven)
name := "medspace_commons_network"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

version := "0.1-PROTOTYPE"

// project description
description := "MeDSpace Commons Network"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false



// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(

	// Logging
	// We only want to use one logging instance but some libs are using slf4j
	// while others use log4j -> we use log4j and bridge slf4j to it

	// https://mvnrepository.com/artifact/ch.qos.logback/logback-core
	"ch.qos.logback" % "logback-classic" % "1.2.3",
	//"log4j" % "log4j" % "1.2.17",
	//"org.slf4j" % "slf4j-log4j12" % "1.7.25",
	//"org.slf4j" % "slf4j-api" % "1.7.25",

	// XML Parser
	//"net.sf.saxon" % "Saxon-HE" % "9.8.0-3",
	"xerces" % "xercesImpl" % "2.11.0",
	//"org.opengis.cite.xerces" % "xercesImpl-xsd11" % "2.12-beta-r1667115",

	// JUnit
	"junit" % "junit" % "4.12" % "test",

	// Javatuples
	"org.javatuples" % "javatuples" % "1.2",

	// Mockrunner JDBC
	"com.mockrunner" % "mockrunner-jdbc" % "1.1.2"
)

// https://mvnrepository.com/artifact/com.typesafe.play/play-ws_2.12
libraryDependencies += "com.typesafe.play" % "play-ws_2.12" % "2.6.6"