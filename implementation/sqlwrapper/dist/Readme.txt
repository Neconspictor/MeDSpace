0. Requirements
Java 8; The java binary has to be accessable from a terminal
The following check in the terminal should output a version >= 1.8 : java -version

1. Extract zip
Extract the sqlwrapper-0.1-PROTOTYPE.zip file 
Change to the new created subfolder sqlwrapper-0.1-PROTOTYPE
You should see the following folder structure:
	/bin  --> contains convenient start scripts 
	/conf --> contains configuration files for the play framework and for logging
	/h2  --> A already initialized h2 database with convenient start scripts
	/lib --> The jar files of the SQL wrapper project
	/medspace --> Contains data specific for MeDSpace; For now it contains the D2R Mapping config file

2. Setup database:
At first we have to start the h2 database.
Go into the folder sqlwrapper-0.1-PROTOTYPE/h2 and execute the following java command in a terminal/command line of your choice:
java -cp h2-1.4.195.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 5000
This will start the tcp server of the h2 database on the port 5000 and allows all clients to connect to it.

3. Setup SQL Wrapper:
The wrapper needs a configuration file, from that it reads the D2R mappings. This config file is specified in the file
sqlwrapper-0.1-PROTOTYPE/conf/application.conf at the "MeDSpaceMappingConfig" configuration item.
By default the config file is set to "./medspace/medspace-d2r-mapping.xml".

You may notice, that the config file is specified by a relative path. The relative path is dependant from the current user working directory.
So if you execute the wrapper from the folder sqlwrapper-0.1-PROTOTYPE, the config file is supposed to be at sqlwrapper-0.1-PROTOTYPE/medspace/medspace-d2r-mapping.xml,
but if you're working in sqlwrapper-0.1-PROTOTYPE/bin the path would resolve to sqlwrapper-0.1-PROTOTYPE/bin/medspace/medspace-d2r-mapping.xml .

This is important to remember, as you have to place the config file to the right location when executing the wrapper. Otherwise the wrapper will complain about not finding the
D2R Mapping config file.

The default file location of the D2R Mapping config file is "sqlwrapper-0.1-PROTOTYPE/medspace/medspace-d2r-mapping.xml". 
So the simplest solution is to execute the start script from "sqlwrapper-0.1-PROTOTYPE" folder.

With a running h2 database, execute now the start script within a terminal (assuming your working directory is "sqlwrapper-0.1-PROTOTYPE"): 
./bin/sqlwrapper (for UNIX)
.\bin\sqlwrapper (for Windows)

The start scripts are automatically created by the sbt build system. I couldn't test yet the UNIX variant, so please give feedback if it does not work.
The start script for the Windows system was tested by myself and should work. If it should not work, you can execute it directly by using java, too:
java -jar "lib/de.unipassau.medspace.sqlwrapper-0.1-PROTOTYPE-launcher.jar" (again assuming being in the directory "sqlwrapper-0.1-PROTOTYPE")


4. Index creation and Logging output
Once started, the wrapper will read the D2R Mapping config file, connect to the h2 database and index the resulting SQL data in the default index directory 
./_work/medspace/index. Logging output is saved to ./logs/application.log

5. Stopping the wrapper and the h2 database
Both applications can be stopped as is usual by using CTRL/COMMAND + C inside the terminal session.

6. Have fun!
The default Port the Wrapper is set to 80, so just browse localhost and you should be brought to the index page of the Wrapper.
The index page lists all available services. A test client can be found /test_gui. If you like to reindex the data (e.g. after changing the sql data),
than execute the reindex REST service by browsing to /reindex .



