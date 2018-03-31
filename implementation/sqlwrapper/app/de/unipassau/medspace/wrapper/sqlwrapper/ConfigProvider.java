package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.play.GeneralConfigProvider;
import de.unipassau.medspace.common.play.ResourceProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * A configuration provider for the SQL wrapper.
 */
public class ConfigProvider extends GeneralConfigProvider {


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
   * @param resourceProvider the resource provider
   * @param shutdownService The shutdown service to use.
   */
  @Inject
  public ConfigProvider(com.typesafe.config.Config playConfig,
                        RDFProvider provider,
                        ResourceProvider resourceProvider,
                        ShutdownService shutdownService) {
    super(playConfig, provider, resourceProvider, shutdownService);

    try {
      init(playConfig, provider, resourceProvider);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace D2RMap configuration done.");
  }


  private void init(Config playConfig, RDFProvider provider,
                    ResourceProvider resourceProvider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {

    String d2rConfigFile = playConfig.getString(D2R_CONFIG_FILE_ID);
    d2rConfigFile = resourceProvider.getResourceAsFile(d2rConfigFile).getAbsolutePath();

    log.debug(D2R_CONFIG_FILE_ID + " = " + d2rConfigFile);
    log.info("Reading MeDSpace D2RMap configuration...");

    d2rConfig = new ConfigurationReader(provider).readConfig(d2rConfigFile);
  }

  /**
   * Provides the d2r mapping configuration.
   * @return the d2r mapping configuration.
   */
  public Configuration getD2rConfig() {
    return d2rConfig;
  }
}