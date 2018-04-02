package de.unipassau.medspace.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Resolves macros in file paths for medspace.
 */
public class PathResolveParser {

  /**
   * A macro to specify the project folder of a medspace application.
   * The project folder is not the folder wherein the application is started (that is the root folder),
   * but the folder where the folders bin, conf, public, etc. are located. The project folder can be used
   * for a folder structure independent from the folder where the application is started.
   */
  public static final String PROJECT_FOLDER_TOKEN = "[project-folder]";

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralWrapperConfigReader.class);


  private final File projectFolder;


  /**
   * Default constructor.
   * Note: DO NOT DELETE, as it is needed for dependency injection.
   * Should NOT be used in user code.
   */
  public PathResolveParser() {
    this.projectFolder = new File(".");
  }



  /**
   * Creates a new PathResolveParser object.
   * @param projectFolder The project folder to use
   */
  public PathResolveParser(File projectFolder) {
    this.projectFolder = projectFolder;
  }

  /**
   * Replaces macros in a file path.
   * @param filePath The file path.
   * @return A file path with resolved macros.
   */
  public String replaceMacros(String filePath) {
    if (filePath.startsWith(PROJECT_FOLDER_TOKEN)) {
      log.debug("Replaced project folder macro: original: " + filePath);

      filePath = filePath.substring(PROJECT_FOLDER_TOKEN.length(), filePath.length());
      filePath = projectFolder.getPath() + filePath;
      log.debug("project folder macro: replacement: " + filePath);
    }

    return filePath;
  }
}