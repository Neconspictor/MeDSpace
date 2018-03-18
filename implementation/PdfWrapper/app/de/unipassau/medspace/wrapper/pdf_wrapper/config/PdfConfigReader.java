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
 * TODO
 */
public class PdfConfigReader {

  private final String pdfConfigSpecificationSchema;

  private final String rdfMappingSpec;

  public PdfConfigReader(String pdfConfigSpecificationSchema, String rdfMappingSpec) {
    this.pdfConfigSpecificationSchema = pdfConfigSpecificationSchema;
    this.rdfMappingSpec = rdfMappingSpec;
  }


  /**
   * TODO
   * @param fileName
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
   */
  public RootMapping parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(RootMapping.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{rdfMappingSpec, pdfConfigSpecificationSchema});
    unmarshaller.setSchema(schema);
    return (RootMapping) unmarshaller.unmarshal(new File(fileName));
  }
}
