package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.play.ProjectResourceManager;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * A configuration provider for the D2R mapping configuration.
 */
public class ConfigProvider implements Provider<Configuration> {


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConfigProvider.class);

  private static final String D2R_CONFIG_FILE_ID = "medspace.d2rmap.config";


  private Configuration d2rConfig;

  /**
   * Creates a new ConfigProvider object.
   * @param playConfig The play configuration.
   * @param provider The RDF provider to use.
   * @param resourceManager the resource manager.
   * @param shutdownService The shutdown service to use.
   */
  @Inject
  public ConfigProvider(com.typesafe.config.Config playConfig,
                        RDFProvider provider,
                        ProjectResourceManager resourceManager,
                        ShutdownService shutdownService) {

    try {
      init(playConfig, provider, resourceManager);
    } catch (Exception  e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace D2RMap configuration done.");
  }

  @Override
  public Configuration get() {
    return d2rConfig;
  }


  private void init(Config playConfig, RDFProvider provider,
                    ProjectResourceManager resourceManager)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {

    String d2rConfigFile = resourceManager
        .getResolvedPath(playConfig.getString(D2R_CONFIG_FILE_ID));

    log.debug(D2R_CONFIG_FILE_ID + " = " + d2rConfigFile);
    log.info("Reading MeDSpace D2RMap configuration...");

    d2rConfig = new ConfigurationReader(provider).readConfig(d2rConfigFile);
    log.info("...readed D2RMap configuration.");
  }
}