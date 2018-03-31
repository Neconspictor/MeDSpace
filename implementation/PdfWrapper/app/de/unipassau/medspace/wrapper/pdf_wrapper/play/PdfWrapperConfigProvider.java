package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.play.GeneralConfigProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.PdfConfigReader;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import static de.unipassau.medspace.common.config.Constants.RDF_MAPPING_SCHEMA;

/**
 * A provider for the configuration of the PDF wrapper.
 */
public class PdfWrapperConfigProvider extends GeneralConfigProvider {

  private static Logger log = LoggerFactory.getLogger(PdfWrapperConfigProvider.class);

  private static final String PDF_CONFIG_FILE_ID = "MeDSpacePdfWrapperConfig";

  private static final String PDF_CONFIG_SPECIFICATION_FILE_ID = "MeDSpacePdfWrapperConfigSpecification";


  private RootMapping pdfConfig;

  /**
   * Creates a new PdfWrapperConfigProvider object.
   * @param playConfig The Play configuration.
   * @param provider The RDF provider
   * @param shutdownService The shutdown service.
   */
  @Inject
  public PdfWrapperConfigProvider(com.typesafe.config.Config playConfig,
                                  RDFProvider provider,
                                  ShutdownService shutdownService) {
    super(playConfig, provider, shutdownService);

    try {
      init(playConfig, provider);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace DDSM Image configuration done.");
  }


  private void init(Config playConfig, RDFProvider provider)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType,
      JAXBException,
      SAXException {

    log.info("Parsing pdf wrapper configuration...");

    String pdfConfigFilePath = playConfig.getString(PDF_CONFIG_FILE_ID);
    String pdfConfigSpecificationFilePath = playConfig.getString(PDF_CONFIG_SPECIFICATION_FILE_ID);

    PdfConfigReader configReader = new PdfConfigReader(pdfConfigSpecificationFilePath,
        RDF_MAPPING_SCHEMA);
    pdfConfig = configReader.parse(pdfConfigFilePath);

    log.info("Parsing pdf wrapper configuration done.");

  }

  /**
   * Provides the PDF wrapper configuration.
   * @return the PDF wrapper configuration.
   */
  public RootMapping getPdfConfig() {
    return pdfConfig;
  }

}