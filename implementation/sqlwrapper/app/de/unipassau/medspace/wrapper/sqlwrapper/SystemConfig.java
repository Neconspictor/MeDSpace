package de.unipassau.medspace.wrapper.sqlwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import com.typesafe.config.Config;
import play.api.Application;

/**
 * This class is used to configure the environment of the play framework.
 * It allows to specify a custom working directory that depends on the store location of the jar file and doesn't change
 * if the application is started on different folders.
 */
@Singleton
public class SystemConfig {


  private static Logger log = LoggerFactory.getLogger(SystemConfig.class);


  private File customWorkingDirectory;

  private String customWorkingDirectoryPath;

  private File playRootDirectory;

  private final Application application;

  /**
   * Creates a new SystemConfig
   * @param environment The environment of the play framework.
   * @param application The Play application.
   * @param playConfig The configuration of the play framework.
   */
  @Inject
  public SystemConfig(Environment environment,
                      Application application,
                      Config playConfig) {

    this.application = application;
    this.playRootDirectory = application.path();
    log.info("play root directory = " + playRootDirectory);
    log.info("environment root path = " + application.environment().rootPath());

    String libDirectory = getPlayLibDirectory();

    // go one folder upwards
    String homePath = libDirectory + "/../";
    customWorkingDirectory = new File(homePath);

    //We result should be a directory and exist
    boolean ok = customWorkingDirectory.exists() && customWorkingDirectory.isDirectory();
    if (!ok) {
      // we cannot handle this errror
      throw new RuntimeException("Home Dirctory does not exist or is no directory: "
          + customWorkingDirectory.getAbsolutePath());
    }

    customWorkingDirectoryPath = customWorkingDirectory.toPath().normalize().toString();

    // assure that we have a trailing slash
    if (!customWorkingDirectoryPath.endsWith("/") && !customWorkingDirectoryPath.endsWith("\\")) {
      customWorkingDirectoryPath += "/";
    }

    //set the current working directory now to the home directory path
    //System.setProperty("user.dir", customWorkingDirectoryPath);
  }


  /**
   * Provides the path to the custom working directory; Path ends with a trailing slash
   * @return The custom working directory path
   */
  public String getCustomWorkingDirectoryPath() {
    return customWorkingDirectoryPath;
  }

  /**
   * Provides the custom working directory as a file.
   * @return The custom working directory.
   */
  public File getCustomWorkingDirectory() {
    return customWorkingDirectory;
  }

  /**
   * Provides the directory the compilation of this class lies.
   * @return The directory where the compilation of this class lies.
   */
  private String getPlayLibDirectory()  {
    URL libURL = getClass().getProtectionDomain().getCodeSource().getLocation();
    File libFolder = null;
    try {
      libFolder = new File(libURL.toURI());
    } catch (URISyntaxException e) {
      log.error("Error while trying to get an URI of the lib fodler URL: ", e);
      // We cannot handle this;
      throw new RuntimeException("Couldn't access lib folder: " + libURL);
    }

    // Is the source code inside a *.jar file ?
    if (libFolder.exists() && libFolder.isFile()) {
      libFolder = libFolder.getParentFile();
    }

    if (!libFolder.isDirectory()) {
      throw new RuntimeException("lib directory path is no directory!: " + libFolder.getAbsolutePath());
    }

    return libFolder.getAbsolutePath();
  }
}