package de.unipassau.medspace.common.play;

import de.unipassau.medspace.common.config.PathResolveParser;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;

/**
 * A provider for a path resolve parser.
 */
public class PathResolveParserProvider implements Provider<PathResolveParser> {

  private final PathResolveParser pathResolveParser;


  /**
   * Creates a new PathResolveParserProvider object.
   * @param resourceProvider The resource provider used to get the prject folder.
   */
  @Inject
  public PathResolveParserProvider(ResourceProvider resourceProvider) {
    File projectFolder = resourceProvider.getProjectFolder();
    pathResolveParser = new PathResolveParser(projectFolder);
  }


  @Override
  public PathResolveParser get() {
    return pathResolveParser;
  }
}