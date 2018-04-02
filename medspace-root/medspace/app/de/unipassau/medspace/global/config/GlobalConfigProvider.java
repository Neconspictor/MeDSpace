package de.unipassau.medspace.global.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.play.ProjectResourceManager;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * A provider for the global MeDSpace configuration.
 */
public class GlobalConfigProvider implements Provider<ConfigMapping> {

  private static final String GLOBAL_CONFIG_FILE_ID = "medspace.global.configFile";

  private static final String GLOBAL_CONFIG_SPECIFICATION_FILE_ID = "medspace.global.specificationFile";

  private static Logger log = LoggerFactory.getLogger(GlobalConfigProvider.class);


  private ConfigMapping globalConfig;

  private String thisBaseURL;

  /**
   * Creates a new GlobalConfigProvider object.
   *
   * @param playConfig The Play configuration.
   * @param resourceManager The resource manager
   * @param shutdownService The shutdown service
   * @param serverConfig The server configuration.
   */
  @Inject
  public GlobalConfigProvider(com.typesafe.config.Config playConfig,
                              ProjectResourceManager resourceManager,
                              ShutdownService shutdownService,
                              ServerConfig serverConfig) {

    try {
      thisBaseURL = serverConfig.getServerURL().toExternalForm();
      init(playConfig, resourceManager);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading global configuration is done.");
  }

  @Override
  public ConfigMapping get() {
    return globalConfig;
  }


  private void init(Config playConfig, ProjectResourceManager resourceManager)
      throws JAXBException,
      IOException,
      SAXException {

    log.info("Parsing global configuration...");

    String globalConfigFilePath = resourceManager
        .getResolvedPath(playConfig.getString(GLOBAL_CONFIG_FILE_ID));

    String globalConfigSpecificationFilePath = resourceManager
        .getResolvedPath(playConfig.getString(GLOBAL_CONFIG_SPECIFICATION_FILE_ID));


    GlobalMeDSpaceConfigReader configReader = new GlobalMeDSpaceConfigReader(globalConfigSpecificationFilePath,
        resourceManager, thisBaseURL);
    globalConfig = configReader.parse(globalConfigFilePath);

    log.info("Parsing the global configuration is done.");
  }
}