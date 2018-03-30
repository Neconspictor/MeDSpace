package de.unipassau.medspace.wrapper.pdf_wrapper.config;

import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
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

  private final String pdfConfigSpecificationSchema;

  private final String rdfMappingSpec;

  /**
   * Creates a new PdfConfigReader object.
   * @param pdfConfigSpecificationSchema The XSD validation schema for the PDF wrapper configuration.
   * @param rdfMappingSpec The XSD validation schema for the RDF mapping.
   */
  public PdfConfigReader(String pdfConfigSpecificationSchema, String rdfMappingSpec) {
    this.pdfConfigSpecificationSchema = pdfConfigSpecificationSchema;
    this.rdfMappingSpec = rdfMappingSpec;
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
    return (RootMapping) unmarshaller.unmarshal(new File(fileName));
  }
}
