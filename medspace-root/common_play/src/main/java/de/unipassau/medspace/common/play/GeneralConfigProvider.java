package de.unipassau.medspace.common.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.GeneralWrapperConfigReader;
import de.unipassau.medspace.common.config.PathResolveParser;
import de.unipassau.medspace.common.rdf.RDFProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A provider for the general wrapper configuration.
 */
public class GeneralConfigProvider implements Provider<GeneralWrapperConfig> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralConfigProvider.class);

  /**
   * The key for thegeneral wrapper config file.
   */
  protected static final String WRAPPER_CONFIG_FILE_ID = "medspace.wrapper.config";

  /**
   * The general wrapper configuration.
   */
  protected GeneralWrapperConfig generalWrapperConfig;


  /**
   * Creates a new GeneralConfigProvider object.
   * @param playConfig The Play configuration.
   * @param provider The RDF provider.
   * @param resourceManager The resource manager
   * @param shutdownService The shutdown service.
   */
  @Inject
  public GeneralConfigProvider(com.typesafe.config.Config playConfig,
                               RDFProvider provider,
                               PathResolveParser pathResolveParser,
                               ProjectResourceManager resourceManager,
                               ShutdownService shutdownService) {

    try {
      init(playConfig, provider, pathResolveParser, resourceManager);
    } catch (Exception  e) {
      log.error("Couldn't init general config provider: ", e);
      log.info("Shutting down application...");
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }
  }

  @Override
  public GeneralWrapperConfig get() {
    return generalWrapperConfig;
  }

  private void init(Config playConfig,
                    RDFProvider provider,
                    PathResolveParser pathResolveParser,
                    ProjectResourceManager resourceManager)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {


    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);
    log.debug(WRAPPER_CONFIG_FILE_ID + " = " + wrapperConfigFile);

    File generalWrapperConfigFile = resourceManager.getResolved(wrapperConfigFile);
    wrapperConfigFile = generalWrapperConfigFile.getAbsolutePath();

    // The general wrapper file has to exist
    if (!generalWrapperConfigFile.exists()) {
      throw new FileNotFoundException("general wrapper config file not found: " + wrapperConfigFile);
    }

    log.info("Reading general wrapper configuration...");

    generalWrapperConfig = new GeneralWrapperConfigReader(provider, pathResolveParser)
        .readConfig(wrapperConfigFile);

    log.info("Reading general wrapper configuration done: ");
    log.debug(generalWrapperConfig.toString());
  }
}