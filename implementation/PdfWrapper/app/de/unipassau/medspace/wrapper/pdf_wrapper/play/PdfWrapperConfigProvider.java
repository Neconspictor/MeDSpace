package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.config.PathResolveParser;
import de.unipassau.medspace.common.play.GeneralConfigProvider;
import de.unipassau.medspace.common.play.ResourceProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.PdfConfigReader;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import static de.unipassau.medspace.common.config.Constants.RDF_MAPPING_SCHEMA;

/**
 * A provider for the configuration of the PDF wrapper.
 */
public class PdfWrapperConfigProvider implements Provider<RootMapping> {

  private static Logger log = LoggerFactory.getLogger(PdfWrapperConfigProvider.class);

  private static final String PDF_CONFIG_FILE_ID = "medspace.wrapper.pdf.config";

  private static final String PDF_CONFIG_SPECIFICATION_FILE_ID = "medspace.wrapper.pdf.specification.config";


  private RootMapping pdfConfig;

  /**
   * Creates a new PdfWrapperConfigProvider object.
   * @param playConfig The Play configuration.
   * @param resourceProvider The resource provider
   * @param shutdownService The shutdown service.
   */
  @Inject
  public PdfWrapperConfigProvider(com.typesafe.config.Config playConfig,
                                  ResourceProvider resourceProvider,
                                  PathResolveParser pathResolveParser,
                                  ShutdownService shutdownService) {
    try {
      init(playConfig, resourceProvider, pathResolveParser);
    } catch (ConfigException.Missing | ConfigException.WrongType | IOException | JAXBException | SAXException e) {
      log.error("Couldn't init config provider: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    log.info("Reading MeDSpace DDSM Image configuration done.");
  }

  @Override
  public RootMapping get() {
    return pdfConfig;
  }


  private void init(Config playConfig,
                    ResourceProvider resourceProvider,
                    PathResolveParser pathResolveParser)
      throws IOException,
      ConfigException.Missing,
      ConfigException.WrongType,
      JAXBException,
      SAXException {

    log.info("Parsing pdf wrapper configuration...");

    String pdfConfigFilePath = playConfig.getString(PDF_CONFIG_FILE_ID);
    pdfConfigFilePath = resourceProvider
        .getResourceAsFile(pdfConfigFilePath)
        .getAbsolutePath();

    String pdfConfigSpecificationFilePath = playConfig.getString(PDF_CONFIG_SPECIFICATION_FILE_ID);
    pdfConfigSpecificationFilePath = resourceProvider
        .getResourceAsFile(pdfConfigSpecificationFilePath)
        .getAbsolutePath();

    PdfConfigReader configReader = new PdfConfigReader(pdfConfigSpecificationFilePath,
        RDF_MAPPING_SCHEMA, pathResolveParser);
    pdfConfig = configReader.parse(pdfConfigFilePath);

    // verify that the pdf root folder is a valid file
    File pdfRoot = new File(pdfConfig.getPdfRootDirectory());
    if (!pdfRoot.isDirectory()) {
      throw new IOException("PDF root folder is not a valid folder: " + pdfRoot);
    }

    log.info("Parsing pdf wrapper configuration done.");

  }
}