package de.unipassau.medspace.wrapper.image_wrapper.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.play.GeneralConfigProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.image_wrapper.config.DDSMConfigReader;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * TODO
 */
public class DdsmConfigProvider extends GeneralConfigProvider {


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(DdsmConfigProvider.class);

  /**
   * TODO
   */
  private static final String DDSM_CONFIG_FILE_ID = "MeDSpaceDdsmConfig";

  /**
   * TODO
   */
  private static final String DDSM_CONFIG_SPECIFICATION_FILE_ID = "MeDSpaceDdsmConfigSpecification";


  /**
   * TODO
   */
  private RootMapping ddsmConfig;

  /**
   * TODO
   * @param playConfig
   * @param provider
   * @param shutdownService
   */
  @Inject
  public DdsmConfigProvider(com.typesafe.config.Config playConfig,
                            RDFProvider provider,
                            ShutdownService shutdownService) {
    super(playConfig, provider, shutdownService);

    try {
      init(playConfig);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init ddsm config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace DDSM Image configuration done.");
  }


  /**
   * TODO
   * @param playConfig
   * @throws IOException
   * @throws ConfigException.Missing
   * @throws ConfigException.WrongType
   * @throws JAXBException
   * @throws SAXException
   */
  private void init(Config playConfig)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType,
      JAXBException,
      SAXException {

    log.info("Parsing ddsm image wrapper configuration...");

    String ddsmConfigFilePath = playConfig.getString(DDSM_CONFIG_FILE_ID);
    String ddsmConfigSpecificationFilePath = playConfig.getString(DDSM_CONFIG_SPECIFICATION_FILE_ID);

    DDSMConfigReader configReader = new DDSMConfigReader(ddsmConfigSpecificationFilePath);
    ddsmConfig = configReader.parse(ddsmConfigFilePath);

    log.info("Parsing ddsm image wrapper configuration done.");
  }

  /**
   * TODO
   * @return
   */
  public RootMapping getDdsmConfig() {
    return ddsmConfig;
  }
}