package de.unipassau.medspace.common.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;


public class ServerConfigProvider {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ServerConfigProvider.class);

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
   * The server configuration.
   */
  protected ServerConfig serverConfig;

  @Inject
  public ServerConfigProvider(com.typesafe.config.Config playConfig,
                              ShutdownService shutdownService) {
    try {
      init(playConfig);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException e) {
      log.error("Couldn't init general config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }
  }

  private void init(Config playConfig)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {

    String addressHTTP = playConfig.getString(ADDRESS_ID_HTTP);
    String addressHTTPS = playConfig.getString(ADDRESS_ID_HTTP);
    int portHTTP = playConfig.getInt(PORT_ID_HTTP);
    int portHTTPS = playConfig.getInt(PORT_ID_HTTP);

    log.debug(ADDRESS_ID_HTTP + " = " + addressHTTP);
    log.debug(PORT_ID_HTTP + " = " + portHTTP);
    //log.debug(ADDRESS_ID_HTTPS + " = " + addressHTTPS);
    //log.debug(PORT_ID_HTTPS + " = " + portHTTPS);

    //right now, only http is supported
    serverConfig = new ServerConfig(HTTP_PROTOCOL, addressHTTP, portHTTP);

    log.info("Reading server configuration done.");
  }

  /**
   * Provides the server configuration.
   * @return the server configuration.
   */
  public ServerConfig getServerConfig() {
    return serverConfig;
  }
}
