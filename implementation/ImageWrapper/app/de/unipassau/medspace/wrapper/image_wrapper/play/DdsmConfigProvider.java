package de.unipassau.medspace.wrapper.image_wrapper.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.play.ProjectResourceManager;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.wrapper.image_wrapper.config.DDSMConfig;
import de.unipassau.medspace.wrapper.image_wrapper.config.DDSM_ConfigReader;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.DDSM_MappingConfigReader;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * A Provider for the DDSM configurations.
 */
public class DdsmConfigProvider {


  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(DdsmConfigProvider.class);

  private static final String DDSM_CONFIG_FILE_ID = "medspace.ddsm.config";

  private static final String DDSM_MAPPING_CONFIG_FILE_ID = "medspace.ddsm.mapping.config";

  private static final String DDSM_CONFIG_SPECIFICATION_FILE_ID = "medspace.ddsm.specification.config";

  private static final String DDSM_MAPPING_CONFIG_SPECIFICATION_FILE_ID = "medspace.ddsm.specification.mapping";


  private DDSMConfig ddsmConfig;

  private RootMapping ddsmMappingConfig;

  /**
   * Creates a new DdsmConfigProvider object.
   *
   * @param playConfig The Play configuration.
   * @param resourceManager the project resource manager
   * @param shutdownService The shutdown service
   */
  @Inject
  public DdsmConfigProvider(com.typesafe.config.Config playConfig,
                            ProjectResourceManager resourceManager,
                            ShutdownService shutdownService) {
    try {
      init(playConfig, resourceManager);
    } catch (Exception e) {
      log.error("Couldn't init ddsm config provider: ", e);
      log.error("Shutting down application...");
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace DDSM Image configuration done.");
  }

  private void init(Config playConfig, ProjectResourceManager resourceManager)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType,
      JAXBException,
      SAXException {

    log.info("Parsing ddsm image wrapper configuration...");

    String ddsmConfigFilePath = resourceManager
        .getResolvedPath(playConfig.getString(DDSM_CONFIG_FILE_ID));

    String ddsmConfigSpecificationFilePath = resourceManager
        .getResolvedPath(playConfig.getString(DDSM_CONFIG_SPECIFICATION_FILE_ID));

    String ddsmMappingConfigFilePath = resourceManager
        .getResolvedPath(playConfig.getString(DDSM_MAPPING_CONFIG_FILE_ID));

    String ddsmMappingConfigSpecificationFilePath = resourceManager
        .getResolvedPath(playConfig.getString(DDSM_MAPPING_CONFIG_SPECIFICATION_FILE_ID));


    // create the mapping config
    DDSM_MappingConfigReader mappingReader = new DDSM_MappingConfigReader(ddsmMappingConfigSpecificationFilePath);
    ddsmMappingConfig = mappingReader.parse(ddsmMappingConfigFilePath);

    // create the ddsm config
    DDSM_ConfigReader configReader = new DDSM_ConfigReader(ddsmConfigSpecificationFilePath, resourceManager);
    ddsmConfig = configReader.parse(ddsmConfigFilePath);

    log.info("Parsing ddsm image wrapper configuration done.");
  }

  /**
   * Provides the general DDSM configuration.
   * @return the general DDSM configuration.
   */
  public DDSMConfig getDdsmConfig() {
    return ddsmConfig;
  }


  /**
   * Provides the DDSM mapping configuration.
   * @return the DDSM mapping configuration.
   */
  public RootMapping getDdsmMappingConfig() {
    return ddsmMappingConfig;
  }
}