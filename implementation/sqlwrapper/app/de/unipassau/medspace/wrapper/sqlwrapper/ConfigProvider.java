package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.GeneralWrapperConfigReader;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A configuration provider for the SQL wrapper.
 */
public class ConfigProvider {


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConfigProvider.class);


  private static final String ADDRESS_ID_HTTP = "play.server.http.address";

  private static final String PORT_ID_HTTP = "play.server.http.port";

  private static final String ADDRESS_ID_HTTPS = "play.server.https.address";

  private static final String PORT_ID_HTTPS = "play.server.https.port";

  private static final String HTTP_PROTOCOL = "http://";

  private static final String HTTPS_PROTOCOL = "https://";

  private static final String WRAPPER_CONFIG_FILE_ID = "MeDSpaceWrapperConfig";

  private static final String D2R_CONFIG_FILE_ID = "MeDSpaceD2rConfig";


  private Configuration d2rConfig;

  private GeneralWrapperConfig generalWrapperConfig;

  private ServerConfig serverConfig;

  /**
   * Creates a new ConfigProvider object.
   * @param playConfig The play configuration.
   * @param provider The RDF provider to use.
   * @param shutdownService The shutdown service to use.
   */
  @Inject
  public ConfigProvider(com.typesafe.config.Config playConfig,
                        RDFProvider provider,
                        ShutdownService shutdownService) {

    try {
      init(playConfig, provider);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace D2RMap configuration done.");
  }


  private void init(Config playConfig, RDFProvider provider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {

    String addressHTTP = playConfig.getString(ADDRESS_ID_HTTP);
    String addressHTTPS = playConfig.getString(ADDRESS_ID_HTTP);
    int portHTTP = playConfig.getInt(PORT_ID_HTTP);
    int portHTTPS = playConfig.getInt(PORT_ID_HTTP);
    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);
    String d2rConfigFile = playConfig.getString(D2R_CONFIG_FILE_ID);

    log.debug(ADDRESS_ID_HTTP + " = " + addressHTTP);
    log.debug(PORT_ID_HTTP + " = " + portHTTP);
    log.debug(ADDRESS_ID_HTTPS + " = " + addressHTTPS);
    log.debug(PORT_ID_HTTPS + " = " + portHTTPS);
    log.debug(WRAPPER_CONFIG_FILE_ID + " = " + wrapperConfigFile);
    log.debug(D2R_CONFIG_FILE_ID + " = " + d2rConfigFile);

    //right now, only http is supported
    serverConfig = new ServerConfig(HTTP_PROTOCOL, addressHTTP, portHTTP);

    File generalWrapperConfigFile = new File(wrapperConfigFile);

    // The general wrapper file has to exist
    if (!generalWrapperConfigFile.exists()) {
      throw new FileNotFoundException("general wrapper config file not found: " + wrapperConfigFile);
    }

    log.info("Reading general wrapper configuration...");

    generalWrapperConfig = new GeneralWrapperConfigReader(provider).readConfig(wrapperConfigFile);

    log.info("Reading general wrapper configuration done: ");
    log.debug(generalWrapperConfig.toString());
    log.info("Reading MeDSpace D2RMap configuration...");

    d2rConfig = new ConfigurationReader(provider).readConfig(d2rConfigFile);
  }

  /**
   * Provides the general wrapper configuration.
   * @return the general wrapper configuration.
   */
  public GeneralWrapperConfig getGeneralWrapperConfig() {
    return generalWrapperConfig;
  }

  /**
   * Provides the server configuration.
   * @return the server configuration.
   */
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  /**
   * Provides the d2r mapping configuration.
   * @return the d2r mapping configuration.
   */
  public Configuration getD2rConfig() {
    return d2rConfig;
  }
}