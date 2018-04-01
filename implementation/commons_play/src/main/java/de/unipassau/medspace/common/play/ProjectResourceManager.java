package de.unipassau.medspace.common.play;

import de.unipassau.medspace.common.config.PathResolveParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * A manager for project resources.
 */
public class ProjectResourceManager implements Provider<PathResolveParser> {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperDependencyInjector.class);

  private final Environment environment;

  private final PathResolveParser pathResolveParser;

  /**
   * Creates a new ProjectResourceManager object.
   * @param environment The paly environment. Used for finding resources.
   */
  @Inject
  public ProjectResourceManager(Environment environment, ShutdownService shutdownService) {
    this.environment = environment;
    File projectFolder = null;

    try {
      projectFolder = getProjectFolder();
    } catch (Exception e) {
      log.error("Couldn't get project folder", e);
      log.info("Shutting down application...");
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    this.pathResolveParser = new PathResolveParser(projectFolder);
  }

  @Override
  public PathResolveParser get() {
    return pathResolveParser;
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

  /**
   * Provides the conf folder of a Play application.
   * @return the conf folder of a Play application.
   */
  public File getApplicationConfFolder() {
    try {
      return getResource("/application.conf")
          .getParentFile();
    } catch (FileNotFoundException e) {
      throw new RuntimeException("/application.conf resource not found!");
    }
  }

  /**
   * Provides a resource by its file path.
   * @param path The file path of the resource.
   * @return The resource.
   * @throws FileNotFoundException If the resource couldn't be found.
   */
  public File getResource(String path) throws FileNotFoundException {
    URL url = environment.resource(path);
    if (url == null) throw new FileNotFoundException("resource not found: " + path);
    File resource =  new File(url.getFile());

    if (!resource.exists()) throw new FileNotFoundException("resource not found: " + path);

    return resource;
  }

  /**
   * Provides a resource or a file by its path.
   * Additionally this methods resolves any MeDSpace path macros.
   * @param path The relative path to the resource or file. The path can contain path macros.
   *
   * @return A macro-resolved file.
   * @throws FileNotFoundException If the specified file or resource is not found
   */
  public File getResolved(String path) throws FileNotFoundException {

    path = pathResolveParser.replaceMacros(path);

    if (isResource(path)) {
      path = environment.resource(path).getFile();
    }
    File file = new File(path);

    if (!file.exists()) throw new FileNotFoundException("resource/file not found: " + file);

    return file;
  }

  /**
   * Provides a resource or a file by its path.
   * Additionally this methods resolves any MeDSpace path macros.
   * @param path The relative path to the resource or file. The path can contain path macros.
   *
   * @return A macro-resolved file path.
   * @throws FileNotFoundException If the specified file or resource is not found
   */
  public String getResolvedPath(String path) throws FileNotFoundException {
    File file = getResolved(path);
    return file.getPath();
  }

  /**
   * Checks if a given file path is a resource.
   * @param filePath the file path
   * @return true if the file path is resource.
   */
  public boolean isResource(String filePath) {
    return filePath.startsWith("/");
  }

  private File getProductionProjectFile() {
    return getApplicationConfFolder().getParentFile();
  }
}