package de.unipassau.medspace.global.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.play.ResourceProvider;
import de.unipassau.medspace.common.play.ServerConfigProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * Created by David Goeth on 3/31/2018.
 */
public class GlobalConfigProvider {

  private static final String GLOBAL_CONFIG_FILE_ID = "medspace.global.configFile";

  private static final String GLOBAL_CONFIG_SPECIFICATION_FILE_ID = "medspace.global.specificationFile";

  private static final String THIS_BASE_URL_TOKEN = "[this]";

  private static Logger log = LoggerFactory.getLogger(GlobalConfigProvider.class);


  private ConfigMapping globalConfig;

  private ServerConfig serverConfig;

  private String thisBaseURL;

  @Inject
  public GlobalConfigProvider(com.typesafe.config.Config playConfig,
                              ResourceProvider resourceProvider,
                              ShutdownService shutdownService,
                              ServerConfigProvider serverConfigProvider) {
    try {
      serverConfig = serverConfigProvider.getServerConfig();
      thisBaseURL = serverConfig.getServerURL().toExternalForm();
      init(playConfig, resourceProvider);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading global configuration is done.");
  }

  public ConfigMapping getGlobalConfig() {
    return globalConfig;
  }

  private void init(Config playConfig, ResourceProvider resourceProvider) throws JAXBException, IOException, SAXException {
    log.info("Parsing global configuration...");

    String globalConfigFilePath = playConfig.getString(GLOBAL_CONFIG_FILE_ID);
    File globalConfigFile = resourceProvider.getResourceAsFile(globalConfigFilePath);
    globalConfigFilePath = globalConfigFile.getAbsolutePath();

    String globalConfigSpecificationFilePath = playConfig.getString(GLOBAL_CONFIG_SPECIFICATION_FILE_ID);
    File globalConfigSpecificationFile = resourceProvider.getResourceAsFile(globalConfigSpecificationFilePath);
    globalConfigSpecificationFilePath = globalConfigSpecificationFile.getAbsolutePath();

    GlobalMeDSpaceConfigReader configReader = new GlobalMeDSpaceConfigReader(globalConfigSpecificationFilePath);
    globalConfig = configReader.parse(globalConfigFilePath);

    process(globalConfig);

    log.info("Parsing the global configuration is done.");
  }

  private void process(ConfigMapping config) {

    String dataCollectorBaseURL = config.getQueryExecutor().getDataCollectorBaseURL();

    dataCollectorBaseURL = replaceThisURL(dataCollectorBaseURL);
    config.getQueryExecutor().setDataCollectorBaseURL(dataCollectorBaseURL);

    String registerBaseURL = config.getQueryExecutor().getRegisterBaseURL();
    registerBaseURL = replaceThisURL(registerBaseURL);
    config.getQueryExecutor().setRegisterBaseURL(registerBaseURL);
  }

  private String replaceThisURL(String baseURL) {
    if (baseURL.startsWith(THIS_BASE_URL_TOKEN)) {
      baseURL = baseURL.substring(THIS_BASE_URL_TOKEN.length(), baseURL.length());
      baseURL = thisBaseURL + baseURL;
    }

    // base URLs have to end with a forward slash
    if (!baseURL.endsWith("/")) {
      baseURL += "/";
    }

    return baseURL;
  }
}