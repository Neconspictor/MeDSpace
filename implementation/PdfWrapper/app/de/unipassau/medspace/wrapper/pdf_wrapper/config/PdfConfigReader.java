package de.unipassau.medspace.wrapper.pdf_wrapper.config;

import de.unipassau.medspace.common.config.PathResolveParser;
import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * A reader for the PDF wrapper configuration.
 */
public class PdfConfigReader {

  private static final String PROJECT_FOLDER_TOKEN = "[project-folder]";

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(PdfConfigReader.class);

  private final String pdfConfigSpecificationSchema;

  private final String rdfMappingSpec;

  private final PathResolveParser resolveParser;

  /**
   * Creates a new PdfConfigReader object.
   * @param pdfConfigSpecificationSchema The XSD validation schema for the PDF wrapper configuration.
   * @param rdfMappingSpec The XSD validation schema for the RDF mapping.
   * @param resolveParser The path resolve parser to use.
   */
  public PdfConfigReader(String pdfConfigSpecificationSchema,
                         String rdfMappingSpec,
                         PathResolveParser resolveParser) {
    this.pdfConfigSpecificationSchema = pdfConfigSpecificationSchema;
    this.rdfMappingSpec = rdfMappingSpec;

    this.resolveParser = resolveParser;
  }


  /**
   * Parses the PDF wrapper RDF mappings from a given file.
   *
   * @param fileName The file to parse.
   * @throws JAXBException If an error related to JAXB occurs.
   * @throws IOException If an IO error occurs.
   * @throws SAXException If an error related to XML parsing occurs.
   */
  public RootMapping parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(RootMapping.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{rdfMappingSpec, pdfConfigSpecificationSchema});
    unmarshaller.setSchema(schema);
    RootMapping rootMapping = (RootMapping) unmarshaller.unmarshal(new File(fileName));

    process(rootMapping);

    return rootMapping;

  }

  private void process(RootMapping rootMapping) {
    String pdfRoot = rootMapping.getPdfRootDirectory();

    if (pdfRoot.startsWith(PROJECT_FOLDER_TOKEN)) {
      log.debug("Replace macros for PDF root folder: " + pdfRoot);
      pdfRoot = resolveParser.replaceMacros(pdfRoot);
      log.debug("PDF root folder is now: " + pdfRoot);
    }

    rootMapping.setPdfRootDirectory(pdfRoot);
  }
}