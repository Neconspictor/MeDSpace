package de.unipassau.medspace.wrapper.pdf_wrapper.config;

import de.unipassau.medspace.common.play.ProjectResourceManager;
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
import java.io.FileNotFoundException;
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

  private final ProjectResourceManager resourceManager;

  /**
   * Creates a new PdfConfigReader object.
   * @param pdfConfigSpecificationSchema The XSD validation schema for the PDF wrapper configuration.
   * @param rdfMappingSpec The XSD validation schema for the RDF mapping.
   * @param resourceManager The resource manager.
   */
  public PdfConfigReader(String pdfConfigSpecificationSchema,
                         String rdfMappingSpec,
                         ProjectResourceManager resourceManager) {
    this.pdfConfigSpecificationSchema = pdfConfigSpecificationSchema;
    this.rdfMappingSpec = rdfMappingSpec;

    this.resourceManager = resourceManager;
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

    resolveFiles(rootMapping);

    return rootMapping;

  }

  private void resolveFiles(RootMapping rootMapping) throws IOException {
    String pdfRoot = rootMapping.getPdfRootDirectory();
    try {
      pdfRoot = resourceManager.getResolvedPath(pdfRoot);
    } catch (FileNotFoundException e) {
      throw new IOException("Couldn't find/resolve PDF root folder: " + pdfRoot, e);
    }

    rootMapping.setPdfRootDirectory(pdfRoot);
  }
}