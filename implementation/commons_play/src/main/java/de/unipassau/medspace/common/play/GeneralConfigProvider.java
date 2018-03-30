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
   * the key for the http address.
   */
  protected static final String ADDRESS_ID_HTTP = "play.server.http.address";

  /**
   * The key for the https address,
   */
  protected static final String ADDRESS_ID_HTTPS = "play.server.https.address";


  /**
   * The http protocol.
   */
  protected static final String HTTP_PROTOCOL = "http://";

  /**
   * The https protocol.
   */
  protected static final String HTTPS_PROTOCOL = "https://";


  /**
   * The key for the http port.
   */
  protected static final String PORT_ID_HTTP = "play.server.http.port";

  /**
   * The key for the https port.
   */
  protected static final String PORT_ID_HTTPS = "play.server.https.port";

  /**
   * The key for thegeneral wrapper config file.
   */
  protected static final String WRAPPER_CONFIG_FILE_ID = "MeDSpaceWrapperConfig";

  /**
   * The general wrapper configuration.
   */
  protected GeneralWrapperConfig generalWrapperConfig;

  /**
   * The server configuration.
   */
  protected ServerConfig serverConfig;


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

    String addressHTTP = playConfig.getString(ADDRESS_ID_HTTP);
    String addressHTTPS = playConfig.getString(ADDRESS_ID_HTTP);
    int portHTTP = playConfig.getInt(PORT_ID_HTTP);
    int portHTTPS = playConfig.getInt(PORT_ID_HTTP);
    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);

    log.debug(ADDRESS_ID_HTTP + " = " + addressHTTP);
    log.debug(PORT_ID_HTTP + " = " + portHTTP);
    //log.debug(ADDRESS_ID_HTTPS + " = " + addressHTTPS);
    //log.debug(PORT_ID_HTTPS + " = " + portHTTPS);
    log.debug(WRAPPER_CONFIG_FILE_ID + " = " + wrapperConfigFile);

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
}