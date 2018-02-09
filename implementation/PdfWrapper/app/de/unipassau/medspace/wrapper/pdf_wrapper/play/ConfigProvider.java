package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.GeneralWrapperConfigReader;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.PdfConfigReader;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.RootParsing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
  private static final String ADDRESS_ID_HTTP = "play.server.http.address";

  /**
   * TODO
   */
  private static final String PORT_ID_HTTP = "play.server.http.port";

  /**
   * TODO
   */
  private static final String ADDRESS_ID_HTTPS = "play.server.https.address";

  /**
   * TODO
   */
  private static final String PORT_ID_HTTPS = "play.server.https.port";


  /**
   * TODO
   */
  private static final String HTTP_PROTOCOL = "http://";

  /**
   * TODO
   */
  private static final String HTTPS_PROTOCOL = "https://";

  /**
   * TODO
   */
  private static final String WRAPPER_CONFIG_FILE_ID = "MeDSpaceWrapperConfig";

  /**
   * TODO
   */
  private static final String PDF_CONFIG_FILE_ID = "MeDSpacePdfWrapperConfig";

  /**
   * TODO
   */
  private static final String PDF_CONFIG_SPECIFICATION_FILE_ID = "MeDSpacePdfWrapperConfigSpecification";

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
   */
  private RootParsing pdfConfig;

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
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace DDSM Image configuration done.");
  }


  /**
   * TODO
   * @param playConfig
   * @param provider
   * @throws IOException
   * @throws ConfigException.Missing
   * @throws ConfigException.WrongType
   * @throws JAXBException
   * @throws SAXException
   */
  private void init(Config playConfig, RDFProvider provider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType,
      JAXBException,
      SAXException {

    String addressHTTP = playConfig.getString(ADDRESS_ID_HTTP);
    String addressHTTPS = playConfig.getString(ADDRESS_ID_HTTP);
    int portHTTP = playConfig.getInt(PORT_ID_HTTP);
    int portHTTPS = playConfig.getInt(PORT_ID_HTTP);
    String wrapperConfigFile = playConfig.getString(WRAPPER_CONFIG_FILE_ID);

    log.debug(ADDRESS_ID_HTTP + " = " + addressHTTP);
    log.debug(PORT_ID_HTTP + " = " + portHTTP);
    log.debug(ADDRESS_ID_HTTPS + " = " + addressHTTPS);
    log.debug(PORT_ID_HTTPS + " = " + portHTTPS);
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

    log.info("Parsing ddsm image wrapper configuration...");

    String pdfConfigFilePath = playConfig.getString(PDF_CONFIG_FILE_ID);
    String pdfConfigSpecificationFilePath = playConfig.getString(PDF_CONFIG_SPECIFICATION_FILE_ID);

    PdfConfigReader configReader = new PdfConfigReader(pdfConfigSpecificationFilePath);
    pdfConfig = configReader.parse(pdfConfigFilePath);

    log.info("Parsing ddsm image wrapper configuration done.");

  }

  /**
   * TODO
   * @return
   */
  public RootParsing getPdfConfig() {
    return pdfConfig;
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
}