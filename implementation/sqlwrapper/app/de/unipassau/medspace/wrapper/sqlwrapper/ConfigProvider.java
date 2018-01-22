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
import java.net.UnknownHostException;

/**
 * TODO
 */
public class ConfigProvider {


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConfigProvider.class);

  /**
   * TODO
   */
  private static final String ADDRESS_ID = "play.server.http.address";

  /**
   * TODO
   */
  private static final String PORT_ID = "play.server.http.port";

  /**
   * TODO
   */
  private static final String WRAPPER_CONFIG_FILE_ID = "MeDSpaceWrapperConfig";

  /**
   * TODO
   */
  private static final String D2R_CONFIG_FILE_ID = "MeDSpaceD2rConfig";


  /**
   * TODO
   */
  private Configuration d2rConfig;

  /**
   * TODO
   */
  private GeneralWrapperConfig generalWrapperConfig;

  /**
   * TODO
   */
  private ServerConfig serverConfig;

  /**
   * TODO
   * @param playConfig
   * @param provider
   * @param shutdownService
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

  /**
   * TODO
   * @param playConfig
   * @param provider
   * @throws IOException
   * @throws ConfigException.Missing
   * @throws ConfigException.WrongType
   */
  private void init(Config playConfig, RDFProvider provider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType {

    String address = playConfig.getString(ADDRESS_ID);
    int port = playConfig.getInt(PORT_ID);
    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);
    String d2rConfigFile = playConfig.getString(D2R_CONFIG_FILE_ID);

    log.debug(ADDRESS_ID + " = " + address);
    log.debug(PORT_ID + " = " + port);
    log.debug(WRAPPER_CONFIG_FILE_ID + " = " + wrapperConfigFile);
    log.debug(D2R_CONFIG_FILE_ID + " = " + d2rConfigFile);

    serverConfig = new ServerConfig(address, port);

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
   * TODO
   * @return
   */
  public GeneralWrapperConfig getGeneralWrapperConfig() {
    return generalWrapperConfig;
  }

  /**
   * TODO
   * @return
   */
  public ServerConfig getServerConfig() {
    return serverConfig;
  }

  /**
   * TODO
   * @return
   */
  public Configuration getD2rConfig() {
    return d2rConfig;
  }
}