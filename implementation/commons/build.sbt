// Project name (artifact name in Maven)
name := "medspace_commons"

// orgnization name (e.g., the package name of the project)
organization := "de.unipassau.medspace"

version := "0.1-PROTOTYPE"

// project description
description := "MeDSpace Commons"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
	// Connector/J
	"mysql" % "mysql-connector-java" % "5.1.42",

	"com.h2database" % "h2" % "1.4.195",

	// Jena
	"org.apache.jena" % "jena-arq" % "3.4.0",
	"org.apache.jena" % "jena-core" % "3.4.0",
	"org.apache.jena" % "jena-iri" % "3.4.0",
	"org.apache.jena" % "jena-tdb" % "3.4.0",
	"org.apache.jena" % "jena-base" % "3.4.0" % "test",
	"org.apache.jena" % "apache-jena-libs" % "3.4.0" pomOnly(),


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

	// HikariCP
	"com.zaxxer" % "HikariCP" % "2.6.3",

	// Javatuples
	"org.javatuples" % "javatuples" % "1.2",

	// Lucene
	"org.apache.lucene" % "lucene-core" % "6.6.0",
	"org.apache.lucene" % "lucene-queryparser" % "6.6.0",
	"org.apache.lucene" % "lucene-analyzers-common" % "6.6.0",

	// Mockrunner JDBC
	"com.mockrunner" % "mockrunner-jdbc" % "1.1.2"
)