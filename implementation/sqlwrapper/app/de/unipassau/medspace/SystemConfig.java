package de.unipassau.medspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import com.typesafe.config.Config;

/**
 * TODO
 */
@Singleton
public class SystemConfig {

  private static Logger log = LoggerFactory.getLogger(SystemConfig.class);
  private File homeDirectory;
  private String homeDirectoryPath;

  @Inject
  public SystemConfig(Environment environment,
                      Config playConfig) {
    String libDirectory = getPlayLibDirectory();

    // go one folder upwards
    String homePath = libDirectory + "/../";
    homeDirectory = new File(homePath);

    //We result should be a directory and exist
    boolean ok = homeDirectory.exists() && homeDirectory.isDirectory();
    if (!ok) {
      // we cannot handle this errror
      throw new RuntimeException("Home Dirctory does not exist or is no directory: "
          + homeDirectory.getAbsolutePath());
    }

    homeDirectoryPath = homeDirectory.toPath().normalize().toString();

    // assure that we have a trailing slash
    if (!homeDirectoryPath.endsWith("/") && !homeDirectoryPath.endsWith("\\")) {
      homeDirectoryPath += "/";
    }

    //set the current working directory now to the home directory path
    //System.setProperty("user.dir", homeDirectoryPath);

  }


  /**
   * Provides the path to the home directory; Path ends with a trailing slash
   * @return The home directory path
   */
  public String getHomeDirectoryPath() {
    return homeDirectoryPath;
  }

  public File getHomeDirectory() {
    return homeDirectory;
  }

  /**
   *
   * @return
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
