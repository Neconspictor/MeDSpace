package de.unipassau.medspace.common.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.GeneralWrapperConfigReader;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.rdf.RDFProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * TODO
 */
public class GeneralConfigProvider {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralConfigProvider.class);

  /**
   * The key for thegeneral wrapper config file.
   */
  protected static final String WRAPPER_CONFIG_FILE_ID = "MeDSpaceWrapperConfig";

  /**
   * The general wrapper configuration.
   */
  protected GeneralWrapperConfig generalWrapperConfig;


  /**
   * Creates a new GeneralConfigProvider object.
   * @param playConfig The Play configuration.
   * @param provider The RDF provider.
   * @param shutdownService The shutdown service.
   */
  @Inject
  public GeneralConfigProvider(com.typesafe.config.Config playConfig,
                        RDFProvider provider,
                        ShutdownService shutdownService) {

    try {
      init(playConfig, provider);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException  e) {
      log.error("Couldn't init general config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }
  }

  private void init(Config playConfig, RDFProvider provider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {


    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);
    log.debug(WRAPPER_CONFIG_FILE_ID + " = " + wrapperConfigFile);

    File generalWrapperConfigFile = new File(wrapperConfigFile);

    // The general wrapper file has to exist
    if (!generalWrapperConfigFile.exists()) {
      throw new FileNotFoundException("general wrapper config file not found: " + wrapperConfigFile);
    }

    log.info("Reading general wrapper configuration...");

    generalWrapperConfig = new GeneralWrapperConfigReader(provider).readConfig(wrapperConfigFile);

    log.info("Reading general wrapper configuration done: ");
    log.debug(generalWrapperConfig.toString());
  }

  /**
   * Provides the general wrapper configuration.
   * @return the general wrapper configuration.
   */
  public GeneralWrapperConfig getGeneralWrapperConfig() {
    return generalWrapperConfig;
  }
}