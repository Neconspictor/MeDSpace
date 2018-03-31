package de.unipassau.medspace.common.play;


import play.Environment;

import javax.inject.Inject;
import java.io.File;

/**
 * A provider for play application resource files (e.g. files in the public or conf folder).
 */
public class ResourceProvider {

  private final Environment environment;

  /**
   * Creates a new ResourceProvider object.
   * @param environment The Play environment to use for locating resources.
   */
  @Inject
  public ResourceProvider(Environment environment) {
    this.environment = environment;
  }

  /**
   * Provides a resource by its relative path.
   * @param relativePath The relative path.
   * @return
   */
  public File getResourceAsFile(String relativePath) {
    String filePath = environment.resource(relativePath).getFile();
    return new File(filePath);
  }

  /**
   * Provides the project folder of a Play application.
   * @return the project folder of a Play application.
   */
  public File getProjectFolder() {

    // The play root folder is different
    // when play is running in production mode.
    if (environment.isProd()) {
      return getProductionProjectFile();
    }

    return environment.rootPath();
  }

  private File getApplicationConfFolder() {
    return getResourceAsFile("/application.conf")
        .getParentFile();
  }

  private File getProductionProjectFile() {
    return getApplicationConfFolder().getParentFile();
  }
}